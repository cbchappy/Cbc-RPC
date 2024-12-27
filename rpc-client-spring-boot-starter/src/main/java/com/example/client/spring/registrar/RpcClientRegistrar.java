package com.example.client.spring.registrar;

import com.example.client.spring.annotation.RpcClientScan;
import com.example.client.spring.scan.RpcClientScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:21
 * @Description 在bean未注册前启动自定义扫描类进行扫描和处理
 */
@Slf4j
public class RpcClientRegistrar implements ImportBeanDefinitionRegistrar {

    //对符合条件的一个接口或一个类调用一次？？？
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        log.debug("RpcClientRegistrar");
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(RpcClientScan.class.getName());
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(annotationAttributes);

        if(attributes != null){
            String[] values = attributes.getStringArray("values");
            String s = StringUtils.collectionToCommaDelimitedString(List.of(values));
            log.debug("doScan basePackages:{}", s);
            RpcClientScanner scanner = new RpcClientScanner(registry);
            scanner.doScan(s);
        }

    }
}
