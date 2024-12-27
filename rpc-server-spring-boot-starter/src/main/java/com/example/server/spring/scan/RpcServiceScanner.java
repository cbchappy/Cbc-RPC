package com.example.server.spring.scan;

import com.example.server.spring.annotation.OpenRpcService;
import com.example.server.spring.factory.SpringServiceImplFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;

import java.io.IOException;
import java.util.Set;

/**
 * @Author Cbc
 * @DateTime 2024/12/26 19:27
 * @Description 扫描指定包并将合适的service实现类添加进工厂
 */
@Slf4j
public class RpcServiceScanner extends ClassPathBeanDefinitionScanner {
    public RpcServiceScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        if(metadata.hasAnnotation(OpenRpcService.class.getName()) && metadata.isConcrete()
            && metadata.getInterfaceNames().length == 1){;
            SpringServiceImplFactory singleton = SpringServiceImplFactory.getSingleton();
            ClassLoader classLoader = RpcServiceScanner.class.getClassLoader();
            try {
                Class<?> implClass = classLoader.loadClass(metadata.getClassName());
                Class<?> interfaceClass = classLoader.loadClass(metadata.getInterfaceNames()[0]);
                log.debug("扫描到impl, implClass:{}, interfaceClass:{}", implClass, interfaceClass);
                singleton.openServiceImpl(implClass, interfaceClass);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }

        }
        return false;
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        return super.doScan(basePackages);
    }
}
