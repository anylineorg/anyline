package org.anyline.data.interceptor;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Procedure;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;

import java.util.List;

public interface QueryInterceptor {
    /**
     * 查询之前执行
     * @param adapter adapter
     * @param prepare prepare
     * @param configs ConfigStore
     * @param conditions conditions
     * @return 1:继续下一个拦截 0:阻断后续拦截器 -1:阻断SQL执行
     */
    public int before(JDBCAdapter adapter, RunPrepare prepare, ConfigStore configs, String ... conditions);
    public int before(Procedure procedure);
    public int after(Run run, List<?> maps, long millis);
    public int after(Run run, EntitySet<?> maps, long millis);
    public int after(Run run, DataSet set, long millis);
    public int after(Procedure procedure, DataSet set, long millis);
    public int after(Run run, int count, long millis);
}
