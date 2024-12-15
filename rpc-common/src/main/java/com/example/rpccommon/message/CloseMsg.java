package com.example.rpccommon.message;

import lombok.Builder;
import lombok.Data;

/**
 * @Author Cbc
 * @DateTime 2024/12/15 19:19
 * @Description 断开连接信息
 */

public class CloseMsg extends RpcMsg{
    private final Integer status; //0   正常关闭    1.协议错误      2.服务端不接受连接 应该刷新instances  重新获取服务

    public Integer getStatus() {
        return status;
    }

    public CloseMsg(CloseStatus closeStatus){
        this.status = closeStatus.code;
    }

    public enum CloseStatus{
        normal(0),
        protocolError(1),
        refuse(2);


        public Integer code;

        CloseStatus(int code){
            this.code = code;
        }


    }

}
