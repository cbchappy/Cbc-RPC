package com.example.rpcclient.handler;

import com.example.rpccommon.message.PingAckMsg;
import com.example.rpccommon.message.PingMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2025/4/20 21:38
 * @Description
 */
public class PingAckHandler extends SimpleChannelInboundHandler<PingAckMsg> {
    public AtomicInteger num = new AtomicInteger(0);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PingAckMsg msg) throws Exception {
        num.set(0);
        ctx.fireChannelRead(msg);;
    }
}
