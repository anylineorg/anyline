package org.anyline.data.jdbc.ds;

import org.anyline.dao.init.springjdbc.DefaultDao;
import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.listener.DMListener;
import org.anyline.service.init.DefaultService;
import org.anyline.util.ConfigTable;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Map;

public class RuntimeHolder {

    private static Logger log = LoggerFactory.getLogger(RuntimeHolder.class);
    private static Map<String, JDBCRuntime> runtimes = new Hashtable();
     private static DefaultListableBeanFactory factory;
    public static void init(DefaultListableBeanFactory factory){
        RuntimeHolder.factory = factory;
    }
    public static void reg(String key, String ds){
        //DataSourceHolder.reg(key);
        String template_key = "anyline.jdbc.template." + key;

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class);
        builder.addPropertyReference("dataSource", ds);
        BeanDefinition definition = builder.getBeanDefinition();
        factory.registerBeanDefinition(template_key, definition);

        JdbcTemplate template = factory.getBean(template_key, JdbcTemplate.class);
        reg(key, template, null);
    }

    public static void reg(String key, JDBCRuntime runtime){
        runtimes.put(key, runtime);
    }

    public static void reg(String key, DataSource ds){
        //DataSourceHolder.reg(key);
        String template_key = "anyline.jdbc.template." + key;

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class);
        builder.addPropertyValue("dataSource", ds);
        BeanDefinition definition = builder.getBeanDefinition();
        factory.registerBeanDefinition(template_key, definition);

        JdbcTemplate template = factory.getBean(template_key, JdbcTemplate.class);
        reg(key, template, null);
    }
    public static void reg(String datasource, JdbcTemplate template, JDBCAdapter adapter){
        log.info("[create jdbc runtime][key:{}]", datasource);
        JDBCRuntime runtime = new JDBCRuntime(datasource, template, adapter);
        runtimes.put(datasource, runtime);
        if(!ConfigTable.IS_MULTIPLE_SERVICE){
            return;
        }
        String dao_key = "anyline.dao." + datasource;
        String service_key = "anyline.service." + datasource;
        log.warn("[instance service][data source:{}][instance id:{}]", datasource, service_key);

        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultDao.class);
        daoBuilder.addPropertyValue("runtime", runtime);
        //daoBuilder.addAutowiredProperty("listener");
        daoBuilder.addPropertyValue("listener", SpringContextUtil.getBean(DMListener.class));
        BeanDefinition daoDefinition = daoBuilder.getBeanDefinition();
        factory.registerBeanDefinition(dao_key, daoDefinition);

        BeanDefinitionBuilder serviceBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultService.class);
        serviceBuilder.addPropertyReference("dao", dao_key);
        serviceBuilder.addPropertyValue("cacheProvider", SpringContextUtil.getBean("anyline.cache.provider"));
        BeanDefinition serviceDefinition = serviceBuilder.getBeanDefinition();
        factory.registerBeanDefinition(service_key, serviceDefinition);

    }
    public static JDBCRuntime getRuntime(){
        return getRuntime(DataSourceHolder.curDataSource());
    }
    public static void destroyRuntime(String key){
        try {
            runtimes.remove(key);
            //注销 service dao template
            if(factory.containsBeanDefinition("anyline.service." + key)){
                factory.destroySingleton("anyline.service." + key);
            }
            if(factory.containsBeanDefinition("anyline.dao." + key)){
                factory.destroySingleton("anyline.dao." + key);
            }
            if(factory.containsBeanDefinition("anyline.jdbc.template." + key)){
                factory.destroySingleton("anyline.jdbc.template." + key);
            }
            if(factory.containsBeanDefinition("anyline.datasource." + key)){
                factory.destroySingleton("anyline.datasource." + key);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static JDBCRuntime getRuntime(String datasource){
        JDBCRuntime runtime = null;
        if(null == datasource){
            runtime = runtimes.get("default");
            if(null == runtime){
                runtime = runtimes.get("common");
            }
        }else {
            runtime = runtimes.get(datasource);
        }
        if(null == runtime){
            throw new RuntimeException("未注册数据源:"+datasource);
        }
        return runtime;
    }
    public static JdbcTemplate getJdbcTemplate(){
        JDBCRuntime runtime = getRuntime();
        if(null != runtime){
            return runtime.getTemplate();
        }
        return null;
    }
    public static DataSource getDataSource(){
        JDBCRuntime runtime = getRuntime();
        if(null != runtime){
            return runtime.getDatasource();
        }
        return null;
    }
    public static JdbcTemplate getJdbcTemplate(String key){
        JDBCRuntime runtime = getRuntime(key);
        if(null != runtime){
            return runtime.getTemplate();
        }
        return null;
    }
    public static DataSource getDataSource(String key){
        JDBCRuntime runtime = getRuntime(key);
        if(null != runtime){
            return runtime.getDatasource();
        }
        return null;
    }
}