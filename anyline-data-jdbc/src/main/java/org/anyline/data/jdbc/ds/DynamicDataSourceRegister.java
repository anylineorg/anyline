package org.anyline.data.jdbc.ds;

import org.anyline.data.jdbc.util.DataSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;

/**
 * 系统启动时解析多数据源配置
 * 需要在启动类上注解 @org.springframework.context.annotation.Import(DynamicDataSourceRegister.class)
 * 注意这时spring上下文还没有初始化完成,不要调用spring context BeanFactory
 */
//@Component
//@Import(DynamicDataSourceRegister.class)
public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    //启动类以Import形式注入时 激活，激活后禁用JDBCHolder
    public static boolean ACTIVE = false;
    private Logger log = LoggerFactory.getLogger(DynamicDataSourceRegister.class);
    //指定默认数据源(springboot2.0默认数据源是hikari如何想使用其他数据源可以自己配置)


    @Override
    public void setEnvironment(Environment environment) {
        ACTIVE = true;
        initDefaultDataSource(environment);
        initSpringDataSources(environment);
    }
    private void initDefaultDataSource(Environment env) {
        // 读取主数据源
        DataSource ds = DataSourceUtil.buildDataSource("spring.datasource",env);
        DynamicDataSource.setDefaultDatasource(ds);
        log.warn("[加载默认数据源]");
    }
    private void initSpringDataSources(Environment env) {
        // 读取配置文件获取更多数据源
        String prefixs = env.getProperty("spring.datasource.list");
        if(null != prefixs){
            for (String prefix : prefixs.split(",")) {
                // 多个数据源
                DataSource ds = DataSourceUtil.buildDataSource("spring.datasource."+prefix,env);
                DynamicDataSource.addDataSource(prefix, ds);
                DataSourceHolder.reg(prefix);
                log.info("[创建数据源][prefix:{}]",prefix);
            }
        }
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {

        //添加默认数据源
         DataSourceHolder.reg("dataSource");
        //添加其他数据源
        for (String key : DynamicDataSource.getDataSources().keySet()) {
            log.info("[注册数据源][key:{}]",key);
            DataSourceHolder.reg(key);
        }

        //创建DynamicDataSource
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DynamicDataSource.class);
        beanDefinition.setSynthetic(true);
        MutablePropertyValues mpv = beanDefinition.getPropertyValues();
        mpv.addPropertyValue("defaultTargetDataSource", DynamicDataSource.getDefaultDatasource());
        mpv.addPropertyValue("targetDataSources", DynamicDataSource.getDataSources());
        //注册 - BeanDefinitionRegistry
        beanDefinitionRegistry.registerBeanDefinition("dataSource", beanDefinition);

    }


}
