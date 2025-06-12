package com.example.test;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:44
 * @Description
 */
@SpringBootApplication
//@RpcClientScan(values = {"com.example.test.service"})
public class SpringTestClient {
    //监控中心  应该提供扩展点和接口来无嵌入实现
    //调用耗时 出错次数  包大小 网速情况 分客户端和服务端
    //监控中心应该从注册中心获取信息 过滤器 通过spi机制加载过滤器 前后过滤器链
    //应该监控多个实例的调用信息
    //工厂获取代理类 -> 调用请求 -> 从注册中心获取实例 -> 负载均衡 ->  filter 发起网络调用 filter-> 接收请求
    public static void main(String[] args) {
        SpringApplication.run(SpringTestClient.class, args);
    }
}
