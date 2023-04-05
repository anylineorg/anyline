package org.anyline.data.jdbc.ds;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.util.DataSourceUtil;
import org.anyline.data.jdbc.util.SQLAdapterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    //默认数据源
    @Autowired(required = false)
    @Qualifier("jdbcTemplate")
    public void setTemplate(JdbcTemplate template){
        reg("default", template, null);
    }
    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        RuntimeHolder.context = ac;
        factory = (DefaultListableBeanFactory)ac.getAutowireCapableBeanFactory();
        load();
    }
    //加载配置文件
    private void load(){
        Environment env = context.getEnvironment();
        // 读取配置文件获取更多数据源
        String prefixs = env.getProperty("spring.datasource.list");
        if(null != prefixs){
            for (String prefix : prefixs.split(",")) {
                // 多个数据源
                DataSource ds = DataSourceUtil.buildDataSource("spring.datasource."+prefix,env);
                reg(prefix, ds);
                log.info("[创建数据源][prefix:{}]",prefix);
            }
        }
    }
    public static void reg(String key, DataSource ds){
        DataSourceHolder.reg(key);
        JdbcTemplate template = new JdbcTemplate(ds);
        reg(key, template, null);
    }



    public static void reg(String datasource, JdbcTemplate template, JDBCAdapter adapter){
        log.warn("[create jdbc runtime][key:{}]", datasource);
        JDBCRuntime runtime = new JDBCRuntime(datasource, template, adapter);
        runtimes.put(datasource, runtime);
    }
    public static JDBCRuntime getRuntime(){
        return getRuntime(DataSourceHolder.getDataSource());
    }
    public static JDBCRuntime getRuntime(String datasource){
        if(null == datasource){
            return getRuntime("default");
        }
        JDBCRuntime runtime = runtimes.get(datasource);
        if(null != runtime){
            if(null == runtime.getAdapter()){
                runtime.setAdapter(SQLAdapterUtil.getAdapter(runtime.getTemplate()));
            }
        }else{
            throw new RuntimeException("未注册数据源:"+datasource);
        }
        return runtime;
    }
}
