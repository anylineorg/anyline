package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Schema;
import org.anyline.metadata.graph.EdgeTable;

public class EdgeType extends EdgeTable {
    protected String keyword = "EDGE"            ;
    public EdgeType(){
    }
    public EdgeType(String name){
        super(name);
    }

    public EdgeType(String schema, String table){
        super(schema, table);
    }
    public EdgeType(Schema schema, String table){
        super(schema, table);
    }
    public EdgeType(String catalog, String schema, String name){
        super(catalog, schema, name);
    }
    public EdgeType(Catalog catalog, Schema schema, String name){
        super(catalog, schema, name);
    }

    @Override
    public String getKeyword(){
        return keyword;
    }
}
