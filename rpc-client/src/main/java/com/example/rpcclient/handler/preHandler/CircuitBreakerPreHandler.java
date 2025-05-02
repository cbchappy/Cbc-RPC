package com.example.rpcclient.handler.preHandler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.server.InstanceService;
import com.example.rpcclient.server.InvokeCenter;
import com.example.rpcclient.tolerant.CircuitBreaker;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import io.netty.channel.Channel;


/**
 * @Author Cbc
 * @DateTime 2025/4/22 16:10
 * @Description
 */
public class CircuitBreakerPreHandler implements PreHandler{
    @Override
    public void handler(Request rq, PreHandlerChain chain, int index) {
        CircuitBreaker circuitBreaker = InstanceService.getCircuitBreaker(chain.getInstance());
        circuitBreaker.inPut(true, false);
    }
}
