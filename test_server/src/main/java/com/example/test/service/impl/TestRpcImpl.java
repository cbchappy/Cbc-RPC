package com.example.test.service.impl;


import com.example.server.spring.annotation.OpenRpcService;
import com.example.test.service.TestRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:53
 * @Description
 */
//@Service


@OpenRpcService
@Service
@Slf4j
public class TestRpcImpl implements TestRpc {

    public TestRpcImpl() {
        log.debug("TestRpcImpl被创建了");
    }

    @Override
    public String get() {
        return "test_res";
    }
}
