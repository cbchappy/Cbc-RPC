package com.example.rpcserver.handler;

import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpcserver.server.RpcServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 22:21
 * @Description 请求处理器
 */
@Slf4j
public class RequestHandler extends SimpleChannelInboundHandler<Request> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request msg){
        //todo 优化错误处理
        log.debug("msgId:{}的请求进入request处理器", msg.getMsgId());
        log.debug("请求接口:{}, 请求方法名:{}, 请求参数类型:{}",
                msg.getInterfaceName(), msg.getMethodName(), msg.getArgsClassNames() == null ? "" : msg.getArgsClassNames());

        Class<?> intefaceClass = null;
        Response response = Response.builder()
                .msgId(msg.getMsgId())
                .build();

        //获取接口
        try {
            intefaceClass = Class.forName(msg.getInterfaceName());
        } catch (ClassNotFoundException e) {
            log.debug(ResponseStatus.INTERFACE_NOT_FOUND.msg);
            response.setStatus(ResponseStatus.INTERFACE_NOT_FOUND.code);
            ctx.channel().writeAndFlush(response);
            throw new RpcException(e);
        }

        //获取接口实现类
        Object next = null;
        Class<?> aClass = null;
        try {
            next = RpcServer.getServiceImpl(intefaceClass);
            aClass = next.getClass();
        } catch (Exception e) {
            log.debug(ResponseStatus.IMPL_NOT_FOUND.msg);
            response.setStatus(ResponseStatus.IMPL_NOT_FOUND.code);
            ctx.channel().writeAndFlush(response);
            throw new RpcException(ResponseStatus.IMPL_NOT_FOUND.msg);
        }

        //完善方法参数类型
        String[] argsClassNames = msg.getArgsClassNames();
        String methodName = msg.getMethodName();
        Class<?>[] paramsType = null;
       if(argsClassNames != null && argsClassNames.length > 0){
           paramsType = new Class[argsClassNames.length];
           for (int i = 0; i < paramsType.length; i++) {
               try {
                   paramsType[i] = RequestHandler.class.getClassLoader().loadClass(argsClassNames[i]);
               } catch (ClassNotFoundException e) {
                   log.debug(ResponseStatus.ARGS_METHOD.msg);
                   response.setStatus(ResponseStatus.ARGS_METHOD.code);
                   ctx.channel().writeAndFlush(response);
                   throw new RpcException(e);
               }
           }
       }

       //获取方法
        Method method = null;
        try {
            method = aClass.getMethod(methodName, paramsType);
        } catch (NoSuchMethodException e) {
            log.debug(ResponseStatus.METHOD_NOT_FOUND.msg);
            response.setStatus(ResponseStatus.METHOD_NOT_FOUND.code);
            ctx.channel().writeAndFlush(response);
            throw new RpcException(e);
        }

        //调用方法
        try {
            Object res = method.invoke(next, msg.getArgs());
            response.setStatus(ResponseStatus.SUCCESS.code);
            response.setRes(res);
            response.setResClassName(res.getClass().getName());
            ctx.channel().writeAndFlush(response);
            log.debug("成功返回结果");
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.debug(ResponseStatus.SERVER_EXCEPTION.msg);
            response.setStatus(ResponseStatus.SERVER_EXCEPTION.code);
            ctx.channel().writeAndFlush(response);
            throw new RpcException(e);
        }

    }


    @Override//捕获错误 记录错误次数 判断是否要更新容错状态
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        log.debug("捕获到错误!!:{}, msg:{}", cause.getClass().getName(), cause.getMessage());
        RpcServer.exceptionCountAdd();
        RpcServer.updateFusing();
        super.exceptionCaught(ctx, cause);
    }

}
