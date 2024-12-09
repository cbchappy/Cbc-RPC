package com.example.rpcclient.proxy;

import com.example.rpcclient.server.ServerCenter;
import com.example.rpccommon.message.Request;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 14:07
 * @Description 代理工厂 生成接口代理类以供进行消息调用
 */
@Slf4j
public class ProxyFactory{
    //复用channel 减少连接次数
    //通过负载均衡获取server
    //封装请求调用
    //注意唯一消息id
    public static Object createProxy(Class<?> interfaceClass){

        return Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(),
                new Class[]{interfaceClass}, new ProxyInvocationHandler());
    }

    private static class ProxyInvocationHandler implements InvocationHandler{

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
           log.debug(proxy.getClass().getInterfaces()[0].getName());
            Request request = Request.builder()
                    .methodName(method.getName())
                    .args(args)
                    .interfaceName(proxy.getClass().getInterfaces()[0].getName())
                    .build();
            return ServerCenter.remoteInvoke(request);
        }
    }
}
