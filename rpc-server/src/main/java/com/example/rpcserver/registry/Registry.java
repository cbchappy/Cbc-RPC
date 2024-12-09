package com.example.rpcserver.registry;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcserver.config.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Properties;

import static com.example.rpcserver.config.RegistryConfig.*;

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
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, RegistryConfig.SERVER_ADDR);
        properties.setProperty(PropertyKeyConst.PASSWORD, RegistryConfig.PASSWORD);
        properties.setProperty(PropertyKeyConst.USERNAME, RegistryConfig.USERNAME);

        namingService = NamingFactory.createNamingService(properties);


        namingService.registerInstance(REGISTRY_SERVER_NAME, GROUP_NAME , REGISTRY_IP, REGISTRY_PORT, CLUSTER_NAME);
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
