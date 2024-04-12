package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Table;

public class EdgeIndex extends org.anyline.metadata.graph.EdgeIndex {
    protected String keyword = "EDGE INDEX"            ;

    public EdgeIndex(){
        super();
    }
    public EdgeIndex(String name){
        super(name);
    }
    public EdgeIndex(Table table, String name, boolean unique){
        super(table, name, unique);
    }
    public EdgeIndex(Table table, String name){
        super(table, name);
    }
    @Override
    public String getKeyword(){
        return keyword;
    }
}
