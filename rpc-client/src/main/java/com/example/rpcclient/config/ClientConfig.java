package com.example.rpcclient.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:27
 * @Description 客户端配置文件
 */
public class ClientConfig {
    public final static String SERVER_ADDR = "localhost:8848"; //nacos总服务地址

    public final static String USERNAME = "nacos";//nacos登录用户名

    public final static String PASSWORD = "nacos";//nacos登录密码

    public final static String SERVER_NAME = "test_server";//待发现的服务名

    public final static String SERVER_GROUP_NAME = "DEFAULT_GROUP";//服务所在的组

    public final static List<String> SERVER_CLUSTER_NAME = new ArrayList<>();//服务的集群列表

    public final static Integer LOAD_BALANCE_CODE = 2; //负载均衡策略码 0 随机 1 轮询 2 权重

    public final static Integer SERIALIZER_TYPE_CODE = 0;//序列化策略码 0 java原始流 1 json 2 hessian

    public final static Integer FAULT_TOLERANT_CODE = 0;//容错重试处理 0 抛错 1 在原有基础重试 2 选另一个重试 默认1
}
