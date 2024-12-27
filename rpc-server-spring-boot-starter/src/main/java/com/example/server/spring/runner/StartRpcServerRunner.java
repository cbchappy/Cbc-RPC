package com.example.server.spring.runner;


import com.example.rpcserver.server.RpcServer;
import com.example.server.spring.factory.SpringServiceImplFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;


/**
 * @Author Cbc
 * @DateTime 2024/12/12 15:48
 * @Description 完成启动任务 1.完善SpringServiceImplFactory  2.将其注册到RpcServer中   3.开启服务
 */
@Slf4j
public class StartRpcServerRunner implements ApplicationRunner {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("StartRpcServerRunner完善和注册factory");
        SpringServiceImplFactory.getSingleton().setApplicationContext(applicationContext);
        RpcServer.setServiceImplFactory(SpringServiceImplFactory.getSingleton());

        log.debug("StartRpcServerRunner开始启动rpcServer");
        RpcServer.startServer();
    }


}
