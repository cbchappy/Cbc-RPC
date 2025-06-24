package com.example.rpcserver.filter;

import com.example.rpccommon.RpcContext;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import io.netty.channel.Channel;

/**
 * @Author Cbc
 * @DateTime 2025/6/22 21:23
 * @Description
 */
public class RpcContextFilter implements ServerFilter{
    @Override
    public Response doFilter(Request request, Channel channel, ServerFilterChain chain, int index) {
        try {
            RpcContext.getContext().setServer(true);
            return chain.doFilter(request, channel, index);
        } finally {
            RpcContext.removeContext();
        }
    }
}
