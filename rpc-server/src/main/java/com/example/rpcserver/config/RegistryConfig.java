package com.example.rpcserver.config;


import java.util.concurrent.TimeUnit;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:13
 * @Description
 */
public class RegistryConfig {
    public final static String SERVER_ADDR = "localhost:8848";

    public final static String REGISTRY_IP = "localhost";

    public final static Integer REGISTRY_PORT = 8080;

    public final static String GROUP_NAME = "DEFAULT_GROUP";

    public final static String CLUSTER_NAME = "DEFAULT";

    public final static String REGISTRY_SERVER_NAME = "test_server";

    public final static String USERNAME = "nacos";

    public final static String PASSWORD = "nacos";

    public final static Integer READ_IDLE_TIME = 5;

    public final static Integer BOSS_THREAD_NUM = 1;

    public final static Integer WORK_THREAD_NUM = 3;

}
