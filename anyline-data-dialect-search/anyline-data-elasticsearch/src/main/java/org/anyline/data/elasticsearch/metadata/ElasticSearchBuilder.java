package org.anyline.data.elasticsearch.metadata;


import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;

import java.util.LinkedHashMap;

public class ElasticSearchBuilder {
    public static LinkedHashMap<String, Object> build(Table table){
        LinkedHashMap<String, Object> map =null;
        if(table instanceof ElasticSearchIndex){
            ElasticSearchIndex index = (ElasticSearchIndex) table;
            map = index.map();
        }else{
            map = new LinkedHashMap<>();
        }
        LinkedHashMap<String, Column> columns = table.getColumns();
        if(null != columns && !columns.isEmpty()){
            LinkedHashMap<String, Object> mappings = new LinkedHashMap<>();
            LinkedHashMap<String, Object> properties = new LinkedHashMap<>();
            mappings.put("properties", properties);
            for(Column column:columns.values()){
                properties.put(column.getName(), build(column));
            }
        }
        return map;
    }
    public static LinkedHashMap<String, Object> build(Column column){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        String type = column.getFullType(DatabaseType.ElasticSearch);
        Boolean index = column.getIndex();
        Boolean store = column.getStore();
        String analyzer = column.getAnalyzer();
        String searchAnalyzer = column.getSearchAnalyzer();
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
        return map;
    }
}