package com.example.rpcclient.handler;

import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.server.ServerCenter;
import com.example.rpccommon.message.PingMsg;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/13 16:01
 * @Description 读空闲事件处理器 达到写空闲定时发送心跳包
 */
@Slf4j
public class WriteIdleEventHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        if(idleStateEvent.state() == IdleState.WRITER_IDLE){
            log.debug("发送心跳包, 间隔时间为{}秒", ClientConfig.PING_INTERVAL);
            ctx.channel().writeAndFlush(new PingMsg());
        }
        super.userEventTriggered(ctx, evt);
    }
}
