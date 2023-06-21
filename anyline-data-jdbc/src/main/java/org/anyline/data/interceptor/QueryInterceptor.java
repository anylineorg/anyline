package org.anyline.data.interceptor;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.entity.PageNavi;
import org.anyline.entity.data.Parameter;
import org.anyline.entity.data.Procedure;
import org.anyline.entity.data.ACTION.SWITCH;

import java.util.List;

public interface QueryInterceptor extends DMInterceptor{

    /**
     * 创建查询SQL之前，可以在这一步修改查询条件
     * @param runtime datasource/adapter/jdbctemplate
     * @param prepare 有或SQL或SQL.id
     * @param configs 查询条件
     * @param conditions  查询条件
     * @return RESULT
     */
    default SWITCH prepare(JDBCRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return SWITCH.CONINUE;
    }

    default SWITCH prepare(JDBCRuntime runtime, Procedure procedure, PageNavi navi){
        return SWITCH.CONINUE;
    }
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime datasource/adapter/jdbctemplate
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param navi 分页
     * @return RESULT
     */
    default SWITCH before(JDBCRuntime runtime, Run run, PageNavi navi){
        return SWITCH.CONINUE;
    }
    default SWITCH before(JDBCRuntime runtime, Procedure procedure, List<Parameter> inputs, List<Parameter> outputs, PageNavi navi){
        return SWITCH.CONINUE;
    }
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime datasource/adapter/jdbctemplate
     * @param success 查询SQL是否执行成功
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param navi 分页
     * @param millis 耗时
     * @return RESULT
     */
    default SWITCH after(JDBCRuntime runtime, Run run, boolean success, Object result, PageNavi navi, long millis){
        return SWITCH.CONINUE;
    }
    default SWITCH after(JDBCRuntime runtime, Procedure procedure, List<Parameter> inputs, List<Parameter> outputs, PageNavi navi, boolean success, Object result, long millis){
        return SWITCH.CONINUE;
    }
}
