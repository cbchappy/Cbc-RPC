package com.example.rpcclient.filter;

import com.example.rpcclient.server.FallBack;
import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpcclient.tolerant.CircuitBreaker;
import com.example.rpccommon.RpcContext;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
                        .rqId(request.getRqId())
                        .build();
            }
            Response response = chain.doFilter(wrapper, request, index);
            if(!response.isAsync()){
                circuitBreaker.isResponse(!response.getStatus().equals(ResponseStatus.SUCCESS.code));
                return response;
            }
            CompletableFuture<?> future = (CompletableFuture<?>) response.getRes();
            future.thenApply((Function<Object, Object>) o -> {
                Response rsp = (Response) o;
                circuitBreaker.isResponse(!rsp.getStatus().equals(ResponseStatus.SUCCESS.code));
                return o;
            });

            return response;
    }
}
