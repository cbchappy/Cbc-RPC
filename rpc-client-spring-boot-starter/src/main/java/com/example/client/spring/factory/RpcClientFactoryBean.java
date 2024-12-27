package com.example.client.spring.factory;

import com.example.rpcclient.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:18
 * @Description 生成远程调用接口的代理bean
 */
@Slf4j
public class RpcClientFactoryBean<T> implements FactoryBean<T> {
    private Class<T> interClass;

    public RpcClientFactoryBean() {
    }

    public RpcClientFactoryBean(Class<T> interClass) throws ClassNotFoundException {
        log.debug("调RpcClientFactoryBean的构造函数, {}", interClass);
        this.interClass = interClass;
    }

    @Override
    public T getObject() throws Exception {
        log.debug("调用RpcClientFactoryBean获取Bean, getBean({})", interClass);
        return (T) ProxyFactory.createProxy(interClass);
    }

    @Override
    public Class<T> getObjectType() {
        return interClass;
    }
}
