package com.example.rpcclient.blance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 16:14
 * @Description  随机策略
 */
public class RandomLoadBalance implements LoadBalance{

    @Override
    public Instance loadBalancingAndGet(List<Instance> instances) {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        int i = current.nextInt(instances.size());
        return instances.get(i);
    }
}
