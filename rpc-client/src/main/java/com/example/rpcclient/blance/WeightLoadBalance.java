package com.example.rpcclient.blance;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 18:20
 * @Description 根据权重调整策略
 */
public class WeightLoadBalance implements LoadBalance{
    @Override
    public Instance loadBalancingAndGet(List<Instance> instances) {
        int len = instances.size();
        double[] arr = new double[len];
        arr[0] = instances.get(0).getWeight();
        for (int i = 1; i < len; i++) {
            arr[i] = arr[i - 1] + instances.get(i).getWeight();
        }
        ThreadLocalRandom current = ThreadLocalRandom.current();
        double random = current.nextDouble(arr[len - 1]);

        //二分法提高效率
        int l = 0;
        int r = len - 1;
        while (l <= r){
            int mid = (l + r) / 2;
            if(arr[mid] < random){
                l = mid + 1;
            }else {
                r = mid - 1;
            }
        }

        return instances.get(l);
    }
}
