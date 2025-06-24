package com.example.rpcclient.filter;

import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpccommon.RpcContext;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @Author Cbc
 * @DateTime 2025/6/19 17:08
 * @Description 移除上下文 注意顺序
 */
public class ContextFilter implements InvokeFilter{
    @Override
    public Response doFilter(InstanceWrapper wrapper, Request request, InvokeFilterChain chain, int index) throws Throwable {
        Map<String, Object> attachment = request.getAttachment();
        Long term = (Long) RpcContext.getContext().get("term");
        attachment.put("term",term == null ? System.currentTimeMillis() + ClientConfig.OVERTIME * 1000 : term);
        Response response;
        try {
            response = chain.doFilter(wrapper, request, index);
        } finally {
            if(!RpcContext.getContext().isServer()){
                RpcContext.removeContext();
            }
        }
        Object res = response.getRes();
        if(res instanceof CompletableFuture<?>){
         ((CompletableFuture<?>) res).whenComplete((BiConsumer<Object, Throwable>) (o, throwable) -> RpcContext.removeContext());
        }
        return response;
    }
}
