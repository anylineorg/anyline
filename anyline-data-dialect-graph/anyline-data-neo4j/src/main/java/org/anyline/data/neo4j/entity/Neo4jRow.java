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

package org.anyline.data.neo4j.entity;

import org.anyline.entity.DataRow;
import org.anyline.entity.graph.GraphRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.util.List;

public class Neo4jRow extends GraphRow {
    public Neo4jRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
    public Neo4jRow(DataRow row) {
        this.putAll(row);
    }


    /**
     * key不要带引号, value用单引号
     * @return json
     */
    public String toJSON(){
        List<String> keys = keys();
        return json(keys);
    }
    public String json(String ... keys){
        return json(BeanUtil.array2list(keys));
    }
    public String json(List<String> keys){
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;
        for (String key : keys) {
            if(!first) {
                builder.append(",");
            }
            first = false;
            Object value = get(key);
            builder.append(key).append(":");
            value(builder, value);
        }
        builder.append("}");
        return builder.toString();
    }
    public void value(StringBuilder builder, Object value){
        if(null == value){
            builder.append("null");
        }else if(value instanceof String){
            builder.append("'").append(value).append("'");
        }else if(BasicUtil.isNumber(value)){
            builder.append(value);
        }else if(value instanceof Boolean){
            builder.append(value);
        }else{
            builder.append("'").append(value).append("'");
        }
    }
    public String setValue(String prefix, List<String> keys){
        StringBuilder builder = new StringBuilder();

        if(BasicUtil.isNotEmpty(prefix)){
            prefix = prefix + ".";
        }
        boolean first = true;
        for (String key : keys) {
            if(!first) {
                builder.append(", ");
            }
            first = false;
            Object value = get(key);
            builder.append(prefix).append(key).append(" = ");
            value(builder, value);
        }
        return builder.toString();
    }
}