package com.example.rpcclient.tolerant;

import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpcclient.server.InvokeServer;
import com.example.rpcclient.server.LoadBalanceServer;
import com.example.rpccommon.message.Request;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

/**
 * @Author Cbc
 * @DateTime 2025/4/20 15:47
 * @Description 并行请求
 */
public class Forking implements FaultTolerant{
    @Override
    public Object faultHandler(InstanceWrapper wrapper, Throwable cause, Request request) {
        CountDownLatch count = new CountDownLatch(2);
        final Object[] res = {null};
        final Throwable[] tbs_1 = {null};
        final Throwable[] tbs_2 = {null};
       CompletableFuture.supplyAsync(new ForkingSupplier<>(count, wrapper, request, res, tbs_1));

       Request rq2 = Request.builder()
               .args(request.getArgs())
               .methodName(request.getMethodName())
               .build();
       rq2.setArgsClassNames(request.getArgsClassNames());

        InstanceWrapper nst = LoadBalanceServer.getOtherInstance(wrapper);
        CompletableFuture.supplyAsync(new ForkingSupplier<>(count, nst, rq2, res, tbs_2));

        try {
            count.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(tbs_1[0] != null && tbs_2[0] != null){
            throw (RuntimeException)tbs_1[0];
        }

        return res[0];
    }

    private static class ForkingSupplier<T> implements Supplier<T>{
        private CountDownLatch count;
        private InstanceWrapper wrapper;
        private Request request;
        private Object[] res;
        private Throwable[] tbs;
        public ForkingSupplier(CountDownLatch count, InstanceWrapper wrapper, Request request, Object[] res, Throwable[] tbs){
            this.count = count;
            this.wrapper = wrapper;
            this.request = request;
            this.res = res;
            this.tbs = tbs;
        }

        @Override
        public T get() {
            try {
                Object o = InvokeServer.doFilterAndGet(request, wrapper);

                res[0] = o;
                count.countDown();
                return (T)o;
            } catch (Throwable e) {
                tbs[0] = e;
                throw new RuntimeException(e);
            }finally {
                count.countDown();
            }
        }
    }
}
