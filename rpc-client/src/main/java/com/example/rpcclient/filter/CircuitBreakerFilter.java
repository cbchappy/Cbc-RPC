package com.example.rpcclient.filter;

import com.example.rpcclient.server.FallBack;
import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpcclient.tolerant.CircuitBreaker;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 16:55
 * @Description 熔断降级过滤器
 */
public class CircuitBreakerFilter implements InvokeFilter{
    @Override
    public Response doFilter(InstanceWrapper wrapper, Request request, InvokeFilterChain chain, int index) throws Throwable {

        CircuitBreaker circuitBreaker = wrapper.getBreaker();

            boolean isCan = circuitBreaker.isRequest();
            if(!isCan){
                Object mock = FallBack.mock(request);
                return Response.builder()
                        .res(mock)
                        .status(ResponseStatus.MOCK.code)
                        .msgId(request.getMsgId())
                        .build();
            }

            Response response = chain.doFilter(wrapper, request, index);
            circuitBreaker.isResponse(!response.getStatus().equals(ResponseStatus.SUCCESS.code));
            return response;
    }
}
