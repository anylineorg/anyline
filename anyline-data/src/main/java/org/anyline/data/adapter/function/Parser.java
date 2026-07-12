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


package org.anyline.data.adapter.function;

import org.anyline.metadata.*;

public interface Parser {

    Object database();

    <T extends Metadata> T parse(String ddl);

    Table parseTable(String ddl);

    View parseView(String ddl);

    Index parseIndex(String ddl);

    Constraint parseConstraint(String ddl);

    Function parseFunction(String ddl);

    Procedure parseProcedure(String ddl);

    Trigger parseTrigger(String ddl);

    Sequence parseSequence(String ddl);

    /** 提取数据库特有的表级DDL选项（Oracle: TABLESPACE+LOGGING, PG: TABLESPACE, MySQL: ENGINE+AUTO_INCREMENT, MSSQL: ON filegroup） */
    void extractTableOptions(String ddl, Table table);


}