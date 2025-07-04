package com.example.rpcclient.tolerant;

import com.example.rpcclient.config.ClientConfig;
import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpcclient.server.InvokeServer;
import com.example.rpcclient.server.LoadBalanceServer;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import lombok.extern.slf4j.Slf4j;


/**
 * @Author Cbc
 * @DateTime 2025/4/20 15:22
 * @Description 切换重试策略
 */
@Slf4j
public class Failover implements FaultTolerant{
    @Override
    public Object faultHandler(InstanceWrapper wrapper, Throwable cause, Request request) {

            Integer status = null;
            Response response = null;
            while (true){
                try {
                    response  = InvokeServer.doFilterAndGet(request, wrapper);
                    status = response.getStatus();

                    if(response.isAsync()){
                        return response.getRes();
                    }
                    if(!response.isSuccess()){
                        if(response.getStatus().equals(ResponseStatus.MOCK.code)){
                            return response.getRes();
                        }
                        throw new RpcException(ResponseStatus.getEnumByCode(status).msg);
                    }
                    return response.getRes();
                } catch (Throwable e) {
                    if(request.getRetryNum() > 0){
                        request.setRetryNum(request.getRetryNum() - 1);
                        wrapper = LoadBalanceServer.getOtherInstance(wrapper);
                    }else {
                        if((status == null || status.intValue() != ResponseStatus.MOCK.code)){
                            throw new RpcException(e);
                        }else {
                            return response.getRes();
                        }
                    }

                }
            }
    }
}
