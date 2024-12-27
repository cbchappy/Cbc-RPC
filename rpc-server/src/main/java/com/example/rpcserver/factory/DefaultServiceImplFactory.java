package com.example.rpcserver.factory;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Cbc
 * @DateTime 2024/12/25 20:58
 * @Description ServiceImplFactory的默认实现 使用了spi机制
 */
@Slf4j
public class DefaultServiceImplFactory implements ServiceImplFactory{
    //存储已经使用过的Object, 节省时间
    private final Map<Class<?>, Object> objectMap = new ConcurrentHashMap<>();
    @Override
    public void openServiceImpl(Class<?> implClass, Class<?> interfaceClass) throws IOException {
        String classPath = getClassPath(implClass);

        log.debug("进行ISP注册, 接口名:{}, 实现类名:{}", interfaceClass, implClass);

        File dir = new File(classPath, "META-INF/services");
        if (!dir.exists()) {
            log.debug("创建目录:{}", dir.mkdirs());
        }

        String fileName = interfaceClass.getName();

        File file = new File(dir.getPath(), fileName);

        if (!file.exists()) {
            log.debug("创建文件:{}", file.createNewFile());
        }

        FileOutputStream stream = new FileOutputStream(file, true);
        stream.write(implClass.getName().getBytes(StandardCharsets.UTF_8));
        stream.write('\n');
        stream.close();
    }

    //没有获取到则返回空值
    @Override
    public Object getServiceImpl(Class<?> interfaceClass) {
        if (objectMap.containsKey(interfaceClass)){
            return objectMap.get(interfaceClass);
        }
        Iterator<?> iterator = ServiceLoader.load(interfaceClass).iterator();
        if(!iterator.hasNext()){
            return null;
        }
        Object next = iterator.next();
        objectMap.put(interfaceClass, next);
        return next;
    }

    //获取类路径
    private static String getClassPath(Class<?> impLclass) {
        ClassLoader classLoader = impLclass.getClassLoader();
        URL resource = classLoader.getResource("");
        if (resource != null) {
            return resource.getPath();
        }
        return null;
    }

}
