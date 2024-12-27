package com.example.server.spring.annotation;

import com.example.server.spring.registrar.RpcServerRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 15:41
 * @Description 选择开放的服务实现类
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Target(ElementType.TYPE)
public @interface OpenRpcService {
}
