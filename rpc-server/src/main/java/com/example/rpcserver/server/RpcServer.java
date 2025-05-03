package com.example.rpcserver.server;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import com.example.rpccommon.util.CommonUtil;
import com.example.rpcserver.config.ServerConfig;
import com.example.rpcserver.factory.DefaultServiceImplFactory;
import com.example.rpcserver.factory.ServiceImplFactory;
import com.example.rpcserver.handler.*;
import com.example.rpcserver.protocol.RpcServerMsgCodec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.RegistryHandler;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.rpcserver.config.ServerConfig.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 15:34
 * @Description rpc服务中心 服务注册和断开  管理服务熔断状态 开放和获取实现类  策略模式
 */
@Slf4j
public class RpcServer {

    private static Channel channel = null;//总服务通道

    private static NioEventLoopGroup boss = null;

    private static NioEventLoopGroup workers = null;

    private static NamingService namingService;//nacos服务端

    private static Instance instance;//服务实例

    private static ServiceImplFactory implFactory = new DefaultServiceImplFactory();//远程调用服务类实现工厂

    private static ExecutorService executorService = Executors.newFixedThreadPool(200);//业务线程池



    public static void setServiceImplFactory(ServiceImplFactory implFactory){
        RpcServer.implFactory = implFactory;
    }


    //开启服务并注册
    public static void startServer() throws InterruptedException, NacosException {
        log.debug("开启rpc服务并注册到nacos");
        boss = new NioEventLoopGroup(BOSS_THREAD_NUM);
        workers = new NioEventLoopGroup(WORK_THREAD_NUM);

        ServerBootstrap bootstrap = new ServerBootstrap();

        channel = bootstrap.group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //head
                        ch.pipeline().addLast(new ProtocolFrameDecoder());//ltc解码器
                        ch.pipeline().addLast(new RpcServerMsgCodec());//ByteBuf编解码
                        ch.pipeline().addLast(new IdleStateHandler(READ_IDLE_TIME, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new ReadIdleStateEventHandler());//读空闲处理器
                        ch.pipeline().addLast(new RequestHandler());//请求处理器
                        ch.pipeline().addLast(new PingHandler());//ping处理器
                        //tail
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .bind(REGISTRY_PORT)
                .sync()
                .channel();

        registryToNacos();

    }

    //停止服务
    public static void stopServer() throws NacosException {

        removeFromNacos();

        channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                boss.shutdownGracefully();
                workers.shutdownGracefully();
            }
        });

    }


    //选择开放的接口
    public static void openServiceImpl(Class<?> implClass, Class<?> interfaceClass) throws IOException {
        implFactory.openServiceImpl(implClass, interfaceClass);
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

    //从nacos移除服务
    private static void removeFromNacos() throws NacosException {
        log.debug("从nacos移除服务, serverName:{}, groupName:{}, ip:{}, port:{}, cluster:{}",
                REGISTRY_SERVER_NAME, GROUP_NAME, REGISTRY_IP, REGISTRY_PORT, CLUSTER_NAME);
        namingService.deregisterInstance(REGISTRY_SERVER_NAME, instance);
    }

    public static void asyncExecute(Runnable runnable){
        executorService.execute(runnable);
    }
}
