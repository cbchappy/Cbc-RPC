package com.example.rpcserver.handler;

import com.example.rpccommon.message.PingAckMsg;
import com.example.rpccommon.message.PingMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2025/4/20 21:16
 * @Description
 */
@Slf4j
public class PingHandler extends SimpleChannelInboundHandler<PingMsg> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PingMsg msg) throws Exception {
        log.debug("收到客户端的心跳包，返回心跳确认包");
        ctx.channel().writeAndFlush(new PingAckMsg());
        ctx.fireChannelRead(msg);
    }
}
