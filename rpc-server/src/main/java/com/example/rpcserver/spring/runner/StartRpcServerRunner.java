package com.example.rpcserver.spring.runner;


import com.example.rpcserver.server.RpcServer;
import com.example.rpcserver.spring.scan.RpcServerScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 15:48
 * @Description
 */
@Slf4j
public class StartRpcServerRunner implements ApplicationRunner {


  private   static final Map<Class<?>, Object> beanMap = new ConcurrentHashMap<>();

    @Autowired
    ApplicationContext applicationContext;




    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<Class<?>> list = RpcServerScan.getList();
        for (Class<?> aClass : list) {
            Object bean = applicationContext.getBean(aClass);
            log.debug("获取到bean:{}", aClass);
            beanMap.put(aClass, bean);
        }
        log.debug("StartRpcServerRunner开始启动rpc server");
        RpcServer.startServer();
    }



    public static Object getBean(Class<?> c){
        Object o = beanMap.get(c);
        log.debug("获取{}的bean, 值为:{}", c, o == null ? "null" : o.getClass());
        return o;
    }
}
