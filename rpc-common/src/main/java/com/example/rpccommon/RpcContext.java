package com.example.rpccommon;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 14:32
 * @Description
 */
public class RpcContext {

    private Map<Object, Object> map = new HashMap<>();

    private static ThreadLocal<RpcContext> contextThreadLocal = ThreadLocal.withInitial(RpcContext::new);


    public static RpcContext getContext(){
        return contextThreadLocal.get();
    }

    public void put(Object key, Object value){
        this.map.put(key, value);
    }

    public Object remove(Object key){
        return this.map.remove(key);
    }

    public Object get(Object key){
        return this.map.get(key);
    }

}
