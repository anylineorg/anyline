/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.chroma.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.chroma.run.ChromaRun;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.anyline.util.LogUtil;

import java.util.*;

@AnylineComponent("anyline.data.adapter.chroma")
public class ChromaAdapter extends AbstractDriverAdapter {

    public ChromaAdapter() {
        super();
    }

    @Override
    public DatabaseType type() {
        return DatabaseType.Chroma;
    }

    @Override
    public boolean supportCatalog() {
        return false;
    }

    @Override
    public boolean supportSchema() {
        return false;
    }

    public ChromaActuator actuator() {
        return (ChromaActuator) actuator;
    }

    @Override
    public String name(Type type) {
        return null;
    }

    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, T meta) {
    }

    @Override
    public LinkedHashMap<String, Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment) {
        return null;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Column query) throws Exception {
        return null;
    }

    @Override
    public String concat(DataRuntime runtime, String... args) {
        return null;
    }

    @Override
    public Run buildSelectRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions) {
        ChromaRun run = new ChromaRun(runtime, prepare.getTableName());
        run.setRuntime(runtime);
        run.setConfigStore(configs);
        run.setPrepare(prepare);
        run.addCondition(conditions);
        if(run.checkValid()) {
            run.init();
            fillSelectContent(runtime, run, placeholder, unicode);
        }
        return run;
    }

    @Override
    public void init(DataRuntime runtime, Run run, ConfigStore configs, String ... conditions) {
        super.init(runtime, run, configs, conditions);
    }

    /**
     * select [命令执行]<br/>
     * Chroma非关系型数据库，不使用SQL文本命令，直接通过SDK调用
     */
    @Override
    public DataSet<DataRow> selects(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run) {
        long fr = System.currentTimeMillis();
        if(null == random) {
            random = random(runtime);
        }
        DataSet set = new DataSet();
        try{
            if(run instanceof ChromaRun) {
                ChromaRun r = (ChromaRun) run;
                if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                    log.info("{}[cmd:select][collection:{}][filter:{}]", random, r.getTableName(), r.getFilter());
                }
            } else {
                if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                    log.info("{}[cmd:select][table:{}]", random, run.getTableName());
                }
            }
            set = actuator().selects(this, runtime, random, system, ACTION.DML.SELECT, table, configs, run, null, null, null);
            if(ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[封装耗时:{}][封装行数:{}]", random, DateUtil.format(System.currentTimeMillis() - fr), set.size());
            }
        }catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("select 异常:", e);
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw new org.anyline.exception.CommandSelectException("query异常:" + e, e);
            } else {
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][table:{}]", random, LogUtil.format("查询异常:", 33) + e, run.getTableName());
                }
            }
        }
        return set;
    }

    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        if(null == random) {
            random = random(runtime);
        }
        if(null != configs) {
            configs.add(run);
        }
        if(log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
            log.info("{}[action:select]{}", random, run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
        }
        try{
            return actuator().maps(this, runtime, random, configs, run);
        }catch(Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                log.error("maps 异常:", e);
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:select]{}", random, LogUtil.format("查询异常:", 33) + e, run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
            if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
                throw new org.anyline.exception.CommandSelectException("query异常:" + e, e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        List<Map<String, Object>> maps = maps(runtime, random, configs, run);
        if(maps != null && !maps.isEmpty()) {
            return maps.get(0);
        }
        return new HashMap<>();
    }

}