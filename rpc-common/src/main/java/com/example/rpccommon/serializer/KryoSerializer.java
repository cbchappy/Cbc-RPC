package com.example.rpccommon.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import com.example.rpccommon.message.Request;
import com.example.rpccommon.message.Response;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Cbc
 * @DateTime 2025/5/8 20:20
 * @Description
 */
@Slf4j
public class KryoSerializer extends RpcSerializer {


    private static final ThreadLocal<Kryo> threadLocal = new ThreadLocal<>();

    @Override
    public byte[] serialize(Object obj) {
        Kryo kryo = getKryo();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        kryo.writeObject(output,obj);//写入null时会报错
        output.close();
        return bos.toByteArray();
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = getKryo();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Input input = new Input(bis);
        T t =  kryo.readObject(input, clazz);//读出null时会报错
        input.close();
        return t;
    }

    public static KryoSerializer getInstance(){
        return Instance.serializer;
    }

    private static class Instance{
        public static KryoSerializer serializer = new KryoSerializer();
    }

    private static Kryo getKryo(){
        Kryo kryo = threadLocal.get();
        if(kryo == null){
            kryo = new Kryo();
            kryo.register(Response.class);
            kryo.register(Request.class);
            kryo.register(String.class);
            kryo.register(String[].class);
            kryo.register(Object.class);
            kryo.register(Object[].class);
            kryo.register(Integer.class);
            kryo.setReferences(false);
            kryo.register(HashMap.class);

        }
        threadLocal.set(kryo);
        return kryo;
    }



}
