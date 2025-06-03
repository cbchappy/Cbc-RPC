package com.example.rpcserver.handler;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2025/5/2 13:36
 * @Description 令牌桶限流
 */
public class MyRateLimiter {
    private final long perNa;//？微秒/个
    private long preTime;//上次更新时间
    private long tokens;//剩余数量
    private final int maxToken;


    public MyRateLimiter(long numPerSc, int maxToken, int initialToken){
        this.perNa = (long) (Math.pow(10, 6) / numPerSc);
        this.maxToken = maxToken;
        this.tokens = initialToken;
        this.preTime = System.nanoTime() / 1000;
    }

    public void acquire(){
        acquire(1);
    }

    public void acquire(int num){
        if(num < 1){
            num = 1;
        }
        long wait = fillAndGet(num);
        if(wait > 0){
            try {
                TimeUnit.MICROSECONDS.sleep(wait);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized long fillAndGet(int num){
        long now = System.nanoTime() / 1000;
        long sub = now - preTime;
        tokens = Math.min(maxToken, tokens + (sub / perNa));
        long rm = sub % perNa;
        preTime = now;
        if(tokens >= 0){
            tokens -= num;
            return 0;
        }
        tokens -= num;
        return -1 * tokens * perNa - rm;
    }

    public synchronized boolean tryAcquire(int num){
        long now = System.nanoTime() / 1000;
        long sub = now - preTime;
        tokens = Math.min(maxToken, tokens + (sub / perNa));
        long rm = sub % perNa;
        preTime = now - rm;
        if(tokens >= 0){
            tokens -= num;
           return true;
        }
        return false;
    }


}
