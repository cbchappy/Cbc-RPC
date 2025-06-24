package com.example.rpcclient.handler;

import com.example.rpcclient.config.ClientConfig;
import com.example.rpccommon.message.PingMsg;
import com.example.rpccommon.util.RPCCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 13:09
 * @Description 进行读空闲处理, 断开空闲过久的连接
 */
@Slf4j
public class ReadIdleStateEventHandler extends ChannelDuplexHandler {
    private  int num = 0;
    private final int interval = ClientConfig.READER_IDLE_TIME / 3;

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        num = 0;
        super.read(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        if(idleStateEvent.state() == IdleState.READER_IDLE){
                num = num + 1;
                if(num == 3){
                    ctx.channel().close();
                }else {
                    log.debug("未接收到客户端消息达到{}秒, 给客户端发送心跳包", interval);
                    ByteBuf buf = RPCCodec.encodePingMsg(ctx.channel());
                    ctx.channel().writeAndFlush(buf);
                }
        }
        super.userEventTriggered(ctx, evt);
    }
}
