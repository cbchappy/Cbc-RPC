package com.example.rpccommon.serializer;

import java.io.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:12
 * @Description java原始字节序列化
 */
public class JavaSerializer extends RpcSerializer{
    @Override
    public byte[] serialize(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("java字节流序列化出错");
        }
    }

    @Override
    public Object deSerialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("java字节流反序列化出错");
        }
    }
}
