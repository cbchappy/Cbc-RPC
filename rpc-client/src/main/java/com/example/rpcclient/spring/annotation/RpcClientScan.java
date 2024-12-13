package com.example.rpcclient.spring.annotation;

import com.example.rpcclient.spring.properties.ClientProperties;
import com.example.rpcclient.spring.registrar.RpcClientRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 13:39
 * @Description 扫描指定包, 寻找添加了@RpcInvoke的接口, 并为其配置代理工厂
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
@Import({RpcClientRegistrar.class, ClientProperties.class})
public @interface RpcClientScan {

    String[] values() default {};
}
