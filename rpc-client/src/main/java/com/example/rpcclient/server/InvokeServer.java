package com.example.rpcclient.server;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.constants.FaultTolerantCode;
import com.example.rpcclient.filter.InvokeFilterChain;
import com.example.rpcclient.handler.InboundHandler;
import com.example.rpcclient.handler.ReadIdleStateEventHandler;
import com.example.rpcclient.tolerant.Failover;
import com.example.rpcclient.tolerant.FaultTolerant;
import com.example.rpcclient.tolerant.Forking;
import com.example.rpccommon.RpcContext;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.*;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import com.example.rpccommon.serializer.RpcSerializer;
import com.example.rpccommon.util.MyBatchQueue;
import com.example.rpccommon.util.RPCCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static com.example.rpcclient.config.ClientConfig.FAULT_TOLERANT_CODE;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 16:29
 * @Description
 */
@Slf4j
public class InvokeServer {

    private static FaultTolerant FAULT_TOLERANT;//根据配置文件获取容错处理实例

    private static final ConcurrentHashMap<Long, ThreadLessExecutor> exeMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, CompletableFuture<Object>> futureMap = new ConcurrentHashMap<>();

    private static InvokeFilterChain chain;

    private static final ExecutorService shareExecutor = Executors.newCachedThreadPool();


    public static void initialize() {

        //初始化容错处理类
        if (FAULT_TOLERANT_CODE == FaultTolerantCode.RETRY) {
            FAULT_TOLERANT = new Failover();
        } else if (FAULT_TOLERANT_CODE == FaultTolerantCode.FORK) {
            FAULT_TOLERANT = new Forking();
        } else {
            FAULT_TOLERANT = new Failover();
        }
        //创建过滤链
        chain = InvokeFilterChain.createChain();

    }


    //进行容错处理并远程调用
    public static Object invoke(Request request) throws Throwable {
        InstanceWrapper wrapper = LoadBalanceServer.loadBalancingAndGet();
        return FAULT_TOLERANT.faultHandler(wrapper, null, request);
    }

    public static Response doFilterAndGet(Request request, InstanceWrapper instance) throws Throwable {
        initializeChannel(instance);
        return chain.doFilter(instance, request, 0);
    }

    public static Response doRemoteInvoke(Request request, InstanceWrapper instance) throws Throwable {
        long over = (Long)request.getAttachment().get("term") - System.currentTimeMillis();
        if(over < 0){
            throw new RuntimeException("请求调用超时!");
        }
        //异步
        if(RpcContext.getContext().get("async") != null){
            CompletableFuture<Object> future = new CompletableFuture<>();
            RpcContext context = RpcContext.getContext();
            future = future.whenComplete((o, throwable) -> {
                RpcContext.restoreContext(context, RpcContext.getContext());
            });
            futureMap.put(request.getRqId(), future);

            //设置定时任务，防止内存泄露
            RpcContext.getTimer().addTask(new Runnable() {
                @Override
                public void run() {
                    CompletableFuture<Object> remove = futureMap.remove(request.getRqId());
                    if(remove != null){
                        remove.completeExceptionally(new RuntimeException("RPC调用超时!!"));
                    }
                }
            }, over, TimeUnit.MILLISECONDS);

            writeRequest(request, instance);
            context.put("future", future);

           return Response.builder()
                    .res(future)
                    .rqId(request.getRqId())
                    .isAsync(true)
                    .build();
        }


        //同步
        try {
            ThreadLessExecutor lessExecutor = new ThreadLessExecutor();
            exeMap.put(request.getRqId(), lessExecutor);
            writeRequest(request, instance);
            return (Response) lessExecutor.await(over, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            log.error("任务报错id:{}", request.getRqId());
            throw e;
        }
        finally {
            exeMap.remove(request.getRqId());
        }
    }


    //channel复用
    private static void initializeChannel(InstanceWrapper wrapper) throws InterruptedException {
        Instance instance = wrapper.getInstance();

        Channel ch = wrapper.getChannel();
        if (ch != null &&  ch.isActive()) {
            return;
        }

        synchronized (wrapper.getLock()) {
            ch = wrapper.getChannel();
            if (ch != null && ch.isActive()) {
                return;
            }
            NioEventLoopGroup worker = new NioEventLoopGroup(1);
            ch = new Bootstrap()
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());//解码
                            ch.pipeline().addLast(new IdleStateHandler(ClientConfig.READER_IDLE_TIME / 3, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new ReadIdleStateEventHandler());//读空闲处理器
                            ch.pipeline().addLast(new InboundHandler(wrapper));
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .connect(instance.getIp(), instance.getPort())
                    .sync()
                    .channel();

            wrapper.setChannel(ch);
            wrapper.setQueue(new MyBatchQueue(ch));

        }

    }


    public static void writeRequest(Request request, InstanceWrapper wrapper){
        ByteBuf buf = RPCCodec.encodeRequest(request, wrapper.getChannel());
        wrapper.getQueue().enqueue(buf);
    }

    public static void PackHandle(ByteBuf in, InstanceWrapper wrapper){
        Channel channel = wrapper.getChannel();
        RPCCodec.Pack pack = RPCCodec.decodePack(channel, in);
        Byte msgType = pack.getMsgType();

        if(msgType.equals(RPCCodec.responseCode)){
            Long rqId = pack.getRqId();
            if(exeMap.containsKey(rqId)){
                threadLessHandle(pack);
            } else if (futureMap.containsKey(rqId)) {
               futureHandle(pack);
            }
            return;
        }
        if(msgType.equals(RPCCodec.pingCode)){
            ByteBuf buf = RPCCodec.encodePingAckMsg(channel);
            wrapper.getQueue().enqueue(buf);
            return;
        }

        if(msgType.equals(RPCCodec.pingAckCode)){
            return;
        }

        channel.close();
        throw new RpcException("错误消息类型!");
    }

    private static void threadLessHandle(RPCCodec.Pack pack){

        Callable<Object> callable = () -> {

            byte[] bytes = pack.getData();

            RpcSerializer serializer = RpcSerializer.getSerializerByCode(pack.getSerializeCode());

            return serializer.deSerialize(bytes, Response.class);
        };

        ThreadLessExecutor lessExecutor = exeMap.remove(pack.getRqId());
        if (lessExecutor != null) {
            lessExecutor.aware(callable);
        }else {
            log.error("过期任务id：{}", pack.getRqId());
        }
    }

    private static void futureHandle(RPCCodec.Pack pack){

        Runnable runnable = () -> {
            CompletableFuture<Object> future = futureMap.remove(pack.getRqId());
            byte[] bytes = pack.getData();

            RpcSerializer serializer = RpcSerializer.getSerializerByCode(pack.getSerializeCode());
            Response response = serializer.deSerialize(bytes, Response.class);
            future.complete(response);
        };

        shareExecutor.execute(runnable);

    }


}
