package com.example.test.service.impl;

import com.example.server.spring.annotation.OpenRpcService;
import com.example.test.service.TestR2;

/**
 * @Author Cbc
 * @DateTime 2025/6/2 21:37
 * @Description
 */
@OpenRpcService
public class TestR2Impl implements TestR2 {
    @Override
    public String getStr() {
        return "第三层";
    }
}
