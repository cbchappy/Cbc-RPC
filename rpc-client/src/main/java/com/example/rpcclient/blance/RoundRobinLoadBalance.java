package com.example.rpcclient.blance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 16:00
 * @Description 轮询策略
 */
public class RoundRobinLoadBalance implements LoadBalance{

    private final static AtomicInteger index = new AtomicInteger(0);

    @Override
    public Instance loadBalancingAndGet(List<Instance> instances) {
        int i = index.getAndIncrement() % instances.size();
        return instances.get(i);
    }

    public static RoundRobinLoadBalance getInstance(){
        return Singleton.instance;
    }

    private static class Singleton{
        public static RoundRobinLoadBalance instance = new RoundRobinLoadBalance();
    }
}
