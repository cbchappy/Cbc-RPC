package com.example.rpcserver.handler;

import com.alibaba.fastjson.JSON;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import com.example.rpcserver.server.RpcServer;
import com.example.rpcserver.spring.runner.StartRpcServerRunner;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 22:21
 * @Description 请求处理器
 */
@Slf4j
@Component()
public class RequestHandler extends SimpleChannelInboundHandler<Request> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request msg){
        log.debug("msgId:{}的请求进入request处理器", msg.getMsgId());
        log.debug("请求接口:{}, 请求方法名:{}, 请求参数类型:{}",
                msg.getInterfaceName(), msg.getMethodName(), msg.getArgsClassNames() == null ? "" : msg.getArgsClassNames());

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
            throw new RpcException(e);
        }

        //先尝试获取bean
        Object next = null;
        next = StartRpcServerRunner.getBean(intefaceClass);


        //获取不到再spi加载
       if(next == null){
           log.debug("spring中没有获取到bean,进行spi加载");
           ServiceLoader<?> load = ServiceLoader.load(intefaceClass);
           next = load.iterator().next();
       }

       if(next == null){
           log.error("两种方式都没有获取到接口实现类");
           response.setStatus(ResponseStatus.SERVER_IMPL_NOT_FOUND.code);
           ctx.channel().writeAndFlush(response);
           throw new RpcException(ResponseStatus.SERVER_IMPL_NOT_FOUND.msg);
       }

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
                   throw new RpcException(e);
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
            throw new RpcException(e);
        }

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
        log.debug("捕获到错误!!:{}", cause.getClass().getName());
        RpcServer.exceptionCountAdd();
        RpcServer.updateFusing();
        super.exceptionCaught(ctx, cause);
    }

}
