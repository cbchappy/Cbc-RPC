package com.example.rpccommon;

import com.example.rpccommon.exception.RpcRequestException;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.serializer.JsonSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:53
 * @Description
 */
@Slf4j
public class Main {

    public static void main(String[] args) {

        Entity entity = new Entity();
        entity.setName("cbc");

        RpcRequestException exception = new RpcRequestException("exception");

        JsonSerializer jsr = new JsonSerializer();

        Request re = Request.builder()
                .methodName("1")
                .interfaceName("1")
                .args(new Object[]{1, "hh", exception})
                .build();

        byte[] bytes = jsr.serialize(re);

        Request r = (Request) jsr.deSerialize(bytes);

        RpcRequestException e = (RpcRequestException) r.getArgs()[2];

        log.debug(e.getMessage());



    }
}
