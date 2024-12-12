package com.example.rpcclient.spring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 13:37
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
@Documented
public @interface RpcInvoke {

}
