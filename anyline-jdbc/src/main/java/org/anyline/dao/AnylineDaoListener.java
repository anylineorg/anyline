package org.anyline.dao;

import org.anyline.entity.DataSet;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.run.RunSQL;

import java.util.List;
import java.util.Map;

public interface AnylineDaoListener {

    public void beforeQuery(RunSQL run);
    public void afterQuery(RunSQL run, List<Map<String,Object>>  maps);
    public void afterQuery(RunSQL run, DataSet set );
    public void beforeCount(RunSQL run);
    public void afterCount(RunSQL run, int count);
    public void beforeExists(RunSQL run);
    public void afterExists(RunSQL run, boolean exists);
    public void beforeUpdate(RunSQL run, String dest, Object obj, String ... columns);
    public void afterUpdate(RunSQL run,int count, String dest, Object obj, String ... columns);
    public void beforeInsert(RunSQL run, String dest, Object obj, boolean checkParimary, String ... columns);
    public void afterInsert(RunSQL run,int count, String dest, Object obj, boolean checkParimary, String ... columns);
    public void beforeBatchInsert(String dest, Object obj, boolean checkParimary, String ... columns);
    public void afterBatchInsert(int count, String dest, Object obj, boolean checkParimary, String ... columns);
    public void beforeExecute(RunSQL run);
    public void afterExecute(RunSQL run, int count);
    public void beforeExecute(Procedure procedure);
    public void afterExecute(Procedure procedure, boolean result);
    public void beforeQuery(Procedure procedure);
    public void afterQuery(Procedure procedure, DataSet set);
    public void beforeDelete(RunSQL run);
    public void afterDelete(RunSQL run, int count);
}
