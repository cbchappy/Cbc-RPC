package com.example.rpcclient.tolerant;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.server.ServerCenter;
import com.example.rpccommon.message.Request;

/**
 * @Author Cbc
 * @DateTime 2024/12/11 12:10
 * @Description 原有instance重试策略
 */
public class RetryFaultTolerant implements FaultTolerant{
    @Override
    public Object faultHandler(Instance instance, Throwable cause, Request request) {
        try {
            return ServerCenter.remoteInvoke(request, instance);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
