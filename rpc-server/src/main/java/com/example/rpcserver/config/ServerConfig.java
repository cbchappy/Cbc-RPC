package com.example.rpcserver.config;


/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:13
 * @Description
 */

public class ServerConfig {
    public static String SERVER_ADDR = "localhost:8848";//nacos服务端地址

    public static String REGISTRY_IP = "localhost";//注册地址

    public static Integer REGISTRY_PORT = 8080;//开放端口

    public static Double WEIGHT = 1.0;//服务器权重

    public static String GROUP_NAME = "DEFAULT_GROUP";//nacos的组名

    public static String CLUSTER_NAME = "DEFAULT";//注册的集群名

    public static String REGISTRY_SERVER_NAME = "test_server";//注册服务名

    public static String USERNAME = "nacos";//服务端账号名

    public static String PASSWORD = "nacos";//服务端密码

    public static Integer READ_IDLE_TIME = 5;//最大读空闲时间, 时间单位为秒

    public static Integer BOSS_THREAD_NUM = 1;//netty的boss线程数

    public static Integer WORK_THREAD_NUM = 3;//netty的workers线程数

    public static Integer FUSING_START_NUM = 30;//熔断开始计算的最小请求服务器次数

    public static Double FUSING_DIVISOR = 0.3;//熔断因子

    public static Integer FUSING_RESTART_TIME = 60 * 60; //单位为秒


}
