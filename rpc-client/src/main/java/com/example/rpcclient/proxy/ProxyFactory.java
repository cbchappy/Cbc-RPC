package com.example.rpcclient.proxy;

import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.server.InvokeServer;
import com.example.rpccommon.RpcContext;
import com.example.rpccommon.message.Request;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 14:07
 * @Description 代理工厂 生成指定接口的代理类以供进行远程调用
 */
@Slf4j
public class ProxyFactory{
//    static {
//        CommonUtil.printLogo();
//    }

    public static <T> T createProxy(Class<T> interfaceClass){
        log.debug("调用createProxy创建代理, {}", interfaceClass);

        return (T) Proxy.newProxyInstance(ProxyFactory.class.getClassLoader(),
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
            request.setSerializeCode(ClientConfig.SERIALIZER_TYPE_CODE.byteValue());
            request.setRetryNum(ClientConfig.RETRY_NUM);
            Object res = InvokeServer.invoke(request);
            if(!(res instanceof CompletableFuture<?>)){
                return res;
            }
            RpcContext.getContext().put("future", res);
            return null;
        }
    }
}
