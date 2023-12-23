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


package org.anyline.data.jdbc.runtime;

import org.anyline.dao.init.springjdbc.DefaultDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.proxy.RuntimeHolderProxy;
import org.anyline.service.init.DefaultService;
import org.anyline.util.ClassUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@Component("anyline.data.runtime.holder.jdbc")
public class JDBCRuntimeHolder extends RuntimeHolder {
    /**
     * 临时数据源
     */
    private static Map<String, DataSource> temporary = new HashMap<>();

    public JDBCRuntimeHolder(){
        RuntimeHolderProxy.reg(DataSource.class,this);
        RuntimeHolderProxy.reg(JdbcTemplate.class,this);
    }

    /**
     * 注册数据源 子类覆盖 生成简单的DataRuntime不注册到spring
     * @param datasource 数据源,如DruidDataSource,MongoClient
     * @param database 数据库,jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter() ,如果不提供则根据ds检测
     * @return DataRuntime
     * @throws Exception 异常 Exception
     */
    public static DataRuntime temporary(Object datasource, String database, DriverAdapter adapter) throws Exception{
        return exeTemporary( datasource, database, adapter);
    }

    @Override
    public DataRuntime callTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
        return exeTemporary( datasource, database, adapter);
    }

    private static DataRuntime exeTemporary(Object datasource, String database, DriverAdapter adapter) throws Exception{
        JDBCRuntime runtime = new JDBCRuntime();
        if(datasource instanceof DataSource){
            String key = "temporary_jdbc";
            //关闭上一个
            close(key);
            temporary.remove(key);
            runtimes.remove(key);
            //DriverAdapterHolder.remove(key);
            //创建新数据源
            runtime.setKey(key);
            runtime.setAdapter(adapter);
            DataSource ds = (DataSource) datasource;
            JdbcTemplate template = new JdbcTemplate(ds);
            runtime.setProcessor(template);
            temporary.put(key, ds);
            log.warn("[创建临时数据源][key:{}][type:{}]", key, datasource.getClass().getSimpleName());
            runtimes.put(key, runtime);
        }else{
            throw new Exception("请提供javax.sql.DataSource兼容类型");
        }
        //runtime.setHolder(this);
        return runtime;
    }
    /**
     * 注册运行环境
     * @param key 数据源前缀
     * @param ds 数据源bean id
     */
    public static DataRuntime reg(String key, String ds){
        //ClientHolder.reg(key);
        String template_key = DataRuntime.ANYLINE_JDBC_TEMPLATE_BEAN_PREFIX +  key;

        BeanDefinitionBuilder jdbc_builder = BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class);
        jdbc_builder.addPropertyReference("dataSource", ds);
        BeanDefinition definition = jdbc_builder.getBeanDefinition();
        factory.registerBeanDefinition(template_key, definition);

        JdbcTemplate template = factory.getBean(template_key, JdbcTemplate.class);
        return reg(key, template, null);
    }


    public static DataRuntime reg(String key, DataSource ds){
        String datasource_key = DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key;
        factory.registerSingleton(datasource_key, ds);

        String template_key = DataRuntime.ANYLINE_JDBC_TEMPLATE_BEAN_PREFIX +  key;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JdbcTemplate.class);
        builder.addPropertyValue("dataSource", ds);
        BeanDefinition definition = builder.getBeanDefinition();
        factory.registerBeanDefinition(template_key, definition);

        JdbcTemplate template = factory.getBean(template_key, JdbcTemplate.class);
        return reg(key, template, null);
    }

    /**
     * 注册运行环境
     * @param datasource 数据源前缀
     * @param template template
     * @param adapter adapter 可以为空 第一次执行时补齐
     */
    public static JDBCRuntime reg(String datasource, JdbcTemplate template, JDBCAdapter adapter){
        log.info("[create jdbc runtime][key:{}]", datasource);
        JDBCRuntime runtime = new JDBCRuntime(datasource, template, adapter);
        if(runtimes.containsKey(datasource)){
            destroy(datasource);
        }
        runtimes.put(datasource, runtime);

        String dao_key = DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  datasource;
        String service_key = DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  datasource;
        log.info("[instance service][data source:{}][instance id:{}]", datasource, service_key);

        BeanDefinitionBuilder daoBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultDao.class);
        //daoBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        daoBuilder.addPropertyValue("runtime", runtime);
        //daoBuilder.addPropertyValue("datasource", datasource);
        //daoBuilder.addPropertyValue("listener", SpringContextUtil.getBean(DMListener.class));
        //daoBuilder.addAutowiredProperty("listener");
        daoBuilder.setLazyInit(true);
        BeanDefinition daoDefinition = daoBuilder.getBeanDefinition();
        factory.registerBeanDefinition(dao_key, daoDefinition);


        BeanDefinitionBuilder serviceBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultService.class);
        //serviceBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        //serviceBuilder.addPropertyValue("datasource", datasource);
        serviceBuilder.addPropertyReference("dao", dao_key);
        //serviceBuilder.addAutowiredProperty("cacheProvider");
        serviceBuilder.setLazyInit(true);
        BeanDefinition serviceDefinition = serviceBuilder.getBeanDefinition();
        factory.registerBeanDefinition(service_key, serviceDefinition);
        return runtime;
    }
    public static void destroy(String key){
        exeDestroy(key);
    }

    private static void exeDestroy(String key){
        try {
            runtimes.remove(key);
            destroyBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX +  key);
            destroyBean(DataRuntime.ANYLINE_DAO_BEAN_PREFIX +  key);
            destroyBean(DataRuntime.ANYLINE_TRANSACTION_BEAN_PREFIX +  key);
            destroyBean(DataRuntime.ANYLINE_JDBC_TEMPLATE_BEAN_PREFIX +  key);

            close(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key);
            destroyBean(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + key);
            log.warn("[注销数据源及相关资源][key:{}]", key);
            //从当前数据源复制的 子源一块注销
            Map<String, DataRuntime> runtimes = runtimes(key);
            for(String item:runtimes.keySet()){
                destroy(item);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void callDestroy(String key){
        exeDestroy(key);
    }
    public static void close(String key){
        Object datasource = null;
        if(factory.containsSingleton(key)){
            datasource = factory.getSingleton(key);
            try {
                closeConnection(datasource);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //临时数据源
        if(temporary.containsKey(key)) {
            try {
                closeConnection(temporary.get(key));
            } catch (Exception e) {
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
        DataRuntime runtime = runtime();
        if(null != runtime){
            return (JdbcTemplate) runtime.getProcessor();
        }
        return null;
    }
    public static DataSource getDataSource(){
        DataRuntime runtime = runtime();
        if(null != runtime){
            JdbcTemplate jdbc = (JdbcTemplate) runtime.getProcessor();
            if(null == jdbc){
                return null;
            }
            return jdbc.getDataSource();
        }
        return null;
    }
    public static JdbcTemplate getJdbcTemplate(String key){
        DataRuntime runtime = runtime(key);
        if(null != runtime){
            return (JdbcTemplate) runtime.getProcessor();
        }
        return null;
    }
    public static DataSource getDataSource(String key){
        DataRuntime runtime = runtime(key);
        if(null != runtime){
            JdbcTemplate jdbc = (JdbcTemplate) runtime.getProcessor();
            if(null == jdbc){
                return null;
            }
            return jdbc.getDataSource();
        }
        return null;
    }
}