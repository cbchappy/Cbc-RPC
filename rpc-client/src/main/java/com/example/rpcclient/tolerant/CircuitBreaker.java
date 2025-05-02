package com.example.rpcclient.tolerant;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpccommon.exception.RpcException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @Author Cbc
 * @DateTime 2025/4/22 16:02
 * @Description 熔断器
 */
public class CircuitBreaker {
    private final int minNum = 20;
    private final AtomicInteger totalNum = new AtomicInteger(0);
    public AtomicInteger exceptionNum = new AtomicInteger(0);
    public AtomicInteger state; //0 关闭   1 半开启  2 开启
    private final AtomicLong preTime = new AtomicLong(0);
    private final long continueTime = 10000;//熔断持续时间
    private  float threshold = 0.5f;//熔断因子
    public CircuitBreaker(){
        this.state = new AtomicInteger(0);
    }

    public void inPut(boolean isRequest, boolean isException){
        if(isRequest){
            isRequest();
        }else {
            isResponse(isException);
        }
    }

    private void isRequest(){
        this.totalNum.incrementAndGet();
        if(totalNum.get() < minNum){
            return;
        }
        int s = state.get();
        if(s == 0){
            return;
        }
        if(s == 2){
            throw new RpcException("处于熔断状态，无法发送请求！");
        }
        long p = preTime.get();
        long c =  System.currentTimeMillis();
        if(c - p < continueTime){
            throw new RpcException("处于熔断状态，无法发送请求！");
        }
        if(preTime.compareAndSet(p, c)){
            state.set(1);
           return;
        }
        if(state.get() != 0){
            throw new RpcException("处于熔断状态，无法发送请求！");
        }
    }

    private void isResponse(boolean isException){

        if(isException){
            int e = this.exceptionNum.incrementAndGet();
            int t = this.totalNum.get();
            if((float) e / t > threshold){
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
