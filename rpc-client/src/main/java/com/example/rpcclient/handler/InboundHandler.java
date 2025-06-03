package com.example.rpcclient.handler;

import com.example.rpcclient.server.InvokeServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author Cbc
 * @DateTime 2025/5/4 15:50
 * @Description
 */
public class InboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                InvokeServer.decodeAndHandler((ByteBuf) msg, ctx.channel());
    }
}
