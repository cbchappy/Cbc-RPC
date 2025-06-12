package com.example.rpcclient.listener;

import com.example.rpcclient.server.InstanceWrapper;
import com.example.rpccommon.constants.ResponseStatus;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 17:51
 * @Description
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class InvokeCompleteEvent extends InvokeEvent{
    private Throwable throwable;
    private InstanceWrapper wrapper;
    private Request request;
    private Response response;

}
