package org.anyline.data.interceptor;

import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.ACTION.SWITCH;

import java.util.List;

public interface InsertInterceptor extends DMInterceptor{

    /**
     * 创建update SQL之前，可以在这一步修改查询条件
     * @param runtime datasource/adapter/jdbctemplate
     * @param dest 表
     * @param data  对象
     * @param columns  需要更新的列
     * @return SWITCH
     */
    default SWITCH prepare(DataRuntime runtime, String dest, Object data, boolean checkPrimary, List<String> columns){ return SWITCH.CONTINUE;}

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime datasource/adapter/jdbctemplate
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @return SWITCH
     */
    default SWITCH before(DataRuntime runtime, Run run, String dest, Object data, boolean checkPrimary, List<String> columns){ return SWITCH.CONTINUE;}
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime datasource/adapter/jdbctemplate
     * @param result 影响行数
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param millis 耗时
     * @return SWITCH
     */
    default SWITCH after(DataRuntime runtime, Run run, String dest, Object data, boolean checkPrimary, List<String> columns, boolean success, int result, long millis){ return SWITCH.CONTINUE;}
}
