package com.example.rpccommon.message;

import com.example.rpccommon.exception.RpcCommonException;
import com.example.rpccommon.exception.RpcRequestException;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 13:34
 * @Description
 */
public abstract class RpcMsg {


    //获取请求类型号码 0 request  1 response 2....
    public static Integer getTypeCode(Object obj){
        if(obj instanceof Request){
            return RpcMsgTypeCode.REQUEST.code;
        } else if (obj instanceof Response) {
            return RpcMsgTypeCode.RESPONSE.code;
        }else if(obj instanceof PingMsg){
            return RpcMsgTypeCode.PINGMSG.code;
        }
        throw new RpcCommonException("消息类型错误");
    }

    public enum Status{
        //1表示正常
        //3开头鉴权错误
        //4开头是request错误
        //5是response错误
        //6是通用错误
        SUCCESS(100),
        NO_PERMISSION(300, "鉴权失败"),
        INTERFACE_NOT_FOUND(401, "请求接口不存在"),
        METHOD_NOT_FOUND(402, "请求调用的方法不存在"),
        SERVER_EXCEPTION(500, "服务端出现未知异常"),
        SERIALIZATION(600, "序列化器异常"),
        ;
        private Integer code;

        private String msg;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        Status(Integer code) {
            this.code = code;
        }

        Status(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    public enum RpcMsgTypeCode{
        REQUEST(0),
        RESPONSE(1),
        PINGMSG(2),
        ;
        public final int code;

        RpcMsgTypeCode(int code) {
            this.code = code;
        }
    }
}
