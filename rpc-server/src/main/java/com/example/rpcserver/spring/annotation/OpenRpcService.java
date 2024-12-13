package com.example.rpcserver.spring.annotation;

import com.example.rpcserver.spring.properties.ServerProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 15:41
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
@Import(ServerProperties.class)
public @interface OpenRpcService {
}
