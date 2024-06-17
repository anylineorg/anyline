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



package org.anyline.data.interceptor;

import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.ACTION.SWITCH;
import org.anyline.metadata.Metadata;

import java.util.List;

public interface DDInterceptor  extends JDBCInterceptor{

    /**
     * 可触发当前拦截器的事件<br/>
     * 拦截多个事件的实现actions(), 拦截一个事件的实现action()
     * @return List
     */
    default List<ACTION.DDL> actions() {return null;}
    default ACTION.DDL action() {return null;}

    /**
     * 在创建SQL之前触发，如果需要影响SQL创建可以在这一步修改metadata, 类型一般是org.anyline.entity.data.Table/Column等
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param action 执行命令
     * @param metadata  类型一般是org.anyline.entity.data.Table/Column等
     * @return SWITCH
     */
    default <T extends Metadata<T>> SWITCH prepare(DataRuntime runtime, String random, ACTION.DDL action, Metadata<T> metadata) {return SWITCH.CONTINUE;}

    /**
     * 在SQL执行之前触发，注意到了这一步，SQL已经创建完成不可修改
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param action 执行命令
     * @param metadata table/column等
     * @param runs 需要执行的SQL 有些命令需要多条SQL完成
     * @return SWITCH
     */
    default <T extends Metadata<T>> SWITCH before(DataRuntime runtime, String random, ACTION.DDL action, Metadata<T> metadata, List<Run> runs) {return SWITCH.CONTINUE;}

    /**
     *
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param action 执行命令
     * @param metadata table/column等
     * @param runs 需要执行的SQL 有些命令需要多条SQL完成
     * @param result SQL是否成功执行
     * @param millis 执行耗时
     * @return SWITCH
     */
    default <T extends Metadata<T>> SWITCH after(DataRuntime runtime, String random, ACTION.DDL action, Metadata<T> metadata, List<Run> runs, boolean result, long millis) {return SWITCH.CONTINUE;}

}
