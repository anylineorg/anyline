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
     * @param conditions  简单过滤条件
     * @return RESULT
     */
    SWITCH prepare(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions);

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @return RESULT
     */
    SWITCH before(DataRuntime runtime, Run run);
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param success 查询SQL是否执行成功
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param millis 耗时
     * @return RESULT
     */
    SWITCH after(DataRuntime runtime, Run run, boolean success, long result, long millis);
}
