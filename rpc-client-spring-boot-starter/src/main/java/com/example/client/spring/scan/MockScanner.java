package com.example.client.spring.scan;

import com.example.client.spring.annotation.Mock;
import com.example.client.spring.annotation.RpcInvoke;
import com.example.client.spring.factory.RpcClientFactoryBean;
import com.example.rpcclient.server.FallBack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * @Author Cbc
 * @DateTime 2025/5/3 17:46
 * @Description
 */
@Slf4j
public class MockScanner extends ClassPathBeanDefinitionScanner {
    public MockScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        resetFilters(false);
        addIncludeFilter(new AnnotationTypeFilter(Mock.class));
        Set<BeanDefinitionHolder> set = super.doScan(basePackages);
        return null;
    }
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        //AnnotatedBeanDefinition 存储bean的注释信息 可以用来筛选合适的目标

        AnnotationMetadata metadata = beanDefinition.getMetadata();
        if(metadata.isConcrete() && metadata.hasAnnotation(Mock.class.getName())){

            String className = beanDefinition.getBeanClassName();

            try {
                Class<?> aClass = Class.forName(className);
                Class<?>[] interfaces = aClass.getInterfaces();
                Constructor<?> constructor = Class.forName(className).getDeclaredConstructor();
                FallBack.registry(interfaces[0], constructor.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

}
