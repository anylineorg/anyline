package org.anyline.data.interceptor;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.run.Run;
import org.anyline.entity.data.ACTION;
import org.anyline.entity.data.ACTION.SWITCH;

import java.util.List;

public interface DDInterceptor  extends JDBCInterceptor{


    /**
     * 可触发当前拦截器的事件<br/>
     * 拦截多个事件的实现actions(),拦截一个事件的实现action()
     * @return List
     */
    default List<ACTION.DDL> actions(){return null;}
    default ACTION.DDL action(){return null;}

    default SWITCH prepare(JDBCRuntime runtime, String random, ACTION.DDL action, Object metadata){return SWITCH.CONTINUE;}
    default SWITCH before(JDBCRuntime runtime, String random, ACTION.DDL action, Object metadata, List<Run> runs){return SWITCH.CONTINUE;}

    /**
     *
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param action 执行命令
     * @param metadata table/column等
     * @param runs 需要执行的SQL 有些命令需要多条SQL完成
     * @param result SQL是否成功执行
     * @param millis 执行耗时
     * @return SWITCH
     */
    default SWITCH after(JDBCRuntime runtime, String random, ACTION.DDL action, Object metadata, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

}
