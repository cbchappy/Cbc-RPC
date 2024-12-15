package com.example.rpcclient.handler;

import com.example.rpccommon.message.CloseMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/15 19:47
 * @Description 服务主动关闭处理器
 */
@Slf4j
public class ServerCloseHandler extends SimpleChannelInboundHandler<CloseMsg> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloseMsg msg) throws Exception {
        log.debug("服务器主动关闭channel");
        ctx.channel().attr(AttributeKey.valueOf("close")).set(msg);
    }
}
