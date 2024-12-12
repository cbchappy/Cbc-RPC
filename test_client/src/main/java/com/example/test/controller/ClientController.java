package com.example.test.controller;

import com.example.test.service.TestServer;
import lombok.extern.slf4j.Slf4j;
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
    private TestServer testServer;

    @RequestMapping("")
    public String get(){
        log.debug("request");
        return testServer.get();
    }

}
