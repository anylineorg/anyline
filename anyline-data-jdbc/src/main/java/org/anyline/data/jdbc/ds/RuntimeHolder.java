package org.anyline.data.jdbc.ds;

import org.anyline.dao.init.springjdbc.DefaultDao;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.util.DataSourceUtil;
import org.anyline.data.listener.DMListener;
import org.anyline.service.init.DefaultService;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Map;

@Component
public class RuntimeHolder  implements ApplicationContextAware {

    private static Logger log = LoggerFactory.getLogger(RuntimeHolder.class);
    private static Map<String, JDBCRuntime> runtimes = new Hashtable();
    private static ApplicationContext context;
    private static DefaultListableBeanFactory factory;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        factory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        SpringContextUtil.applicationContext = context;
        load();
    }
    //加载配置文件
    private void load(){
        Environment env = context.getEnvironment();
        // 读取配置文件获取更多数据源
        String prefixs = env.getProperty("spring.datasource.list");
        boolean multiple = false;
        if(null != prefixs){
            for (String prefix : prefixs.split(",")) {
                // 多个数据源
                DataSource ds = DataSourceUtil.buildDataSource("spring.datasource."+prefix,env);
                reg(prefix, ds);
                multiple = true;
                log.info("[创建数据源][prefix:{}]",prefix);
            }
        }
        //默认数据源 有多个数据源的情况下 再注册anyline.service.default
        JdbcTemplate template = SpringContextUtil.getBean(JdbcTemplate.class);
        if(null != template) {
            if(multiple) {
                reg("default", template, null);
            }
            JDBCRuntime runtime = new JDBCRuntime("common", template, null);
            runtimes.put("common", runtime);
        }


    }

    public static void reg(String key, DataSource ds){
        DataSourceHolder.reg(key);
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
