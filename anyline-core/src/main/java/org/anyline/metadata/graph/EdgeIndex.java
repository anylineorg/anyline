package org.anyline.metadata.graph;

import org.anyline.metadata.Table;

public class EdgeIndex extends GraphIndex {

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
}
