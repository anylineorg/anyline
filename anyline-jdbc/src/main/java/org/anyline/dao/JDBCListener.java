package org.anyline.dao;

import org.anyline.entity.DataSet;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.run.RunSQL;

import java.util.List;
import java.util.Map;

public interface JDBCListener {

    public void beforeTotal(AnylineDao dao, RunSQL run);
    public void afterTotal(AnylineDao dao, RunSQL run, int total);
    public void beforeQuery(AnylineDao dao, RunSQL run);
    public void afterQuery(AnylineDao dao, RunSQL run, List<Map<String,Object>>  maps);
    public void afterQuery(AnylineDao dao, RunSQL run, DataSet set );
    public void beforeCount(AnylineDao dao, RunSQL run);
    public void afterCount(AnylineDao dao, RunSQL run, int count);
    public void beforeExists(AnylineDao dao, RunSQL run);
    public void afterExists(AnylineDao dao, RunSQL run, boolean exists);
    public boolean beforeUpdate(AnylineDao dao, RunSQL run, String dest, Object obj, String ... columns);
    public void afterUpdate(AnylineDao dao, RunSQL run,int count, String dest, Object obj, String ... columns);
    public boolean beforeInsert(AnylineDao dao, RunSQL run, String dest, Object obj, boolean checkParimary, String ... columns);
    public void afterInsert(AnylineDao dao, RunSQL run,int count, String dest, Object obj, boolean checkParimary, String ... columns);
    public boolean beforeBatchInsert(AnylineDao dao, String dest, Object obj, boolean checkParimary, String ... columns);
    public void afterBatchInsert(AnylineDao dao, int count, String dest, Object obj, boolean checkParimary, String ... columns);
    public boolean beforeExecute(AnylineDao dao, RunSQL run);
    public void afterExecute(AnylineDao dao, RunSQL run, int count);
    public boolean beforeExecute(AnylineDao dao, Procedure procedure);
    public void afterExecute(AnylineDao dao, Procedure procedure, boolean result);
    public void beforeQuery(AnylineDao dao, Procedure procedure);
    public void afterQuery(AnylineDao dao, Procedure procedure, DataSet set);
    public boolean beforeDelete(AnylineDao dao, RunSQL run);
    public void afterDelete(AnylineDao dao, RunSQL run, int count);
}
