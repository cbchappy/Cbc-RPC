package com.example.client.spring.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @Author Cbc
 * @DateTime 2025/5/3 17:43
 * @Description 标识为降级处理
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface Mock {
}
