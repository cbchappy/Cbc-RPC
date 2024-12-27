package com.example.server.spring.annotation;

import com.example.server.spring.properties.ServerProperties;
import com.example.server.spring.runner.StartRpcServerRunner;
import com.example.server.spring.registrar.RpcServerRegistrar;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 15:43
 * @Description 开启服务
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({RpcServerRegistrar.class, StartRpcServerRunner.class, ServerProperties.class}) //导入重要的包
public @interface StartRpcServer {
    String[] values() default {};
}
