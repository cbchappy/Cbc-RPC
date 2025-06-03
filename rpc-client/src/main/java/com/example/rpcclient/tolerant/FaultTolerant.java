package com.example.rpcclient.tolerant;

import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpccommon.message.Request;

/**
 * @Author Cbc
 * @DateTime 2024/12/11 12:04
 * @Description 容错处理
 */
public interface FaultTolerant {
    Object faultHandler(InstanceWrapper wrapper, Throwable cause, Request request);
}
