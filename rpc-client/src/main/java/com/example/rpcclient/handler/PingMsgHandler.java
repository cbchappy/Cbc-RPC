package com.example.rpcclient.handler;

import com.example.rpccommon.message.PingMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 12:20
 * @Description
 */
@Slf4j
public class PingMsgHandler extends SimpleChannelInboundHandler<PingMsg> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PingMsg msg) throws Exception {
        log.debug("返回心跳包");
        ctx.channel().writeAndFlush(new PingMsg());
    }
}
