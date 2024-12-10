package com.example.rpccommon.exception;

import com.example.rpccommon.constants.ResponseStatus;



/**
 * @Author Cbc
 * @DateTime 2024/12/8 12:58
 * @Description 客户端异常
 */
public class RpcRequestException extends RpcException{

    public RpcRequestException(String msg){
        super(msg);
    }

    public RpcRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcRequestException(Throwable cause) {
        super(cause);
    }

    public RpcRequestException(ResponseStatus status){
        super("请求错误, 错误码:" + status.code + ", 错误内容:" + status.msg);
    }
}
