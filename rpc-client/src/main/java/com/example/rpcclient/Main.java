package com.example.rpcclient;

import com.example.rpccommon.message.Request;
import com.example.rpccommon.serializer.HessianSerializer;
import com.example.rpccommon.serializer.KryoSerializer;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2025/1/11 19:30
 * @Description
 */
@Slf4j
public class Main {
    int i = 0;

    public static void main(String[] args) {
        HessianSerializer hessianSerializer = HessianSerializer.getInstance();
        KryoSerializer kryoSerializer = KryoSerializer.getInstance();
        Request request = Request.builder()
                .args(null)
                .methodName(null)
                .args(null)
                .build();
        count(() -> {hessianSerializer.serialize(request);});
        count(() -> {kryoSerializer.serialize(request);});
    }

    public static void count(Runnable runnable){
        long s = System.nanoTime();
        runnable.run();
        System.out.println(System.nanoTime() - s);
    }
}
