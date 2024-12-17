package com.example.test.service.impl;

import com.example.rpcserver.spring.annotation.OpenRpcService;
import com.example.test.service.TestRpc;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:53
 * @Description
 */
//@Service


@OpenRpcService
public class TestRpcImpl implements TestRpc {
    @Override
    public String get() {
        return "test_res";
    }
}
