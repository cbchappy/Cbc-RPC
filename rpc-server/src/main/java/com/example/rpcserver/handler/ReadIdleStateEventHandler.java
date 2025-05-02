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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 13:09
 * @Description 进行读空闲处理, 断开空闲过久的连接
 */
@Slf4j
public class ReadIdleStateEventHandler extends ChannelDuplexHandler {
    private int num = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        if(idleStateEvent.state() == IdleState.READER_IDLE){
                num++;
                if(num == 3){
                    ctx.channel().close();
                }else {
                    log.debug("未接收到客户端消息达到{}秒, 给客户端发送心跳包", ServerConfig.READ_IDLE_TIME);
                    ctx.channel().writeAndFlush(new PingMsg());
                }
        }
        super.userEventTriggered(ctx, evt);
    }
}
