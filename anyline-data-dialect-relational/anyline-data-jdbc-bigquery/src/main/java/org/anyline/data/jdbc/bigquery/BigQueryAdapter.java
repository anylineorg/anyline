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


package org.anyline.data.jdbc.bigquery;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.function.SystemFunctionFactory;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.AbstractJDBCAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.type.DatabaseType;

/**
 * Google BigQuery(GoogleSQL)<br/>
 * 参考 https://cloud.google.com/bigquery/docs/reference/standard-sql/<br/>
 * 层级: project.dataset.table(catalog=project, schema=dataset), 标识符用反引号, 分页 LIMIT n OFFSET m
 */
@AnylineComponent("anyline.data.jdbc.adapter.bigquery")
public class BigQueryAdapter extends AbstractJDBCAdapter implements JDBCAdapter {
    public DatabaseType type() {
        return DatabaseType.GoogleBigQuery;
    }

    public BigQueryAdapter() {
        super();
        //BigQuery 标识符用反引号, 如 `project.dataset.table`
        delimiterFr = "`";
        delimiterTo = "`";
        for(BigQueryTypeMetadataAlias alias : BigQueryTypeMetadataAlias.values()) {
            clear(alias);
        }
        for(BigQueryTypeMetadataAlias alias : BigQueryTypeMetadataAlias.values()) {
            reg(alias);
            alias(alias.name(), alias.standard());
        }
        for(BigQueryFunction fn : BigQueryFunction.values()) {
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