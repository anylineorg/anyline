/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.jdbc.util;

import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DataSourceUtil {

    public static final Log log = LogProxy.get(DataSourceUtil.class);

    public static final String POOL_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";

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
    public static DataSource build(Map params) {
        try {
            String type = (String)params.get("pool");
            if(BasicUtil.isEmpty(type)) {
                type = (String)params.get("type");
            }
            if (type == null) {
                throw new Exception("未设置数据源类型(如:pool=com.zaxxer.hikari.HikariDataSource)");
            }
            Class<? extends DataSource> poolClass = (Class<? extends DataSource>) Class.forName(type);
            Object driver =  BeanUtil.propertyNvl(params, "driver","driver-class","driver-class-name");
            Object url =  BeanUtil.propertyNvl(params, "url","jdbc-url");
            Object user =  BeanUtil.propertyNvl(params, "user","username");
            DataSource datasource =  poolClass.newInstance();
            Map<String, Object> map = new HashMap<String, Object>();
            map.putAll(params);
            map.put("url", url);
            map.put("jdbcUrl", url);
            map.put("driver", driver);
            map.put("driverClass", driver);
            map.put("driverClassName", driver);
            map.put("user", user);
            map.put("username", user);
            BeanUtil.setFieldsValue(datasource, map, false);
            return datasource;
        } catch (Exception e) {
            log.error("[注册数据源失败]", e);
        }
        return null;
    }

    /**
     * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
      * @param pool 连接池类型 如 com.zaxxer.hikari.HikariDataSource
     * @param driver 驱动类 如 com.mysql.cj.jdbc.Driver
     * @param url url
     * @param user 用户名
     * @param password 密码
     * @return DataSource
     * @throws Exception 异常 Exception
     */
    public static DataSource build( String pool, String driver, String url, String user, String password) {
        Map<String, String> param = new HashMap<String, String>();
        param.put("type", pool);
        param.put("driverClassName", driver);
        param.put("url", url);
        param.put("user", user);
        param.put("password", password);
        return build(param);
    }

    /**
     * 注册数据源(生产环境不要调用这个方法，这里只设置几个必需参数用来测试)
     * @param url url
     * @param type 数据库类型
     * @param user 用户名
     * @param password 密码
     * @return DataSource
     * @throws Exception 异常 Exception
     */
    public static DataSource build(DatabaseType type, String url, String user, String password) {
        return build(POOL_TYPE_DEFAULT, type.driver(), url, user, password);
    }

}
