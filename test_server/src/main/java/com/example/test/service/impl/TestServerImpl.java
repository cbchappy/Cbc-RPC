package com.example.test.service.impl;

import ch.qos.logback.classic.Logger;
import com.example.rpcserver.spring.annotation.OpenRpcService;
import com.example.test.service.TestServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:53
 * @Description
 */
//@Service
@Slf4j
@OpenRpcService
public class TestServerImpl implements TestServer {

    public TestServerImpl() {
        log.debug("被构建了");
    }

    @Override
    public String get() {
        return "test_res";
    }
}
