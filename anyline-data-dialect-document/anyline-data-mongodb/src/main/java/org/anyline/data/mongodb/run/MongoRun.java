/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.mongodb.run;

import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Table;
import org.bson.conversions.Bson;

public class MongoRun extends TableRun implements Run {
    private Bson filter;
    private Bson update;
    public MongoRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }
    public MongoRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }

    public Bson getFilter() {
        return filter;
    }

    public void setFilter(Bson filter) {
        this.filter = filter;
    }

    public Bson getUpdate() {
        return update;
    }

    public void setUpdate(Bson update) {
        this.update = update;
    }
}
