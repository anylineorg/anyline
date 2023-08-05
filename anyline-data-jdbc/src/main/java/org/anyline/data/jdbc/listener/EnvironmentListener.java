package org.anyline.data.jdbc.listener;

import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.jdbc.runtime.JDBCRuntime;
import org.anyline.data.jdbc.runtime.JDBCRuntimeHolder;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.util.ConfigTable;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("anyline.listener.jdbc.EnvironmentListener")
public class EnvironmentListener implements ApplicationContextAware {
    public static Logger log = LoggerFactory.getLogger(EnvironmentListener.class);

    private static ApplicationContext context;
    private static DefaultListableBeanFactory factory;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        factory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        SpringContextUtil.init(context);
        JDBCRuntimeHolder.init(factory);
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
                try {
                    //返回bean id
                    String ds = DataSourceHolder.reg(prefix, "spring.datasource." + prefix, env);
                    if(null != ds) {
                      /*  //注册事务管理器
                        DataSourceHolder.regTransactionManager(prefix, ds);
                        //记录数据源列表
                        DataSourceHolder.reg(prefix);
                        //注册运行环境
                        RuntimeHolder.reg(prefix, ds);
                        //以上几行在reg内部已经执行了
                        */
                        multiple = true;
                        log.info("[创建数据源][prefix:{}][bean:{}]", prefix, ds);
                    }
                }catch (Exception e){
                    log.error("[创建数据源失败][prefix:{}][msg:{}]", prefix, e.toString());
                }
            }
        }
        //默认数据源 有多个数据源的情况下 再注册anyline.service.default
        //如果单个数据源 只通过@serveri注解 注册一个anyline.service
        //anyline.service.default 用来操作主数据源
        //anyline.service.sso 用来操作sso数据源
        //anyline.service.common 用来操作所有数据源
        JdbcTemplate template = SpringContextUtil.getBean(JdbcTemplate.class);
        if(null != template) {
            if(multiple) {
                //注册一个默认运行环境
                JDBCRuntimeHolder.reg("default", template, null);
                if(ConfigTable.IS_OPEN_PRIMARY_TRANSACTION_MANAGER){
                    //注册一个主事务管理器
                    DataSourceHolder.regTransactionManager("primary", template.getDataSource(), true);
                }
            }
            //注册一个通用运行环境(可切换数据源)
            DataRuntime runtime = new JDBCRuntime("common", template, null);
            JDBCRuntimeHolder.reg("common", runtime);
        }
    }


}
