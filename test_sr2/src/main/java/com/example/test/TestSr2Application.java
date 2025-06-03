package com.example.test;

import com.example.server.spring.annotation.StartRpcServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StartRpcServer(values = {"com.example.test.service"})
public class TestSr2Application {

    public static void main(String[] args) {
        SpringApplication.run(TestSr2Application.class, args);
    }

}
