package org.anyline.metadata.graph;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Schema;

import java.io.Serializable;

public class EdgeTable extends GraphTable implements Serializable {

    public EdgeTable(){
    }
    public EdgeTable(String name){
        super(name);
    }

    public EdgeTable(String schema, String table){
        super(schema, table);
    }
    public EdgeTable(Schema schema, String table){
        super(schema, table);
    }
    public EdgeTable(String catalog, String schema, String name){
        super(catalog, schema, name);
    }
    public EdgeTable(Catalog catalog, Schema schema, String name){
        super(catalog, schema, name);
    }
}
