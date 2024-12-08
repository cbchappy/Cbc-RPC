package com.example.rpcclient.findServer;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.config.FindServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Properties;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:26
 * @Description
 */
@Slf4j
public class FindServer {
    private static NamingService namingService;

    public static void findServer() throws NacosException {
        log.debug("111");
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, FindServerConfig.SERVER_ADDR);
        properties.setProperty(PropertyKeyConst.PASSWORD, FindServerConfig.PASSWORD);
        properties.setProperty(PropertyKeyConst.USERNAME, FindServerConfig.USERNAME);

        namingService = NamingFactory.createNamingService(properties);

        List<Instance> list = namingService.getAllInstances(FindServerConfig.FIND_SERVER_NAME);

        for (Instance instance : list) {
            log.debug("ip:{}", instance.getIp());
            log.debug("port:{}", instance.getPort());
        }
    }
}
