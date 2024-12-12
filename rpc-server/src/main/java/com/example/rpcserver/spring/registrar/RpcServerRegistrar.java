package com.example.rpcserver.spring.registrar;

import com.example.rpcserver.spring.annotation.StartRpcServer;
import com.example.rpcserver.spring.scan.RpcServerScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 16:10
 * @Description
 */
@Slf4j
public class RpcServerRegistrar implements ImportBeanDefinitionRegistrar {

    @Autowired
    private Environment environment;
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        log.debug("RpcServerRegistrar");
        Map<String, Object> map = metadata.getAnnotationAttributes(StartRpcServer.class.getName());
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(map);

        if(attributes != null){
            String[] values = attributes.getStringArray("values");
            String s = StringUtils.collectionToCommaDelimitedString(List.of(values));
            log.debug(" RpcServerScan doScan: {}", s);
            RpcServerScan scan = new RpcServerScan(registry);
            scan.doScan(s);
        }
    }
}
