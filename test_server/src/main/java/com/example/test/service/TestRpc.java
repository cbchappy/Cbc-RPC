package com.example.test.service;

import com.example.rpcserver.spring.annotation.OpenRpcService;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:52
 * @Description
 */
@OpenRpcService
public interface TestRpc {

    String get();
}
