/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.util;

import org.anyline.data.param.ConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Table;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.util.Collection;

public class DataSourceUtil {
    private static final Log log = LogProxy.get(DataSourceUtil.class);

    public static String[] parseRuntime(Metadata meta) {
        if(null != meta) {
            return parseRuntime(meta.getName());
        }
        return new String[2];
    }

    /**
     * &lt;ds&gt;.table
     * @param src src
     * @return string[]
     */
    public static String[] parseRuntime(String src) {
        String[] result = new String[2];
        result[1] = src;
        String runtime = null;
        if(null != src && src.startsWith("<")) {
            int fr = src.indexOf("<");
            int to = src.indexOf(">");
            if(fr != -1) {
                runtime = src.substring(fr+1, to);
                src = src.substring(to+1);
                result[0] = runtime;
                result[1] = src;
            }
        }
        return result;
    }

    /**
     * 解析数据源, 并返回修改后的SQL
     * &lt;mysql_ds&gt;crm_user
     * @param src  src
     * @return String
     */
    public static Table parseDest(String src, ConfigStore configs) {
        if(null == src) {
            return null;
        }
        Table result = new Table();
        String up = src.toUpperCase().trim();
        //<sso>pw_user
        if(src.startsWith("<")) {
            int fr = src.indexOf("<");
            int to = src.indexOf(">");
            if(fr != -1) {
                String datasource = src.substring(fr+1, to);
                src = src.substring(to+1);
                result.setDataSource(datasource);
            }
        }
        //pw_user<id, code>
        if(src.endsWith(">")) {
            int fr = src.lastIndexOf("<");
            if(fr != -1) {
                String[] keys = src.substring(fr + 1, src.length() - 1).split(",");
                src = src.substring(0, fr);
                result.setPrimaryKey(keys);
            }
        }
        if(src.contains(" ") || up.startsWith("SELECT")) {
            result.setText(src);
        }else if(src.contains(":")) {
            result.setId(src);
        }
        //show tables
        //sys_user as m
        //sys_user  m 不解析
        if(!up.startsWith("SELECT") && !BasicUtil.checkEl(up)) {
            result.setName(src);
        }
        return result;
    }
    public static Table parseDest(String dest, Object obj, ConfigStore configs) {
        Table table = null;
        //有表的根据表解析
        if(BasicUtil.isNotEmpty(dest) || null == obj) {
            return parseDest(dest, configs);
        }
        //没有表的根据 对象解析

        if(obj instanceof DataRow) {
            DataRow row = (DataRow)obj;
            table = parseDest(row.getDest(), configs);
        }else if(obj instanceof DataSet) {
            DataSet<DataRow> set = (DataSet)obj;
            if(!set.isEmpty()) {
                table = parseDest(set.getRow(0).getDest(), configs);
            }
        } else if (obj instanceof Collection) {
            Collection list = (Collection)obj;
            if(!list.isEmpty()) {
                Object first = list.iterator().next();
                if(first instanceof DataRow) {
                    table = parseDest(((DataRow)first).getDest(), configs);
                }else {
                    String tableName = EntityAdapterProxy.table(first.getClass(), true);
                    table = parseDest(tableName, configs);
                }
            }
        } else{
            table = EntityAdapterProxy.table(obj.getClass());
        }
        return table;
    }

    /**
     * 从数据源中解析jdbc-url(不建立连接)<br/>
     * 支持 HikariDataSource#getJdbcUrl DruidDataSource#getUrl 以及 jdbcUrl/url 等属性
     * @param datasource 数据源(DataSource)
     * @return String 解析不到返回null
     */
    public static String parseUrl(Object datasource) {
        // SQLServerDataSource#getURL OracleDataSource#getURL 是大写后缀
        String url = parseAttribute(datasource, new String[]{"getJdbcUrl", "getUrl", "getURL"}, new String[]{"jdbcUrl", "url"});
        if(null != url) {
            // 部分数据源(如PGSimpleDataSource)返回的是补全了默认参数的url,这里只保留url主体,避免参数干扰特征识别
            int idx = url.indexOf("?");
            if(idx > 0) {
                url = url.substring(0, idx);
            }
        }
        return url;
    }

    /**
     * 从数据源中解析驱动类(不建立连接)<br/>
     * 支持 DruidDataSource#getDriverClassName 以及 driverClassName/driverClass 等属性
     * @param datasource 数据源(DataSource)
     * @return String 解析不到返回null
     */
    public static String parseDriver(Object datasource) {
        return parseAttribute(datasource, new String[]{"getDriverClassName", "getDriverClass", "getDriver"}, new String[]{"driverClassName", "driverClass", "driver"});
    }

    /**
     * 从数据源中读取属性(不建立连接)<br/>
     * 先尝试public方法,再尝试属性(含父类属性)
     * @param datasource 数据源
     * @param methods 候选方法名
     * @param fields 候选属性名
     * @return String
     */
    private static String parseAttribute(Object datasource, String[] methods, String[] fields) {
        if(null == datasource) {
            return null;
        }
        Class<?> clazz = datasource.getClass();
        for(String method:methods) {
            try {
                Method m = clazz.getMethod(method);
                String result = parseAttributeValue(m.invoke(datasource));
                if(BasicUtil.isNotEmpty(result)) {
                    return result;
                }
            }catch (Throwable e) {
                // 有些数据源的属性只允许写入,不允许读取,读取时会主动抛出异常(如未初始化的数据源),这里忽略
                if(log.isDebugEnabled()) {
                    log.debug("[解析数据源属性][忽略异常][数据源:{}][方法:{}][异常:{}]", datasource.getClass(), method, e.toString());
                }
            }
        }
        while(null != clazz && clazz != Object.class) {
            for(String field:fields) {
                try {
                    Field f = clazz.getDeclaredField(field);
                    f.setAccessible(true);
                    String result = parseAttributeValue(f.get(datasource));
                    if(BasicUtil.isNotEmpty(result)) {
                        return result;
                    }
                }catch (Throwable e) {
                    // 同上,属性不可读或不允许反射访问时忽略
                    if(log.isDebugEnabled()) {
                        log.debug("[解析数据源属性][忽略异常][数据源:{}][属性:{}][异常:{}]", datasource.getClass(), field, e.toString());
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 数据源属性值转换<br/>
     * 只接受字符串,Driver实例取类名(如SimpleDriverDataSource#getDriver)<br/>
     * 其他类型(如URL对象)不作为特征,避免误判
     * @param value 属性值
     * @return String
     */
    private static String parseAttributeValue(Object value) {
        if(null == value) {
            return null;
        }
        if(value instanceof String) {
            return (String) value;
        }
        if(value instanceof Driver) {
            return value.getClass().getName();
        }
        return null;
    }

    public static String parseAdapterKey(String url) {
        return parseParamValue(url, "adapter");
    }
    public static String parseCatalog(String url) {
        return parseParamValue(url, "catalog");
    }
    public static String parseSchema(String url) {
        return parseParamValue(url, "schema");
    }
    public static String parseRole(String url) {
        String role = parseParamValue(url, "user_role");
        if(BasicUtil.isEmpty(role)){
            role = parseParamValue(url, "userRole");
        }
        if(BasicUtil.isEmpty(role)){
            role = parseParamValue(url, "user-role");
        }
        if(BasicUtil.isEmpty(role)){
            role = parseParamValue(url, "role");
        }
        return role;
    }
    public static String parseParamValue(String url, String key) {
        String value = null;
        if(null != url && url.contains(key)) {
            value = RegularUtil.cut(url, key+"=", "&");
            if(BasicUtil.isEmpty(value)) {
                value = RegularUtil.cut(url, key+"=", RegularUtil.TAG_END);
            }
        }
        return value;
    }
}