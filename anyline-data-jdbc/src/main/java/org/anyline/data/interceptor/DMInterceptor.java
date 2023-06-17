package org.anyline.data.interceptor;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;

import java.util.List;

public interface DMInterceptor extends JDBCInterceptor{

    enum ACTION{
        QUERY           ("查询"),
        INSERT          ("插入"),
        UPDATE          ("更新"),
        DELETE          ("删除"),
        EXISTS          ("是否存在"),
        COUNT           ("行数统计"),

        ;
        private final String title;
        ACTION(String title){
            this.title = title;
        }
    }

    /**
     * 可触发当前拦截器的事件<br/>
     * 拦截多个事件的实现actions(),拦截一个事件的实现action()
     * @return List
     */
    default List<ACTION> actions(){return null;}
    default ACTION action(){return null;}

    /**
     * 创建查询(删除)SQL之前，可以在这一步修改查询条件
     * @param runtime
     * @param prepare
     * @param configs
     * @param conditions
     * @return
     */
    SWITCH before(JDBCRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions);
    SWITCH after(JDBCRuntime runtime, Object result, RunPrepare prepare, ConfigStore configs, String ... conditions);
}
