package com.example.rpcclient.filter;

import com.example.rpcclient.listener.InvokeCompleteEvent;
import com.example.rpcclient.listener.InvokeEventPublisher;
import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 20:19
 * @Description 事件触发过滤器
 */
public class InvokeEventFilter implements InvokeFilter{
    @Override
    public Response doFilter(InstanceWrapper wrapper, Request request, InvokeFilterChain chain, int index) throws Throwable {

        Response response = null;
        InvokeCompleteEvent event = InvokeCompleteEvent.builder()
                .request(request)
                .wrapper(wrapper)
                .build();
        try {
            response = chain.doFilter(wrapper, request, index);
            event.setResponse(response);
        } catch (Throwable e) {
            event.setThrowable(e);
            throw e;
        } finally {
           InvokeEventPublisher.publishEvent(event);
        }

        return response;
    }
}
