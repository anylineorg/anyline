package org.anyline.data.jdbc.runtime;

import org.anyline.dao.init.springjdbc.FixDao;
import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.service.init.FixService;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


public class JdbcRuntimeHolder extends org.anyline.data.runtime.RuntimeHolder {

    /**
     * 注册运行环境
     * @param key 数据源前缀
     * @param ds 数据源bean id
     */
    public static void reg(String key, String ds){
        //ClientHolder.reg(key);
        String template_key = "anyline.jdbc.template." + key;

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class);
        builder.addPropertyReference("dataSource", ds);
        BeanDefinition definition = builder.getBeanDefinition();
        factory.registerBeanDefinition(template_key, definition);

        JdbcTemplate template = factory.getBean(template_key, JdbcTemplate.class);
        reg(key, template, null);
    }

    public static void reg(String key, DataRuntime runtime){
        runtimes.put(key, runtime);
    }

    public static void reg(String key, DataSource ds){
        //ClientHolder.reg(key);
        String template_key = "anyline.jdbc.template." + key;

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class);
        builder.addPropertyValue("dataSource", ds);
        BeanDefinition definition = builder.getBeanDefinition();
        factory.registerBeanDefinition(template_key, definition);

        JdbcTemplate template = factory.getBean(template_key, JdbcTemplate.class);
        reg(key, template, null);
    }

    /**
     * 注册运行环境
     * @param datasource 数据源前缀
     * @param template template
     * @param adapter adapter 可以为空 第一次执行时补齐
     */
    public static void reg(String datasource, JdbcTemplate template, JDBCAdapter adapter){
        log.info("[create jdbc runtime][key:{}]", datasource);
        DataRuntime runtime = new JdbcRuntime(datasource, template, adapter);
        if(runtimes.containsKey(datasource)){
            destroy(datasource);
        }
        runtimes.put(datasource, runtime);
        if(!ConfigTable.IS_MULTIPLE_SERVICE){
            return;
        }
        String dao_key = "anyline.dao." + datasource;
        String service_key = "anyline.service." + datasource;
        log.info("[instance service][data source:{}][instance id:{}]", datasource, service_key);

        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.genericBeanDefinition(FixDao.class);
        //daoBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        daoBuilder.addPropertyValue("runtime", runtime);
        daoBuilder.addPropertyValue("datasource", datasource);
        //daoBuilder.addPropertyValue("listener", SpringContextUtil.getBean(DMListener.class));
        //daoBuilder.addAutowiredProperty("listener");
        daoBuilder.setLazyInit(true);
        BeanDefinition daoDefinition = daoBuilder.getBeanDefinition();
        factory.registerBeanDefinition(dao_key, daoDefinition);


        BeanDefinitionBuilder serviceBuilder = BeanDefinitionBuilder.genericBeanDefinition(FixService.class);
        //serviceBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        serviceBuilder.addPropertyValue("datasource", datasource);
        serviceBuilder.addPropertyReference("dao", dao_key);
        //serviceBuilder.addAutowiredProperty("cacheProvider");
        serviceBuilder.setLazyInit(true);
        BeanDefinition serviceDefinition = serviceBuilder.getBeanDefinition();
        factory.registerBeanDefinition(service_key, serviceDefinition);


    }
    public static void destroyRuntime(String key){
        destroy(key);
    }
    public static void destroy(String key){
        try {
            runtimes.remove(key);
            //注销 service dao template
            if(factory.containsBeanDefinition("anyline.service." + key)){
                factory.destroySingleton("anyline.service." + key);
                factory.removeBeanDefinition("anyline.service." + key);
            }
            if(factory.containsBeanDefinition("anyline.dao." + key)){
                factory.destroySingleton("anyline.dao." + key);
                factory.removeBeanDefinition("anyline.dao." + key);
            }
            if(factory.containsBeanDefinition("anyline.jdbc.template." + key)){
                factory.destroySingleton("anyline.jdbc.template." + key);
                factory.removeBeanDefinition("anyline.jdbc.template." + key);
            }
            if(factory.containsBeanDefinition("anyline.transaction." + key)){
                factory.destroySingleton("anyline.transaction." + key);
                factory.removeBeanDefinition("anyline.transaction." + key);
            }
            if(factory.containsBeanDefinition("anyline.datasource." + key)){
                factory.destroySingleton("anyline.datasource." + key);
                factory.removeBeanDefinition("anyline.datasource." + key);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static JdbcTemplate getJdbcTemplate(){
        DataRuntime runtime = getRuntime();
        if(null != runtime){
            return (JdbcTemplate) runtime.getClient();
        }
        return null;
    }
    public static DataSource getDataSource(){
        DataRuntime runtime = getRuntime();
        if(null != runtime){
            JdbcTemplate jdbc = (JdbcTemplate) runtime.getClient();
            return jdbc.getDataSource();
        }
        return null;
    }
    public static JdbcTemplate getJdbcTemplate(String key){
        DataRuntime runtime = getRuntime(key);
        if(null != runtime){
            return (JdbcTemplate) runtime.getClient();
        }
        return null;
    }
    public static DataSource getDataSource(String key){
        DataRuntime runtime = getRuntime(key);
        if(null != runtime){
            JdbcTemplate jdbc = (JdbcTemplate) runtime.getClient();
            return jdbc.getDataSource();
        }
        return null;
    }
}