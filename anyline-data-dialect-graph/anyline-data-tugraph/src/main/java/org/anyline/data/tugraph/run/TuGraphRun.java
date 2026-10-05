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


package org.anyline.data.tugraph.run;

import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Table;

import java.util.List;

/**
 * TuGraph 命令封装<br/>
 * 参考图数据库(neo4j/nebula)的方式: 不自己合成命令, 只承载 TuGraph 特有的检索参数(向量/topK/过滤条件)<br/>
 * 命令内容由父类 AbstractDriverAdapter.buildSelectRun 统一合成
 */
public class TuGraphRun extends TableRun implements Run {

    private String graph;              // 子图名(TuGraph 一个实例内可有多个子图, 默认 default)

    public TuGraphRun(DataRuntime runtime) {
        super(runtime, (Table) null);
    }

    public TuGraphRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }

    public TuGraphRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    @Override
    public String format(String cmd) {
        return cmd;
    }
}