package com.example.rpccommon.message;

import com.example.rpccommon.constants.RpcMsgTypeCode;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 16:48
 * @Description
 */
public class PingMsg extends RpcMsg{
    @Override
    public int getTypeCode() {
        return RpcMsgTypeCode.PINGMSG;
    }
}
