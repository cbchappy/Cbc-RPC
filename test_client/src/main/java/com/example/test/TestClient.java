package com.example.test;

import com.example.rpcclient.proxy.ProxyFactory;
import com.example.test.service.TestRpc;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Cbc
 * @DateTime 2024/12/10 18:03
 * @Description
 */
@Slf4j
public class TestClient {

    public static void main(String[] args) throws InterruptedException {
        //直接调用代理工厂生成要远程调用接口的代理实例即可
        TestRpc proxy = (TestRpc) ProxyFactory.createProxy(TestRpc.class);
        String s = proxy.get();//直接调用方法便可进行远程调用
        log.debug(s);
    }

}
