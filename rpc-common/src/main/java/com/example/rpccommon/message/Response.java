package com.example.rpccommon.message;

import com.example.rpccommon.Entity;
import com.example.rpccommon.serializer.JsonSerializer;
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


    private Integer serializationId;

    private Integer status;

    private Object res;

    private String resClassName;




}
