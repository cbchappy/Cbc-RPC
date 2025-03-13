package com.example.test;

import com.example.rpcserver.server.RpcServer;
import com.example.test.service.TestRpc;
import com.example.test.service.impl.TestRpcImpl;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:53
 * @Description
 */
public class TestServer {

    public static void main(String[] args) throws Exception {
        RpcServer.openServiceImpl(TestRpcImpl.class, TestServer.class);
        RpcServer.startServer();
    }


}

