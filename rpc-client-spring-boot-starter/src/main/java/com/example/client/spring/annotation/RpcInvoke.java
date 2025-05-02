package com.example.client.spring.annotation;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 13:37
 * @Description 作用在接口上 表明这个接口可进行远程调用，扫描时为其生成代理类并注入为bean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Service
public @interface RpcInvoke {

}
