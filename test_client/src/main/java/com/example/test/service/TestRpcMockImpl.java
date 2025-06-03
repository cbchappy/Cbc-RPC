package com.example.test.service;


/**
 * @Author Cbc
 * @DateTime 2025/5/3 17:56
 * @Description
 */
//@Mock
public class TestRpcMockImpl implements TestRpc{
    @Override
    public String get() {
        return "降级处理";
    }

    @Override
    public String error(String s) {
        return null;
    }
}
