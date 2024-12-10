package com.example.test.service.impl;

import com.example.test.service.TestServer;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:53
 * @Description
 */
public class TestServerImpl implements TestServer {
    @Override
    public String get() {
        return "test_res";
    }
}
