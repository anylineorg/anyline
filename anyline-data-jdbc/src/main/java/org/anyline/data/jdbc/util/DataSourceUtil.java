package org.anyline.data.jdbc.util;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ClassUtil;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSourceUtil {

    private static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";

    @SuppressWarnings("unchecked")
    public static DataSource buildDataSource(String prefix, Environment env) {
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

            Class<? extends DataSource> dataSourceType = (Class<? extends DataSource>) Class.forName(type);
            String driverClassName = BeanUtil.value(prefix, env, "driver","driver-class","driver-class-name");
            String url = BeanUtil.value(prefix, env, "url","jdbc-url");
            String username = BeanUtil.value(prefix, env,"user","username","user-name");
            String password = BeanUtil.value(prefix, env, "password");

            DataSource ds =  dataSourceType.newInstance();
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("url", url);
            map.put("jdbcUrl", url);
            map.put("driver",driverClassName);
            map.put("driverClass",driverClassName);
            map.put("driverClassName",driverClassName);
            map.put("user",username);
            map.put("username",username);
            map.put("password",password);
            BeanUtil.setFieldsValue(ds, map, false);
            return ds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
