package com.example.rpccommon.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.rpccommon.message.PingMsg;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;

import lombok.extern.slf4j.Slf4j;


import java.nio.charset.StandardCharsets;

/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:17
 * @Description json序列化 类似数组参数仍然会有风险！！！
 */
//todo 完善json
@Slf4j
public class JsonSerializer extends RpcSerializer {


    @Override
    public byte[] serialize(Object obj) {
        //转为json
        String s = JSON.toJSONString(obj);

        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object deSerialize(byte[] bytes) {
        String json = new String(bytes, StandardCharsets.UTF_8);

        JSONObject jo = JSON.parseObject(json);

        if (jo.getInnerMap().containsKey("status")) {
            return deSerializedResponse(jo);
        } else if (jo.getInnerMap().containsKey("msgId")) {
            return deSerializedRequest(jo);
        }

       return jo.toJavaObject(PingMsg.class);
    }

    private static Response deSerializedResponse(JSONObject jo) {
        Response rsp = jo.toJavaObject(Response.class);

        if (rsp.getRes() instanceof JSONObject) {
            JSONObject r = (JSONObject) rsp.getRes();
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
            throw new RuntimeException(e);
        }

        return rqs;
    }
}
