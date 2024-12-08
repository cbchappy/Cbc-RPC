package com.example.rpccommon.exception;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 16:51
 * @Description
 */
public class RpcCommonException extends RpcException{
    public RpcCommonException() {
    }

    public RpcCommonException(String message) {
        super(message);
    }

    public RpcCommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcCommonException(Throwable cause) {
        super(cause);
    }
}
