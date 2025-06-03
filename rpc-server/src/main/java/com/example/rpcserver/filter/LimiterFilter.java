package com.example.rpcserver.filter;

import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpcserver.handler.MyRateLimiter;
import io.netty.channel.Channel;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 15:56
 * @Description
 */
public class LimiterFilter implements ServerFilter{
    private final MyRateLimiter limiter = new MyRateLimiter(100000, 100, 50);
    @Override
    public Response doFilter(Request request, Channel channel, ServerFilterChain chain, int index) {
        if(!limiter.tryAcquire(1)){
            return Response.builder()
                    .status(ResponseStatus.LIMITER.code)
                    .msgId(request.getMsgId())
                    .build();
        }
        return chain.doFilter(request, channel, index);
    }
}
