package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Schema;
import org.anyline.metadata.graph.VertexTable;

public class Tag extends VertexTable {
    protected String keyword = "TAG"            ;
    public Tag(){
    }
    public Tag(String name){
        super(name);
    }

    public Tag(String schema, String table){
        super(schema, table);
    }
    public Tag(Schema schema, String table){
        super(schema, table);
    }
    public Tag(String catalog, String schema, String name){
        super(catalog, schema, name);
    }
    public Tag(Catalog catalog, Schema schema, String name){
        super(catalog, schema, name);
    }

    @Override
    public String getKeyword(){
        return keyword;
    }
}
