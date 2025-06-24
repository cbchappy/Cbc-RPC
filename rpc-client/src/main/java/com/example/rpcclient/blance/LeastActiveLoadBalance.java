package com.example.rpcclient.blance;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.listener.InvokeCompleteEvent;
import com.example.rpcclient.listener.InvokeEvent;
import com.example.rpcclient.listener.InvokeEventListener;
import com.example.rpcclient.listener.InvokeEventPublisher;
import com.example.rpcclient.server.InstanceManageCenter;
import com.example.rpcclient.server.InstanceWrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author Cbc
 * @DateTime 2025/5/3 15:01
 * @Description 最少活跃调用
 */
public class LeastActiveLoadBalance implements LoadBalance{
    private final ConcurrentHashMap<InstanceWrapper, AtomicLong> countMap;

    public LeastActiveLoadBalance(){
        countMap = new ConcurrentHashMap<>();

        InvokeEventListener.addListener(new InvokeEventListener() {
            public void listen(InvokeEvent event){
                InvokeCompleteEvent completeEvent = (InvokeCompleteEvent) event;
                countMap.get(completeEvent.getWrapper()).decrementAndGet();
            }
        }, InvokeCompleteEvent.class);

    }

    @Override
    public InstanceWrapper loadBalancingAndGet(List<InstanceWrapper> instances) {
        long num = Long.MAX_VALUE;
        InstanceWrapper in = null;
        double w = Integer.MAX_VALUE;
        for (InstanceWrapper key : instances) {
            AtomicLong v = countMap.get(key);
            if(v == null){
                v = new AtomicLong(0);
                countMap.put(key, v);
            }
            long g = v.get();
            if(g < num || (g == num && w < key.getInstance().getWeight())){
                num = g;
                w = key.getInstance().getWeight();
                in = key;
            }
        }
        countMap.get(in).incrementAndGet();//+1
        return in;
    }

}
