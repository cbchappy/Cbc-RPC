package com.example.rpcserver.server;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import com.example.rpccommon.constants.SerializerCode;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.*;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import com.example.rpccommon.serializer.RpcSerializer;
import com.example.rpccommon.util.BatchExecutorQueue;
import com.example.rpccommon.util.MyBatchQueue;
import com.example.rpccommon.util.RPCCodec;
import com.example.rpccommon.util.SpanReportClient;
import com.example.rpcserver.config.ServerConfig;
import com.example.rpcserver.factory.DefaultServiceImplFactory;
import com.example.rpcserver.factory.ServiceImplFactory;
import com.example.rpcserver.filter.ServerFilterChain;
import com.example.rpcserver.handler.InboundHandler;
import com.example.rpcserver.handler.ReadIdleStateEventHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.*;

import static com.example.rpcserver.config.ServerConfig.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 15:34
 * @Description rpc服务中心 服务注册和断开  管理服务熔断状态 开放和获取实现类  策略模式
 */
@Slf4j
public class RpcServer {
    private static ServerFilterChain filterChain = ServerFilterChain.createChain();

    private static Channel serverChannel = null;//总服务通道

    private static NioEventLoopGroup boss = null;

    private static NioEventLoopGroup workers = null;
    private static ConcurrentHashMap<String, Object[]> methodMap = new ConcurrentHashMap<>();

    private static NamingService namingService;//nacos服务端

    private static Instance instance;//服务实例

    private static ConcurrentHashMap<Channel, MyBatchQueue> queueMap = new ConcurrentHashMap<>();

    private static ServiceImplFactory implFactory = new DefaultServiceImplFactory();//远程调用服务类实现工厂

    private static final ExecutorService executorService = new ThreadPoolExecutor(200, 200,
            0L, TimeUnit.MILLISECONDS,
            new SynchronousQueue<>(), (r, executor) -> {
                throw new RpcException("任务溢出!!!");
            });//业务线程池


    public static void setServiceImplFactory(ServiceImplFactory implFactory){
        RpcServer.implFactory = implFactory;
    }


    //开启服务并注册
    public static void startServer() throws InterruptedException, NacosException {
        log.debug("开启rpc服务并注册到nacos");
        boss = new NioEventLoopGroup(BOSS_THREAD_NUM);
        workers = new NioEventLoopGroup(WORK_THREAD_NUM);//
        ServerBootstrap bootstrap = new ServerBootstrap();
        serverChannel = bootstrap.group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //head
                        ch.pipeline().addLast(new ProtocolFrameDecoder());//ltc解码器
                        ch.pipeline().addLast(new IdleStateHandler(READ_IDLE_TIME, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new ReadIdleStateEventHandler());//读空闲处理器
                        ch.pipeline().addLast(new InboundHandler());
                        queueMap.put(ch, new MyBatchQueue(ch));
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .bind(REGISTRY_PORT)
                .sync()
                .channel();

        if(TRACE){
            SpanReportClient.startReport();
        }

        registryToNacos();

    }

    //获取实现
    public static Object getServiceImpl(Class<?> interfaceClass){
        return implFactory.getServiceImpl(interfaceClass);
    }

    //注册服务到nacos
    private static void registryToNacos() throws NacosException {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, ServerConfig.SERVER_ADDR);
        properties.setProperty(PropertyKeyConst.PASSWORD, ServerConfig.PASSWORD);
        properties.setProperty(PropertyKeyConst.USERNAME, ServerConfig.USERNAME);

        namingService = NamingFactory.createNamingService(properties);

        instance = new Instance();
        instance.setClusterName(CLUSTER_NAME);
        instance.setIp(REGISTRY_IP);
        instance.setPort(REGISTRY_PORT);
        instance.setWeight(WEIGHT);
        namingService.registerInstance(REGISTRY_SERVER_NAME, GROUP_NAME , instance);
        log.debug("注册服务到nacos, serverName:{}, groupName:{}, ip:{}, port:{}, cluster:{}, weight:{}",
                REGISTRY_SERVER_NAME, GROUP_NAME, REGISTRY_IP, REGISTRY_PORT, CLUSTER_NAME, WEIGHT);
    }



    public static Response handleRequest(Request msg, Channel channel){

        Class<?> intefaceClass = null;
        Response response = Response.builder()
                .rqId(msg.getRqId())
                .serializeCode(msg.getSerializeCode())
                .build();

        String key = msg.getInterfaceName() +
                "-" +
                msg.getMethodName() +
                Arrays.toString(msg.getArgsClassNames());

        Object[] objects = methodMap.get(key);
        if(objects != null){
            Method m = (Method) objects[0];
            //调用方法
            try {
                Object res = m.invoke(objects[1], msg.getArgs());
                response.setStatus(ResponseStatus.SUCCESS.code);
                response.setRes(res);
                response.setResClassName(res.getClass().getName());
                log.debug("成功返回结果");
                return response;
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.debug(ResponseStatus.SERVER_EXCEPTION.msg);
                response.setStatus(ResponseStatus.SERVER_EXCEPTION.code);
                response.setThrowable(e);
                return response;
            }


        }

        //获取接口
        try {
            String interfaceName = msg.getInterfaceName();
            intefaceClass = Class.forName(interfaceName);
        } catch (ClassNotFoundException e) {
            log.debug(ResponseStatus.INTERFACE_NOT_FOUND.msg);
            response.setStatus(ResponseStatus.INTERFACE_NOT_FOUND.code);
            response.setThrowable(e);
            return response;
        }

        //获取接口实现类
        Object next = null;
        Class<?> aClass = null;
        try {
            next = RpcServer.getServiceImpl(intefaceClass);
            aClass = next.getClass();
        } catch (Exception e) {
            log.debug(ResponseStatus.IMPL_NOT_FOUND.msg);
            response.setStatus(ResponseStatus.IMPL_NOT_FOUND.code);
            response.setThrowable(e);
            return response;
        }

        //完善方法参数类型
        String[] argsClassNames = msg.getArgsClassNames();
        String methodName = msg.getMethodName();
        Class<?>[] paramsType = null;
        if(argsClassNames != null && argsClassNames.length > 0){
            paramsType = new Class[argsClassNames.length];
            for (int i = 0; i < paramsType.length; i++) {
                try {
                    paramsType[i] = Class.forName(argsClassNames[i]);
                } catch (ClassNotFoundException e) {
                    log.debug(ResponseStatus.ARGS_METHOD.msg);
                    response.setStatus(ResponseStatus.ARGS_METHOD.code);
                    response.setThrowable(e);
                    return response;
                }
            }
        }

        //获取方法
        Method method;
        try {
            method = aClass.getMethod(methodName, paramsType);
        } catch (NoSuchMethodException e) {
            log.debug(ResponseStatus.METHOD_NOT_FOUND.msg);
            response.setStatus(ResponseStatus.METHOD_NOT_FOUND.code);
            response.setThrowable(e);
            return response;
        }
        Object[] v = {method, next};
        methodMap.put(key, v);

        //调用方法
        try {
            Object res = method.invoke(next, msg.getArgs());
            response.setStatus(ResponseStatus.SUCCESS.code);
            response.setRes(res);
            response.setResClassName(res.getClass().getName());
            log.debug("成功返回结果");
            return response;
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.debug(ResponseStatus.SERVER_EXCEPTION.msg);
            response.setStatus(ResponseStatus.SERVER_EXCEPTION.code);
            response.setThrowable(e);
            return response;
        }

    }

    public static void writeResponse(Response response, Channel channel){
        ByteBuf buf = RPCCodec.encodeResponse(response, channel);
        MyBatchQueue queue = queueMap.get(channel);
        queue.enqueue(buf);
    }

    public static void asyncHandlePack(ByteBuf buf, Channel channel){
        executorService.execute(() -> handlePack(buf, channel));
    }

   private static void handlePack(ByteBuf buf, Channel channel){
        RPCCodec.Pack pack = RPCCodec.decodePack(channel, buf);
        Byte serializeCode = pack.getSerializeCode();
        Byte msgType = pack.getMsgType();

        if(msgType.equals(RPCCodec.requestCode)){
            RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializeCode);
            Request request = serializer.deSerialize(pack.getData(), Request.class);
            request.setRqId(pack.getRqId());
            request.setSerializeCode(pack.getSerializeCode());
            Response response = filterChain.doFilter(request, channel, 0);
            writeResponse(response, channel);
            return;
        }

        if(msgType.equals(RPCCodec.pingCode)){
            queueMap.get(channel).enqueue(RPCCodec.encodePingAckMsg(channel));
            return;
        }

        if(msgType.equals(RPCCodec.pingAckCode)){
            return;
        }

        channel.close();
        throw new RpcException("错误消息类型!");
    }
}
