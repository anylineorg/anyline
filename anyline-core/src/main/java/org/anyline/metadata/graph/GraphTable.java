package org.anyline.metadata.graph;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Column;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.BasicUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphTable extends Table<GraphTable> implements Serializable {

    public GraphTable(){
    }
    public GraphTable(String name){
        if(null != name){
            if(name.contains(":") || name.contains(" ")){
                //自定义XML或sql
                this.name = name;
            }else {
                if (name.contains(".")) {
                    String[] tmps = name.split("\\.");
                    if (tmps.length == 2) {
                        this.schema = new Schema(tmps[0]);
                        this.name = tmps[1];
                    } else if (tmps.length == 3) {
                        this.catalog = new Catalog(tmps[0]);
                        this.schema = new Schema(tmps[1]);
                        this.name = tmps[2];
                    }
                } else {
                    this.name = name;
                }
            }
        }else {
            this.name = name;
        }
    }

    public GraphTable(String schema, String table){
        this(null, schema, table);
    }
    public GraphTable(Schema schema, String table){
        this(null, schema, table);
    }
    public GraphTable(String catalog, String schema, String name){
        if(BasicUtil.isNotEmpty(catalog)) {
            this.catalog = new Catalog(catalog);
        }
        if(BasicUtil.isNotEmpty(schema)) {
            this.schema = new Schema(schema);
        }
        this.name = name;
    }
    public GraphTable(Catalog catalog, Schema schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }


}
