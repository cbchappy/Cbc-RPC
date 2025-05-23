package com.example.rpcclient.constants;

/**
 * @Author Cbc
 * @DateTime 2024/12/9 18:59
 * @Description 负载均衡码
 */
public class LoadBalanceTypeCode {

    public static final Integer RANDOM_LOAD_BALANCE = 0;//随机

    public static final Integer ROUND_ROBIN_LOAD_BALANCE = 1;//轮询

    public static final Integer WEIGHT_LOAD_BALANCE = 2;//权重

    public static final Integer Least_Active_Balance = 3;//最少调用

}
