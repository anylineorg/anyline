package org.anyline.metadata.graph;

import org.anyline.metadata.Table;

public class VertexIndex extends GraphIndex {

    public VertexIndex(){
        super();
    }
    public VertexIndex(String name){
        super(name);
    }
    public VertexIndex(Table table, String name, boolean unique){
        super(table, name, unique);
    }
    public VertexIndex(Table table, String name){
        super(table, name);
    }
}
