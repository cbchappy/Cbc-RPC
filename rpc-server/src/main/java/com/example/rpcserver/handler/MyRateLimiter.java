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
    private final long perNa;//？纳秒/个
    private long preTime;//上次更新时间
    private int tokens;//剩余数量
    private final int maxToken;

    public MyRateLimiter(long perNa, int maxToken, int initialToken){
        this.perNa = perNa;
        this.maxToken = maxToken;
        this.tokens = initialToken;
        this.preTime = System.nanoTime();
    }

    public synchronized void acquire(){
        //补充令牌
        long wait = fillToken();
        //获取令牌
        if(tokens > 0){
            tokens--;
            return;
        }
        //等待
        try {
            TimeUnit.NANOSECONDS.sleep(wait);
            fillToken();
            tokens--;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    private long fillToken(){
        long now = System.nanoTime();
        long sub = now - preTime;
        long add = sub / perNa;
        long y = perNa - sub % perNa;
        tokens = (int) Math.min(maxToken, tokens + add);
        preTime = now;
        return y;
    }
}
