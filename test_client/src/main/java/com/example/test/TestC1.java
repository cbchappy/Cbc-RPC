package com.example.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @Author Cbc
 * @DateTime 2025/3/13 14:15
 * @Description
 */
public class TestC1 {

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup worker = new NioEventLoopGroup(1);
        Channel channel = bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TestHandler());
                        ch.pipeline().addLast(new TestHandler());
                    }
                })
                .connect(new InetSocketAddress("localhost", 5555))
                .sync()
                .channel();
        ByteBuf buffer = channel.alloc().buffer(12);
        buffer.writeInt(12);
        channel.writeAndFlush(buffer);

        while (true){
            Thread.sleep(2000);
            System.out.println("open:" + channel.isOpen());
            System.out.println( "isWritable:"+channel.isWritable());
        }


//        channel.writeAndFlush("client---1");
//        Thread.sleep(5000);
//        channel.writeAndFlush("client---2");
//        channel.close().addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                System.out.println("close");
//            }
//        });
    }
}
