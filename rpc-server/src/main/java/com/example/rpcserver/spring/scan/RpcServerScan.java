package com.example.rpcserver.spring.scan;

import com.example.rpcserver.server.RpcServer;
import com.example.rpcserver.spring.annotation.OpenRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.*;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 16:13
 * @Description
 */
@Slf4j
public class RpcServerScan extends ClassPathBeanDefinitionScanner {

    private final static List<Class<?>> list = new ArrayList<>();

    public RpcServerScan(BeanDefinitionRegistry registry) {
        super(registry);
    }


    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {

        Set<BeanDefinitionHolder> set = super.doScan(basePackages);

        return set;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        log.debug("isCandidateComponent");
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.hasAnnotation(OpenRpcService.class.getName()) && metadata.isConcrete();
    }

    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {

        log.debug("checkCandidate:{}", beanName);
        try {
            Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());
            list.add(aClass.getInterfaces()[0]);
            RpcServer.openServiceImpl(aClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return super.checkCandidate(beanName, beanDefinition);
    }


    @Override
    public void addIncludeFilter(TypeFilter includeFilter) {
        log.debug("filter");
        super.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
    }

    public static List<Class<?>> getList(){
        return list;
    }
}
