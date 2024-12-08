package com.example.rpccommon.serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author Cbc
 * @DateTime 2024/12/8 12:20
 * @Description Hessian序列化
 */
public class HessianSerializer extends RpcSerializer{
    @Override
    public byte[] serialize(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Hessian2Output hop = new Hessian2Output(bos);
        try {
            hop.writeObject(obj);
            hop.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bos.toByteArray();
    }

    @Override
    public Object deSerialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Hessian2Input hip = new Hessian2Input(bis);
        try {
            Object o = hip.readObject();
            hip.close();
            return o;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
