package com.example.rpcserver.handler;


import com.example.rpccommon.message.Request;
import com.example.rpccommon.util.BatchExecutorQueue;
import com.example.rpcserver.server.RpcServer;
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
        RpcServer.asyncExecute(() -> RpcServer.decodeAndHandler((ByteBuf) msg, ctx.channel()));
    }
}
