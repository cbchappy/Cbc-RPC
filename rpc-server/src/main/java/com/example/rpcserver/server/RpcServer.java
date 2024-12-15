package com.example.rpcserver.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import com.example.rpcserver.config.ServerConfig;
import com.example.rpcserver.handler.FusingHandler;
import com.example.rpcserver.handler.ReadIdleStateEventHandler;
import com.example.rpcserver.handler.RegistryHandler;
import com.example.rpcserver.handler.RequestHandler;
import com.example.rpcserver.protocol.RpcServerMsgCodec;
import com.example.rpcserver.registry.Registry;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.rpcserver.config.ServerConfig.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 15:34
 * @Description rpc服务中心
 */
@Slf4j
public class RpcServer {
    private final static AtomicInteger count = new AtomicInteger(0);//计数服务被调用的次数

    private final static AtomicBoolean isFusing = new AtomicBoolean(false);//判断是否是熔断状态
    private final static AtomicInteger exceptionCount = new AtomicInteger(0);//记录错误次数

    private static Channel channel = null;

    private static NioEventLoopGroup boss = null;

    private static NioEventLoopGroup workers = null;

    //开启服务并注册
    public static void startServer() throws InterruptedException, NacosException {
        log.debug("开启rpc服务");
        boss = new NioEventLoopGroup(BOSS_THREAD_NUM);
        workers = new NioEventLoopGroup(WORK_THREAD_NUM);

        //初始化数据
        count.set(0);
        isFusing.set(false);
        exceptionCount.set(0);

        ServerBootstrap bootstrap = new ServerBootstrap();

        channel = bootstrap.group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //head
                        ch.pipeline().addLast(new ProtocolFrameDecoder());//ltc解码器
                        ch.pipeline().addLast(new RpcServerMsgCodec());//ByteBuf编解码
                        ch.pipeline().addLast(new FusingHandler());//熔断处理器
                        ch.pipeline().addLast(new RegistryHandler());//注册处理器
                        ch.pipeline().addLast(new IdleStateHandler(READ_IDLE_TIME, 0, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new ReadIdleStateEventHandler());//读空闲处理器
                        ch.pipeline().addLast(new RequestHandler());//请求处理器
                        //tail
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
        if (!dir.exists()) {
            log.debug("创建目录:{}", dir.mkdirs());
        }

        Class<?> anInterface = interfaces[0];
        String fileName = anInterface.getName();

        File file = new File(dir.getPath(), fileName);

        if (!file.exists()) {
            log.debug("创建文件:{}", file.createNewFile());
        }

        FileOutputStream stream = new FileOutputStream(file, true);
        stream.write(impLclass.getName().getBytes(StandardCharsets.UTF_8));
        stream.write('\n');
        stream.close();


    }

    //获取类路径
    private static String getClassPath(Class<?> impLclass) {
        ClassLoader classLoader = impLclass.getClassLoader();
        URL resource = classLoader.getResource("");
        if (resource != null) {
            return resource.getPath();
        }
        return null;
    }

    //获取熔断状态
    public static boolean isFusing() {
        return isFusing.get();
    }

    //错误次数加一
    public static int exceptionCountAdd() {
        return exceptionCount.addAndGet(1);
    }

    //总次数加一
    public static void countAdd() {
        count.addAndGet(1);
    }

    //更新熔断状态 注意线程安全 双重检查
    public static void updateFusing() throws NacosException {
        int c = count.get();
        if (c >= FUSING_START_NUM && (double) exceptionCount.get() / c >= FUSING_DIVISOR) {
            if(isFusing.get()){
                return;
            }
            synchronized (count){
                if(isFusing.get()){
                    return;
                }
                log.debug("-----服务开启熔断-----");
                isFusing.set(true);

                NioEventLoopGroup executors = new NioEventLoopGroup(1);
                stopServer();
                executors.schedule(() -> {
                    try {
                        startServer();
                        executors.shutdownGracefully();
                    } catch (InterruptedException | NacosException e) {
                        throw new RuntimeException(e);
                    }
                }, FUSING_RESTART_TIME, TimeUnit.SECONDS);

            }
        }
    }
}
