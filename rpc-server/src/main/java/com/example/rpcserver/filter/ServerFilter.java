package com.example.rpcserver.filter;

import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import io.netty.channel.Channel;


/**
 * @Author Cbc
 * @DateTime 2025/6/2 15:31
 * @Description
 */
public interface ServerFilter {
    Response doFilter(Request request, Channel channel, ServerFilterChain chain, int index);
}
