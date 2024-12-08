package com.example.rpcclient;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.rpcclient.config.FindServerConfig;
import com.example.rpcclient.findServer.FindServer;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 21:32
 * @Description
 */
public class Test {

    public static void main(String[] args) throws NacosException {
        FindServer.findServer();
    }
}
