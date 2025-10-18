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

import org.anyline.entity.graph.GraphRow;
import org.anyline.util.BasicUtil;

import java.util.List;

public class Neo4jRow extends GraphRow {
    public Neo4jRow() {
        primaryKeys.add("id");
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }

    /**
     * key不要带引号, value用单引号
     * @return json
     */
    public String toJSON(){
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        List<String> keys = keys();
        boolean first = true;
        for (String key : keys) {
            if(!first) {
                builder.append(",");
            }
            first = false;
            builder.append(key).append(":");
            Object value = get(key);
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
        builder.append("}");
        return builder.toString();
    }
}