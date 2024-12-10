package com.example.test;

import com.example.rpcserver.server.RpcServer;
import com.example.test.service.impl.TestServerImpl;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:53
 * @Description
 */
public class Main {

    public static void main(String[] args) throws Exception {
        RpcServer.openServiceImpl(TestServerImpl.class);
        RpcServer.startServer();
    }
}
