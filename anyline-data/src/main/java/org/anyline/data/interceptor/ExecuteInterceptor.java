package org.anyline.data.interceptor;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.ACTION.SWITCH;
import org.anyline.metadata.Procedure;

public interface ExecuteInterceptor extends DMInterceptor{

    /**
     * 创建 SQL之前，可以在这一步修改查询条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs  执行条件
     * @param conditions 执行条件
     * @return RESULT
     */
    default ACTION.SWITCH prepare(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){ return SWITCH.CONTINUE;}
    default SWITCH prepare(DataRuntime runtime, Procedure procedure){ return SWITCH.CONTINUE;}

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @return RESULT
     */
    default SWITCH before(DataRuntime runtime, Run run){ return SWITCH.CONTINUE;}


    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param procedure 存储过程
     * @return RESULT
     */
    default SWITCH before(DataRuntime runtime, Procedure procedure){ return SWITCH.CONTINUE;}
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param result 影响行数
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param millis 耗时
     * @return RESULT
     */
    default SWITCH after(DataRuntime runtime, Run run, boolean success, int result, long millis){ return SWITCH.CONTINUE;}
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param result 执行结果
     * @param procedure 存储过程
     * @param millis 耗时
     * @return RESULT
     */
    default SWITCH after(DataRuntime runtime, Procedure procedure, boolean success, boolean result, long millis){ return SWITCH.CONTINUE;}
}
