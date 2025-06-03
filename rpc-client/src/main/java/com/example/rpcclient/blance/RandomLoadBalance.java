package com.example.rpcclient.blance;

import com.example.rpcclient.server.InstanceWrapper;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 16:14
 * @Description  随机策略
 */
public class RandomLoadBalance implements LoadBalance{

    @Override
    public InstanceWrapper loadBalancingAndGet(List<InstanceWrapper> instances) {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        int i = current.nextInt(instances.size());
        return instances.get(i);
    }
}
