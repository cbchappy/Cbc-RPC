package com.example.rpcserver.spring.annotation;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 15:41
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)//从spring获取bean
public @interface OpenRpcService {
}
