package com.example.client.spring.scan;

import com.example.client.spring.annotation.RpcInvoke;
import com.example.client.spring.factory.RpcClientFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:23
 * @Description 自定义扫描类 扫描添加了@RpcInvoke注解的接口, 并为其注入代理工厂
 */
@Slf4j
public class RpcClientScanner extends ClassPathBeanDefinitionScanner {

    public RpcClientScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> set = super.doScan(basePackages);

        if(set.size() > 0){
            for (BeanDefinitionHolder holder : set) {
                log.debug("process holder");
                GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
                String beanClassName = definition.getBeanClassName();
                definition.setBeanClass(RpcClientFactoryBean.class);//FactoryBean的实现类 会从其获取bean
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);//依赖的注入方式
                definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);//注入FactoryBean时自动转换
            }
        }

        return set;
    }


    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        //AnnotatedBeanDefinition 存储bean的注释信息 可以用来筛选合适的目标
        log.debug("isCandidateComponent");
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface() && metadata.isIndependent() && metadata.hasAnnotation(RpcInvoke.class.getName());
    }
}
