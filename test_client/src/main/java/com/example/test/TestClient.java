package com.example.test;

import com.alibaba.fastjson.JSON;
import com.example.rpcclient.proxy.ProxyFactory;
import com.example.test.service.TestRpc;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 18:03
 * @Description test
 */
@Slf4j
public class TestClient {

    public static void main(String[] args) throws InterruptedException {
        TestRpc proxy = ProxyFactory.createProxy(TestRpc.class);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10,
                1000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        for (int i = 0; i < 200; i++) {
            executor.execute(proxy::get);
        }
    }



    public class MyThreadFactory implements ThreadFactory{
        AtomicInteger integer = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("cbcThread-" + integer.getAndIncrement());
            return thread;
        }
    }

    public static class P extends Thread{
        private Semaphore l;
        private Semaphore r;
        private String n;
        private int num = 0;
        @Override
        public void run() {
            try {
               while (num < 5){
                   l.acquire();
                   r.acquire();
                   System.out.println("<-->"+ n +"<-->");
                   l.release();
                   r.release();
                   Thread.sleep(1000);
                   num++;
               }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public P(Semaphore l, Semaphore r, String n){
            this.l = l;
            this.r = r;
            this.n = n;
        }


    }


    public static enum Te{
        A,
        B;

        public void p(){
            System.out.println("llll");
        }
    }




}
