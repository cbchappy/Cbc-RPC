package com.example.rpcclient.handler.preHandler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpccommon.message.Request;
import io.netty.channel.Channel;


/**
 * @Author Cbc
 * @DateTime 2025/4/22 16:06
 * @Description
 */
public interface PreHandler {
    void handler(Request rq, PreHandlerChain chain, int index);
}
