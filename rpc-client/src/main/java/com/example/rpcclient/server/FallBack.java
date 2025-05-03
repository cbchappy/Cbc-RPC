package com.example.rpcclient.server;

import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Cbc
 * @DateTime 2025/5/3 15:52
 * @Description 降级处理类
 */
@Slf4j
public class FallBack {
    private static ConcurrentHashMap<Class<?>, Object> map = new ConcurrentHashMap<>();

    public static void registry(Class<?> cls,  Object obj){
        log.debug("注册降级处理实例");
        map.put(cls, obj);
    }

    public static Object mock(Request rq) {
        try {
            log.debug("降级");
            Class<?> aClass = Class.forName(rq.getInterfaceName());
            Object o = map.get(aClass);
            if(o == null){
                throw new RpcException("熔断，无法请求");
            }
            String[] names = rq.getArgsClassNames();
            Class<?>[] cs = new Class[names == null ? 0 : names.length];
            if(cs.length > 0){
                for (int i = 0; i < cs.length; i++) {
                    cs[i] = Class.forName(names[i]);
                }
            }
            Method method = aClass.getMethod(rq.getMethodName(), cs);
            return method.invoke(o, rq.getArgs());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
