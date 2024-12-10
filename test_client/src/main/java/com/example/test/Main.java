package com.example.test;

import com.example.rpcclient.proxy.ProxyFactory;
import com.example.rpcclient.server.ServerCenter;
import com.example.test.service.TestServer;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 18:03
 * @Description
 */
@Slf4j
public class Main {

    public static void main(String[] args) throws InterruptedException {
       TestServer testServer = (TestServer) ProxyFactory.createProxy(TestServer.class);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(8, 8, 6,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        testServer.get();
        for (int i = 0; i < 5000; i++) {
            executor.execute(testServer::get);
        }
        Thread.sleep(6000);
        ServerCenter.countMap.forEach((s, integer) -> System.out.println(s + ":" + integer));
        final int[] i = {0};
        ServerCenter.countMap.values().forEach(integer -> i[0] += integer.get());
        System.out.println("MapAll:" + Arrays.toString(i));
        System.out.println("AtomicAll:" + ServerCenter.all.get());
    }
}
