package com.example.rpcclient.spring.factory;

import com.example.rpcclient.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:18
 * @Description 生成远程调用接口的代理bean
 */
@Slf4j
public class RpcClientFactoryBean implements FactoryBean {
    private Class<?> interClass;

    public RpcClientFactoryBean() {
    }

//    public RpcClientFactoryBean(Class<?> interClass) {
//        log.debug("RpcClientFactoryBean代理接口名:{}", interClass);
//        this.interClass = interClass;
//    }

    public RpcClientFactoryBean(String s) throws ClassNotFoundException {
        log.debug("----FactoryBean----:{}", s);
        interClass = this.getClass().getClassLoader().loadClass(s);
    }

    @Override
    public Object getObject() throws Exception {
        log.debug("RpcClientFactoryBean getBean");
        return ProxyFactory.createProxy(interClass);
    }

    @Override
    public Class<?> getObjectType() {
        return interClass;
    }
}
