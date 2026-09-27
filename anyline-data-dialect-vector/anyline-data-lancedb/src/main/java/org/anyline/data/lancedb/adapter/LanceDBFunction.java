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


package org.anyline.data.lancedb.adapter;


import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseType;

import java.util.List;

/**
 * LanceDB function definitions.
 * LanceDB is a vector database and does not support SQL-style functions.
 * This enum provides minimal compatibility.
 */

public enum LanceDBFunction implements SystemFunction {
    ;

    private boolean support = true;
    private List<String> params;

    @Override
    public META meta() {
        return null;
    }

    @Override
    public String title() {
        return name();
    }

    @Override
    public DatabaseType database() {
        return DatabaseType.LanceDB;
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