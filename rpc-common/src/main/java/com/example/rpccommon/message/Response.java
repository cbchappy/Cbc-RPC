package com.example.rpccommon.message;


import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:45
 * @Description
 */
@Data
@Builder
@AllArgsConstructor
public class Response extends RpcMsg{

    public Response(){

    }


    private Integer msgId;

    private Integer status;

    private Object res;

    private String resClassName;

    private transient Throwable throwable;


    @Override
    public int getTypeCode() {
        return RpcMsgTypeCode.RESPONSE;
    }

    public boolean isSuccess(){
        return status.equals(ResponseStatus.SUCCESS.code);
    }
}
