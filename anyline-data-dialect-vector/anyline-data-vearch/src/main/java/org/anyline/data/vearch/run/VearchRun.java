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


package org.anyline.data.vearch.run;

import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Table;

import java.util.List;

/**
 * Vearch 命令封装<br/>
 * 参考图数据库(neo4j/nebula)的方式: 不自己合成命令, 只承载 Vearch 特有的检索参数(向量/topK/过滤条件)<br/>
 * 命令内容由父类 AbstractDriverAdapter.buildSelectRun 统一合成
 */
public class VearchRun extends TableRun implements Run {

    private List<Float> queryVector;   // 检索向量(ANN 检索)
    private int topK = 10;             // 返回条数(topK/limit/targetHits)
    private String filter;             // 过滤条件(原生表达式或json)

    public VearchRun(DataRuntime runtime) {
        super(runtime, (Table) null);
    }

    public VearchRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }

    public VearchRun(DataRuntime runtime, Table table) {
        super(runtime, table);
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
}