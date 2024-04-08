package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Table;
import org.anyline.metadata.graph.VertexIndex;

public class TagIndex extends VertexIndex {
    protected String keyword = "TAG INDEX"            ;

    public TagIndex(){
        super();
    }
    public TagIndex(String name){
        super(name);
    }
    public TagIndex(Table table, String name, boolean unique){
        super(table, name, unique);
    }
    public TagIndex(Table table, String name){
        super(table, name);
    }
    @Override
    public String getKeyword(){
        return keyword;
    }
}
