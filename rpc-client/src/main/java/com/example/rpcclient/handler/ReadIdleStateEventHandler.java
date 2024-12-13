package com.example.rpcclient.handler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.config.ClientConfig;
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
public class ReadIdleStateEventHandler extends ChannelDuplexHandler {
    private final Instance instance;

    public ReadIdleStateEventHandler(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
        if(idleStateEvent.state() == IdleState.READER_IDLE){
            log.debug("连接达到{}秒, 开始断开连接", ClientConfig.CONNECT_IDLE_TIME);
            ServerCenter.stopChannel(instance);
        }
        super.userEventTriggered(ctx, evt);
    }
}
