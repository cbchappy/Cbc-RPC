package com.example.rpccommon.exception;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 16:55
 * @Description
 */
public class RpcException extends RuntimeException{
    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

}
