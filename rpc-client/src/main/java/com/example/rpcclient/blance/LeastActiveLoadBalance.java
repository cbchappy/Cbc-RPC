package com.example.rpcclient.blance;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.server.InstanceManageCenter;
import com.example.rpcclient.server.InstanceWrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author Cbc
 * @DateTime 2025/5/3 15:01
 * @Description 最少活跃调用  //todo 添加事件监听
 */
public class LeastActiveLoadBalance implements LoadBalance{
    private final ConcurrentHashMap<Instance, AtomicLong> countMap;

    public LeastActiveLoadBalance(){
        countMap = new ConcurrentHashMap<>();
        InstanceManageCenter.addObserver((oldList, newList) -> {
            if(newList != null){
                for (Instance instance : newList) {
                    if(!countMap.containsKey(instance)){
                        countMap.put(instance, new AtomicLong(0));
                    }
                }
            }
            if(oldList != null){
                for (Instance instance : oldList) {
                    if(newList != null && !newList.contains(instance)){
                        countMap.remove(instance);
                    }
                }
            }
        });
    }

    @Override
    public InstanceWrapper loadBalancingAndGet(List<InstanceWrapper> instances) {
        long num = Long.MAX_VALUE;
        Instance in = null;
        double w = Integer.MAX_VALUE;
        for (Map.Entry<Instance, AtomicLong> entry : countMap.entrySet()) {
            long g = entry.getValue().get();
            Instance key = entry.getKey();
            if(g < num || (g == num && w < key.getWeight())){
                num = g;
                w = key.getWeight();
                in = key;
            }
        }
        countMap.get(in).incrementAndGet();//+1
        for (InstanceWrapper wrapper : instances) {
            if(wrapper.getInstance() == in){
                return wrapper;
            }
        }
        return null;
    }
    public void subOne(Instance instance){
        AtomicLong aLong = countMap.get(instance);
        if(aLong != null){
            aLong.decrementAndGet();
        }
    }
}
