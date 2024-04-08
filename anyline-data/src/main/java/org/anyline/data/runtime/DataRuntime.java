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

package org.anyline.data.runtime;

import org.anyline.data.adapter.DriverAdapter;

public interface DataRuntime {
    String ANYLINE_DATASOURCE_BEAN_PREFIX = "anyline.datasource.";
    String ANYLINE_DATABASE_BEAN_PREFIX = "anyline.database.";
    String ANYLINE_JDBC_TEMPLATE_BEAN_PREFIX = "anyline.jdbc.template.";
    String ANYLINE_TRANSACTION_BEAN_PREFIX = "anyline.transaction.";
    String ANYLINE_DAO_BEAN_PREFIX = "anyline.dao.";
    String ANYLINE_SERVICE_BEAN_PREFIX = "anyline.service.";

    DriverAdapter getAdapter() ;
    void setAdapter(DriverAdapter adapter);

    String origin();
    void origin(String origin);
    String getFeature(boolean connection) ;
    default String getFeature(){
        return getFeature(false);
    }

    void setFeature(String feature) ;

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
