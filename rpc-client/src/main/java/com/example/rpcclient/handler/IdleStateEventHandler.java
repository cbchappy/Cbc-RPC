package com.example.rpcclient.handler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.server.ServerCenter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 20:13
 * @Description 写空闲事件处理器
 */
@Slf4j
public class IdleStateEventHandler extends ChannelDuplexHandler {
    private final Instance instance;

    public IdleStateEventHandler(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("写空闲");
        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        if(idleStateEvent.state() == IdleState.WRITER_IDLE){
            log.debug("处理写空闲事件");
            ServerCenter.stopChannel(instance);
        }
        super.userEventTriggered(ctx, evt);
    }
}
