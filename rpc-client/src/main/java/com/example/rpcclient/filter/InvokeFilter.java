package com.example.rpcclient.filter;

import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;



/**
 * @Author Cbc
 * @DateTime 2025/5/18 16:46
 * @Description
 */
public interface InvokeFilter {
    Response doFilter(InstanceWrapper wrapper,  Request request, InvokeFilterChain chain, int index) throws Throwable;
}
