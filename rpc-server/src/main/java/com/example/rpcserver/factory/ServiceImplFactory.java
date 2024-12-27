package com.example.rpcserver.factory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Cbc
 * @DateTime 2024/12/25 20:43
 * @Description 服务实现类工厂, 可交由别人自定义, 增加了扩展性
 */
public interface ServiceImplFactory {

    void openServiceImpl(Class<?> implClass, Class<?> interfaceClass) throws IOException;

    Object getServiceImpl(Class<?> interfaceClass);
}
