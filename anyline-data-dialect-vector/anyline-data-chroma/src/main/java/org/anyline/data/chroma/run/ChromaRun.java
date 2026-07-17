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


package org.anyline.data.chroma.run;

import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Table;
import org.anyline.util.LogUtil;

import java.util.List;

public class ChromaRun extends TableRun implements Run {

    private String filter;  // Chroma metadata filter expression

    public ChromaRun(DataRuntime runtime) {
        super(runtime, (Table)null);
    }

    public ChromaRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }

    public ChromaRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String format(String cmd) {
        return cmd;
    }

    public String log(ACTION.DML action, Boolean placeholder) {
        StringBuilder builder = new StringBuilder();
        if(null != metadata) {
            builder.append("[").append(metadata.getClass().getSimpleName()).append(":").append(metadata.getName()).append("]");
        }
        if(placeholder) {
            List<Object> values = getValues();
            if(null!= values && !values.isEmpty()) {
                builder.append("\n[param:");
                builder.append(LogUtil.param(null, getValues()));
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