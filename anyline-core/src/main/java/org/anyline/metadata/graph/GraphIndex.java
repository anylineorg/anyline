package org.anyline.metadata.graph;

import org.anyline.metadata.Index;
import org.anyline.metadata.Table;

public class GraphIndex extends Index {

    public GraphIndex(){
        super();
    }
    public GraphIndex(String name){
        super(name);
    }
    public GraphIndex(Table table, String name, boolean unique){
        super(table, name, unique);
    }
    public GraphIndex(Table table, String name){
        super(table, name);
    }
}
