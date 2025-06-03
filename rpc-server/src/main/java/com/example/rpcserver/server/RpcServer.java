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
import com.example.rpccommon.util.SpanReportClient;
import com.example.rpcserver.config.ServerConfig;
import com.example.rpcserver.factory.DefaultServiceImplFactory;
import com.example.rpcserver.factory.ServiceImplFactory;
import com.example.rpcserver.filter.ServerFilterChain;
import com.example.rpcserver.handler.InboundHandler;
import com.example.rpcserver.handler.ReadIdleStateEventHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
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

                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(REGISTRY_PORT)
                .sync()
                .channel();
        //todo 启动链路追踪收集
        SpanReportClient.startReport();

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


    public static void asyncExecute(Runnable runnable){
        executorService.execute(runnable);
    }


    public static void encodeAndWriteFlush(RpcMsg msg, Channel channel){

                ByteBuf out = channel.alloc().buffer();
                //从上下文获取 序列化方式 1
                Object o = channel.attr(AttributeKey.valueOf("serializerTypeCode")).get();
                //获取序列化方式
                byte serializerTypeCode = o == null ? SerializerCode.JDK.byteValue() : (byte) o;

                //版本号 1
                out.writeByte(ProtocolConfig.getVersion());
                //魔数 4
                out.writeInt(ProtocolConfig.getMagic());
                //请求类型 1
                int msgTypeCode = msg.getTypeCode();
                out.writeByte(msgTypeCode);
                //额外消息码
                out.writeByte(0);
                //序列化方式 1
                out.writeByte(serializerTypeCode);

                if(msgTypeCode != RpcMsgTypeCode.RESPONSE){
                    out.writeInt(0);
                    out.writeInt(0);
                    channel.writeAndFlush(out);
                    return;
                }

                out.writeInt(((Response) msg).getMsgId());

                RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);

                byte[] bytes = serializer.serialize(msg);
                //内容长度 4
                out.writeInt(bytes.length);

                out.writeBytes(bytes);

                log.debug("编码, magic:{}, version:{}, msgTypeCode:{}, serializerTypeCode:{}, len:{}",
                        ProtocolConfig.getMagic(), ProtocolConfig.getVersion(), msgTypeCode, serializerTypeCode, bytes.length);

                channel.eventLoop().execute(() -> channel.writeAndFlush(out));



    }



    public static void decodeAndHandler(ByteBuf in, Channel channel){

        log.debug("ByteBuf-in的长度:{}", in.readableBytes());

        byte version = in.readByte();//版本

        int magic = in.readInt();//魔数

        //校验失败
        if(magic !=  ProtocolConfig.getMagic() || version != ProtocolConfig.getVersion()){
            log.error("协议校验失败, 关闭channel");
            channel.writeAndFlush(new CloseMsg(CloseMsg.CloseStatus.protocolError));
            channel.close();
            in.clear();//清除, 表示已经读完了, 不用自己释放, 本身有释放的功能
            return;
        }

        byte msgTypeCode = in.readByte();//消息类型

        byte extra = in.readByte();//额外

        byte serializerTypeCode = in.readByte();//序列化方式

        int msgId = in.readInt();//消息id

        int len = in.readInt();//长度

        if(msgTypeCode == RpcMsgTypeCode.PINGMSG){
            channel.writeAndFlush(new PingAckMsg());
            return;
        }

        //将序列化方式添加进参数
        channel.attr(AttributeKey.valueOf("serializerTypeCode")).set(serializerTypeCode);


        byte[] bytes = new byte[len];
        in.readBytes(bytes, 0, len);

        log.debug("解码, magic:{}, version:{}, msgTypeCode:{}, serializerTypeCode:{}, len:{}",
                magic, version, msgTypeCode, serializerTypeCode, len);

        RpcSerializer serializer = RpcSerializer.getSerializerByCode(serializerTypeCode);
        Class<?> aClass = RpcMsg.getClassByTypeCode(msgTypeCode);
        Object o = serializer.deSerialize(bytes, aClass);
        in.release();
        //过滤并处理
        Response response = filterChain.doFilter((Request) o, channel, 0);

        encodeAndWriteFlush(response, channel);

    }

    public static Response handlerRequest(Request msg, Channel channel){

        Class<?> intefaceClass = null;
        Response response = Response.builder()
                .msgId(msg.getMsgId())
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
                RpcServer.encodeAndWriteFlush(response, channel);
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
}
