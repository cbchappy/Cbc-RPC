package com.example.rpcclient.handler.postHandler;

import com.example.rpcclient.server.InstanceService;
import com.example.rpcclient.server.InvokeCenter;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.exception.RpcRequestException;
import com.example.rpccommon.exception.RpcResponseException;
import com.example.rpccommon.message.Response;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.Objects;

/**
 * @Author Cbc
 * @DateTime 2025/4/20 18:31
 * @Description
 */
public class DefaultPostHandler implements PostHandler{
    private DefaultPostHandler(){

    }
    @Override
    public void handler(Response msg, PostHandlerChain chain, int index) {


        if(!InvokeCenter.promiseMap.containsKey(msg.getMsgId())){
           msg.setStatus(ResponseStatus.TIME_OUT.code);
            chain.doHandle(msg, index);
            return;
        }
        Integer msgId = msg.getMsgId();
        DefaultPromise<Object> promise = InvokeCenter.promiseMap.remove(msgId);

        Integer status = msg.getStatus();

        if(Objects.equals(status, ResponseStatus.SUCCESS.code)){
            promise.setSuccess(msg.getRes());
            return;
        }

        if(status < 500){
            promise.setFailure(new RpcRequestException(ResponseStatus.getEnumByCode(status)));
        }else {
            promise.setFailure(new RpcResponseException(ResponseStatus.getEnumByCode(status)));
        }

        chain.doHandle(msg, index);

    }

    public static DefaultPostHandler getInstance(){
        return Singleton.handler;
    }

    private static class Singleton{
        public static DefaultPostHandler handler = new DefaultPostHandler();
    }


}
