package com.example.test;


import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 16:54
 * @Description
 */ //Netty4BatchWriteQueue
@SpringBootApplication
@EnableDubbo
//@StartRpcServer(values = {"com.example.test.service"})
//@RpcClientScan(values = {"com.example.test.service"})
public class SpringTestServer {
    public static void main(String[] args) {
        SpringApplication.run(SpringTestServer.class, args);
    }
}
