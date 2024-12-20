package com.example.rpcserver.registry;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcserver.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.example.rpcserver.config.ServerConfig.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:09
 * @Description //开启服务 --> 注册服务
 */
@Slf4j
public class Registry {

    private static NamingService namingService;


   //注册服务到nacos
    public static void registryServer() throws NacosException {


        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, ServerConfig.SERVER_ADDR);
        properties.setProperty(PropertyKeyConst.PASSWORD, ServerConfig.PASSWORD);
        properties.setProperty(PropertyKeyConst.USERNAME, ServerConfig.USERNAME);

        namingService = NamingFactory.createNamingService(properties);

        Instance instance = new Instance();
        instance.setClusterName(CLUSTER_NAME);
        instance.setIp(REGISTRY_IP);
        instance.setPort(REGISTRY_PORT);
        instance.setWeight(WEIGHT);
        namingService.registerInstance(REGISTRY_SERVER_NAME, GROUP_NAME , instance);
        log.debug("注册服务到nacos, serverName:{}, groupName:{}, ip:{}, port:{}, cluster:{}",
                REGISTRY_SERVER_NAME, GROUP_NAME, REGISTRY_IP, REGISTRY_PORT, CLUSTER_NAME);


    }



    //从nacos移除服务
    public static void removeRegistry() throws NacosException {
        log.debug("从nacos移除服务, serverName:{}, groupName:{}, ip:{}, port:{}, cluster:{}",
                REGISTRY_SERVER_NAME, GROUP_NAME, REGISTRY_IP, REGISTRY_PORT, CLUSTER_NAME);
        namingService.deregisterInstance(REGISTRY_SERVER_NAME, GROUP_NAME, REGISTRY_IP, REGISTRY_PORT, CLUSTER_NAME);
    }

}
