package com.example.rpccommon;

import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Response;
import com.example.rpccommon.util.MyWheelTimer;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 14:32
 * @Description
 */
public class RpcContext {
    private static final MyWheelTimer TIMER = new MyWheelTimer(200, 512);

    @Getter @Setter
    private boolean isServer;

    private final Map<Object, Object> map = new HashMap<>();

    private static final ThreadLocal<RpcContext> contextThreadLocal = ThreadLocal.withInitial(RpcContext::new);

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

    public static  <T> CompletableFuture<T> asyncInvoke(Callable<T> call){
        try {
            RpcContext.getContext().put("async", true);
            Object res = call.call();
            Object o = RpcContext.getContext().get("future");
            if(o instanceof CompletableFuture){
                CompletableFuture<T> future = (CompletableFuture<T>) o;
                CompletableFuture<T> futureRes = new CompletableFuture<>();
                 future = future.whenComplete((t, throwable) -> {
                  if(throwable != null){
                      futureRes.completeExceptionally(throwable);
                      return;
                  }
                     Response rsp = (Response) t;
                  if(!rsp.isSuccess()){
                      futureRes.completeExceptionally(new RuntimeException("异步调用错误"));
                      return;
                  }
                  futureRes.complete((T) rsp.getRes());
                 });
                return futureRes;
            }

            return (CompletableFuture<T>) CompletableFuture.completedFuture(res);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }finally {
            RpcContext.removeContext();
        }
    }

    public static void restoreContext(RpcContext src, RpcContext target){
        target.map.putAll(src.map);
    }

    public static void removeContext(){
        contextThreadLocal.remove();
    }

    public static MyWheelTimer getTimer(){
        return TIMER;
    }
}
