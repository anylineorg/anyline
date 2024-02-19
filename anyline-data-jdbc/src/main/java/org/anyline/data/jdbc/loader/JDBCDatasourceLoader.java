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


package org.anyline.data.jdbc.loader;

import org.anyline.data.datasource.DatasourceHolder;
import org.anyline.data.jdbc.datasource.JDBCDatasourceHolder;
import org.anyline.data.jdbc.runtime.JDBCRuntimeHolder;
import org.anyline.data.listener.DatasourceLoader;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.util.BasicUtil;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("anyline.data.datasource.loader.jdbc")
public class JDBCDatasourceLoader implements DatasourceLoader {
    public static Logger log = LoggerFactory.getLogger(JDBCDatasourceLoader.class);

    private static DefaultListableBeanFactory factory;
    public List<String> load(ApplicationContext context){
        List<String> list = new ArrayList<>();
        Environment env = context.getEnvironment();
        factory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        SpringContextUtil.init(context);
        JDBCRuntimeHolder.init(factory);
        JDBCDatasourceHolder.loadCache();
        boolean loadDefault = true; //是否需要加载default
        if(!DatasourceHolder.contains("default")){
            //如果还没有注册默认数据源
            // 项目中可以提前注册好默认数据源 如通过@Configuration注解先执行注册 也可以在spring启动完成后覆盖默认数据源
            JdbcTemplate jdbc = null;
            try{
                jdbc = SpringContextUtil.getBean(JdbcTemplate.class);
            }catch (Exception e){}
            DataRuntime runtime = null;
            if(null != jdbc){
                try {
                    runtime = JDBCRuntimeHolder.reg("default", jdbc, null);
                    loadDefault = false;
                }catch (Exception e){
                    runtime = null;
                }
            }else{
                DataSource datasource = null;
                try{
                    datasource = SpringContextUtil.getBean(DataSource.class);
                }catch (Exception e){
                    runtime = null;
                }
                if(null != datasource){
                    try {
                        runtime = JDBCDatasourceHolder.reg("default", datasource, false);
                        loadDefault = false;
                    }catch (Exception e){
                        runtime = null;
                        e.printStackTrace();
                    }
                }
            }
            //有不支持通过connection返回获取连接信息的驱动，所以从配置文件中获取
            if(null != runtime) {
                String driver = DatasourceHolder.value(env, "spring.datasource.,anyline.datasource.", "driver,driver-class,driver-class-name", String.class, null);
                String url = DatasourceHolder.value(env, "spring.datasource.,anyline.datasource.", "url,jdbc-url", String.class, null);
                runtime.setDriver(driver);
                runtime.setUrl(url);
                if (BasicUtil.isNotEmpty(url)) {
                    runtime.setAdapterKey(RuntimeHolder.parseAdapterKey(url));
                }else{
                    String adapterKey = DatasourceHolder.value(env, "spring.datasource.,anyline.datasource.", "adapter", String.class, null);
                    if(BasicUtil.isNotEmpty(adapterKey)){
                        runtime.setAdapterKey(adapterKey);
                    }
                }
            }
        }else{
            loadDefault = false;
        }
        list.addAll(load(env, "spring.datasource", loadDefault));
        list.addAll(load(env, "anyline.datasource", loadDefault));
        //TODO 项目指定一个前缀
        return list;
    }

    //加载配置文件

    /**
     *
     * @param env 配置文件
     * @param head 前缀
     * @param loadDefault 是否加载默认数据源
     * @return keys
     */
    private List<String> load(Environment env, String head, boolean loadDefault){
        //加载成功的前缀 crm, sso
        List<String> list = new ArrayList<>();
        if(loadDefault) {
            String def = JDBCDatasourceHolder.reg("default", head, env);
            if (null != def) {
                list.add(def);
            }
        }
        //默认数据源
        //多数据源
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
                    String ds = JDBCDatasourceHolder.reg(prefix, head + "." + prefix, env);
                    if(null != ds) {
                        list.add(ds);
                    }
                }catch (Exception e){
                    log.error("[注入数据源失败][type:JDBC][key:{}][msg:{}]", prefix, e.toString());
                }
            }
        }
        return list;
    }
}
