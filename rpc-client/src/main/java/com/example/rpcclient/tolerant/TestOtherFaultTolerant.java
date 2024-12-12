package com.example.rpcclient.tolerant;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.server.ServerCenter;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;

/**
 * @Author Cbc
 * @DateTime 2024/12/11 12:15
 * @Description 获取其他实例进行重试
 */
public class TestOtherFaultTolerant implements FaultTolerant{
    @Override
    public Object faultHandler(Instance instance, Throwable cause, Request request) {
        try {
            Instance otherInstance = ServerCenter.getOtherInstance(instance);
            return ServerCenter.remoteInvoke(request, otherInstance);
        } catch (Throwable e) {
            throw new RpcException(e);
        }
    }
}
