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


package org.anyline.data.qdrant.adapter;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.qdrant.run.QdrantRun;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Type;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.ConfigTable;

import java.util.LinkedHashMap;

/**
 * Qdrant 适配器<br/>
 * 参考图数据库(neo4j/nebula)的方式: Adapter只负责生成命令/封装Run, 不调用驱动<br/>
 * 真正的调用由 QdrantActuator 通过按官方REST API实现的 QdrantClient 完成
 */
@AnylineComponent("anyline.data.adapter.qdrant")
public class QdrantAdapter extends AbstractDriverAdapter {
    public static final Log log = LogProxy.get(QdrantAdapter.class);

    public QdrantAdapter() {
        super();
    }

    @Override
    public DatabaseType type() {
        return DatabaseType.Qdrant;
    }

    @Override
    public boolean supportCatalog() {
        return false;
    }

    @Override
    public boolean supportSchema() {
        return false;
    }

    public QdrantActuator actuator() {
        return (QdrantActuator) actuator;
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

    /**
     * 参考图数据库(neo4j/nebula)的方式: 不覆盖buildSelectRun<br/>
     * 由父类AbstractDriverAdapter.buildSelectRun统一执行 init(占位符解析/configs/unions/元数据校验) 与 fillSelectContent<br/>
     * 这里只负责决定Run的类型, 用来承载 Qdrant 特有的检索参数(向量/topK/过滤条件)
     */
    @Override
    public Run initSelectRun(DataRuntime runtime, RunPrepare prepare) {
        QdrantRun run = new QdrantRun(runtime, prepare.getTableName());
        run.setRuntime(runtime);
        run.setPrepare(prepare);
        run.action(ACTION.DML.SELECT);
        return run;
    }

    /**
     * 原来只调super(恒返回-1), 这里连通到actuator
     */
    @Override
    public long count(DataRuntime runtime, String random, Run run) {
        try {
            return actuator().count(this, runtime, random, null, run);
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("count 异常:", e);
            }
        }
        return -1;
    }
}