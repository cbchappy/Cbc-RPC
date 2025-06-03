package com.example.rpcclient.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.example.rpccommon.util.SpanReportClient;

/**
 * @Author Cbc
 * @DateTime 2025/4/29 20:41
 * @Description
 */
public class ClientBootstrap {

    public static void initializeClient() throws NacosException {
        //todo 重构
//        //启动Nacos服务发现
//        InstanceService.findServer();
//        //初始化调用中心
//        InvokeCenter.initializeInvoke();
        InstanceManageCenter.findServer();
        LoadBalanceServer.initialize();
        InvokeServer.initialize();
        //todo 启动链路追踪收集
        SpanReportClient.startReport();
    }
}
