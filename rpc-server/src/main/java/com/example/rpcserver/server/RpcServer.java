package com.example.rpcserver.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import com.example.rpcserver.handler.ReadIdleStateEventHandler;
import com.example.rpcserver.handler.RegistryHandler;
import com.example.rpcserver.handler.RequestHandler;
import com.example.rpcserver.protocol.RpcServerMsgCodec;
import com.example.rpcserver.registry.Registry;

import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import static com.example.rpcserver.config.RegistryConfig.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 15:34
 * @Description rpc服务中心
 */
@Slf4j
public class RpcServer {

    private static Channel channel = null;

    private static NioEventLoopGroup boss = null;

    private static NioEventLoopGroup workers = null;

    //开启服务并注册
    public static void startServer() throws InterruptedException, NacosException {
        log.debug("开启rpc服务");
        boss = new NioEventLoopGroup(BOSS_THREAD_NUM);
        workers = new NioEventLoopGroup(WORK_THREAD_NUM);

        ServerBootstrap bootstrap = new ServerBootstrap();

        channel = bootstrap.group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtocolFrameDecoder());
                        ch.pipeline().addLast(new RpcServerMsgCodec());
                        ch.pipeline().addLast(new RegistryHandler());//注册处理器
                        ch.pipeline().addLast(new IdleStateHandler(READ_IDLE_TIME, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new ReadIdleStateEventHandler());
                        ch.pipeline().addLast(new RequestHandler());
                    }
                })
                .bind(REGISTRY_PORT)
                .sync()
                .channel();


        Registry.registryServer();

    }

    //停止服务
    public static void stopServer() throws NacosException {

        Registry.removeRegistry();

        channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                boss.shutdownGracefully();
                workers.shutdownGracefully();
            }
        });

    }

    //暴露接口服务，允许接口被远程调用
    public static void openServiceImpl(Class<?> impLclass) throws IOException {
        String classPath = getClassPath(impLclass);



        Class<?>[] interfaces = impLclass.getInterfaces();

        log.debug("进行ISP注册, 接口名:{}, 实现类名:{}", interfaces[0], impLclass);

        File dir = new File(classPath, "META-INF/services");
        if(!dir.exists()){
            log.debug("创建目录:{}", dir.mkdirs());
        }

        Class<?> anInterface = interfaces[0];
        String fileName = anInterface.getName();

        File file = new File(dir.getPath(), fileName);

        if(!file.exists()){
            log.debug("创建文件:{}", file.createNewFile());
        }

        FileOutputStream stream = new FileOutputStream(file, true);
        stream.write(impLclass.getName().getBytes(StandardCharsets.UTF_8));
        stream.write('\n');
        stream.close();


    }

    //获取类路径
    private static String getClassPath(Class<?> impLclass){
        ClassLoader classLoader = impLclass.getClassLoader();
        URL resource = classLoader.getResource("");
        if (resource != null) {
            return resource.getPath();
        }
        return null;
    }
}
