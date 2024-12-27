package com.example.server.spring.factory;

import com.example.rpcserver.factory.ServiceImplFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author Cbc
 * @DateTime 2024/12/25 21:11
 * @Description 替换默认实现类  单例模式
 */
public class SpringServiceImplFactory implements ServiceImplFactory {

    private  ApplicationContext applicationContext;

    //存储包含了@OpenRpcService的实现类的接口, 进行一些约束, 防止调用到没有开放的接口
    private final Set<Class<?>> set = new HashSet<>();


    @Override
    public void openServiceImpl(Class<?> implClass, Class<?> interfaceClass) throws IOException {
        set.add(interfaceClass);
    }

    @Override
    public Object getServiceImpl(Class<?> interfaceClass) {
        if(!set.contains(interfaceClass)){
            return null;
        }
        return applicationContext.getBean(interfaceClass);
    }

    public void setApplicationContext(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }

    public static SpringServiceImplFactory getSingleton(){
        return Instance.FACTORY;
    }

    private static class Instance{
        private final static SpringServiceImplFactory FACTORY = new SpringServiceImplFactory();
    }

}
