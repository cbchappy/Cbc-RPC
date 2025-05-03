package com.example.rpcclient.server;

import com.alibaba.nacos.api.exception.NacosException;

/**
 * @Author Cbc
 * @DateTime 2025/4/29 20:41
 * @Description
 */
public class ClientBootstrap {

    public static void initializeClient() throws NacosException {
        //启动Nacos服务发现
        InstanceService.findServer();
        //初始化
        InvokeCenter.initializeInvoke();
    }
}
