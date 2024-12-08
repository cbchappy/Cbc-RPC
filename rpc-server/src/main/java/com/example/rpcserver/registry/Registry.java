package com.example.rpcserver.registry;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.message.PingMsg;
import com.example.rpcserver.config.RegistryConfig;

import java.util.Properties;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:09
 * @Description
 */
public class Registry {
    private static NamingService namingService;

    public static void registryServer() throws NacosException {
        //todo 可以注册多个服务
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, RegistryConfig.SERVER_ADDR);
        properties.setProperty(PropertyKeyConst.PASSWORD, RegistryConfig.PASSWORD);
        properties.setProperty(PropertyKeyConst.USERNAME, RegistryConfig.USERNAME);

        namingService = NamingFactory.createNamingService(properties);

        namingService.registerInstance(RegistryConfig.REGISTRY_SERVER_NAME, RegistryConfig.REGISTRY_IP, RegistryConfig.REGISTRY_PORT);

    }
}
