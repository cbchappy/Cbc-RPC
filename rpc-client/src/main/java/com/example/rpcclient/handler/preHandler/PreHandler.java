package com.example.rpcclient.handler.preHandler;

import io.netty.channel.Channel;


/**
 * @Author Cbc
 * @DateTime 2025/4/22 16:06
 * @Description
 */
public interface PreHandler {
    Object handler(Channel channel, Object rq, PreHandlerChain chain, int index);
}
