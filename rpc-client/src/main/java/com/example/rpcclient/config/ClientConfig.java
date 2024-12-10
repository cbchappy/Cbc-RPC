package com.example.rpcclient.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:27
 * @Description
 */
public class ClientConfig {
    public final static String SERVER_ADDR = "localhost:8848";

    public final static String USERNAME = "nacos";

    public final static String PASSWORD = "nacos";

    public final static String SERVER_NAME = "test_server";

    public final static String SERVER_GROUP_NAME = "DEFAULT_GROUP";

    public final static List<String> SERVER_CLUSTER_NAME = new ArrayList<>();

    public final static Integer LOAD_BALANCE_CODE = 2; //0 随机 1 轮询 2 权重

    public final static Integer SERIALIZER_TYPE_CODE = 0;
}
