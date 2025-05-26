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

package org.anyline.data.jdbc.datasource;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.datasource.init.AbstractDataSourceLoader;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.transaction.init.DefaultTransactionManage;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@AnylineComponent("anyline.environment.data.datasource.loader.jdbc")
public class JDBCDataSourceLoader extends AbstractDataSourceLoader implements DataSourceLoader {

    private final JDBCDataSourceHolder holder = JDBCDataSourceHolder.instance();

    @Override
    public DataSourceHolder holder() {
        return holder;
    }

    @Override
    public List<String> load() {
        List<String> list = new ArrayList<>();
        boolean loadDefault = true; //是否需要加载default
        if(!ConfigTable.environment().containsBean(DataRuntime.ANYLINE_DATASOURCE_BEAN_PREFIX + ".default")) {
            //如果还没有注册默认数据源
            // 项目中可以提前注册好默认数据源 如通过@Configuration注解先执行注册 也可以在spring启动完成后覆盖默认数据源
            DataSource datasource = null;
            DataRuntime runtime = null;

            try{
                datasource = ConfigTable.environment().getBean(DataSource.class);
            }catch (Exception e) {
                runtime = null;
            }
            if(null != datasource) {
                try {
                    runtime =  holder().create("default", datasource, false);
                    loadDefault = false;
                }catch (Exception e) {
                    runtime = null;
                    log.error("加载JDBC数据源 异常:", e);
                }
            }

            //有不支持通过connection返回获取连接信息的驱动，所以从配置文件中获取
            if(null != runtime) {
                String driver = ConfigTable.environment().string("spring.datasource.,anyline.datasource.", "driver,driver-class,driver-class-name");
                String url = ConfigTable.environment().string( "spring.datasource.,anyline.datasource.", "url,jdbc-url");
                runtime.setDriver(driver);
                runtime.setUrl(url);
                if (BasicUtil.isNotEmpty(url)) {
                    runtime.setAdapterKey(DataSourceUtil.parseAdapterKey(url));
                }else{
                    String adapterKey = ConfigTable.environment().string("spring.datasource.,anyline.datasource.", "adapter");
                    if(BasicUtil.isNotEmpty(adapterKey)) {
                        runtime.setAdapterKey(adapterKey);
                    }
                }
                TransactionManage.reg("default", new DefaultTransactionManage(datasource));
            }
        }else{
            loadDefault = false;
        }
        list.addAll(load("spring.datasource", loadDefault));
        list.addAll(load("anyline.datasource", loadDefault));
        //项目提供数据源前缀
        String prefix = ConfigTable.environment().string("anyline.datasource,spring.datasource", "prefix");
        if(BasicUtil.isNotEmpty(prefix)) {
            list.addAll(load(prefix, loadDefault));
        }
        return list;
    }

}
