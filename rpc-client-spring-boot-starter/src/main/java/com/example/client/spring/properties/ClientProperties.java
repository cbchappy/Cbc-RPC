package com.example.client.spring.properties;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.example.rpcclient.config.ClientConfig.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/13 12:24
 * @Description 读取application的所有相关属性
 */
@ConfigurationProperties("cbc.rpc")
@Data
@Slf4j
public class ClientProperties implements InitializingBean {

    private Nacos nacos;
    private Connect connect;


    @Data
    public static class Connect{
        private Integer loadBalanceCode;
        private Integer faultTolerantCode;
        private Integer serializerTypeCode;
        private Integer pingInterval;
        private Integer maxIdleTime;
        private Boolean longConnection;
        private Integer retryNum;
        private Integer overTime;
    }

    @Data
    public static class Nacos{
        private Config config;
        private Discovery discovery;

        @Data
        public static class Config{
            private String serverAddr;
            private String userName;
            private String password;
        }

        @Data
        public static class Discovery{
            private String serverName;
            private String group;
            private List<String> clusters;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("初始化ClientProperties完成, 将属性导入ClientConfig");
        log.debug("ClientProperties:{}", JSON.toJSONString(this));
        if(nacos != null){
            Nacos.Config config = nacos.getConfig();
            Nacos.Discovery discovery = nacos.getDiscovery();
            if(config != null){
                SERVER_ADDR = config.serverAddr == null ? SERVER_ADDR : config.serverAddr;
                USERNAME = config.userName == null ? USERNAME : config.userName;
                PASSWORD = config.password == null ? PASSWORD : config.password;
            }
            if(discovery != null){
               SERVER_NAME = discovery.serverName == null ? SERVER_NAME : discovery.serverName;
               SERVER_GROUP_NAME = discovery.group == null ? SERVER_GROUP_NAME : discovery.group;
               SERVER_CLUSTER_NAME = discovery.clusters == null ? SERVER_CLUSTER_NAME : discovery.clusters;
            }
        }
        if(connect != null){
            LOAD_BALANCE_CODE = connect.loadBalanceCode == null ? LOAD_BALANCE_CODE : connect.getLoadBalanceCode();
            SERIALIZER_TYPE_CODE = connect.serializerTypeCode == null ? SERIALIZER_TYPE_CODE : connect.getSerializerTypeCode();
            FAULT_TOLERANT_CODE = connect.getFaultTolerantCode() == null ? FAULT_TOLERANT_CODE : connect.getFaultTolerantCode();
            PING_INTERVAL = connect.pingInterval == null ? PING_INTERVAL : connect.getPingInterval();
            CONNECT_IDLE_TIME = connect.maxIdleTime == null ? CONNECT_IDLE_TIME : connect.getMaxIdleTime();
            LONG_CONNECTION = connect.longConnection == null ? LONG_CONNECTION : connect.getLongConnection();
            RETRY_NUM = connect.retryNum == null ? RETRY_NUM : connect.retryNum;
            OVERTIME = connect.overTime == null ? OVERTIME : connect.overTime;
        }
    }
}
