package com.example.rpccommon.message;

import com.example.rpccommon.constants.RpcMsgTypeCode;

/**
 * @Author Cbc
 * @DateTime 2025/4/20 21:07
 * @Description
 */
public class PingAckMsg extends RpcMsg{
    @Override
    public int getTypeCode() {
        return RpcMsgTypeCode.PING_ACK;
    }
}
