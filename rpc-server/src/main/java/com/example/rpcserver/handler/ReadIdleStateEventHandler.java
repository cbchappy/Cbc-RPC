package com.example.rpcserver.handler;

import com.example.rpccommon.message.PingMsg;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 13:09
 * @Description
 */
@Slf4j
public class ReadIdleStateEventHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        if(idleStateEvent.state() == IdleState.READER_IDLE){
            log.debug("触发读空闲, 进行关闭客户端channel");
            ctx.channel().close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }
}
