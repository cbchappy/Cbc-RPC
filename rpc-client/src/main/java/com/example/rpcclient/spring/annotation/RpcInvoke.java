package com.example.rpcclient.spring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 13:37
 * @Description 作用在接口上 表明这个接口可进行远程调用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
@Documented
public @interface RpcInvoke {

}
