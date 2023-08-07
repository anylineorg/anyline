package org.anyline.data.mongo.listener;

import org.anyline.data.mongo.runtime.DatabaseHolder;
import org.anyline.data.mongo.runtime.MongoRuntimeHolder;
import org.anyline.listener.EnvironmentListener;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("anyline.listener.mongo.EnvironmentListener")
public class MongoEnvironmentListener implements ApplicationContextAware {
    public static Logger log = LoggerFactory.getLogger(EnvironmentListener.class);

    private static ApplicationContext context;
    private static DefaultListableBeanFactory factory;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        factory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        SpringContextUtil.init(context);
        MongoRuntimeHolder.init(factory);
        int qty = load("spring.datasource");
        qty += load("anyline.datasource");
        if(qty > 0) {
            loadDefault();
        }
     }
    private void loadDefault(){

        //默认数据源 有多个数据源的情况下 再注册anyline.service.default
        //如果单个数据源 只通过@serveri注解 注册一个anyline.service
        //anyline.service.default 用来操作主数据源
        //anyline.service.sso 用来操作sso数据源
        //anyline.service.common 用来操作所有数据源
      /*  JdbcTemplate template = (JdbcTemplate)SpringContextUtil.getBean("jdbcTemplate");
        if(null != template) {
            //注册一个默认运行环境
            JDBCRuntimeHolder.reg("default", template, null);
            if(ConfigTable.IS_OPEN_PRIMARY_TRANSACTION_MANAGER){
                //注册一个主事务管理器
                DataSourceHolder.regTransactionManager("primary", template.getDataSource(), true);
            }
            //注册一个通用运行环境(可切换数据源)
            DataRuntime runtime = new JDBCRuntime("common", template, null);
            JDBCRuntimeHolder.reg("common", runtime);
        }else{
            //加载anyline.datasource.url格式定义
        }*/
    }
    //加载配置文件
    private int load(String head){
        int qty = 0;
        Environment env = context.getEnvironment();
        // 读取配置文件获取更多数据源
        String prefixs = env.getProperty(head + ".list");
        if(null == prefixs){
            prefixs = env.getProperty(head + "-list");
        }
        if(null != prefixs){
            for (String prefix : prefixs.split(",")) {
                // 多个数据源
                try {
                    //返回bean id
                    String ds = DatabaseHolder.reg(prefix, head + "." + prefix, env);
                    if(null != ds) {
                        qty ++;
                        log.info("[创建数据源][prefix:{}][bean:{}]", prefix, ds);
                    }
                }catch (Exception e){
                    log.error("[创建数据源失败][prefix:{}][msg:{}]", prefix, e.toString());
                }
            }
        }
        return qty;
    }


}