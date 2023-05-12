package org.anyline.data.jdbc.util;

import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSourceUtil {

    private static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";

    public static Logger log = LoggerFactory.getLogger(DataSourceUtil.class);


    public static String buildDataSource(String key, String prefix, Environment env) {
        try {
            if(BasicUtil.isNotEmpty(prefix) && !prefix.endsWith(".")){
                prefix += ".";
            }
            String type = BeanUtil.value(prefix, env, "type");
            if(null == type){
                type = BeanUtil.value("spring.datasource.", env, "type");
            }
            if (type == null) {
                type = DATASOURCE_TYPE_DEFAULT;
            }


            String driverClassName = BeanUtil.value(prefix, env, "driver","driver-class","driver-class-name");
            String url = BeanUtil.value(prefix, env, "url","jdbc-url");
            String username = BeanUtil.value(prefix, env,"user","username","user-name");
            String password = BeanUtil.value(prefix, env, "password");

           //DataSource ds =  dataSourceType.newInstance();
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("url", url);
            map.put("jdbcUrl", url);
            map.put("driver",driverClassName);
            map.put("driverClass",driverClassName);
            map.put("driverClassName",driverClassName);
            map.put("user",username);
            map.put("username",username);
            map.put("password",password);
            //BeanUtil.setFieldsValue(ds, map, false);
            return regDatasource(key, map);
            //return ds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建数据源
     * @param params 数据源参数
     * 	  pool 连接池类型 如 com.zaxxer.hikari.HikariDataSource
     * 	  driver 驱动类 如 com.mysql.cj.jdbc.Driver
     * 	  url url
     * 	  user 用户名
     * 	  password 密码
     * @return DataSource
     * @throws Exception 异常 Exception
     */
    @SuppressWarnings("unchecked")
    public static DataSource buildDataSource(Map params) throws Exception{
        try {
            String type = (String)params.get("pool");
            if(BasicUtil.isEmpty(type)){
                type = (String)params.get("type");
            }
            if (type == null) {
                throw new Exception("未设置数据源类型(如:pool=com.zaxxer.hikari.HikariDataSource)");
            }
            Class<? extends DataSource> poolClass = (Class<? extends DataSource>) Class.forName(type);
            Object driver =  BeanUtil.propertyNvl(params,"driver","driver-class","driver-class-name");
            Object url =  BeanUtil.propertyNvl(params,"url","jdbc-url");
            Object user =  BeanUtil.propertyNvl(params,"user","username");
            DataSource ds =  poolClass.newInstance();
            Map<String,Object> map = new HashMap<String,Object>();
            map.putAll(params);
            map.put("url", url);
            map.put("jdbcUrl", url);
            map.put("driver",driver);
            map.put("driverClass",driver);
            map.put("driverClassName",driver);
            map.put("user",user);
            map.put("username",user);
            BeanUtil.setFieldsValue(ds, map, false);
            return ds;
        } catch (Exception e) {
            log.error("[注册数据源失败][数据源:{}][msg:{}]", e.toString());
        }
        return null;
    }

    public static String regDatasource(String key, Map params) throws Exception{
        String ds_id = "anyline.datasource." + key;
        try {
            String type = (String)params.get("pool");
            if(BasicUtil.isEmpty(type)){
                type = (String)params.get("type");
            }
            if (type == null) {
               // throw new Exception("未设置数据源类型(如:pool=com.zaxxer.hikari.HikariDataSource)");
                type = DATASOURCE_TYPE_DEFAULT;
            }
            Class<? extends DataSource> poolClass = (Class<? extends DataSource>) Class.forName(type);

            Object driver =  BeanUtil.propertyNvl(params,"driver","driver-class","driver-class-name");
            if(null == driver){
                return null;
            }
            Class.forName(driver.toString());
            Object url =  BeanUtil.propertyNvl(params,"url","jdbc-url");
            Object user =  BeanUtil.propertyNvl(params,"user","username");
            Map<String,Object> map = new HashMap<String,Object>();
            map.putAll(params);
            map.put("url", url);
            map.put("jdbcUrl", url);
            map.put("driver",driver);
            map.put("driverClass",driver);
            map.put("driverClassName",driver);
            map.put("user",user);
            map.put("username",user);

            DefaultListableBeanFactory factory =(DefaultListableBeanFactory) SpringContextUtil.getApplicationContext().getAutowireCapableBeanFactory();

            //数据源
            BeanDefinitionBuilder ds_builder = BeanDefinitionBuilder.genericBeanDefinition(poolClass);
            List<Field> fields = ClassUtil.getFields(poolClass, false, false);
            for(Field field:fields){
                String name = field.getName();
                Object value = map.get(name);
                value = ConvertAdapter.convert(value, field.getType());
                if(null != value) {
                    ds_builder.addPropertyValue(name, value);
                }
            }

            BeanDefinition ds_definition = ds_builder.getBeanDefinition();
            factory.registerBeanDefinition(ds_id, ds_definition);


        } catch (Exception e) {
            log.error("[注册数据源失败][数据源:{}][msg:{}]", key, e.toString());
            return null;
        }
        return ds_id;
    }
    /**
     * 根据配置文件设置对象属性值
     * @param obj 对象
     * @param prefix 前缀
     * @param env 配置文件环境
     */
    private static void setFieldsValue(Object obj, String prefix, Environment env ){
        List<String> fields = ClassUtil.getFieldsName(obj.getClass());
        for(String field:fields){
            String value = BeanUtil.value(prefix, env, field);
            if(BasicUtil.isNotEmpty(value)) {
                BeanUtil.setFieldValue(obj, field, value);
            }
        }
    }

}
