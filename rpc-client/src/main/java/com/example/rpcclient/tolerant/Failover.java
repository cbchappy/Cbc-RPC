package com.example.rpcclient.tolerant;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.example.rpcclient.blance.RoundRobinLoadBalance;
import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.server.InstanceService;
import com.example.rpcclient.server.InvokeCenter;

import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import lombok.extern.slf4j.Slf4j;


/**
 * @Author Cbc
 * @DateTime 2025/4/20 15:22
 * @Description 切换重试策略
 */
@Slf4j
public class Failover implements FaultTolerant{
    @Override
    public Object faultHandler(Instance instance, Throwable cause, Request request) {
            int num = 0;
            while (true){
                num++;
                try {
                    return InvokeCenter.doRemoteInvoke(request, instance);
                } catch (Throwable e) {
                    if(num == ClientConfig.RETRY_NUM){
                        throw new RpcException(e);
                    }
                    instance = InstanceService.getOtherInstance(instance);
                }
            }
    }
}
