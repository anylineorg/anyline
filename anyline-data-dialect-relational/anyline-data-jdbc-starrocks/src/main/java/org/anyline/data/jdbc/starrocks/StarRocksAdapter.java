/*
 * Copyright 2006-2023 www.anyline.org
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

package org.anyline.data.jdbc.starrocks;

import org.anyline.annotation.Component;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.MySQLGenusAdapter;
import org.anyline.metadata.type.DatabaseType;

@Component("anyline.data.jdbc.adapter.starrocks")
public class StarRocksAdapter extends MySQLGenusAdapter implements JDBCAdapter {
    public DatabaseType type() {
        return DatabaseType.StarRocks;
    }

    public StarRocksAdapter() {
        super();
        delimiterFr = "`";
        delimiterTo = "`";
        for (StarRocksTypeMetadataAlias alias: StarRocksTypeMetadataAlias.values()) {
            reg(alias);
            alias(alias.name(), alias.standard());
        }
    }

}
