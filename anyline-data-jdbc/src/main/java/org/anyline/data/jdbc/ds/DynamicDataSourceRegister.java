package org.anyline.data.jdbc.ds;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统启动时解析多数据源配置
 * 需要在启动类上注解 @org.springframework.context.annotation.Import(DynamicDataSourceRegister.class)
 * 注意这时spring上下文还没有初始化完成,不要调用spring context BeanFactory
 */
//@Component
//@Import(DynamicDataSourceRegister.class)
public class DynamicDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    private Logger log = LoggerFactory.getLogger(DynamicDataSourceRegister.class);
    //指定默认数据源(springboot2.0默认数据源是hikari如何想使用其他数据源可以自己配置)
    private static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";


    @Override
    public void setEnvironment(Environment environment) {
        initDefaultDataSource(environment);
        initSpringDataSources(environment);
    }
    private void initDefaultDataSource(Environment env) {
        // 读取主数据源
        DataSource ds = buildDataSource("spring.datasource",env);
        DynamicDataSource.setDefaultDatasource(ds);
    }
    private void initSpringDataSources(Environment env) {
        // 读取配置文件获取更多数据源
        String prefixs = env.getProperty("spring.datasource.list");
        if(null != prefixs){
            for (String prefix : prefixs.split(",")) {
                // 多个数据源
                DataSource ds = buildDataSource("spring.datasource."+prefix,env);
                DynamicDataSource.addDataSource(prefix, ds);
                DataSourceHolder.reg(prefix);
                log.warn("[创建数据源][prefix:{}]",prefix);
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


    @SuppressWarnings("unchecked")
    public static DataSource buildDataSource(String prefix, Environment env) {
        try {
            if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")){
                prefix += ".";
            }
            String type = BeanUtil.value(prefix, env, "type");
            if(null == type){
                type = BeanUtil.value("spring.datasource.", env, "type");
            }
            if (type == null) {
                type = DATASOURCE_TYPE_DEFAULT;
            }

            Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName(type);
            String driverClassName = BeanUtil.value(prefix, env, "driver","driver-class","driver-class-name");
            String url = BeanUtil.value(prefix, env, "url","jdbc-url");
            String username = BeanUtil.value(prefix, env,"user","username","user-name");
            String password = BeanUtil.value(prefix, env, "password");

            DataSource ds =  dataSourceType.newInstance();
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("url", url);
            map.put("jdbcUrl", url);
            map.put("driver",driverClassName);
            map.put("driverClass",driverClassName);
            map.put("driverClassName",driverClassName);
            map.put("user",username);
            map.put("username",username);
            map.put("password",password);
            BeanUtil.setFieldsValue(ds, map);
            return ds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据配置文件设置对象属性值
     * @param obj 对象
     * @param prefix 前缀
     * @param env 配置文件环境
     */
    private static void setFieldsValue(Object obj, String prefix, Environment env ){
        List<String> fields = ClassUtil.getFieldsName(obj.getClass());
        for(String field:fields){
            String value = BeanUtil.value(prefix, env, field);
            if(BasicUtil.isNotEmpty(value)) {
                BeanUtil.setFieldValue(obj, field, value);
            }
        }
    }


}
