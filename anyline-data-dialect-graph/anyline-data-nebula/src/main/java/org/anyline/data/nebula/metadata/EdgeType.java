package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Table;

public class EdgeType extends Table {
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