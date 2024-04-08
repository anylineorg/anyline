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

package org.anyline.environment.spring.data.jdbc.datasource;

import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.DataSourceLoader;
import org.anyline.data.jdbc.datasource.JDBCDataSourceLoader;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.environment.spring.data.jdbc.runtime.SpringJDBCRuntimeHolder;
import org.anyline.environment.spring.data.transaction.SpringTransactionManage;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


@Component("anyline.environment.spring.data.datasource.loader.jdbc")
public class SpringJDBCDataSourceLoader extends JDBCDataSourceLoader implements DataSourceLoader {
    private SpringJDBCDataSourceHolder holder = SpringJDBCDataSourceHolder.instance();
    @Override
    public List<String> load() {
        List<String> list = new ArrayList<>();
        holder.loadCache();
        boolean loadDefault = true; //是否需要加载default
        if(!DataSourceHolder.contains("default")){
            //如果还没有注册默认数据源
            // 项目中可以提前注册好默认数据源 如通过@Configuration注解先执行注册 也可以在spring启动完成后覆盖默认数据源
            JdbcTemplate jdbc = null;
            DataSource datasource = null;
            try{
                jdbc = ConfigTable.worker.getBean(JdbcTemplate.class);
            }catch (Exception e){}
            DataRuntime runtime = null;
            if(null != jdbc){
                try {
                    runtime = SpringJDBCRuntimeHolder.instance().reg("default", jdbc, null);
                    datasource = jdbc.getDataSource();
                    loadDefault = false;
                }catch (Exception e){
                    runtime = null;
                }
            }else{
                try{
                    datasource = ConfigTable.worker.getBean(DataSource.class);
                }catch (Exception e){
                    runtime = null;
                }
                if(null != datasource){
                    try {
                        runtime =  holder.create("default", datasource, false);
                        loadDefault = false;
                    }catch (Exception e){
                        runtime = null;
                        e.printStackTrace();
                    }
                }
            }
            //有不支持通过connection返回获取连接信息的驱动，所以从配置文件中获取
            if(null != runtime) {
                Object def = ConfigTable.worker.getBean("anyline.service.default");
                if(null == ConfigTable.worker.getBean("anyline.service") && null != def) {
                    ConfigTable.worker.regBean("anyline.service", def);
                }
                String driver = ConfigTable.worker.string("spring.datasource.,anyline.datasource.", "driver,driver-class,driver-class-name");
                String url = ConfigTable.worker.string( "spring.datasource.,anyline.datasource.", "url,jdbc-url");
                runtime.setDriver(driver);
                runtime.setUrl(url);
                if (BasicUtil.isNotEmpty(url)) {
                    runtime.setAdapterKey(DataSourceUtil.parseAdapterKey(url));
                }else{
                    String adapterKey = ConfigTable.worker.string("spring.datasource.,anyline.datasource.", "adapter");
                    if(BasicUtil.isNotEmpty(adapterKey)){
                        runtime.setAdapterKey(adapterKey);
                    }
                }
                DataSourceTransactionManager dm = (DataSourceTransactionManager) ConfigTable.worker.getBean("transactionManager");
                if(null != dm){
                    TransactionManage manage = new SpringTransactionManage(dm);
                    TransactionManage.reg("default", manage);
                }else{
                    TransactionManage.reg("default", new SpringTransactionManage(datasource));
                }
            }
        }else{
            loadDefault = false;
        }
        list.addAll(load("spring.datasource", loadDefault));
        list.addAll(load("anyline.datasource", loadDefault));
        //TODO 项目指定一个前缀

        return list;
    }


}
