package com.example.rpcclient.filter;

import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpcclient.server.InvokeServer;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 16:45
 * @Description
 */
public class InvokeFilterChain {
    private final List<InvokeFilter> filterList = new ArrayList<>();
    public void addFilter(InvokeFilter filter){
        filterList.add(filter);
    }

    public Response doFilter(InstanceWrapper wrapper, Request request, int index) throws Throwable {
        if(index < filterList.size()){
            InvokeFilter filter = filterList.get(index);
            return filter.doFilter(wrapper, request, this,index + 1);
        }
        return InvokeServer.doRemoteInvoke(request, wrapper);
    }

    public static InvokeFilterChain createChain(){
        InvokeFilterChain chain = new InvokeFilterChain();
        chain.addFilter(new TraceFilter());
        chain.addFilter(new InvokeEventFilter());
        chain.addFilter(new CircuitBreakerFilter());
        return chain;
    }
}
