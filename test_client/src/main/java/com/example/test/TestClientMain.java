package com.example.test;

import com.example.rpcclient.proxy.ProxyFactory;
import com.example.test.service.TestServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 18:03
 * @Description
 */
@Slf4j
public class TestClientMain {

    public static void main(String[] args) throws InterruptedException {
        TestServer proxy = (TestServer) ProxyFactory.createProxy(TestServer.class);
        for (int i = 0; i < 3; i++) {
            String err = proxy.get();
            System.err.println( "----------" + err + "----------");
        }
    }

    public static class Test{

        public Test(Class<?> clazz) {
            log.debug(String.valueOf(clazz));
        }
    }
}
