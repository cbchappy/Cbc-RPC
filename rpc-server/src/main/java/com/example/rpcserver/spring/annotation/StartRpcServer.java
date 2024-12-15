package com.example.rpcserver.spring.annotation;

import com.example.rpcserver.spring.properties.ServerProperties;
import com.example.rpcserver.spring.runner.StartRpcServerRunner;
import com.example.rpcserver.spring.registrar.RpcServerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 15:43
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({RpcServerRegistrar.class, StartRpcServerRunner.class, ServerProperties.class})
//扫描注解 记录启用了注解的bean
// 开启rpc  注册到nacos 修改handler逻辑 先获取尝试从factory获取bean 获取不到再重新创建类
public @interface StartRpcServer {
    String[] values() default {};
}
