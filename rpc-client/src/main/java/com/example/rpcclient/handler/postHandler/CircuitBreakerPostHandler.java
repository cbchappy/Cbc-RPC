package com.example.rpcclient.handler.postHandler;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.server.InstanceService;
import com.example.rpcclient.tolerant.CircuitBreaker;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Response;

/**
 * @Author Cbc
 * @DateTime 2025/4/22 16:54
 * @Description
 */
public class CircuitBreakerPostHandler implements PostHandler{
    @Override
    public void handler(Response response, PostHandlerChain chain, int index) {
        Instance instance = chain.getInstance();
        CircuitBreaker circuitBreaker = InstanceService.getCircuitBreaker(instance);
        circuitBreaker.inPut(false, !response.getStatus().equals(ResponseStatus.SUCCESS.code));
    }
}
