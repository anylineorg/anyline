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

import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.init.DefaultEnvironmentWorker;
import org.anyline.adapter.init.JavaTypeAdapter;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.DataType;
import org.anyline.proxy.ConvertProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.ConfigTable;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.data.tran.DataSourceProxy;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author noear 2025/1/8 created
 */
public class AnylinePlugin implements Plugin {
    protected static Log log = LogProxy.get(DefaultEnvironmentWorker.class);

    @Override
    public void start(AppContext context) throws Throwable {
        log.debug("solon environment start");

        ConfigTable.setEnvironment(new SolonEnvironmentWorker());
        DefaultEnvironmentWorker.start();

        context.subWrapsOfType(DataSource.class, bw -> {
            try {
                DataSource ds = new DataSourceProxy(bw.raw()); //todo:使用 DataSourceProxy 对接 solon 事务管理

                DataSourceHolder.reg(bw.name(), ds);
                if (bw.typed()) {
                    DataSourceHolder.reg("default", ds);
                }
            } catch (Exception e) {
                log.error("注册数据源异常", e);
            }
        });

        //todo:有下面两个，不再需要事件了（时实获取 bean）

        context.subBeansOfType(Convert.class, convert -> {
            Class origin = convert.getOrigin();
            Class target = convert.getTarget();
            Map<Class, Convert> map = ConvertProxy.converts.get(origin);
            if (null == map) {
                map = new Hashtable<>();
                ConvertProxy.converts.put(origin, map);
            }
            map.put(target, convert);

            //设置Java数据类型对应的转换器
            DataType type = JavaTypeAdapter.types.get(origin);
            if (null != type) {
                type.convert(convert);
            }
        });

        context.subBeansOfType(EntityAdapter.class, adapter -> {
            Class type = adapter.type();
            EntityAdapterProxy.push(type, adapter);
            EntityAdapter.sort(EntityAdapterProxy.adapters.get(type));

            List<Class> types = adapter.types();
            if (null != types) {
                for (Class t : types) {
                    EntityAdapterProxy.push(t, adapter);
                    EntityAdapter.sort(EntityAdapterProxy.adapters.get(t));
                }
            }
        });
    }
}