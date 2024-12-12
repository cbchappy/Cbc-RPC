package com.example.rpcclient.spring.scan;

import com.example.rpcclient.spring.annotation.RpcInvoke;
import com.example.rpcclient.spring.factory.RpcClientFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.TypeFilter;
import java.util.Set;

/**
 * @Author Cbc
 * @DateTime 2024/12/12 14:23
 * @Description
 */
@Slf4j
public class RpcClientScanner extends ClassPathBeanDefinitionScanner {

    public RpcClientScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public void addIncludeFilter(TypeFilter includeFilter) {
        log.debug("addIncludeFilter");
        super.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {

        Set<BeanDefinitionHolder> set = super.doScan(basePackages);

        if(set.size() > 0){
            for (BeanDefinitionHolder holder : set) {
                log.debug("process holder");
                GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
                String beanClassName = definition.getBeanClassName();
                definition.setBeanClass(RpcClientFactoryBean.class);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
            }
        }

        return set;
    }


    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        log.debug("isCandidateComponent");
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface() && metadata.isIndependent() && metadata.hasAnnotation(RpcInvoke.class.getName());
    }
}
