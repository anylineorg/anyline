package org.anyline.metadata.graph;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Schema;

import java.io.Serializable;

/**
 * 为什么不用VertexType或VertexCollection,因为type容易误会成一个属性,collection容易误会成结果集
 */
public class VertexTable extends GraphTable implements Serializable {

    public VertexTable(){
    }
    public VertexTable(String name){
        super(name);
    }

    public VertexTable(String schema, String table){
        super(schema, table);
    }
    public VertexTable(Schema schema, String table){
        super(schema, table);
    }
    public VertexTable(String catalog, String schema, String name){
        super(catalog, schema, name);
    }
    public VertexTable(Catalog catalog, Schema schema, String name){
        super(catalog, schema, name);
    }
}
