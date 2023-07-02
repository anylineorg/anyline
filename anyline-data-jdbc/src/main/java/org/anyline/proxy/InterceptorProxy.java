package org.anyline.proxy;

import org.anyline.data.interceptor.*;
import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Parameter;
import org.anyline.metadata.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.anyline.metadata.ACTION.SWITCH;
import org.anyline.metadata.ACTION.DDL;

import java.util.*;

@Component("anyline.interceptor.proxy")
public class InterceptorProxy {

    private static Map<ACTION.DDL, List<DDInterceptor>> dds = new HashMap<>();
    private static List<QueryInterceptor> queryInterceptors = new ArrayList<>();
    private static List<CountInterceptor> countInterceptors = new ArrayList<>();
    private static List<UpdateInterceptor> updateInterceptors = new ArrayList<>();
    private static List<InsertInterceptor> insertInterceptors = new ArrayList<>();
    private static List<DeleteInterceptor> deleteInterceptors = new ArrayList<>();
    private static List<ExecuteInterceptor> executeInterceptors = new ArrayList<>();

    @Autowired(required=false)
    public void setQueryInterceptors(Map<String, QueryInterceptor> interceptors) {
        for(QueryInterceptor interceptor:interceptors.values()){
            queryInterceptors.add(interceptor);
        }
        JDBCInterceptor.sort(queryInterceptors);
    }
    @Autowired(required=false)
    public void setCountInterceptors(Map<String, CountInterceptor> interceptors) {
        for(CountInterceptor interceptor:interceptors.values()){
            countInterceptors.add(interceptor);
        }
        JDBCInterceptor.sort(countInterceptors);
    }
    @Autowired(required=false)
    public void setUpdateInterceptors(Map<String, UpdateInterceptor> interceptors) {
        for(UpdateInterceptor interceptor:interceptors.values()){
            updateInterceptors.add(interceptor);
        }
        JDBCInterceptor.sort(updateInterceptors);
    }
    @Autowired(required=false)
    public void setInsertInterceptors(Map<String, InsertInterceptor> interceptors) {
        for(InsertInterceptor interceptor:interceptors.values()){
            insertInterceptors.add(interceptor);
        }
        JDBCInterceptor.sort(insertInterceptors);
    }
    @Autowired(required=false)
    public void setDeleteInterceptors(Map<String, DeleteInterceptor> interceptors) {
        for(DeleteInterceptor interceptor:interceptors.values()){
            deleteInterceptors.add(interceptor);
        }
        JDBCInterceptor.sort(insertInterceptors);
    }
    @Autowired(required=false)
    public void setExecuteInterceptors(Map<String, ExecuteInterceptor> interceptors) {
        for(ExecuteInterceptor interceptor:interceptors.values()){
            executeInterceptors.add(interceptor);
        }
        JDBCInterceptor.sort(executeInterceptors);
    }
    @Autowired(required=false)
    public void setDDInterceptors(Map<String, DDInterceptor> interceptors) {
        for(DDInterceptor interceptor:interceptors.values()){
            List<DDL> actions = interceptor.actions();
            if(null != actions){
                for(DDL action:actions){
                    reg(action, interceptor);
                }
            }
            DDL action = interceptor.action();
            if(null != action){
                reg(action, interceptor);
            }
        }
        //排序
        for(List<DDInterceptor> list:dds.values()){
            JDBCInterceptor.sort(list);
        }
    }
    public void reg(DDL action, DDInterceptor interceptor){
        List<DDInterceptor> interceptors = dds.get(action);
        if(null == interceptors){
            interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
    }

    /*******************************************************************************************************************
     *
     *                                  DML
     *
     * ****************************************************************************************************************/

    public static SWITCH prepareQuery(JDBCRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors){
            swt = interceptor.prepare(runtime, prepare, configs, conditions);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH prepareQuery(JDBCRuntime runtime, String random, Procedure procedure, PageNavi navi){
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors){
            swt = interceptor.prepare(runtime, procedure, navi);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeQuery(JDBCRuntime runtime, String random, Run run, PageNavi navi){
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors){
            swt = interceptor.before(runtime, run, navi);
            if(swt == SWITCH.SKIP){
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeQuery(JDBCRuntime runtime, String random, Procedure procedure, List<Parameter> inputs, List<Parameter> outputs, PageNavi navi){
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors){
            swt = interceptor.before(runtime, procedure, inputs, outputs, navi);
            if(swt == SWITCH.SKIP){
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterQuery(JDBCRuntime runtime, String random, Run run, boolean exe, Object result, PageNavi navi, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors){
            swt = interceptor.after(runtime, run, exe, result, navi, millis);
            if(swt == SWITCH.SKIP){
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH afterQuery(JDBCRuntime runtime, String random, Procedure procedure, List<Parameter> inputs, List<Parameter> outputs, PageNavi navi, boolean success, Object resutl, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors){
            swt = interceptor.after(runtime, procedure, inputs, outputs, navi, success, resutl, millis);
            if(swt == SWITCH.SKIP){
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }


    public static SWITCH prepareCount(JDBCRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
        SWITCH swt = SWITCH.CONTINUE;
        for(CountInterceptor interceptor:countInterceptors){
            swt = interceptor.prepare(runtime, prepare, configs, conditions);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeCount(JDBCRuntime runtime, String random, Run run){
        SWITCH swt = SWITCH.CONTINUE;
        for(CountInterceptor interceptor:countInterceptors){
            swt = interceptor.before(runtime, run);
            if(swt == SWITCH.SKIP){
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterCount(JDBCRuntime runtime, String random, Run run, boolean exe, int result, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        for(CountInterceptor interceptor:countInterceptors){
            swt = interceptor.after(runtime, run, exe, result, millis);
            if(swt == SWITCH.SKIP){
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }


    public static SWITCH prepareUpdate(JDBCRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns){
        SWITCH swt = SWITCH.CONTINUE;
        for(UpdateInterceptor interceptor:updateInterceptors){
            swt = interceptor.prepare(runtime, dest, data, configs, columns);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeUpdate(JDBCRuntime runtime, String random, Run run, String dest, Object data, ConfigStore configs, List<String> columns){
        SWITCH swt = SWITCH.CONTINUE;
        for(UpdateInterceptor interceptor:updateInterceptors){
            swt = interceptor.before(runtime, run, dest, data, configs, columns);
            if(swt == SWITCH.SKIP){
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterUpdate(JDBCRuntime runtime, String random, Run run, String dest, Object data, ConfigStore configs, List<String> columns, boolean success, int result, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        for(UpdateInterceptor interceptor:updateInterceptors){
            swt = interceptor.after(runtime, run, dest, data, configs, columns, success, result, millis);
            if(swt == SWITCH.SKIP){
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }


    public static SWITCH prepareInsert(JDBCRuntime runtime, String random, String dest, Object data, boolean checkPrimary, List<String> columns){
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors){
            swt = interceptor.prepare(runtime, dest, data,checkPrimary,  columns);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeInsert(JDBCRuntime runtime, String random, Run run, String dest, Object data, boolean checkPrimary,  List<String> columns){
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors){
            swt = interceptor.before(runtime, run, dest, data,checkPrimary,  columns);
            if(swt == SWITCH.SKIP){
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterInsert(JDBCRuntime runtime, String random, Run run, String dest, Object data, boolean checkPrimary, List<String> columns, boolean success, int result, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors){
            swt = interceptor.after(runtime, run, dest, data,checkPrimary,  columns, success, result, millis);
            if(swt == SWITCH.SKIP){
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH prepareDelete(JDBCRuntime runtime, String random, String table, String key, Collection values){
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors){
            swt = interceptor.prepare(runtime,table, key, values);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH prepareDelete(JDBCRuntime runtime, String random, String table, ConfigStore configs, String ... conditions){
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors){
            swt = interceptor.prepare(runtime,table, configs, conditions);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH prepareDelete(JDBCRuntime runtime, String random, String dest, Object obj, String... columns){
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors){
            swt = interceptor.prepare(runtime, dest, obj, columns);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeDelete(JDBCRuntime runtime, String random, Run run){
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors){
            swt = interceptor.before(runtime, run);
            if(swt == SWITCH.SKIP){
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterDelete(JDBCRuntime runtime, String random, Run run, boolean success, int result, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors){
            swt = interceptor.after(runtime, run, success, result, millis);
            if(swt == SWITCH.SKIP){
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }


    public static SWITCH prepareExecute(JDBCRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors){
            swt = interceptor.prepare(runtime, prepare, configs, conditions);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH prepareExecute(JDBCRuntime runtime, String random, Procedure procedure){
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors){
            swt = interceptor.prepare(runtime, procedure);
            if(swt == SWITCH.SKIP){
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeExecute(JDBCRuntime runtime, String random, Run run){
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors){
            swt = interceptor.before(runtime, run);
            if(swt == SWITCH.SKIP){
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH beforeExecute(JDBCRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs){
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors){
            swt = interceptor.before(runtime, procedure, sql, inputs, outputs);
            if(swt == SWITCH.SKIP){
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterExecute(JDBCRuntime runtime, String random, Run run, boolean success, int result, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors){
            swt = interceptor.after(runtime, run, success, result, millis);
            if(swt == SWITCH.SKIP){
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterExecute(JDBCRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs, boolean success, boolean result, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors){
            swt = interceptor.after(runtime, procedure, sql, inputs, outputs, success, result, millis);
            if(swt == SWITCH.SKIP){
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }
    /*******************************************************************************************************************
     *
     *                                  DDL
     *
     * ****************************************************************************************************************/

    public static SWITCH prepare(JDBCRuntime runtime, String random, ACTION.DDL action, Object metadata){
        SWITCH swt = SWITCH.CONTINUE;
        List<DDInterceptor> interceptors = dds.get(action);
        if(null != interceptors){
            for(DDInterceptor interceptor:interceptors){
                swt = interceptor.prepare(runtime, random, action, metadata);
                if(swt != SWITCH.CONTINUE){
                    //跳过后续的 after
                    return swt;
                }
            }
        }
        return swt;
    }

    public static SWITCH before(JDBCRuntime runtime, String random,  ACTION.DDL action, Object metadata, List<Run> runs){
        SWITCH swt = SWITCH.CONTINUE;
        List<DDInterceptor> interceptors = dds.get(action);
        if(null != interceptors){
            for(DDInterceptor interceptor:interceptors){
                swt = interceptor.before(runtime, random, action, metadata, runs);
                if(swt != SWITCH.CONTINUE){
                    //跳过后续的 after
                    return swt;
                }
            }
        }
        return swt;
    }
    public static SWITCH after(JDBCRuntime runtime, String random,  ACTION.DDL action, Object metadata, List<Run> runs, boolean result, long millis){
        SWITCH swt = SWITCH.CONTINUE;
        List<DDInterceptor> interceptors = dds.get(action);
        if(null != interceptors){
            for(DDInterceptor interceptor:interceptors){
                swt = interceptor.after(runtime, random, action, metadata, runs, result, millis);
                if(swt != SWITCH.CONTINUE){
                    //跳过后续的 after
                    return swt;
                }
            }
        }
        return swt;
    }
}
