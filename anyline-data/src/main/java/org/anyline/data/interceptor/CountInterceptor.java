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
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.ACTION.SWITCH;

public interface CountInterceptor extends DMInterceptor{

    /**
     * 创建COUNT SQL之前，可以在这一步修改查询条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return RESULT
     */
    SWITCH prepare(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @return RESULT
     */
    SWITCH before(DataRuntime runtime, String random, Run run, ConfigStore configs);
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param success 查询SQL是否执行成功
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param millis 耗时
     * @return RESULT
     */
    SWITCH after(DataRuntime runtime, String random, Run run, ConfigStore configs, boolean success, long result, long millis);
}
