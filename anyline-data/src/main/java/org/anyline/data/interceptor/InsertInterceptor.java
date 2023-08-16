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
import org.anyline.metadata.ACTION.SWITCH;

import java.util.List;

public interface InsertInterceptor extends DMInterceptor{

    /**
     * 创建update SQL之前，可以在这一步修改查询条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表
     * @param data  对象
     * @param columns  需要更新的列
     * @return SWITCH
     */
    default SWITCH prepare(DataRuntime runtime, String dest, Object data, boolean checkPrimary, List<String> columns){ return SWITCH.CONTINUE;}

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @return SWITCH
     */
    default SWITCH before(DataRuntime runtime, Run run, String dest, Object data, boolean checkPrimary, List<String> columns){ return SWITCH.CONTINUE;}
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param result 影响行数
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param millis 耗时
     * @return SWITCH
     */
    default SWITCH after(DataRuntime runtime, Run run, String dest, Object data, boolean checkPrimary, List<String> columns, boolean success, long result, long millis){ return SWITCH.CONTINUE;}
}
