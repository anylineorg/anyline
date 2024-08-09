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

package org.anyline.data.datasource;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.exception.DataSourceUsingException;

public interface DataSourceMonitor {
    /**
     * 数据源是否正被使用<br/>
     * 注意 有些动态数源中可能连接多个数据源 需要遍历检测<br/>
     * 需要在项目中实现,在注销和覆盖数据源时会调用当前方法，如果数据源正在使用会根据情况抛出异常或忽略注销<br/>
     * 通常情况下是检测活动中的连接数量
     * @param key 数据源名称
     * @param datasource 数据源 一般会是一个连接池实例<br/>
     *                   如 HikariDataSource.getHikariPoolMXBean().getActiveConnections()<br/>
     *                   DruidDataSource.getActiveCount()<br/>
     *                   其他类型参考相应DataRuntime.getProcessor()的返回值 如ElasticSearchRuntime.client(org.elasticsearch.client.RestClient)
     * @return true:使用中
     */
    default boolean using(DataRuntime runtime, String key, Object datasource) {
        return false;
    }

    /**
     * 注销数据源之前会调用
     * @param key 数据源名称
     * @param datasource 数据源
     * @return  0:当前方法内未释放 需要上层继续操作,如调用datasource.close()  1:当前方法内已释放 不需要上层继续操作
     * @throws DataSourceUsingException 如果抛出异常，上层方法会抛出异常并中断注销
     */
    default int destroy(DataRuntime runtime, String key, Object datasource) throws DataSourceUsingException {
        if(using(runtime, key, datasource)){
            throw new DataSourceUsingException(key, datasource);
        }
        //可以在这里释放相关资源 并返回 1
        return 0;
    }

    /**
     * 数据源特征 默认不需要实现  由上层方法自动提取一般会通过 driver_产品名_url 合成
     * @param datasource 数据源
     * @return String 返回null由上层自动提取
     */
    default String feature(DataRuntime runtime,Object datasource) {
        return null;
    }

    /**
     * 数据源唯一标识 如果不实现则默认feature
     * @param datasource 数据源
     * @return String 返回null由上层自动提取
     */
    default String key(DataRuntime runtime, Object datasource) {
        return feature(runtime,datasource);
    }

    /**
     * 根据数据源定位adapter,如果实现了这个方法就不需要实现feature
     * @param datasource 数据源
     * @return String 返回null由上层自动提取
     */
    default DriverAdapter adapter(DataRuntime runtime, Object datasource) {
        return null;
    }

    /**
     * 上层方法完成adapter定位后调用,可以在这里缓存,下一次定位提供给adapter(Object datasource)
     * @param datasource 数据源
     * @param adapter DriverAdapter
     * @return DriverAdapter 如果没有问题原样返回，如果有问题可以修正或返回null, 如果返回null上层方法会抛出adapter定位失败的异常
     */
    default DriverAdapter after(DataRuntime runtime, Object datasource, DriverAdapter adapter) {
        return adapter;
    }

    /**
     * 同一个数据源是否保持同一个adapter<br/>
     * 这里通常根据类型判断 如HikariDataSource DruidDataSource<br/>
     * 针对同一个数据源对应多个不同类型数据库时才需要返回false(如一些动态数据源类型)<br/>
     * @param datasource 数据源
     * @return boolean false:每次操作都会检测一次adapter true:同一数据源使用同一个adapter
     */
    default boolean keepAdapter(DataRuntime runtime, Object datasource) {
        return true;
    }
}
