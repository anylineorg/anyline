package org.anyline.data.interceptor;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.ACTION.SWITCH;
import org.anyline.metadata.Procedure;

public interface QueryInterceptor extends DMInterceptor{

    /**
     * 创建查询SQL之前，可以在这一步修改查询条件
     * @param runtime datasource/adapter/jdbctemplate
     * @param prepare 包含表或自定义SQL
     * @param configs 查询条件
     * @param conditions  查询条件
     * @return RESULT
     */
    default SWITCH prepare(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){ return SWITCH.CONTINUE;}

    default SWITCH prepare(DataRuntime runtime, Procedure procedure, PageNavi navi){ return SWITCH.CONTINUE;}
    /**
     * 合计总数之后调用，行数页数等信息在navi中,到这一步SQL已创建完成
     * @param runtime datasource/adapter/jdbctemplate
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param navi 分页
     * @return RESULT
     */
    default SWITCH before(DataRuntime runtime, Run run, PageNavi navi){ return SWITCH.CONTINUE;}
    default SWITCH before(DataRuntime runtime, Procedure procedure, PageNavi navi){ return SWITCH.CONTINUE;}
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime datasource/adapter/jdbctemplate
     * @param success 查询SQL是否执行成功
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param navi 分页
     * @param millis 耗时
     * @return RESULT
     */
    default SWITCH after(DataRuntime runtime, Run run, boolean success, Object result, PageNavi navi, long millis){ return SWITCH.CONTINUE;}
    default SWITCH after(DataRuntime runtime, Procedure procedure, PageNavi navi, boolean success, Object result, long millis){ return SWITCH.CONTINUE;}
}
