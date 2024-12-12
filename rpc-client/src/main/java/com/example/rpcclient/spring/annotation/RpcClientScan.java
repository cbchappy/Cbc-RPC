package com.example.rpcclient.spring.annotation;

import com.example.rpcclient.spring.registrar.RpcClientRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 13:39
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
@Import(RpcClientRegistrar.class)
public @interface RpcClientScan {

    String[] values() default {};
}
