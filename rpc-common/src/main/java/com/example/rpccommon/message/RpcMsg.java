package com.example.rpccommon.message;

import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import com.example.rpccommon.exception.RpcCommonException;
import com.example.rpccommon.exception.RpcRequestException;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 13:34
 * @Description
 */
public abstract class RpcMsg implements Serializable {


    private static final AtomicInteger generateId = new AtomicInteger(Integer.MIN_VALUE);
    //获取请求类型号码 0 request  1 response 2....
    public static Integer getTypeCode(Object obj) {
        if (obj instanceof Request) {
            return RpcMsgTypeCode.REQUEST;
        } else if (obj instanceof Response) {
            return RpcMsgTypeCode.RESPONSE;
        } else if (obj instanceof PingMsg) {
            return RpcMsgTypeCode.PINGMSG;
        }
        throw new RpcCommonException("消息类型错误");
    }

    public static Integer generateId(){
        return generateId.incrementAndGet();
    }


    public static RpcMsg typeConversion(Object o, int code){
        if(code == RpcMsgTypeCode.REQUEST){
            return (Request) o;
        } else if (code == RpcMsgTypeCode.RESPONSE) {
            return (Response) o;
        }else if(code == RpcMsgTypeCode.PINGMSG){
            return (PingMsg) o;
        }
        throw new RpcCommonException(RpcExceptionMsg.MESSAGE_CONVERSION_ERROR);
    }



}
