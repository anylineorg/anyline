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


package org.anyline.data.arango.run;

import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * ArangoDB 执行命令封装<br/>
 * 承载 AQL 查询字符串与绑定参数, 以及更新数据/过滤条件等
 */
public class ArangoRun extends TableRun implements Run {

    /** AQL 查询字符串(完整 AQL 语句或 WHERE 子句片段) */
    private String cmd;
    /** AQL 绑定变量 key-value */
    private Map<String, Object> bindVars;
    /** 更新数据 Map(对应 AQL UPDATE ... WITH @update 中的 @update) */
    private Map<String, Object> updateData;
    /** 过滤器条件 Map(用于构建 AQL WHERE 子句) */
    private Map<String, Object> filter;

    public ArangoRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }
    public ArangoRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }

    // ===== AQL =====

    public String getCmd() {
        return cmd;
    }
    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    // ===== bindVars =====

    public Map<String, Object> getBindVars() {
        if(null == bindVars) {
            bindVars = new HashMap<>();
        }
        return bindVars;
    }
    public void setBindVars(Map<String, Object> bindVars) {
        this.bindVars = bindVars;
    }
    public void addBindVar(String key, Object value) {
        if(null == bindVars) {
            bindVars = new HashMap<>();
        }
        bindVars.put(key, value);
    }

    // ===== updateData =====

    public Map<String, Object> getUpdateData() {
        return updateData;
    }
    public void setUpdateData(Map<String, Object> updateData) {
        this.updateData = updateData;
    }

    // ===== filter =====

    public Map<String, Object> getFilter() {
        return filter;
    }
    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }

    @Override
    public boolean checkValid() {
        return true;
    }
}