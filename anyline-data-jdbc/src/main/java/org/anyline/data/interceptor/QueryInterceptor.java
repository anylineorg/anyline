package org.anyline.data.interceptor;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.entity.PageNavi;

public interface QueryInterceptor extends DMInterceptor{

    /**
     * 创建查询SQL之前，可以在这一步修改查询条件
     * @param runtime datasource/adapter/jdbctemplate
     * @param prepare 有或SQL或SQL.id
     * @param configs 查询条件
     * @param conditions  查询条件
     * @return RESULT
     */
    SWITCH prepare(JDBCRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions);

    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime datasource/adapter/jdbctemplate
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param navi 分页
     * @return RESULT
     */
    SWITCH before(JDBCRuntime runtime, Run run, PageNavi navi);
    /**
     * 合计总数之前调用，到这一步SQL已创建完成
     * @param runtime datasource/adapter/jdbctemplate
     * @param exe 查询SQL是否执行过
     * @param run 查询SQL(包含SQL体，查询条件，查询参数值)
     * @param navi 分页
     * @param millis 耗时
     * @return RESULT
     */
    SWITCH after(JDBCRuntime runtime, Run run, boolean exe, Object result, PageNavi navi, long millis);
}
