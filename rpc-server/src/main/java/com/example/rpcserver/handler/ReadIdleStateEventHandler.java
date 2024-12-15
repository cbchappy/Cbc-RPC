package com.example.rpcserver.handler;

import com.example.rpccommon.message.PingMsg;
import com.example.rpcserver.config.ServerConfig;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

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
            log.debug("未接收到客户端消息达到{}秒, 开始断开客户端连接", ServerConfig.READ_IDLE_TIME);
            ctx.channel().close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }
}
