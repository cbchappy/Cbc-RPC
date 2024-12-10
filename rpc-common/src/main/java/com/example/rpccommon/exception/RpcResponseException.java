package com.example.rpccommon.exception;

import com.example.rpccommon.constants.ResponseStatus;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 13:01
 * @Description rpc服务端异常
 */
public class RpcResponseException extends RpcException{
    public RpcResponseException() {
    }

    public RpcResponseException(String message) {
        super(message);
    }

    public RpcResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcResponseException(Throwable cause) {
        super(cause);
    }

    public RpcResponseException(ResponseStatus status){
        super("请求错误, 错误码:" + status.code + ", 错误内容:" + status.msg);
    }
}
