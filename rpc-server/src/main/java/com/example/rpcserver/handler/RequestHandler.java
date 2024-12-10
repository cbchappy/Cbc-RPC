package com.example.rpcserver.handler;

import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ServiceLoader;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 22:21
 * @Description 请求处理器
 */
@Slf4j
public class RequestHandler extends SimpleChannelInboundHandler<Request> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request msg){
        log.debug("进入request处理器");
        Class<?> intefaceClass = null;
        Response response = Response.builder()
                .msgId(msg.getMsgId())
                .build();

        try {
            intefaceClass = Class.forName(msg.getInterfaceName());
        } catch (ClassNotFoundException e) {
            log.debug(ResponseStatus.INTERFACE_NOT_FOUND.msg);
            response.setStatus(ResponseStatus.INTERFACE_NOT_FOUND.code);
            ctx.channel().writeAndFlush(response);
            return;
        }

        ServiceLoader<?> load = ServiceLoader.load(intefaceClass);
        Object next = load.iterator().next();



        Class<?> aClass = next.getClass();
        String[] argsClassNames = msg.getArgsClassNames();
        String methodName = msg.getMethodName();
        Class<?>[] paramsType = null;

       if(argsClassNames != null && argsClassNames.length > 0){
           paramsType = new Class[argsClassNames.length];
           for (int i = 0; i < paramsType.length; i++) {
               try {
                   paramsType[i] = Class.forName(argsClassNames[i]);
               } catch (ClassNotFoundException e) {
                   log.debug(ResponseStatus.ARGS_METHOD.msg);
                   response.setStatus(ResponseStatus.ARGS_METHOD.code);
                   ctx.channel().writeAndFlush(response);
                   return;
               }
           }
       }

        Method method = null;
        try {
            method = aClass.getMethod(methodName, paramsType);
        } catch (NoSuchMethodException e) {
            log.debug(ResponseStatus.METHOD_NOT_FOUND.msg);
            response.setStatus(ResponseStatus.METHOD_NOT_FOUND.code);
            ctx.channel().writeAndFlush(response);
            return;
        }

        try {
            Object res = method.invoke(next, msg.getArgs());
            response.setStatus(ResponseStatus.SUCCESS.code);
            response.setRes(res);
            response.setResClassName(res.getClass().getName());
            ctx.channel().writeAndFlush(response);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.debug(ResponseStatus.SERVER_EXCEPTION.msg);
            response.setStatus(ResponseStatus.SERVER_EXCEPTION.code);
            ctx.channel().writeAndFlush(response);
        }

    }

}
