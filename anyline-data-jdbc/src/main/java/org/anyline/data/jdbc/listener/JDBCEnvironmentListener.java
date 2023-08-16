/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
public class JDBCEnvironmentListener implements ApplicationContextAware {
    public static Logger log = LoggerFactory.getLogger(JDBCEnvironmentListener.class);

    private static ApplicationContext context;
    private static DefaultListableBeanFactory factory;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        factory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        SpringContextUtil.init(context);
        JDBCRuntimeHolder.init(factory);
        JdbcTemplate template = SpringContextUtil.getBean(JdbcTemplate.class);
        int qty = load("spring.datasource");
        qty += load("anyline.datasource");
        if(null != template) {
            load(template, qty>0);
        }
    }
    private void load(JdbcTemplate template, boolean multiple){
        //默认数据源 有多个数据源的情况下 再注册anyline.service.default
        //如果单个数据源 只通过@serveri注解 注册一个anyline.service
        //anyline.service.default 用来操作主数据源
        //anyline.service.sso 用来操作sso数据源
        //anyline.service.common 用来操作所有数据源
        if(null != template) {
            if(multiple) {
                //多数据源环境中,注册一个默认运行环境(只操作默认数据源不可)
                JDBCRuntimeHolder.reg("default", template, null);
                if (ConfigTable.IS_OPEN_PRIMARY_TRANSACTION_MANAGER) {
                    //注册一个主事务管理器
                    DataSourceHolder.regTransactionManager("primary", template.getDataSource(), true);
                }
            }
            //注册一个通用运行环境(可切换数据源)
            DataRuntime runtime = new JDBCRuntime("common", template, null);
            JDBCRuntimeHolder.reg("common", runtime);
        }else{
            //加载anyline.datasource.url格式定义的数据源
        }
    }
    //加载配置文件
    private int load(String head){
        int qty = 0;
        Environment env = context.getEnvironment();
        // 读取配置文件获取更多数据源 anyline.datasource.list
        String prefixs = env.getProperty(head + ".list");
        if(null == prefixs){
            //anyline.datasource-list
            prefixs = env.getProperty(head + "-list");
        }
        if(null != prefixs){
            for (String prefix : prefixs.split(",")) {
                // 多个数据源
                try {
                    //返回 datasource的bean id
                    // sso, anyline.datasource.sso, env
                    String ds = DataSourceHolder.reg(prefix, head + "." + prefix, env);
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
