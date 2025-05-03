package com.example.test.service;


import com.example.client.spring.annotation.RpcInvoke;
import org.springframework.stereotype.Service;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:52
 * @Description
 */
@RpcInvoke
//@Service
public interface TestRpc {
    String get();

    String error(String s);
}


// String error(String v);