package com.example.rpcclient.server;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.AbstractEventListener;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.blance.*;
import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.tolerant.CircuitBreaker;
import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.rpcclient.config.ClientConfig.*;
import static com.example.rpcclient.config.ClientConfig.SERVER_CLUSTER_NAME;
import static com.example.rpcclient.constants.LoadBalanceTypeCode.*;

/**
 * @Author Cbc
 * @DateTime 2025/1/11 15:08
 * @Description 服务实例管理 获取实例 实例更新--->关闭弃用的实例的连接
 */
@Slf4j
public class InstanceService {
    //服务实例  熔断器
    private static volatile NamingService namingService;//nacos总服务端
    private static volatile List<Instance> instances;//获取到的所有服务实例
    public final static LoadBalance LOAD_BALANCE;//根据配置文件获取负载均衡实例
    private final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();//instances的读写锁
    private static final List<UpdateInstancesConsumer> consumerList = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<Instance, CircuitBreaker> CB_MAP = new ConcurrentHashMap<>();

    static {
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
    }//初始化负载均衡类


    //服务发现 只启动一次
    public static void findServer() throws NacosException {

        if(namingService == null){

            synchronized (InstanceService.class){
                if(namingService != null){
                    return;
                }
                log.debug("启动服务发现");

                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.SERVER_ADDR, ClientConfig.SERVER_ADDR);
                properties.setProperty(PropertyKeyConst.PASSWORD, ClientConfig.PASSWORD);
                properties.setProperty(PropertyKeyConst.USERNAME, ClientConfig.USERNAME);

                namingService = NamingFactory.createNamingService(properties);

                log.debug("ClientConfig.SERVER_ADDR:{}", ClientConfig.SERVER_ADDR);

                List<Instance> list = namingService.selectInstances(SERVER_NAME, SERVER_GROUP_NAME, SERVER_CLUSTER_NAME, true);

                //熔断器
                for (Instance instance : list) {
                    CB_MAP.put(instance, new CircuitBreaker());
                }
                //确保实例更新熔断器也更新
                UpdateInstancesConsumer consumer = new UpdateInstancesConsumer() {
                    @Override
                    public void consume(List<Instance> oldList, List<Instance> newList) {
                        for (Instance instance : newList) {
                            CB_MAP.put(instance, CB_MAP.getOrDefault(instance, new CircuitBreaker()));
                        }
                        if(oldList == null){
                            return;
                        }
                        for (Instance instance : oldList) {
                            if(!newList.contains(instance)){
                                CB_MAP.remove(instance);
                            }
                        }
                    }
                };
                addUpdateInstancesConsumer(consumer);


                updateInstances(list);
                log.debug("服务发现个数:{}", instances.size());
                if(instances == null || instances.size() == 0){
                    throw new RpcException(RpcExceptionMsg.NOT_FOUND_SERVER);
                }
                listenServer();

            }

        }

    }

    //监听服务实例变化
    private static void listenServer() throws NacosException {

        final ExecutorService executorService = Executors.newFixedThreadPool(1);

        EventListener serviceListener = new AbstractEventListener() {
            @Override
            public void onEvent(Event event) {
                if (event instanceof NamingEvent namingEvent) {
                    List<Instance> list = namingEvent.getInstances();
                    List<Instance> collect = list.stream().filter(Instance::isHealthy).toList();
                    if(collect.size() == 0){
                        throw new RpcException(RpcExceptionMsg.NOT_FOUND_SERVER);
                    }
                    log.debug("服务实例发生变化, 现有健康服务个数为:{}", collect.size() );
                    updateInstances(collect);
                }
            }
            @Override
            public Executor getExecutor() {
                return executorService;
            }
        };
        namingService.subscribe(SERVER_NAME, SERVER_GROUP_NAME, SERVER_CLUSTER_NAME, serviceListener);
    }

    //更新实例 线程安全
    private static void updateInstances(List<Instance> list){
        rwl.writeLock().lock();
        for (UpdateInstancesConsumer consumer : consumerList) {
            consumer.consume(instances, list);
        }
        instances = list;
        log.debug("更新instances");
        rwl.writeLock().unlock();
    }

    //获取负载均衡后的可用服务
    public static Instance getAvailableServer() throws NacosException {
        rwl.readLock().lock();
        try {
            return LOAD_BALANCE.loadBalancingAndGet(instances);
        } finally {
            rwl.readLock().unlock();
        }
    }

    public static void addUpdateInstancesConsumer(UpdateInstancesConsumer consumer){
        consumerList.add(consumer);
    }

    //获得不同的实例
    public static Instance getOtherInstance(Instance instance){

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

    public static CircuitBreaker getCircuitBreaker(Instance instance){
        return CB_MAP.get(instance);
    }

    public static List<Instance> getInstances(){
        return instances;
    }

    public static LoadBalance getLoadBalance(){
        return LOAD_BALANCE;
    }

    @FunctionalInterface
    public interface UpdateInstancesConsumer{
        void consume(List<Instance> oldList, List<Instance> newList);
    }





}
