package com.example.test;


import com.example.rpccommon.util.MyWheelTimer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author Cbc
 * @DateTime 2025/5/6 19:19
 * @Description
 */
public class Main {

    public static void main(String[] args) throws Exception {
//        ClientBootstrap.initializeClient();
        MyWheelTimer timer = new MyWheelTimer(200, 65);
        long now = System.currentTimeMillis();

        AtomicLong num = new AtomicLong(0);
        AtomicLong exceptionNum = new AtomicLong(0);
        for(int i = 0; i < 200000; i++){
            int v = 400;
            final int finalI = i;
            timer.addTask(() -> {
                long s = System.currentTimeMillis() - now;
                long c = s - v;
                c = Math.abs(c);
                if(c > 200){
                    exceptionNum.incrementAndGet();
                }
                num.incrementAndGet();
                if(num.get() == 200000){
                    System.out.println(finalI);
                    System.out.println("exceptionNum是：" + exceptionNum.get() + "   num是:" +  num.get());
                }

            }, v, TimeUnit.MILLISECONDS);
        }

    }
//
}
//192.168.51.92