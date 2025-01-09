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
package org.anyline.environment.solon;

import org.anyline.adapter.init.DefaultEnvironmentWorker;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.util.ConfigTable;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.data.tran.DataSourceProxy;

import javax.sql.DataSource;

/**
 * @author noear 2025/1/8 created
 */
public class SolonEnvironmentPlugin implements Plugin {
    protected static Log log = LogProxy.get(DefaultEnvironmentWorker.class);

    @Override
    public void start(AppContext context) throws Throwable {
        log.debug("solon environment start");

        ConfigTable.setEnvironment(new SolonEnvironmentWorker());

        context.onEvent(AppLoadEndEvent.class, e->{
            log.debug("solon end event");
            SolonAutoConfiguration.init();
            DefaultEnvironmentWorker.start();
        });

        context.subWrapsOfType(DataSource.class, bw -> {
            try {
                DataSource ds = new DataSourceProxy(bw.raw()); //todo:使用 DataSourceProxy 对接 solon 事务管理
                String name = bw.name();
                if(name.startsWith("anyline.")){
                    return;
                }
                DataSourceHolder.reg(name, ds);
                if (bw.typed()) {
                    DataSourceHolder.reg("default", ds);
                }
            } catch (Exception e) {
                log.error("注册数据源异常", e);
            }
        });
    }
}