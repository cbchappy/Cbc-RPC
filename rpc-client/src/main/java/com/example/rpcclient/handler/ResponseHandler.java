package com.example.rpcclient.handler;

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

/**
 * @Author Cbc
 * @DateTime 2024/12/9 20:29
 * @Description 响应处理器
 */
@Slf4j
public class ResponseHandler extends SimpleChannelInboundHandler<Response> {

    private static final Map<Integer, DefaultPromise<Object>> promiseMap = new ConcurrentHashMap<>();

    public static Map<Integer, DefaultPromise<Object>> getMap(){
        return promiseMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        log.debug("处理response");
        Integer msgId = msg.getMsgId();

        DefaultPromise<Object> promise = promiseMap.remove(msgId);

        Integer status = msg.getStatus();

        if(Objects.equals(status, ResponseStatus.SUCCESS.code)){
            promise.setSuccess(msg.getRes());
            return;
        }

        if(status < 500){
           promise.setFailure(new RpcRequestException(ResponseStatus.getEnumByCode(status)));
        }else {
            promise.setFailure(new RpcResponseException(ResponseStatus.getEnumByCode(status)));
        }

    }
}
