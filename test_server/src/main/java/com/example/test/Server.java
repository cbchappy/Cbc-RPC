package com.example.test;

import com.example.rpcserver.spring.annotation.StartRpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 16:54
 * @Description
 */
@SpringBootApplication
@StartRpcServer(values = {"com.example.test.service"})
public class Server {
    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }
}
