package com.example.test;


import com.example.client.spring.annotation.RpcClientScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:44
 * @Description
 */
@SpringBootApplication
@RpcClientScan(values = {"com.example.test.service"})
public class SpringTestClient {
    public static void main(String[] args) {
        SpringApplication.run(SpringTestClient.class, args);
    }
}
