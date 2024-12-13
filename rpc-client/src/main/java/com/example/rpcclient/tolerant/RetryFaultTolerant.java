package com.example.rpcclient.tolerant;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.server.ServerCenter;
import com.example.rpccommon.message.Request;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/11 12:10
 * @Description 进行重试策略
 */
@Slf4j
public class RetryFaultTolerant implements FaultTolerant{
    @Override
    public Object faultHandler(Instance instance, Throwable cause, Request request) {
        try {
            if(request.getRetryNum() == ClientConfig.RETRY_NUM.intValue()){
                throw cause;
            }
            request.setRetryNum(request.getRetryNum() + 1);
            log.debug("执行重试策略, 重试{}次", request.getRetryNum());
            return ServerCenter.remoteInvoke(request);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
