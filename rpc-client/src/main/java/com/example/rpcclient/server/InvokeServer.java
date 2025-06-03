package com.example.rpcclient.server;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.constants.FaultTolerantCode;
import com.example.rpcclient.filter.InvokeFilterChain;
import com.example.rpcclient.handler.InboundHandler;
import com.example.rpcclient.tolerant.Failover;
import com.example.rpcclient.tolerant.FaultTolerant;
import com.example.rpcclient.tolerant.Forking;
import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import com.example.rpccommon.message.PingAckMsg;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpccommon.message.RpcMsg;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import com.example.rpccommon.serializer.RpcSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

import static com.example.rpcclient.config.ClientConfig.FAULT_TOLERANT_CODE;
import static com.example.rpcclient.config.ClientConfig.OVERTIME;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 16:29
 * @Description //todo 事件监听解决最少调用负载均衡后置处理问题
 */
@Slf4j
public class InvokeServer {

    public static ExecutorService shareExecutor = Executors.newCachedThreadPool();//共享线程池

    private static FaultTolerant FAULT_TOLERANT;//根据配置文件获取容错处理实例

    private static ConcurrentHashMap<Integer, ThreadLessExecutor> exeMap = new ConcurrentHashMap<>();

    private static InvokeFilterChain chain;

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


        try {
            ThreadLessExecutor lessExecutor = new ThreadLessExecutor();
            exeMap.put(request.getMsgId(), lessExecutor);
            encodeAndWriteFlush(request, instance.getChannel());
            return (Response) lessExecutor.await(OVERTIME, TimeUnit.SECONDS);

        }catch (Exception e){
            log.error("任务报错id:{}", request.getMsgId());
            throw e;
        }
        finally {
            exeMap.remove(request.getMsgId());
        }
    }


    //channel复用
    private static void initializeChannel(InstanceWrapper wrapper) throws InterruptedException {
        Instance instance = wrapper.getInstance();

        Channel ch = wrapper.getChannel();
        if (ch != null) {
            return;
        }

        synchronized (wrapper.getLock()) {
            ch = wrapper.getChannel();
            if (ch != null) {
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
                            ch.pipeline().addLast(new InboundHandler());
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .connect(instance.getIp(), instance.getPort())
                    .sync()
                    .channel();

            wrapper.setChannel(ch);

        }

    }

    public static void encodeAndWriteFlush(RpcMsg msg, Channel channel) {
        ByteBuf out = channel.alloc().buffer();
        //版本 1
        out.writeByte(ProtocolConfig.getVersion());
        //魔数 4
        out.writeInt(ProtocolConfig.getMagic());
        //消息类型 1
        int msgTypeCode = msg.getTypeCode();
        out.writeByte(msgTypeCode);
        //额外信息码 1
        out.writeByte(0);
        //序列化方式 1
        byte serializerTypeCode = ClientConfig.SERIALIZER_TYPE_CODE.byteValue();
        out.writeByte(serializerTypeCode);

        if (msgTypeCode != RpcMsgTypeCode.REQUEST) {
            out.writeInt(0);
            out.writeInt(0);
            return;
        }

        //消息id 4
        out.writeInt(((Request) msg).getMsgId());
        RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);

        byte[] bytes = serializer.serialize(msg);
        //内容长度 4
        out.writeInt(bytes.length);
        out.writeBytes(bytes);

        channel.eventLoop().execute(() -> channel.writeAndFlush(out));
    }


    public static void decodeAndHandler(ByteBuf in, Channel channel) {
        //版本 1
        byte version = in.readByte();
        //魔数 4
        int magic = in.readInt();
        //消息类型 1
        byte msgTypeCode = in.readByte();//根据消息类型进行相应处理 1
        //额外信息码 1
        byte extra = in.readByte();
        //序列化方式码 1
        byte serializerTypeCode = in.readByte();// 1
        //消息id
        int msgId = in.readInt();

        if (msgTypeCode == RpcMsgTypeCode.PING_ACK) {
            //todo 更新空闲时间
            in.release();
            return;
        } else if (msgTypeCode == RpcMsgTypeCode.PINGMSG) {
            in.release();
            encodeAndWriteFlush(new PingAckMsg(), channel);
            return;
        }

        Callable<Object> callable = () -> {
            //长度 4
            int len = in.readInt();

            byte[] bytes = new byte[len];
            in.readBytes(bytes, 0, len);

            RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);
            Class<?> aClass = RpcMsg.getClassByTypeCode(msgTypeCode);
            Object o = serializer.deSerialize(bytes, aClass);

            in.release();

            return o;
        };

        ThreadLessExecutor lessExecutor = exeMap.remove(msgId);
        if (lessExecutor != null) {
            lessExecutor.aware(callable);
        }else {
            log.error("过期任务id：{}", msgId);
        }
    }


}
