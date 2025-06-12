package com.example.test.controller;

import com.example.test.service.TestRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    @DubboReference
    private TestRpc testServer;

    @RequestMapping("")
    public String get(){
        log.debug("request");
        return testServer.get();
    }

    @RequestMapping("/testError")
    public String testError(){
        log.debug("进行错误测试");
        return testServer.error("kkk");
    }

}
