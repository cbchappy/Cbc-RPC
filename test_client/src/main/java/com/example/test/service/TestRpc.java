package com.example.test.service;


import com.example.client.spring.annotation.RpcInvoke;

/**
 * @Author Cbc
 * @DateTime 2025/5/3 17:56
 * @Description
 */
@RpcInvoke
public interface TestRpc {
    String get();

    String error(String s);

}


// String error(String v);