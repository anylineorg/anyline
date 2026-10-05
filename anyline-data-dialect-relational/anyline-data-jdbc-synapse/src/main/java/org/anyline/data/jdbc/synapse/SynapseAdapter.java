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


package org.anyline.data.jdbc.synapse;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.function.SystemFunctionFactory;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.AbstractJDBCAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;

/**
 * Microsoft Azure Synapse Analytics(T-SQL)<br/>
 * 参考 https://learn.microsoft.com/en-us/sql/t-sql/language-reference<br/>
 * 层级: database.schema.table(catalog=database), 标识符用[], 分页必须用 OFFSET n ROWS FETCH NEXT m ROWS ONLY(T-SQL 不支持 LIMIT)
 */
@AnylineComponent("anyline.data.jdbc.adapter.synapse")
public class SynapseAdapter extends AbstractJDBCAdapter implements JDBCAdapter {
    public DatabaseType type() {
        return DatabaseType.AzureSynapse;
    }

    public SynapseAdapter() {
        super();
        //T-SQL 标识符用[]
        delimiterFr = "[";
        delimiterTo = "]";
        for(SynapseTypeMetadataAlias alias : SynapseTypeMetadataAlias.values()) {
            clear(alias);
        }
        for(SynapseTypeMetadataAlias alias : SynapseTypeMetadataAlias.values()) {
            reg(alias);
            alias(alias.name(), alias.standard());
        }
        for(SynapseFunction fn : SynapseFunction.values()) {
            SystemFunctionFactory.reg(type(), fn);
        }
    }

    @Override
    public boolean supportCatalog() {
        return true;
    }

    @Override
    public boolean supportSchema() {
        return true;
    }

    /**
     * T-SQL 不支持 LIMIT, 用 OFFSET n ROWS FETCH NEXT m ROWS ONLY<br/>
     * 注意: OFFSET/FETCH 必须配合 ORDER BY 使用
     */
    @Override
    public String mergeFinalSelect(DataRuntime runtime, Run run) {
        return pageOffsetNext(runtime, run);
    }
}