package com.example.rpccommon.serializer;

import com.example.rpccommon.constants.RpcExceptionMsg;
import com.example.rpccommon.constants.SerializerCode;
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
        if(code == SerializerCode.JDK){
            map.put(code, new JavaSerializer());
        } else if (code == SerializerCode.HESSIAN) {
            map.put(code, new HessianSerializer());
        } else if (code == SerializerCode.JSON) {
            map.put(code, new JsonSerializer());
        }else {
            throw new RpcCommonException(RpcExceptionMsg.SERIALIZER_NOTFOUND);
        }
        return map.get(code);
    }


}
