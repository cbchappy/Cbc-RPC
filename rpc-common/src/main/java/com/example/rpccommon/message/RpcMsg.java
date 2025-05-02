package com.example.rpccommon.message;

import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import com.example.rpccommon.exception.RpcCommonException;
import com.example.rpccommon.exception.RpcRequestException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 13:34
 * @Description
 */
public abstract class RpcMsg implements Serializable {
    private final static Map<Integer, Class<?>> CLASS_MAP = new HashMap<>();

    static {
        CLASS_MAP.put(RpcMsgTypeCode.CLOSE, CloseMsg.class);
        CLASS_MAP.put(RpcMsgTypeCode.REQUEST, Request.class);
        CLASS_MAP.put(RpcMsgTypeCode.PINGMSG, PingMsg.class);
        CLASS_MAP.put(RpcMsgTypeCode.RESPONSE, Response.class);
        CLASS_MAP.put(RpcMsgTypeCode.PING_ACK, PingAckMsg.class);
    }


    private static final AtomicInteger generateId = new AtomicInteger(Integer.MIN_VALUE);//自增, 维护唯一id
    public abstract int getTypeCode();

    public static Integer generateId(){
        return generateId.incrementAndGet();
    }


    public static Class<?> getClassByTypeCode(int code){
        return CLASS_MAP.get(code);
    }

}
