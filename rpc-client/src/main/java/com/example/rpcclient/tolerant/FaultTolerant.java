package com.example.rpcclient.tolerant;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpccommon.message.Request;

/**
 * @Author Cbc
 * @DateTime 2024/12/11 12:04
 * @Description 容错处理
 */
public interface FaultTolerant {
    Object faultHandler(Instance instance, Throwable cause, Request request);
}
