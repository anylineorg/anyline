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

package org.anyline.data.elasticsearch.metadata;

import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import java.util.LinkedHashMap;

public class ElasticSearchBuilder {
    public static LinkedHashMap<String, Object> build(Table table) {
        LinkedHashMap<String, Object> map =null;
        if(table instanceof ElasticSearchIndex) {
            ElasticSearchIndex index = (ElasticSearchIndex) table;
            map = index.map();
        }else{
            map = new LinkedHashMap<>();
        }
        LinkedHashMap<String, Column> columns = table.getColumns();
        if(null != columns && !columns.isEmpty()) {
            LinkedHashMap<String, Object> mappings = new LinkedHashMap<>();
            LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
            mappings.put("properties", properties);
            for(Column column:columns.values()) {
                properties.put(column.getName(), build(column));
            }
            map.put("mappings", mappings);
        }
        return map;
    }
    public static LinkedHashMap<String, Object> build(Column column) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        String type = column.getFullType(DatabaseType.ElasticSearch);
        Boolean index = column.getIndex();
        Boolean store = column.getStore();
        String analyzer = column.getAnalyzer();
        String searchAnalyzer = column.getSearchAnalyzer();
        String similarity = column.getSimilarity(); //相似度算法
        if(BasicUtil.isNotEmpty(type)) {
            map.put("type", type);
        }
        if(null != index) {
            map.put("index", index);
        }
        if(null != store) {
            map.put("store", store);
        }
        if(BasicUtil.isNotEmpty(analyzer)) {
            map.put("analyzer", analyzer);
        }
        if(BasicUtil.isNotEmpty(searchAnalyzer)) {
            map.put("search_analyzer", searchAnalyzer);
        }
        //向量类型 维度
        if(null != type && type.toLowerCase().contains("vector")) {
            Integer dims = column.getDimension();
            if(null == dims || dims == 0) {
                dims = column.getPrecisionLength();
            }
            if (null != dims && dims > 0) {
                map.put("dims", dims);
            }
        }
        if(BasicUtil.isNotEmpty(similarity)) {
            map.put("similarity", similarity);
        }
        return map;
    }
}