package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Table;
import org.anyline.metadata.graph.EdgeTable;

public class EdgeType extends EdgeTable {
    public EdgeType() {

    }

    public EdgeType(String name) {
        setName(name);
    }

    public EdgeType(String space, String name) {
        setCatalog(space);
        setName(name);
    }

    @Override
    public String getKeyword() {
        return "TAG";
    }
}