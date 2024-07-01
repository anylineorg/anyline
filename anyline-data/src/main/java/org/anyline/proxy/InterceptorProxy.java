/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.proxy;

import org.anyline.data.interceptor.*;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.ACTION.DDL;
import org.anyline.metadata.ACTION.SWITCH;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Procedure;
import org.anyline.metadata.Table;

import java.util.*;

public class InterceptorProxy {

    private static final Map<ACTION.DDL, List<DDInterceptor>> dds = new HashMap<>();
    private static final List<QueryInterceptor> queryInterceptors = new ArrayList<>();
    private static final List<CountInterceptor> countInterceptors = new ArrayList<>();
    private static final List<UpdateInterceptor> updateInterceptors = new ArrayList<>();
    private static final List<InsertInterceptor> insertInterceptors = new ArrayList<>();
    private static final List<DeleteInterceptor> deleteInterceptors = new ArrayList<>();
    private static final List<ExecuteInterceptor> executeInterceptors = new ArrayList<>();

    public static void setQueryInterceptors(Map<String, QueryInterceptor> interceptors) {
        queryInterceptors.addAll(interceptors.values());
        JDBCInterceptor.sort(queryInterceptors);
    }

    public static void setCountInterceptors(Map<String, CountInterceptor> interceptors) {
        countInterceptors.addAll(interceptors.values());
        JDBCInterceptor.sort(countInterceptors);
    }

    public static void setUpdateInterceptors(Map<String, UpdateInterceptor> interceptors) {
        updateInterceptors.addAll(interceptors.values());
        JDBCInterceptor.sort(updateInterceptors);
    }

    public static void setInsertInterceptors(Map<String, InsertInterceptor> interceptors) {
        insertInterceptors.addAll(interceptors.values());
        JDBCInterceptor.sort(insertInterceptors);
    }

    public static void setDeleteInterceptors(Map<String, DeleteInterceptor> interceptors) {
        deleteInterceptors.addAll(interceptors.values());
        JDBCInterceptor.sort(insertInterceptors);
    }

    public static void setExecuteInterceptors(Map<String, ExecuteInterceptor> interceptors) {
        executeInterceptors.addAll(interceptors.values());
        JDBCInterceptor.sort(executeInterceptors);
    }

    public static void setDDInterceptors(Map<String, DDInterceptor> interceptors) {
        for(DDInterceptor interceptor:interceptors.values()) {
            List<DDL> actions = interceptor.actions();
            if(null != actions) {
                for(DDL action:actions) {
                    reg(action, interceptor);
                }
            }
            DDL action = interceptor.action();
            if(null != action) {
                reg(action, interceptor);
            }
        }
        //排序
        for(List<DDInterceptor> list:dds.values()) {
            JDBCInterceptor.sort(list);
        }
    }
    public static void reg(DDL action, DDInterceptor interceptor) {
        List<DDInterceptor> interceptors = dds.get(action);
        if(null == interceptors) {
            interceptors = new ArrayList<>();
            dds.put(action, interceptors);
        }
        interceptors.add(interceptor);
    }

    /*******************************************************************************************************************
     *
     *                                  DML
     *
     * ****************************************************************************************************************/

    public static SWITCH prepareQuery(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors) {
            swt = interceptor.prepare(runtime, random, prepare, configs, conditions);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH prepareQuery(DataRuntime runtime, String random, Procedure procedure, PageNavi navi) {
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors) {
            swt = interceptor.prepare(runtime, random, procedure, navi);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeQuery(DataRuntime runtime, String random, Run run, PageNavi navi) {
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors) {
            swt = interceptor.before(runtime, random, run, navi);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeQuery(DataRuntime runtime, String random, Procedure procedure, PageNavi navi) {
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors) {
            swt = interceptor.before(runtime, random, procedure, navi);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterQuery(DataRuntime runtime, String random, Run run, boolean exe, Object result, PageNavi navi, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors) {
            swt = interceptor.after(runtime, random, run, exe, result, navi, millis);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH afterQuery(DataRuntime runtime, String random, Procedure procedure, PageNavi navi, boolean success, Object result, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(QueryInterceptor interceptor:queryInterceptors) {
            swt = interceptor.after(runtime, random, procedure, navi, success, result, millis);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH prepareCount(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        SWITCH swt = SWITCH.CONTINUE;
        for(CountInterceptor interceptor:countInterceptors) {
            swt = interceptor.prepare(runtime, random, prepare, configs, conditions);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeCount(DataRuntime runtime, String random, Run run) {
        SWITCH swt = SWITCH.CONTINUE;
        for(CountInterceptor interceptor:countInterceptors) {
            swt = interceptor.before(runtime, random, run);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterCount(DataRuntime runtime, String random, Run run, boolean exe, long result, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(CountInterceptor interceptor:countInterceptors) {
            swt = interceptor.after(runtime, random, run, exe, result, millis);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH prepareUpdate(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        SWITCH swt = SWITCH.CONTINUE;
        for(UpdateInterceptor interceptor:updateInterceptors) {
            swt = interceptor.prepare(runtime, random, batch, dest, data, configs, columns);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeUpdate(DataRuntime runtime, String random, Run run, Table dest, Object data, ConfigStore configs, List<String> columns) {
        SWITCH swt = SWITCH.CONTINUE;
        for(UpdateInterceptor interceptor:updateInterceptors) {
            swt = interceptor.before(runtime, random, run, dest, data, configs, columns);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterUpdate(DataRuntime runtime, String random, Run run, Table dest, Object data, ConfigStore configs, List<String> columns, boolean success, long result, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(UpdateInterceptor interceptor:updateInterceptors) {
            swt = interceptor.after(runtime, random, run, dest, data, configs, columns, success, result, millis);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH prepareInsert(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String> columns) {
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors) {
            swt = interceptor.prepare(runtime, random, batch, dest, data, columns);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH prepareInsert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs) {
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors) {
            swt = interceptor.prepare(runtime, random, dest, prepare, configs);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH beforeInsert(DataRuntime runtime, String random, Run run, Table dest, Object data, List<String> columns) {
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors) {
            swt = interceptor.before(runtime, random, run, dest, data, columns);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH beforeInsert(DataRuntime runtime, String random, Run run, Table dest, RunPrepare prepare, ConfigStore configs) {
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors) {
            swt = interceptor.before(runtime, random, run, dest, prepare, configs);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH afterInsert(DataRuntime runtime, String random, Run run, Table dest, Object data, List<String> columns, boolean success, long result, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors) {
            swt = interceptor.after(runtime, random, run, dest, data, columns, success, result, millis);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }


    public static SWITCH afterInsert(DataRuntime runtime, String random, Run run, Table dest, RunPrepare prepare, ConfigStore configs, boolean success, long result, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(InsertInterceptor interceptor:insertInterceptors) {
            swt = interceptor.after(runtime, random, run, dest, prepare, configs, success, result, millis);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH prepareDelete(DataRuntime runtime, String random, int batch, Table table, String key, Collection values) {
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors) {
            swt = interceptor.prepare(runtime, random, batch, table, key, values);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH prepareDelete(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String ... conditions) {
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors) {
            swt = interceptor.prepare(runtime, random, batch, table, configs, conditions);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH prepareDelete(DataRuntime runtime, String random, int batch, Table dest, Object obj, String... columns) {
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors) {
            swt = interceptor.prepare(runtime, random, batch, dest, obj, columns);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeDelete(DataRuntime runtime, String random, Run run) {
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors) {
            swt = interceptor.before(runtime, random, run);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterDelete(DataRuntime runtime, String random, Run run, boolean success, long result, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(DeleteInterceptor interceptor:deleteInterceptors) {
            swt = interceptor.after(runtime, random, run, success, result, millis);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH prepareExecute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors) {
            swt = interceptor.prepare(runtime, random, prepare, configs, conditions);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH prepareExecute(DataRuntime runtime, String random, Procedure procedure) {
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors) {
            swt = interceptor.prepare(runtime, random, procedure);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 prepare
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH beforeExecute(DataRuntime runtime, String random, Run run) {
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors) {
            swt = interceptor.before(runtime, random, run);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }

    public static SWITCH beforeExecute(DataRuntime runtime, String random, Procedure procedure) {
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors) {
            swt = interceptor.before(runtime, random, procedure);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 before
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterExecute(DataRuntime runtime, String random, Run run, boolean success, long result, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors) {
            swt = interceptor.after(runtime, random, run, success, result, millis);
            if(swt == SWITCH.SKIP) {
                //跳过后续的 after
                return swt;
            }
        }
        return swt;
    }
    public static SWITCH afterExecute(DataRuntime runtime, String random, Procedure procedure, boolean success, boolean result, long millis) {
        SWITCH swt = SWITCH.CONTINUE;
        for(ExecuteInterceptor interceptor:executeInterceptors) {
            swt = interceptor.after(runtime, random, procedure, success, result, millis);
            if(swt == SWITCH.SKIP) {
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

    public static <T extends Metadata<T>> SWITCH prepare(DataRuntime runtime, String random, ACTION.DDL action, Metadata<T> metadata) {
        SWITCH swt = metadata.swt();//SWITCH.CONTINUE;
        List<DDInterceptor> interceptors = dds.get(action);
        if(null != interceptors) {
            for(DDInterceptor interceptor:interceptors) {
                swt = interceptor.prepare(runtime, random, action, metadata);
                metadata.swt(swt);
                if(swt != SWITCH.CONTINUE) {
                    //跳过后续的 after
                    return swt;
                }
            }
        }
        return swt;
    }

    public static <T extends Metadata<T>> SWITCH before(DataRuntime runtime, String random, ACTION.DDL action, Metadata<T> metadata, List<Run> runs) {
        SWITCH swt = metadata.swt();//SWITCH.CONTINUE;
        List<DDInterceptor> interceptors = dds.get(action);
        if(null != interceptors) {
            for(DDInterceptor interceptor:interceptors) {
                swt = interceptor.before(runtime, random, action, metadata, runs);
                metadata.swt(swt);
                if(swt != SWITCH.CONTINUE) {
                    //跳过后续的 after
                    return swt;
                }
            }
        }
        return swt;
    }
    public static <T extends Metadata<T>> SWITCH before(DataRuntime runtime, String random, ACTION.DDL action, Metadata<T> metadata, Run run, List<Run> runs) {
        SWITCH swt = metadata.swt();//SWITCH.CONTINUE;
        List<DDInterceptor> interceptors = dds.get(action);
        if(null != interceptors) {
            for(DDInterceptor interceptor:interceptors) {
                swt = interceptor.before(runtime, random, action, metadata, run, runs);
                metadata.swt(swt);
                if(swt != SWITCH.CONTINUE) {
                    //跳过后续的 after
                    return swt;
                }
            }
        }
        return swt;
    }
    public static <T extends Metadata<T>> SWITCH after(DataRuntime runtime, String random, ACTION.DDL action, Metadata<T> metadata, List<Run> runs, boolean result, long millis) {
        SWITCH swt = metadata.swt();//SWITCH.CONTINUE;
        List<DDInterceptor> interceptors = dds.get(action);
        if(null != interceptors) {
            for(DDInterceptor interceptor:interceptors) {
                swt = interceptor.after(runtime, random, action, metadata, runs, result, millis);
                metadata.swt(swt);
                if(swt != SWITCH.CONTINUE) {
                    //跳过后续的 after
                    return swt;
                }
            }
        }
        return swt;
    }
    public static <T extends Metadata<T>> SWITCH after(DataRuntime runtime, String random, ACTION.DDL action, Metadata<T> metadata, Run run, List<Run> runs, boolean result, long millis) {
        SWITCH swt = metadata.swt();//SWITCH.CONTINUE;
        List<DDInterceptor> interceptors = dds.get(action);
        if(null != interceptors) {
            for(DDInterceptor interceptor:interceptors) {
                swt = interceptor.after(runtime, random, action, metadata, run, runs, result, millis);
                metadata.swt(swt);
                if(swt != SWITCH.CONTINUE) {
                    //跳过后续的 after
                    return swt;
                }
            }
        }
        return swt;
    }
}
