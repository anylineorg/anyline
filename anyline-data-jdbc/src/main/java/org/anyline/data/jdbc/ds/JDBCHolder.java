package org.anyline.data.jdbc.ds;

import org.anyline.data.jdbc.util.DataSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Map;

@Component
public class JDBCHolder implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(JDBCHolder.class);
    private static Map<String, JdbcTemplate> jdbcs = new Hashtable<>();
    private static ApplicationContext context;
    private static boolean init = false;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        JDBCHolder.context = context;
        init();
    }
    public JDBCHolder(){
        init();
    }
    private static void init(){
        if(init){
            return;
        }
        if(DynamicDataSourceRegister.ACTIVE){
            return;
        }
        if(null == context){
            return;
        }
        //加载数据源
        Environment env = context.getEnvironment();

        String prefixs = env.getProperty("spring.datasource.list");
        if(null != prefixs){
            for (String prefix : prefixs.split(",")) {
                // 多个数据源
                try {
                    DataSource ds = DataSourceUtil.buildDataSource("spring.datasource." + prefix, env);
                    DataSourceHolder.reg(prefix, ds);
                    reg(prefix, ds);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        init = true;
    }
    public static void reg(String key, DataSource ds){
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbcs.put(key, jdbc);
        String bean_name = "jdbc." + key;
        if (null != context && !context.containsBean(bean_name)) {
            ((ConfigurableApplicationContext) context).getBeanFactory().registerSingleton(bean_name, ds);
        }
        log.info("[创建JDBC][key:{}]", bean_name);
    }
    public static JdbcTemplate getJdbc(){
        if(DynamicDataSourceRegister.ACTIVE){
            return getJdbc("default");
        }
        return getJdbc(DataSourceHolder.getDataSource());
    }
    public static JdbcTemplate getJdbc(String key){
        if(!init){
            init();
        }
        if(null == key || "dataSources".equals(key)){
            return jdbcs.get("default");
        }
        JdbcTemplate jdbc = jdbcs.get(key);
        return jdbc;
    }
    @Autowired(required=false)
    public void setJdbc(JdbcTemplate jdbc){
        jdbcs.put("default", jdbc);
        log.info("[创建JDBC][key:default]");
    }
}
