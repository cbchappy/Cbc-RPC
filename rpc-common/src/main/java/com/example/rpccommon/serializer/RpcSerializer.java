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

    public abstract byte[] serialize(Object obj);

    public abstract <T> T deSerialize(byte[] bytes, Class<T> clazz);

    //单例模式
    public static RpcSerializer getSerializerByCode(int code){
            if(code == SerializerCode.JDK){
               return JavaSerializer.getInstance();
            } else if (code == SerializerCode.HESSIAN) {
                return HessianSerializer.getInstance();
            } else if (code == SerializerCode.JSON) {
                return JsonSerializer.getInstance();
            } else if (code == SerializerCode.Kryo) {
                return KryoSerializer.getInstance();
            }
        throw new RpcCommonException(RpcExceptionMsg.SERIALIZER_NOTFOUND);
    }


}
