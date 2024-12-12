package com.example.rpcclient.server;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.AbstractEventListener;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.blance.LoadBalance;
import com.example.rpcclient.blance.RandomLoadBalance;
import com.example.rpcclient.blance.RoundRobinLoadBalance;
import com.example.rpcclient.blance.WeightLoadBalance;
import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.constants.FaultTolerantCode;
import com.example.rpcclient.handler.IdleStateEventHandler;
import com.example.rpcclient.handler.ResponseHandler;
import com.example.rpcclient.protocol.RpcClientMsgCodec;
import com.example.rpcclient.tolerant.DoNothingFaultTolerant;
import com.example.rpcclient.tolerant.FaultTolerant;
import com.example.rpcclient.tolerant.RetryFaultTolerant;
import com.example.rpcclient.tolerant.TestOtherFaultTolerant;
import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import static com.example.rpcclient.config.ClientConfig.*;
import static com.example.rpcclient.constants.LoadBalanceTypeCode.*;


/**
 * @Author Cbc
 * @DateTime 2024/12/9 14:15
 * @Description 服务管理中心   管理所有网络连接 //todo 注意考虑线程安全问题  先进行服务注册
 */
@Slf4j
public class ServerCenter {
    //todo 容错处理
    //todo 更新instance也要更新channelMap 处理掉一些连接问题
    private static final Map<Instance, Channel> channelMap = new ConcurrentHashMap<>();//根据实例存储channel 进行channel复用

    private static volatile NamingService namingService;//nacos总服务端

    private static volatile List<Instance> instances;//获取到的所有服务实例

    //todo delete
    public final static AtomicInteger all = new AtomicInteger(0);//计数

    //todo countMap
    public final static Map<String, AtomicInteger> countMap = new ConcurrentHashMap<>(7);//计数

    private final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();//instances的读写锁

    private final static LoadBalance LOAD_BALANCE;//根据配置文件获取负载均衡实例

    private final static FaultTolerant FAULT_TOLERANT;//根据配置文件获取容错处理实例

    static {
        if(RANDOM_LOAD_BALANCE == LOAD_BALANCE_CODE){
            LOAD_BALANCE = new RandomLoadBalance();
            log.debug("开启随机策略");
        } else if (WEIGHT_LOAD_BALANCE == LOAD_BALANCE_CODE) {
            LOAD_BALANCE = new WeightLoadBalance();
            log.debug("开启权重策略");
        } else if (ROUND_ROBIN_LOAD_BALANCE == LOAD_BALANCE_CODE) {
            log.debug("开启轮询策略");
            LOAD_BALANCE = new RoundRobinLoadBalance();
        }else {
            log.debug("开启轮询策略");
            LOAD_BALANCE = new RoundRobinLoadBalance();
            throw new RpcException(RpcExceptionMsg.LOAD_BALANCE_NOT_FOUND);
        }
    }//初始化负载均衡类

    static {
        if(FAULT_TOLERANT_CODE == FaultTolerantCode.DO_NOTHING){
            FAULT_TOLERANT = new DoNothingFaultTolerant();
        } else if (FAULT_TOLERANT_CODE == FaultTolerantCode.RETRY) {
            FAULT_TOLERANT = new RetryFaultTolerant();
        } else if (FAULT_TOLERANT_CODE == FaultTolerantCode.TEST_OTHER) {
            FAULT_TOLERANT = new TestOtherFaultTolerant();
        }else {
            FAULT_TOLERANT = new TestOtherFaultTolerant();
            throw new RpcException(RpcExceptionMsg.FAULT_TOLERANT_NOT_FOUND);
        }
    }//初始化容错处理类

    //服务发现 只启动一次
    private static void findServer() throws NacosException {
        log.debug("启动服务发现");

        if(namingService == null){

            synchronized (ServerCenter.class){
                if(namingService != null){
                    return;
                }
                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.SERVER_ADDR, ClientConfig.SERVER_ADDR);
                properties.setProperty(PropertyKeyConst.PASSWORD, ClientConfig.PASSWORD);
                properties.setProperty(PropertyKeyConst.USERNAME, ClientConfig.USERNAME);

                namingService = NamingFactory.createNamingService(properties);

                instances = namingService.selectInstances(SERVER_NAME, SERVER_GROUP_NAME, SERVER_CLUSTER_NAME,true);
                log.debug("服务发现个数:{}", instances.size());
                if(instances == null || instances.size() == 0){
                    throw new RpcException(RpcExceptionMsg.NOT_FOUND_SERVER);
                }
                listenServer();

            }

        }



    }

    //监听服务实例变化
    private static void listenServer() throws NacosException {

        final ExecutorService executorService = Executors.newFixedThreadPool(1);

        EventListener serviceListener = new AbstractEventListener() {
            @Override
            public void onEvent(Event event) {
                if (event instanceof NamingEvent ) {
                    NamingEvent namingEvent = (NamingEvent) event;
                    List<Instance> list = namingEvent.getInstances();
                    List<Instance> collect = list.stream().filter(Instance::isHealthy).toList();
                    if(collect.size() == 0){
                        throw new RpcException(RpcExceptionMsg.NOT_FOUND_SERVER);
                    }
                    log.debug("服务实例发生变化, 现有健康服务个数为:{}", collect.size() );
                    updateInstances(collect);
                }
            }
            @Override
            public Executor getExecutor() {
                return executorService;
            }
        };
        namingService.subscribe(SERVER_NAME, SERVER_GROUP_NAME, SERVER_CLUSTER_NAME, serviceListener);
    }

    //更新实例 线程安全
    private static void updateInstances(List<Instance> list){
        rwl.writeLock().lock();
        HashSet<Instance> set = new HashSet<>(channelMap.keySet());
        set.forEach(ServerCenter::stopChannel);
        instances = list;
        for (Instance instance : list) {
           if(countMap.get(instance.getInstanceId()) == null){
               countMap.put(instance.getInstanceId(), new AtomicInteger());
           }
        }
        log.debug("更新instances");
        rwl.writeLock().unlock();
    }

    //获取负载均衡后的可用服务
    private static Instance getAvailableServer(){
        rwl.readLock().lock();
        try {
            return LOAD_BALANCE.loadBalancingAndGet(instances);
        } finally {
            rwl.readLock().unlock();
        }
    }


    //进行远程调用
    public static Object remoteInvoke(Request request) throws Throwable {
        return remoteInvoke(request, null);
    }
    //进行远程调用
    public static Object remoteInvoke(Request request, Instance instance) throws Throwable {
        if(namingService == null){
            findServer();
        }

        if(instance == null){
            instance = getAvailableServer();
        }
        Channel channel = getChannel(instance);

        DefaultPromise<Object> promise = new DefaultPromise<>(channel.eventLoop());

        ResponseHandler.getMap().put(request.getMsgId(), promise);

        channel.writeAndFlush(request);

        promise.await();

        if(promise.isSuccess()){
            log.debug("MsgId{}:成功获取结果!!", request.getMsgId());
            return promise.get();
        } else if(request.getIsFaultTolerant() == 0){
            log.debug("MsgId{}:进行容错处理!!", request.getMsgId());
            request.setIsFaultTolerant(1);
            return FAULT_TOLERANT.faultHandler(instance, promise.cause(), request);
        }else {
            log.debug("MsgId{}:容错后抛出错误", request.getMsgId());
            throw promise.cause();
        }
    }
    //channel复用
    private static Channel getChannel(Instance instance) throws InterruptedException {

        if (channelMap.containsKey(instance)){
            return channelMap.get(instance);
        }

        synchronized (instance){
            if (channelMap.containsKey(instance)){
                log.debug("进行channel复用");
                return channelMap.get(instance);
            }
            NioEventLoopGroup work = new NioEventLoopGroup(1);
            Channel channel = new Bootstrap()
                    .group(work)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());//解码
                            ch.pipeline().addLast(new RpcClientMsgCodec());//codec
                            ch.pipeline().addLast(new ResponseHandler());//响应处理器
                            //到达指定空闲时间触发事件
                            ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new IdleStateEventHandler(instance));//处理空闲事件

                        }
                    })
                    .connect(instance.getIp(), instance.getPort())
                    .sync()
                    .channel();

            channelMap.put(instance, channel);

            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                log.debug("关闭eventLoop");
                channelMap.remove(instance);
                future.channel().eventLoop().shutdownGracefully();
            });
            log.debug("连接到新channel");
            return channel;
        }

    }

    //停止channel
    public static void stopChannel(Instance instance){
        Channel channel = channelMap.get(instance);
        if(channel == null){
            return;
        }
        channel.close();
    }

    //获取与参数不同的实例
    public static Instance getOtherInstance(Instance instance){
        rwl.readLock().lock();
        try {
            for (Instance in : instances) {
                if(!in.equals(instance)){
                    return in;
                }
            }
            return null;
        } finally {
            rwl.readLock().unlock();
        }
    }


}
