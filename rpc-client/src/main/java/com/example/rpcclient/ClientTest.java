package com.example.rpcclient;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.rpcclient.handler.IdleStateEventHandler;
import com.example.rpcclient.handler.PingMsgHandler;
import com.example.rpcclient.handler.ResponseHandler;
import com.example.rpcclient.protocol.RpcClientMsgCodec;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:32
 * @Description 处理器先后顺序不能错乱
 */
@Slf4j
public class ClientTest {

    public static void main(String[] args) throws NacosException, InterruptedException {
        NioEventLoopGroup work = new NioEventLoopGroup(1);
        Channel channel = new Bootstrap()
                .group(work)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtocolFrameDecoder());//解码
                        ch.pipeline().addLast(new RpcClientMsgCodec());//codec
                        ch.pipeline().addLast(new IdleStateHandler(0, 8, 0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new IdleStateEventHandler(null));
                        ch.pipeline().addLast(new ResponseHandler());//响应处理器
                        ch.pipeline().addLast(new PingMsgHandler());//心跳处理器
                    }
                })
                .connect("localhost", 8080)
                .sync()
                .channel();
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().eventLoop().shutdownGracefully();
                log.debug("关闭channel!!!!");
            }
        });

        channel.writeAndFlush(Request.builder().build());

    }
}
