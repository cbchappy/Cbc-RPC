package com.example.rpcclient.config;

import java.nio.file.FileAlreadyExistsException;
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

    public static Integer LOAD_BALANCE_CODE = 2; //负载均衡策略码 0 随机 1 轮询 2 权重

    public static Integer SERIALIZER_TYPE_CODE = 1;//序列化策略码 0 java原始流 1 json 2 hessian

    public static Integer FAULT_TOLERANT_CODE = 0;//容错处理 0 进行重试

    public static Integer PING_INTERVAL = 1;//心跳包发送间隔 单位为秒

    public static Integer CONNECT_IDLE_TIME = 5;//连接空闲多久后中断 单位为秒

    public static Boolean LONG_CONNECTION = true;//是否开启长连接

    public static Integer RETRY_NUM = 5;//开启重试策略后的最大重试次数

    public static Integer OVERTIME = 10;//单个请求超时时间 单位为秒

    public static Boolean MONITOR_LOG = true;//服务实例信息详细监控日志



}
