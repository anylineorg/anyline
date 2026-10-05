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


package org.anyline.data.jdbc.dremio;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.function.SystemFunctionFactory;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.AbstractJDBCAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;

/**
 * Dremio<br/>
 * 参考 https://docs.dremio.com/current/reference/sql/<br/>
 * 层级: catalog(space/folder).schema.table, 标识符用双引号, 分页 LIMIT n OFFSET m
 */
@AnylineComponent("anyline.data.jdbc.adapter.dremio")
public class DremioAdapter extends AbstractJDBCAdapter implements JDBCAdapter {
    public DatabaseType type() {
        return DatabaseType.Dremio;
    }

    public DremioAdapter() {
        super();
        //Dremio 标识符用双引号
        delimiterFr = "\"";
        delimiterTo = "\"";
        for(DremioTypeMetadataAlias alias : DremioTypeMetadataAlias.values()) {
            clear(alias);
        }
        for(DremioTypeMetadataAlias alias : DremioTypeMetadataAlias.values()) {
            reg(alias);
            alias(alias.name(), alias.standard());
        }
        for(DremioFunction fn : DremioFunction.values()) {
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
     * LIMIT n OFFSET m
     */
    @Override
    public String mergeFinalSelect(DataRuntime runtime, Run run) {
        return pageLimitOffset(runtime, run);
    }
}