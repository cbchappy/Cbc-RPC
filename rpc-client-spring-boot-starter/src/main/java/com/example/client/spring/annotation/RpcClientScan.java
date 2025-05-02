package com.example.client.spring.annotation;

import com.example.client.spring.properties.ClientProperties;
import com.example.client.spring.registrar.RpcClientRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 13:39
 * @Description 导入RpcClientRegistrar和ClientProperties
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
@Import({RpcClientRegistrar.class, ClientProperties.class})
public @interface RpcClientScan {

    String[] values() default {};
}
