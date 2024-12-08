package com.example.rpccommon.util;

import com.example.rpccommon.config.ProtocolConfig;
import com.example.rpccommon.exception.RpcCommonException;
import com.example.rpccommon.exception.RpcException;
import com.example.rpccommon.message.RpcMsg;
import io.netty.buffer.ByteBuf;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 17:05
 * @Description
 */
public class RpcUtil {

    public static void verifyMsg(ByteBuf buf){
        if(buf.readInt() != ProtocolConfig.getMagic()){
            throw new RpcCommonException(RpcException.RpcExceptionMsg.VERIFY_ERROR.getMsg());
        } else if (buf.readByte() != ProtocolConfig.getVersion()) {
            throw new RpcCommonException(RpcException.RpcExceptionMsg.VERIFY_ERROR.getMsg());
        }
    }
}
