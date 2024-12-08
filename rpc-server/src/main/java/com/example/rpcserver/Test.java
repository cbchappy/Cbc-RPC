package com.example.rpcserver;

import com.example.rpcserver.registry.Registry;



/**
 * @Author Cbc
 * @DateTime 2024/12/8 19:09
 * @Description
 */
public class Test {

    public static void main(String[] args) throws Exception {

        Registry.registryServer();


        Thread.sleep(20000L);

    }
}
