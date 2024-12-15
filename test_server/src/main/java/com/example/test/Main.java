package com.example.test;

import com.example.rpcserver.server.RpcServer;
import com.example.test.service.impl.TestServerImpl;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;

import java.io.IOException;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 17:53
 * @Description
 */
public class Main {

    public static void main(String[] args) throws Exception {
        start();
    }

    public static void start() throws Exception {
        RpcServer.openServiceImpl(TestServerImpl.class);
        RpcServer.startServer();
    }
}
