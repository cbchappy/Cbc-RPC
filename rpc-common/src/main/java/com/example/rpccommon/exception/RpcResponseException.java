package com.example.rpccommon.exception;

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
}
