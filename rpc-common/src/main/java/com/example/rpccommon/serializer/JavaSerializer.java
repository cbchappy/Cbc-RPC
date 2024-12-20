package com.example.rpccommon.serializer;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:12
 * @Description java原始字节序列化
 */
@Slf4j
public class JavaSerializer extends RpcSerializer{

    private JavaSerializer(){

    }

    @Override
    public byte[] serialize(Object obj) {
        log.debug("jdk方式序列化");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
            log.error("序列化失败");
            throw new RuntimeException("java字节流序列化出错");
        }
    }

    @Override
    public  <T> T deSerialize(byte[] bytes, Class<T> clazz){
        log.debug("jdk方式反序列化");
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        }catch (Exception e){
            log.error("反序列化失败");
            e.printStackTrace();
            throw new RuntimeException("java字节流反序列化出错");
        }
    }

    public static JavaSerializer getInstance(){
        return Singleton.serializer;
    }

    private static class Singleton{
        public static JavaSerializer serializer = new JavaSerializer();
    }

}
