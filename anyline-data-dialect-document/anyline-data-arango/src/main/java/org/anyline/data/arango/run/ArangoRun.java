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

import com.arangodb.entity.BaseDocument;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Table;

import java.util.List;

/**
 * ArangoDB 执行命令封装<br/>
 * 承载 AQL 查询字符串与绑定参数, 以及更新数据/过滤条件等<br/>
 * cmd/vars/updateData/filter 属性定义在父类 AbstractRun 中<br/><br/>
 * Adapter 负责将数据转换为 ArangoDB 原生格式 (BaseDocument) 存入 documents,<br/>
 * 同时保存原始数据引用 (sourceObjects) 用于写回 _key/_id<br/>
 * Actuator 只需从 documents 取出 BaseDocument, 调用 driver 即可
 */
public class ArangoRun extends TableRun implements Run {

    /** Adapter 已转换好的 BaseDocument 列表, Actuator 直接用于调用 driver */
    private List<BaseDocument> documents;
    /** 原始数据对象引用 (与 documents 一一对应), Actuator 用于写回 _key/_id */
    private List<Object> sourceObjects;

    public ArangoRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }
    public ArangoRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }
    public ArangoRun(DataRuntime runtime) {
        super(runtime);
    }

    public List<BaseDocument> getDocuments() {
        return documents;
    }
    public void setDocuments(List<BaseDocument> documents) {
        this.documents = documents;
    }
    public List<Object> getSourceObjects() {
        return sourceObjects;
    }
    public void setSourceObjects(List<Object> sourceObjects) {
        this.sourceObjects = sourceObjects;
    }

    /**
     * ArangoDB INSERT 用 documents (BaseDocument), SELECT 用 builder (AQL)<br/>
     * 覆写父类的默认 false, 根据实际情况判断
     */
    @Override
    public boolean checkValid() {
        if(null != documents && !documents.isEmpty()) {
            return true;
        }
        if(null != getBuilder() && getBuilder().length() > 0) {
            return true;
        }
        return super.checkValid();
    }

    @Override
    public boolean isEmpty() {
        if(null != documents && !documents.isEmpty()) {
            return false;
        }
        return super.isEmpty();
    }
}