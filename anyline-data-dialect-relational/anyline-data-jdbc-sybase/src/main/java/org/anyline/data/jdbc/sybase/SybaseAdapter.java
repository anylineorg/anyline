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


package org.anyline.data.jdbc.sybase;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.AbstractJDBCAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;

/**
 * Sybase(SAP ASE, T-SQL 同源)<br/>
 * 参考 https://help.sap.com/docs/SAP_ASE<br/>
 * 类型体系与 SQL Server 接近, 分页用 TOP(不支持 LIMIT / OFFSET-FETCH), 标识符用双引号
 */
@AnylineComponent("anyline.data.jdbc.adapter.sybase")
public class SybaseAdapter extends AbstractJDBCAdapter implements JDBCAdapter {
    public DatabaseType type() {
        return DatabaseType.Sybase;
    }

    public SybaseAdapter() {
        super();
        //ASE 标识符引用符
        delimiterFr = "\"";
        delimiterTo = "\"";
        for(SybaseTypeMetadataAlias alias : SybaseTypeMetadataAlias.values()) {
            clear(alias);
        }
        for(SybaseTypeMetadataAlias alias : SybaseTypeMetadataAlias.values()) {
            reg(alias);
            alias(alias.name(), alias.standard());
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
     * ASE 不支持 LIMIT/OFFSET-FETCH, 用 TOP n
     */
    @Override
    public String mergeFinalSelect(DataRuntime runtime, Run run) {
        return pageTop(runtime, run);
    }
}