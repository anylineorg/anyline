package org.anyline.data.jdbc.runtime;

import org.anyline.dao.init.springjdbc.FixDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.proxy.RuntimeHolderProxy;
import org.anyline.service.init.FixService;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;


@Component("anyline.data.runtime.holder.jdbc")
public class JDBCRuntimeHolder extends RuntimeHolder {

    public JDBCRuntimeHolder(){
        RuntimeHolderProxy.reg(DataSource.class,this);
    }
    @Override
    public DataRuntime runtime(String key, Object datasource, String database, DriverAdapter adapter) throws Exception{
        JDBCRuntime runtime = new JDBCRuntime();
        if(datasource instanceof DataSource){
            runtime.setKey(key);
            runtime.setAdapter(adapter);
            DataSource ds = (DataSource) datasource;
            JdbcTemplate template = new JdbcTemplate(ds);
            runtime.setClient(template);
            log.warn("[注册数据源][key:{}][type:{}]", key, datasource.getClass().getSimpleName());
        }else{
            throw new Exception("请提供javax.sql.DataSource");
        }
        return runtime;
    }

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
        String datasource_key = "anyline.datasource." + key;
        factory.registerSingleton(datasource_key, ds);

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
        DataRuntime runtime = new JDBCRuntime(datasource, template, adapter);
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
            destroyBean("anyline.service." + key);
            destroyBean("anyline.dao." + key);
            destroyBean("anyline.template." + key);
            destroyBean("anyline.transaction." + key);

            close("anyline.datasource." + key);
            destroyBean("anyline.datasource." + key);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void close(String ds){
        Object datasource = null;
        if(factory.containsSingleton(ds)){
            datasource = factory.getSingleton(ds);
            try {
                closeConnection(datasource);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void closeConnection(Object ds) throws Exception{
        Method method = ClassUtil.getMethod(ds.getClass(), "close");
        if(null != method){
            method.invoke(ds);
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