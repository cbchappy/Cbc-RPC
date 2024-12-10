package com.example.rpcserver;

import ch.qos.logback.core.joran.action.IADataForComplexProperty;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import com.example.rpcserver.handler.ReadIdleStateEventHandler;
import com.example.rpcserver.handler.RequestHandler;
import com.example.rpcserver.protocol.RpcServerMsgCodec;
import com.example.rpcserver.server.RpcServer;
import com.example.rpcserver.server.STImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * @Author Cbc
 * @DateTime 2024/12/8 19:09
 * @Description
 */
@Slf4j
public class ServerTest {

    public static void main(String[] args) throws Exception {
        RpcServer.openServiceImpl(STImpl.class);

    }

    public static void test1() throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup workers = new NioEventLoopGroup(2);
        ServerBootstrap bootstrap = new ServerBootstrap();
        Channel channel = bootstrap.group(boss, workers)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtocolFrameDecoder());
                        ch.pipeline().addLast(new RpcServerMsgCodec());
                        ch.pipeline().addLast(new RequestHandler());
                        ch.pipeline().addLast(new IdleStateHandler(3, 0, 0));
                        ch.pipeline().addLast(new ReadIdleStateEventHandler());
                    }
                }).bind(8080)
                .sync()
                .channel();
    }
}
