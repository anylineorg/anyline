package org.anyline.metadata.graph;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;

import java.io.Serializable;

public class GraphTable extends Table<GraphTable> implements Serializable {

    public GraphTable(){
    }
    public GraphTable(String name){
        super(name);
    }

    public GraphTable(String schema, String table){
        super(schema, table);
    }
    public GraphTable(Schema schema, String table){
        super(schema, table);
    }
    public GraphTable(String catalog, String schema, String name){
        super(catalog, schema, name);
    }
    public GraphTable(Catalog catalog, Schema schema, String name){
        super(catalog, schema, name);
    }
}
