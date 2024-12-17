package com.example.test;

import com.example.rpcclient.spring.annotation.RpcClientScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:44
 * @Description
 */
@SpringBootApplication
@RpcClientScan(values = {"com.example.test.service"})
public class Client {
    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
    }
}
