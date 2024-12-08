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

    public enum RpcExceptionMsg{
        SERIALIZER_NOTFOUND("无法找到序列化器"),
        VERIFY_ERROR("消息解码校验不通过"),
        ;

        private final String msg;

        RpcExceptionMsg(String msg) {
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }
    }
}
