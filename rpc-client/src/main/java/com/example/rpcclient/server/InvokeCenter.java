package com.example.rpcclient.server;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.blance.LeastActiveLoadBalance;
import com.example.rpcclient.constants.FaultTolerantCode;
import com.example.rpcclient.constants.LoadBalanceTypeCode;
import com.example.rpcclient.handler.*;
import com.example.rpcclient.handler.postHandler.PostHandlerChain;
import com.example.rpcclient.handler.preHandler.PreHandlerChain;
import com.example.rpcclient.protocol.RpcClientMsgCodec;
import com.example.rpcclient.tolerant.CircuitBreaker;
import com.example.rpcclient.tolerant.Failover;
import com.example.rpcclient.tolerant.FaultTolerant;
import com.example.rpcclient.tolerant.Forking;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.PingMsg;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.*;
import static com.example.rpcclient.config.ClientConfig.*;
import static com.example.rpcclient.server.InstanceService.getAvailableServer;

/**
 * @Author Cbc
 * @DateTime 2025/4/20 16:09
 * @Description 调用中心
 */
@Slf4j
public class InvokeCenter {

    //todo 容错处理
    //todo 更新instance也要更新channelMap 处理掉弃用的连接
    private static final Map<Instance, Channel> channelMap = new ConcurrentHashMap<>();//根据实例存储channel 进行channel复用

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();//todo 执行业务逻辑

    public static final Map<Integer, DefaultPromise<Object>> promiseMap = new ConcurrentHashMap<>();

    public static final Map<Channel, PostHandlerChain> POSTCHAIN_MAP = new ConcurrentHashMap<>();//前置处理

    public static final Map<Channel, PreHandlerChain> PRECHAIN_MAP = new ConcurrentHashMap<>();//后置处理

    private static FaultTolerant FAULT_TOLERANT;//根据配置文件获取容错处理实例

    public static void initializeInvoke(){
        //初始化容错处理类
        if (FAULT_TOLERANT_CODE == FaultTolerantCode.RETRY) {
            FAULT_TOLERANT = new Failover();
        } else if (FAULT_TOLERANT_CODE == FaultTolerantCode.FORK) {
            FAULT_TOLERANT = new Forking();
        } else {
            FAULT_TOLERANT = new Failover();
        }

        //在instances更新时弃用掉不健康的连接
        InstanceService.addUpdateInstancesConsumer((o, n) -> {
            channelMap.keySet().removeIf(next -> !n.contains(next));
        });
    }


    //进行远程调用
    public static Object remoteInvoke(Request request) throws Throwable {
        Instance instance = getAvailableServer();
        return FAULT_TOLERANT.faultHandler(instance, null, request);
    }


    public static Object doRemoteInvoke(Request request, Instance instance) throws Throwable {

        try {

            Channel channel = getChannel(instance);

            CircuitBreaker circuitBreaker = InstanceService.getCircuitBreaker(instance);
            boolean isCan = circuitBreaker.isRequest();

            if(!isCan){
                Object mock = FallBack.mock(request);
                completeRequest(false, request, instance, true);
                return mock;
            }

            DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());

            promiseMap.put(request.getMsgId(), promise);

            EXECUTOR_SERVICE.execute(() -> {
                //前置处理
                PRECHAIN_MAP.get(channel).doHandle(request, 0);

                channel.writeAndFlush(request);
            });

            if (!promise.await(OVERTIME, TimeUnit.SECONDS)) {
                promiseMap.remove(request.getMsgId());
                throw new RuntimeException(RpcExceptionMsg.REQUEST_OVERTIME);
            }

            promiseMap.remove(request.getMsgId());

            if (promise.isSuccess()) {
                log.debug("MsgId{}:成功获取结果!!", request.getMsgId());
                completeRequest(true,  request, instance, false);
                return promise.get();
            } else {
                throw promise.cause();
            }

        } catch (Throwable e) {
            completeRequest(false, request, instance, false);
            throw new RpcException(e);
        }

    }

    //channel复用
    private static Channel getChannel(Instance instance) throws InterruptedException {

        if (channelMap.containsKey(instance)) {
            return channelMap.get(instance);
        }

        synchronized (instance) {
            if (channelMap.containsKey(instance)) {
                log.debug("进行channel复用");
                return channelMap.get(instance);
            }
            NioEventLoopGroup work = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
            PingAckHandler pingAckHandler = new PingAckHandler();
            Channel channel = new Bootstrap()
                    .group(work)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());//解码
                            ch.pipeline().addLast(new RpcClientMsgCodec());//codec
                            ch.pipeline().addLast(new ResponseHandler());//响应处理器
                            ch.pipeline().addLast(pingAckHandler);

                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true)
                    .connect(instance.getIp(), instance.getPort())
                    .sync()
                    .channel();

            POSTCHAIN_MAP.put(channel, PostHandlerChain.createPostHandlerChain(instance));
           PRECHAIN_MAP.put(channel, PreHandlerChain.createPreHandlerChain(instance));
            channelMap.put(instance, channel);

            //定时发送心跳
            ScheduledExecutorService sExe = Executors.newScheduledThreadPool(1);
            sExe.scheduleWithFixedDelay(() -> {
                        int v = pingAckHandler.num.incrementAndGet();
                        if(v >= 3){
                            channel.close();
                            return;
                        }
                        channel.writeAndFlush(new PingMsg());
                    },
                    30, 1, TimeUnit.SECONDS);


            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                channelMap.remove(instance);
                PRECHAIN_MAP.remove(channel);
                POSTCHAIN_MAP.remove(channel);
                sExe.shutdown();
            });


            log.debug("连接到新channel");
            return channel;
        }

    }


    private static void completeRequest(boolean isSuccess,Request rq, Instance instance, boolean isFallback){

        EXECUTOR_SERVICE.execute(() -> {
            if(LoadBalanceTypeCode.Least_Active_Balance.equals(LOAD_BALANCE_CODE)){
                LeastActiveLoadBalance balance = (LeastActiveLoadBalance) InstanceService.LOAD_BALANCE;
                balance.subOne(instance);
            }
            if(isFallback){
                return;
            }
            CircuitBreaker circuitBreaker = InstanceService.getCircuitBreaker(instance);
            circuitBreaker.isResponse(!isSuccess);
        });

    }


}
