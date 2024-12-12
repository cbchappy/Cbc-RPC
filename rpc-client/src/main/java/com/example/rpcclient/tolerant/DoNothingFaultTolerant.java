package com.example.rpcclient.tolerant;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;

/**
 * @Author Cbc
 * @DateTime 2024/12/11 12:14
 * @Description 什么都不做 即直接抛出错误即可
 */
public class DoNothingFaultTolerant implements FaultTolerant{
    @Override
    public Object faultHandler(Instance instance, Throwable cause, Request request) {
        try {
            throw cause;
        } catch (Throwable e) {
            throw new RpcException(e);
        }
    }
}
