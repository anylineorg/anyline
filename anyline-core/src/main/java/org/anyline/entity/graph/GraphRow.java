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

package org.anyline.entity.graph;

import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GraphRow extends OriginRow implements Serializable {
    private Object pv;
    public GraphRow() {}
    public Object getPrimaryValue() {
        return pv;
    }

    @Override
    public DataRow setPrimaryValue(Object value) {
        pv = value;
        return this;
    }
    protected List<DataRow> nodes = new ArrayList<>();

    public String name() {
        return getTableName();
    }

    public List<DataRow> nodes() {
        return nodes;
    }

    public void nodes(List<DataRow> nodes) {
        this.nodes = nodes;
    }
    public void nodes(DataRow ... nodes) {
        for (DataRow node : nodes) {
            this.nodes.add(node);
        }
    }
    public DataRow attributes() {
        return attributes;
    }
}
