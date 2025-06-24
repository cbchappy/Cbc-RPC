package com.example.test.controller;

import com.example.rpccommon.RpcContext;
import com.example.test.service.TestRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;


/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:46
 * @Description
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class ClientController {
    @Autowired
//    @DubboReference
    private TestRpc testServer;

    @RequestMapping("")
    public String get() throws Exception {
//        CompletableFuture<String> future = RpcContext.asyncInvoke(new Callable<String>() {
//            @Override
//            public String call() throws Exception {
//
//            }
//        });
//        return future.get();
        return testServer.get();
    }

    @RequestMapping("/testError")
    public String testError(){
        log.debug("进行错误测试");
        return testServer.error("kkk");
    }

}
