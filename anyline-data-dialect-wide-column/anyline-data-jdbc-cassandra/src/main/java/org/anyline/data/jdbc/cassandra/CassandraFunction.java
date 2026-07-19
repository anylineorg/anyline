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


package org.anyline.data.jdbc.cassandra;

import org.anyline.data.metadata.function.*;
import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseType;

import java.util.List;

/**
 * Cassandra CQL function mappings.
 * CQL supports a limited set of functions compared to standard SQL.
 * Functions not supported by Cassandra map to empty formula.
 */
public enum CassandraFunction implements SystemFunction {

    /* *****************************************************************************************************************
     *                                              Math Functions
     * ****************************************************************************************************************/
    ABS(MathFunction.ABS, "ABS", "ABS(${expression})"),

    /* *****************************************************************************************************************
     *                                              String Functions
     * ****************************************************************************************************************/

    /* *****************************************************************************************************************
     *                                              Date/Time Functions
     * ****************************************************************************************************************/
    DATE_OF(DateTimeFunction.TO_DATE_FUNC, "dateOf", "dateOf(${date})"),
    UNIX_TIMESTAMP_OF(DateTimeFunction.UNIX_TIMESTAMP, "unixTimestampOf", "unixTimestampOf(${date})"),
    TO_DATE(DateTimeFunction.TO_DATE_FUNC, "toDate", "toDate(${expression})"),

    /* *****************************************************************************************************************
     *                                              NULL Functions
     * ****************************************************************************************************************/
    COALESCE(NullFunction.COALESCE, "COALESCE", "COALESCE(${expression1}, ${expression2})"),

    /* *****************************************************************************************************************
     *                                              UUID Functions
     * ****************************************************************************************************************/
    UUID(UuidFunction.UUID, "uuid", "uuid()"),

    /* *****************************************************************************************************************
     *                                              Aggregate Functions
     * ****************************************************************************************************************/
    COUNT(AggregateFunction.COUNT, "COUNT", "COUNT(${expression})"),
    SUM(AggregateFunction.SUM, "SUM", "SUM(${expression})"),
    AVG(AggregateFunction.AVG, "AVG", "AVG(${expression})"),
    MAX(AggregateFunction.MAX, "MAX", "MAX(${expression})"),
    MIN(AggregateFunction.MIN, "MIN", "MIN(${expression})"),

    /* *****************************************************************************************************************
     *                                              CAST Functions
     * ****************************************************************************************************************/
    CAST(TypeCastFunction.CAST, "CAST", "CAST(${expression} AS ${type})"),

    /* *****************************************************************************************************************
     *                                              System Functions
     * ****************************************************************************************************************/
    ;

    private boolean support = true;
    private List<String> params;
    private String title;
    private final META meta;
    private String formula;

    CassandraFunction(META meta, String title, String formula) {
        this.meta = meta;
        this.title = title;
        this.formula = formula;
    }

    @Override
    public META meta() {
        return meta;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String formula() {
        return formula;
    }

    @Override
    public DatabaseType database() {
        return DatabaseType.Cassandra;
    }

    @Override
    public List<String> params() {
        return params;
    }

    @Override
    public void params(List<String> params) {
        this.params = params;
    }

    @Override
    public boolean support() {
        return support;
    }

    @Override
    public void support(boolean support) {
        this.support = support;
    }
}