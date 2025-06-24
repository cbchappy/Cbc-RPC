package com.example.rpccommon.message;


import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.constants.RpcMsgTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;


/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:45
 * @Description
 */
@Data
@Builder
@AllArgsConstructor
public class Response implements Serializable {

    public Response() {

    }


    private transient Byte serializeCode;

    private transient Long rqId;

    private transient boolean isAsync;

    private Integer status;

    private Object res;

    private String resClassName;

    private transient Throwable throwable;


    public boolean isSuccess() {
        return status.equals(ResponseStatus.SUCCESS.code);
    }
}
