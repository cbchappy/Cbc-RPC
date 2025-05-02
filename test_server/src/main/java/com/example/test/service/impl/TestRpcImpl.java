package com.example.test.service.impl;



import com.example.test.service.TestRpc;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:53
 * @Description
 */
//@Service


//@OpenRpcService
@DubboService
@Service
public class TestRpcImpl implements TestRpc {
    private AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger ex = new AtomicInteger(0);

    public TestRpcImpl() {

    }

    @Override
    public String get() {
//        count.addAndGet(1);
//        if(count.get() % 20 == 0){
//            ex.addAndGet(1);
//            throw new RuntimeException();
//        }
//        log.debug("\ncount: {}, ex: {}\n", count.get(), ex.get());
        return "test_res";
    }
}
