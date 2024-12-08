package com.example.rpccommon.serializer;

import com.example.rpccommon.exception.RpcCommonException;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.exception.RpcRequestException;


import java.util.HashMap;
import java.util.Map;

/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:11
 * @Description
 */
public abstract class RpcSerializer {

    private final static Map<Integer, RpcSerializer> map = new HashMap<>();
    public abstract byte[] serialize(Object obj);

    public abstract Object deSerialize(byte[] bytes);

    public static RpcSerializer getSerializerByCode(int code){
        if(map.containsKey(code)){
            return map.get(code);
        }
        if(code == SerializerCode.JDK.code){
            map.put(code, new JavaSerializer());
        } else if (code == SerializerCode.HESSIAN.code) {
            map.put(code, new HessianSerializer());
        } else if (code == SerializerCode.JSON.code) {
            map.put(code, new JsonSerializer());
        }else {
            throw new RpcCommonException(RpcException.RpcExceptionMsg.SERIALIZER_NOTFOUND.getMsg());
        }
        return map.get(code);
    }


    public enum SerializerCode {
        JDK(0),
        JSON(1),
        HESSIAN(2),
        ;
        private final int code;
        SerializerCode(int code) {
            this.code = code;
        }
        public Integer getCode(){
            return code;
        }
    }



}
