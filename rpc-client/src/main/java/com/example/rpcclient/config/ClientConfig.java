package com.example.rpcclient.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:27
 * @Description 客户端配置文件
 */
public class ClientConfig {
    public static String SERVER_ADDR = "localhost:8848"; //nacos总服务地址

    public static String USERNAME = "nacos";//nacos登录用户名

    public static String PASSWORD = "nacos";//nacos登录密码

    public static String SERVER_NAME = "test_server";//待发现的服务名

    public static String SERVER_GROUP_NAME = "DEFAULT_GROUP";//服务所在的组

    public static List<String> SERVER_CLUSTER_NAME = new ArrayList<>();//服务的集群列表

    public static Integer LOAD_BALANCE_CODE = 0; //负载均衡策略码 0 随机 1 轮询 2 权重 3最少调用

    public static Integer SERIALIZER_TYPE_CODE = 2;//序列化策略码 0 java原始流 1 json 2 hessian  3 kryo

    public static Integer FAULT_TOLERANT_CODE = 0;//0 故障切换  1 并行调用

    public static Integer PING_INTERVAL = 30;//心跳包发送间隔 单位为秒

    public static Integer CONNECT_IDLE_TIME = 5;//连接空闲多久后中断 单位为秒

    public static Boolean LONG_CONNECTION = true;//是否开启长连接

    public static Integer RETRY_NUM = 1;//开启重试策略后的最大重试次数

    public static Integer OVERTIME = 3;//单个请求超时时间 单位为秒

    public static Boolean MONITOR_LOG = true;//服务实例信息详细监控日志

    public static Boolean TRACE = false;

    public static Integer READER_IDLE_TIME = 60;//单位秒

}
