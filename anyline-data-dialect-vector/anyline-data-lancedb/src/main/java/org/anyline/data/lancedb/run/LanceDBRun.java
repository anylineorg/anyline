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


package org.anyline.data.lancedb.run;

import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.auto.init.DefaultAutoCondition;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.Compare;
import org.anyline.entity.DataRow;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Table;
import org.anyline.util.BeanUtil;
import org.anyline.util.LogUtil;

import java.util.List;
import java.util.Map;

public class LanceDBRun extends TableRun implements Run {

    private String filter;      // LanceDB filter expression (e.g., "id > 100")
    private String vectorField; // vector search field name
    private List<Float> queryVector; // vector search query vector
    private int topK = 10;      // k-NN top k
    private boolean isVectorSearch = false; // whether this is a vector search query

    public LanceDBRun(DataRuntime runtime) {
        super(runtime);
    }

    public LanceDBRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }

    public LanceDBRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }

    public void addRowCondition(DataRow row) {
        if (null != row) {
            for (String key : row.keySet()) {
                Object value = row.get(key);
                if (null != value) {
                    DefaultAutoCondition cond = new DefaultAutoCondition(Compare.EMPTY_VALUE_SWITCH.IGNORE, Compare.EQUAL, null, key, value);
                    addCondition(cond);
                }
            }
        }
    }

    public void addEntityCondition(Object entity) {
        if (null != entity) {
            Map<String, Object> map = BeanUtil.object2map(entity);
            if (null != map && !map.isEmpty()) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (null != entry.getValue()) {
                        DefaultAutoCondition cond = new DefaultAutoCondition(Compare.EMPTY_VALUE_SWITCH.IGNORE, Compare.EQUAL, null, entry.getKey(), entry.getValue());
                        addCondition(cond);
                    }
                }
            }
        }
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public ConditionChain getConditionChain() {
        return (ConditionChain) super.getConditionChain();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getVectorField() {
        return vectorField;
    }

    public void setVectorField(String vectorField) {
        this.vectorField = vectorField;
    }

    public List<Float> getQueryVector() {
        return queryVector;
    }

    public void setQueryVector(List<Float> queryVector) {
        this.queryVector = queryVector;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public boolean isVectorSearch() {
        return isVectorSearch;
    }

    public void setVectorSearch(boolean vectorSearch) {
        isVectorSearch = vectorSearch;
    }

    @Override
    public String format(String cmd) {
        return cmd;
    }

    public String log(ACTION.DML action, Boolean placeholder) {
        StringBuilder builder = new StringBuilder();
        List<String> keys = null;
        if(null != metadata) {
            builder.append("[").append(metadata.getClass().getSimpleName()).append(":").append(metadata.getName()).append("]");
        }
        if(placeholder) {
            List<Object> values = getValues();
            if(null!= values && !values.isEmpty()) {
                builder.append("\n[param:");
                builder.append(LogUtil.param(keys, getValues()));
                builder.append("];");
            }
        }
        return builder.toString();
    }

    @Override
    public boolean isEmpty() {
        if(null != metadata) {
            return false;
        }
        return true;
    }

}