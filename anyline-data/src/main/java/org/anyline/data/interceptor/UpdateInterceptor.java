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

import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.ACTION.SWITCH;
import org.anyline.metadata.Table;

import java.util.List;

public interface UpdateInterceptor extends DMInterceptor{

    /**
     * 创建update SQL之前，可以在这一步修改查询条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param configs 过滤条件及相关配置
     * @param data  对象
     * @param columns  需要更新的列
     * @return RESULT
     */
    default ACTION.SWITCH prepare(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) { return SWITCH.CONTINUE;}

    /**
     * 创建update SQL之前，可以在这一步修改查询条件
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 一般通过TableBuilder生成
     * @param data K-DataRow.VariableValue 更新值key:需要更新的列 value:通常是关联表的列用DataRow.VariableValue表示，也可以是常量
     * @return RESULT
     */
    default ACTION.SWITCH prepare(DataRuntime runtime, String random, TablePrepare prepare, DataRow data, ConfigStore configs) { return SWITCH.CONTINUE;}

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @return RESULT
     */
    default SWITCH before(DataRuntime runtime, String random, Run run, Table dest, Object data, ConfigStore configs, List<String> columns) { return SWITCH.CONTINUE;}

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param data K-DataRow.VariableValue 更新值key:需要更新的列 value:通常是关联表的列用DataRow.VariableValue表示，也可以是常量
     * @return RESULT
     */
    default SWITCH before(DataRuntime runtime, String random, Run run, TablePrepare prepare, DataRow data, ConfigStore configs) { return SWITCH.CONTINUE;}

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param result 影响行数
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param millis 耗时
     * @return RESULT
     */
    default SWITCH after(DataRuntime runtime, String random, Run run, Table dest, Object data, ConfigStore configs, List<String> columns, boolean success, long result, long millis) { return SWITCH.CONTINUE;}

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param result 影响行数
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param prepare 一般通过TableBuilder生成
     * @param data K-DataRow.VariableValue 更新值key:需要更新的列 value:通常是关联表的列用DataRow.VariableValue表示，也可以是常量
     * @param millis 耗时
     * @return RESULT
     */
    default SWITCH after(DataRuntime runtime, String random, Run run, TablePrepare prepare, DataRow data, ConfigStore configs, boolean success, long result, long millis) { return SWITCH.CONTINUE;}
}
