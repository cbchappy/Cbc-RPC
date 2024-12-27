package com.example.server.spring.properties;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.example.rpcserver.config.ServerConfig.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/13 12:08
 * @Description
 */
@ConfigurationProperties(prefix = "cbc.rpc")
@Data
@Slf4j
public class ServerProperties implements InitializingBean {
    private Nacos nacos;
    private Connect connect;


    @Data
    public static class Connect{
        private Integer readIdleTime;
        private Integer bossThreadNum;
        private Integer workerThreadNum;
        private Integer StartFusingNum;
        private Double FusingDivisor;
        private Integer fusingRestartTime;
    }



    @Data
    public static class Nacos{
        private Config config;
        private Registry registry;
        @Data
        public static class Config{
            private String serverAddr;
            private String userName;
            private String password;
        }

        @Data
        public static class Registry{
            private String ip;
            private Integer port;
            private String group;
            private String cluster;
            private String serverName;
            private Double weight;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("初始化ServerProperties完成, 将属性导入ServerConfig");
        log.debug("ServerProperties:{}", JSON.toJSONString(this));
        if(nacos != null){
            Nacos.Config config = nacos.getConfig();
            Nacos.Registry registry = nacos.getRegistry();
            if(config != null){
                SERVER_ADDR = config.serverAddr == null ? SERVER_ADDR : config.serverAddr;
                USERNAME = config.userName == null ? USERNAME : config.userName;
                PASSWORD = config.password == null ? PASSWORD : config.password;
            }
            if(registry != null){
                REGISTRY_SERVER_NAME = registry.serverName == null ?  REGISTRY_SERVER_NAME : registry.serverName;
                GROUP_NAME = registry.group == null ? GROUP_NAME : registry.group;
               REGISTRY_IP = registry.ip == null ? REGISTRY_IP : registry.ip;
               REGISTRY_PORT = registry.port == null ? REGISTRY_PORT : registry.port;
               CLUSTER_NAME = registry.cluster == null ? CLUSTER_NAME : registry.cluster;
               WEIGHT = registry.weight == null ? WEIGHT : registry.weight;
            }
        }
        if(connect != null){
           READ_IDLE_TIME = connect.readIdleTime == null ? READ_IDLE_TIME : connect.readIdleTime;
           BOSS_THREAD_NUM = connect.getBossThreadNum() == null ? BOSS_THREAD_NUM : connect.getBossThreadNum();
           WORK_THREAD_NUM = connect.workerThreadNum == null ? WORK_THREAD_NUM : connect.workerThreadNum;
           FUSING_START_NUM = connect.StartFusingNum == null ? FUSING_START_NUM : connect.StartFusingNum;
           FUSING_DIVISOR = connect.FusingDivisor == null ? FUSING_DIVISOR : connect.FusingDivisor;
           FUSING_RESTART_TIME = connect.fusingRestartTime == null ? FUSING_RESTART_TIME : connect.fusingRestartTime;
        }
    }
}
