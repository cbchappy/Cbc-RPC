package com.example.rpcclient.tolerant;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @Author Cbc
 * @DateTime 2025/4/22 16:02
 * @Description 熔断器
 */
public class CircuitBreaker {
    private final int minNum = 25;
    private final AtomicInteger totalNum = new AtomicInteger(0);
    public AtomicInteger exceptionNum = new AtomicInteger(0);
    public AtomicInteger state; //0 关闭   1 半开启  2 开启
    private final AtomicLong preTime = new AtomicLong(0);
    private final long continueTime = 10000;//熔断持续时间
    private final float threshold = 0.5f;//熔断因子
    public CircuitBreaker(){
        this.state = new AtomicInteger(0);
    }

    public boolean isRequest(){
        this.totalNum.incrementAndGet();
        if(totalNum.get() < minNum){
            return true;
        }
        int s = state.get();
        if(s == 0){
            return true;
        }
        if(s == 1){
            return false;
        }
        long p = preTime.get();
        long c =  System.currentTimeMillis();
        if(c - p < continueTime){
           return false;
        }
        if(preTime.compareAndSet(p, c)){
            state.set(1);
           return true;
        }
        return state.get() == 0;
    }

    public void isResponse(boolean isException){

        if(isException){
            int e = this.exceptionNum.incrementAndGet();
            int t = this.totalNum.get();
            if(t >= minNum && (float) e / t > threshold){
                this.preTime.set(System.currentTimeMillis());
                this.state.set(2);
            }
            return;
        }
        if(state.get() == 1){
            this.totalNum.set(0);
            this.exceptionNum.set(0);
            state.set(0);
        }
    }

}
