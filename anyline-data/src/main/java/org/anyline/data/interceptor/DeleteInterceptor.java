package org.anyline.data.interceptor;

import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.metadata.ACTION.SWITCH;

import java.util.Collection;
import java.util.List;

public interface DeleteInterceptor extends DMInterceptor{

    /**
     * 创建delete SQL之前，可以在这一步修改查询条件
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表
     * @param data  对象
     * @param columns  需要更新的列
     * @return RESULT
     */
    default SWITCH prepare(DataRuntime runtime, String dest, Object data, boolean checkPrimary, List<String> columns){ return SWITCH.CONTINUE;}

    default SWITCH prepare(DataRuntime runtime, String table, ConfigStore configs, String ... conditions){ return SWITCH.CONTINUE;}
    default SWITCH prepare(DataRuntime runtime, String table, String key, Collection values){ return SWITCH.CONTINUE;}
    default SWITCH prepare(DataRuntime runtime, String table, Object obj, String ... columns){ return SWITCH.CONTINUE;}

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
     * @param result 影响行数
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param millis 耗时
     * @return RESULT
     */
    default SWITCH after(DataRuntime runtime, Run run, boolean success, int result, long millis){ return SWITCH.CONTINUE;}
}
