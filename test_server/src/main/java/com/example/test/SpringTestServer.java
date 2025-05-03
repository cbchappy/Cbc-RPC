package com.example.test;



import com.example.server.spring.annotation.StartRpcServer;
import org.graalvm.polyglot.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 16:54
 * @Description
 */
@SpringBootApplication
//@EnableDubbo
@StartRpcServer(values = {"com.example.test.service"})
public class SpringTestServer {
    public static void main(String[] args) {
        SpringApplication.run(SpringTestServer.class, args);
    }
}
