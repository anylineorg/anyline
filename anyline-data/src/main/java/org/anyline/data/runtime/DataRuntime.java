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

package org.anyline.data.runtime;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.entity.authorize.Role;

import java.util.LinkedHashMap;
import java.util.List;

public interface DataRuntime {
    String ANYLINE_DATASOURCE_BEAN_PREFIX = "anyline.datasource.";
    String ANYLINE_DATABASE_BEAN_PREFIX = "anyline.database.";
    String ANYLINE_JDBC_TEMPLATE_BEAN_PREFIX = "anyline.jdbc.template.";
    String ANYLINE_TRANSACTION_BEAN_PREFIX = "anyline.transaction.";
    String ANYLINE_DAO_BEAN_PREFIX = "anyline.dao.";
    String ANYLINE_SERVICE_BEAN_PREFIX = "anyline.service.";

    DriverAdapter getAdapter() ;
    void setAdapter(DriverAdapter adapter);

    /**
     * 是否管理员帐号 影响元数据查询
     * @return boolean
     */
    LinkedHashMap<String, Role> roles();
    void roles(LinkedHashMap<String, Role> roles);
    void roles(List<Role> roles);
    void roles(Role ... roles);
    /**
     * 返回复制源的id(有些数据源是通过自动复制，在注销时会把复制出来的数据源一块注销)
     * @return String
     */
    String origin();
    void origin(String origin);

    /**
     * 返回数据库特征用来定位adapter
     * @param connection 是否连接数据源
     * @return String
     */
    String getFeature(boolean connection) ;
    default String getFeature() {
        return getFeature(false);
    }

    void setFeature(String feature) ;

    /**
     * 返回最后一次检测的数据库特征
     * @return String
     */
    String getLastFeature() ;

    void setLastFeature(String feature) ;

    /**
     * 返回数据库版本
     * @return String
     */
    String getVersion();

    void setVersion(String version) ;
    void setDriver(String driver);
    String getDriver();
    void setUrl(String url);
    String getUrl();
    String getKey() ;
    void setKey(String key) ;
    Object getProcessor() ;
    void setProcessor(Object processor);

    String datasource();

    /**
     * 配置文件中设置adapter标识(url参数或adapter属性)
     * @param adapter adapter标识
     */
    void setAdapterKey(String adapter);
    String getAdapterKey();
    void setSchema(String schema);
    String getSchema();
    void setCatalog(String catalog);
    String getCatalog();

    void setHolder(RuntimeHolder holder);
    RuntimeHolder getHolder();
    boolean destroy() throws Exception;

}
