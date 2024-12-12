package com.example.test.service;

import com.example.rpcclient.spring.annotation.RpcInvoke;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:52
 * @Description
 */
@RpcInvoke
public interface TestServer {

    String get();
}
