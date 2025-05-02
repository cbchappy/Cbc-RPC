package com.example.rpcclient.handler;

import com.example.rpcclient.server.InvokeCenter;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.exception.RpcRequestException;
import com.example.rpccommon.exception.RpcResponseException;
import com.example.rpccommon.message.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 20:29
 * @Description 响应处理器
 */
@Slf4j
public class ResponseHandler extends SimpleChannelInboundHandler<Response> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        log.debug("处理response");
        ExecutorService executorService = InvokeCenter.EXECUTOR_SERVICE;
        executorService.execute(() -> InvokeCenter.POSTCHAIN_MAP.get(ctx.channel()).doHandle(msg, 0));
        ctx.fireChannelRead(msg);
    }


}
