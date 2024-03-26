package org.anyline.metadata.graph;

import org.anyline.metadata.Column;

public class GraphProperty extends Column {

    /**
     * 相关表
     * @param update 是否检测update
     * @return table
     */
    public GraphType getGraphType(boolean update) {
        if(update){
            if(null != table && null != table.getUpdate()){
                return (GraphType) table.getUpdate();
            }
        }
        return (GraphType)table;
    }

    public GraphType getGraphType() {
        return getGraphType(false);
    }

    public void setType(GraphType table) {
        this.table = table;
    }

    public String getGraphTypeName(boolean update) {
        GraphType type = getGraphType(update);
        if(null != type){
            return type.getName();
        }
        return null;
    }

    public String getGraphTypeName() {
        return getGraphTypeName(false);
    }

}
