package com.example.test.service;

import com.example.rpcclient.spring.annotation.RpcInvoke;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:52
 * @Description
 */
@RpcInvoke
public interface TestRpc {
    String get();

    String error(String s);
}


// String error(String v);