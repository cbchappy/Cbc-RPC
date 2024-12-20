package com.example.rpccommon.serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianOutput;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 12:20
 * @Description Hessian序列化
 */
@Slf4j
public class HessianSerializer extends RpcSerializer{

    private HessianSerializer(){}
    @Override
    public byte[] serialize(Object obj) {
        log.debug("Hessian方式序列化");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output hop = new Hessian2Output(bos);
        try {
            hop.writeObject(obj);
            hop.close();
        } catch (IOException e) {
            log.error("序列化失败");
            throw new RuntimeException(e);
        }

        return bos.toByteArray();
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        log.debug("Hessian方式反序列化");
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Hessian2Input hip = new Hessian2Input(bis);
        try {
            Object o = hip.readObject();
            hip.close();
            return (T) o;
        } catch (IOException e) {
            log.error("反序列化成功");
            throw new RuntimeException(e);
        }

    }
    //单例模式
    public static HessianSerializer getInstance(){
        return Singleton.serializer;
    }

    private static class Singleton{
        public static HessianSerializer serializer = new HessianSerializer();
    }
}
