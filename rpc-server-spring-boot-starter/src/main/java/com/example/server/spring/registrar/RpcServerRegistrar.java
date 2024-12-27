package com.example.server.spring.registrar;

import com.example.server.spring.annotation.OpenRpcService;
import com.example.server.spring.annotation.StartRpcServer;
import com.example.server.spring.factory.SpringServiceImplFactory;
import com.example.server.spring.scan.RpcServiceScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 16:10
 * @Description  将添加了@OpenRpcService的类视为开放   通过工厂进行添加 在run时注册applicationContext
 */

@Slf4j
public class RpcServerRegistrar implements ImportBeanDefinitionRegistrar {

    //!!!!只会在Import它的那个类上进行方法调用, 也就是metadata存储的是Import它的那个类的信息
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        log.debug("在RpcServerRegistrar启动扫描类");
        Map<String, Object> map = metadata.getAnnotationAttributes(StartRpcServer.class.getName());
        if (map != null) {
            String[] value = (String[]) map.get("values");
            if(value.length > 0){
                String s = StringUtils.collectionToCommaDelimitedString(List.of(value));
                RpcServiceScanner scanner = new RpcServiceScanner(registry);
                scanner.doScan(s);
            }
        }

    }
}
