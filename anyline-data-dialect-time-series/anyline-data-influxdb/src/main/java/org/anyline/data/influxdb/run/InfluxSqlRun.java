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

package org.anyline.data.influxdb.run;

import org.anyline.data.runtime.DataRuntime;

public class InfluxSqlRun extends InfluxRun{
    protected String sql;
    public InfluxSqlRun(DataRuntime runtime, String sql) {
        super(runtime);
        this.sql = sql;
    }

    public InfluxSqlRun sql(String sql) {
        this.sql = sql;
        return this;
    }
    public String sql() {
        return sql;
    }
}
