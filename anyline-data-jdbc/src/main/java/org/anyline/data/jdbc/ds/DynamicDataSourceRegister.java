package org.anyline.data.jdbc.ds;

import org.anyline.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 系统启动时解析多数据源配置
 * 需要在启动类上注解 @org.springframework.context.annotation.Import(DynamicDataSourceRegister.class)
 * 注意这时spring上下文还没有初始化完成,不要调用spring context BeanFactory
 */
//@Component
//@Import(DynamicDataSourceRegister.class)
public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar {
    private Logger log = LoggerFactory.getLogger(DynamicDataSourceRegister.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        String ex = "2023-03-31之后的版本不需要DynamicDataSourceRegister,解决方式:在启动类上删除Import注解.\n原因请参考 http://doc.anyline.org";
        log.error(LogUtil.format(ex, 31));

        throw new RuntimeException(ex);

    }


}
