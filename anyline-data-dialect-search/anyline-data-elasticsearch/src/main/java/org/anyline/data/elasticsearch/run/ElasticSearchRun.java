/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.elasticsearch.run;

import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Table;
import org.anyline.util.LogUtil;

import java.util.List;

public class ElasticSearchRun extends TableRun implements Run {
    private String method;
    private String endpoint;

    public ElasticSearchRun(DataRuntime runtime) {
        super(runtime, (Table)null);
    }

    public ElasticSearchRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }

    public ElasticSearchRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    public boolean isEmptyCondition() {
        //不检测更新条件 直接POST
        return false;
    }

    @Override
    public String format(String cmd) {
        //不要删除换行命令中有要求
        return cmd;
    }
    public String log(ACTION.DML action, Boolean placeholder) {
        StringBuilder builder = new StringBuilder();
        List<String> keys = null;
        builder.append("[method:").append(method).append("][endpoint:").append(endpoint).append("]");
        String cmd = null;
        if(action == ACTION.DML.SELECT) {
            cmd = getFinalQuery(placeholder);
        }else if(action == ACTION.DML.COUNT) {
            cmd = getTotalQuery(placeholder);
        }else if(action == ACTION.DML.UPDATE) {
            keys = getUpdateColumns();
            cmd = getFinalUpdate(placeholder);
        }else if(action == ACTION.DML.INSERT) {
            keys = getInsertColumns();
            cmd = getFinalInsert(placeholder);
        }else if(action == ACTION.DML.EXECUTE) {
            cmd = getFinalExecute(placeholder);
        }else if(action == ACTION.DML.DELETE) {
            cmd = getFinalDelete(placeholder);
        }else if(action == ACTION.DML.EXISTS) {
            cmd = getFinalExists(placeholder);
        }
        if(null != cmd && !cmd.isEmpty()) {
            builder.append("[cmd:\n").append(cmd);
            builder.append("\n]");
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
        if(null != endpoint) {
            return false;
        }
        return true;
    }

}
