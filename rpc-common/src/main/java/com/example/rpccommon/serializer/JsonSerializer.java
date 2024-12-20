package com.example.rpccommon.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.rpccommon.message.*;

import lombok.extern.slf4j.Slf4j;


import java.nio.charset.StandardCharsets;

/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:17
 * @Description json序列化 记得在要序列化属性加上setter和getter方法
 */
//todo json序列化容易出bug
@Slf4j
public class JsonSerializer extends RpcSerializer {

    private JsonSerializer(){}

    @Override
    public byte[] serialize(Object obj) {
        log.debug("json方式序列化");
        //转为json
        String s = JSON.toJSONString(obj);

        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz){
        log.debug("json方式反序列化");
        String json = new String(bytes, StandardCharsets.UTF_8);

        JSONObject jo = JSON.parseObject(json);


        if (clazz.equals(Response.class)) {
            return (T) deSerializedResponse(jo);
        } else if (clazz.equals(Request.class)) {
            return (T) deSerializedRequest(jo);
        } else if (clazz.equals(CloseMsg.class)) {
            return jo.toJavaObject(clazz);
        }

        return jo.toJavaObject(clazz);
    }

    private static Response deSerializedResponse(JSONObject jo) {
        Response rsp = jo.toJavaObject(Response.class);

        if (rsp.getRes() instanceof JSONObject r) {
            try {
                rsp.setRes(r.toJavaObject(Class.forName(rsp.getResClassName())));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return rsp;
    }

    private static Request deSerializedRequest(JSONObject jo) {
        Request rqs = jo.toJavaObject(Request.class);
        Object[] args = rqs.getArgs();
        if (args == null || args.length == 0) {
            return rqs;
        }

        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof JSONObject) {
                    Class<?> aClass = Class.forName(rqs.getArgsClassNames()[i]);
                    args[i] = ((JSONObject) args[i]).toJavaObject(aClass);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return rqs;
    }


    public static JsonSerializer getInstance(){
        return Singleton.serializer;
    }

    private static class Singleton{
        public static JsonSerializer serializer = new JsonSerializer();
    }
}
