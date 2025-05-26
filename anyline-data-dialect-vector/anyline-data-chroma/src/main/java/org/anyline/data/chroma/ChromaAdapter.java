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

package org.anyline.data.chroma;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Column;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Type;
import org.anyline.metadata.type.DatabaseType;

import java.util.LinkedHashMap;

@AnylineComponent("anyline.data.adapter.chroma")
public class ChromaAdapter extends AbstractDriverAdapter {
    @Override
    public DatabaseType type() {
        return DatabaseType.Chroma;
    }

    @Override
    public boolean supportCatalog() {
        return false;
    }

    @Override
    public boolean supportSchema() {
        return false;
    }

    @Override
    public String name(Type type) {
        return null;
    }

    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, T meta) {

    }

    @Override
    public LinkedHashMap<String, Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment) {
        return null;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Column query) throws Exception {
        return null;
    }

    @Override
    public String concat(DataRuntime runtime, String... args) {
        return null;
    }
}
