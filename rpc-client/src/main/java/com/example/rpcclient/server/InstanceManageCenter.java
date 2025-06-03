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
import com.example.rpcclient.config.ClientConfig;
import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.rpcclient.config.ClientConfig.*;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 15:20
 * @Description 服务实例管理中心
 */
@Slf4j
public class InstanceManageCenter {
    //服务实例  熔断器
    private static volatile NamingService namingService;//nacos总服务端
    private static volatile List<Instance> instances;//获取到的所有服务实例
    private static List<InstancesObserver> observerList = new ArrayList<>();

    //服务发现 只启动一次
    public static void findServer() throws NacosException {

        if(namingService == null){

            synchronized (InstanceManageCenter.class){
                if(namingService != null){
                    return;
                }
                log.debug("启动服务发现");

                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.SERVER_ADDR, ClientConfig.SERVER_ADDR);
                properties.setProperty(PropertyKeyConst.PASSWORD, ClientConfig.PASSWORD);
                properties.setProperty(PropertyKeyConst.USERNAME, ClientConfig.USERNAME);

                namingService = NamingFactory.createNamingService(properties);

                List<Instance> list = namingService.selectInstances(SERVER_NAME, SERVER_GROUP_NAME, SERVER_CLUSTER_NAME, true);

                notifyInstancesObserver(list);
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
                    log.info("服务实例发生变化, 现有健康服务个数为:{}", collect.size() );
                    log.debug("服务实例发生变化, 现有健康服务个数为:{}", collect.size() );
                    notifyInstancesObserver(collect);
                }
            }
            @Override
            public Executor getExecutor() {
                return executorService;
            }
        };
        namingService.subscribe(SERVER_NAME, SERVER_GROUP_NAME, SERVER_CLUSTER_NAME, serviceListener);
    }


    public static void notifyInstancesObserver(List<Instance> newList){
        for (InstancesObserver observer : observerList) {
            observer.update(instances, newList);
        }
        instances = newList;
        log.debug("更新instances");
    }

    public static void addObserver(InstancesObserver observer){
        observerList.add(observer);
    }

    public static List<Instance> getInstances(){
        return instances;
    }

    @FunctionalInterface
    public interface InstancesObserver{
        void update(List<Instance> oldList, List<Instance> newList);
    }


}
