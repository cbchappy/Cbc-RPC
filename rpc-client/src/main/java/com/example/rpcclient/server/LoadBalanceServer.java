package com.example.rpcclient.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.blance.*;
import com.example.rpcclient.tolerant.CircuitBreaker;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.rpcclient.config.ClientConfig.LOAD_BALANCE_CODE;
import static com.example.rpcclient.constants.LoadBalanceTypeCode.*;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 16:08
 * @Description
 */
@Slf4j
public class LoadBalanceServer {
    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();//instances的读写锁

    private static List<InstanceWrapper> instances = new ArrayList<>();

    public  static LoadBalance LOAD_BALANCE;//根据配置文件获取负载均衡实例

    //初始化负载均衡
    public static void initialize(){
        List<Instance> list = InstanceManageCenter.getInstances();
        for (Instance instance : list) {
            InstanceWrapper wrapper = InstanceWrapper.builder()
                    .breaker(new CircuitBreaker())
                    .lock(new Object())
                    .channel(null)
                    .instance(instance)
                    .build();
            instances.add(wrapper);
        }
        InstanceManageCenter.addObserver((oldList, newList) -> {
            log.info("load_oberver");
            rwl.writeLock().lock();
            List<InstanceWrapper> newInstances = new ArrayList<>(newList.size());
            try {
                for (InstanceWrapper instanceWrapper : instances) {
                    if(newList.contains(instanceWrapper.getInstance())){
                        newInstances.add(instanceWrapper);
                    }else {
                        //关闭连接
                        Channel channel = instanceWrapper.getChannel();
                        if(channel != null){
                            channel.close();
                        }
                    }
                }
                for (Instance instance : newList) {
                    if(!oldList.contains(instance)){
                        InstanceWrapper wrapper = InstanceWrapper.builder()
                                .breaker(new CircuitBreaker())
                                .lock(new Object())
                                .channel(null)
                                .instance(instance)
                                .build();
                        newInstances.add(wrapper);
                    }
                }
                instances = newInstances;
                log.info("in_len-{}", instances.size());
            } finally {
                rwl.writeLock().unlock();
            }
        });


        if(RANDOM_LOAD_BALANCE.equals(LOAD_BALANCE_CODE)){
            LOAD_BALANCE = new RandomLoadBalance();
            log.debug("开启随机策略");
        } else if (WEIGHT_LOAD_BALANCE.equals(LOAD_BALANCE_CODE)) {
            LOAD_BALANCE = new WeightLoadBalance();
            log.debug("开启权重策略");
        } else if (ROUND_ROBIN_LOAD_BALANCE.equals(LOAD_BALANCE_CODE)) {
            log.debug("开启轮询策略");
            LOAD_BALANCE = new RoundRobinLoadBalance();
        }else if(Least_Active_Balance.equals(LOAD_BALANCE_CODE)){
            log.debug("开启最少调用策略");
            LOAD_BALANCE = new LeastActiveLoadBalance();
        }else {
            log.debug("开启轮询策略");
            LOAD_BALANCE = new RoundRobinLoadBalance();
        }
    }

    //获取负载均衡后的可用服务
    public static InstanceWrapper loadBalancingAndGet() throws NacosException {
        rwl.readLock().lock();
        try {
      return LOAD_BALANCE.loadBalancingAndGet(instances);
        } finally {
            rwl.readLock().unlock();
        }
    }

        //获得不同的实例
    public static InstanceWrapper getOtherInstance(InstanceWrapper instance){

        rwl.readLock().lock();
        try {
            int index = (int) (System.currentTimeMillis() % instances.size());
            if (instances.get(index) == instance) {
                index = (index + 1) % instances.size();
            }
            return instances.get(index);
        }finally {
            rwl.readLock().unlock();
        }
    }
}
