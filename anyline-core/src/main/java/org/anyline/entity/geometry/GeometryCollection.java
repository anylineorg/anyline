package org.anyline.entity.geometry;

import java.util.ArrayList;
import java.util.List;

public class GeometryCollection extends Geometry{
    private List<Geometry> collection = new ArrayList<>();
    public List<Geometry> collection(){
        return collection;
    }
    public GeometryCollection add(Geometry geometry){
        collection.add(geometry);
        return this;
    }
    public String toString(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("GeometryCollection");
        }
        builder.append("(");
        boolean first = true;
        for(Geometry geometry:collection){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(geometry.toString(true));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public String sql() {
        return sql(true, true);
    }

    public List<Geometry> getCollection() {
        return collection;
    }

    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     * @param bracket 是否包含()
     * @return String
     */
    public String sql(boolean tag, boolean bracket){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("GeometryCollection");
        }
        if(bracket) {
            builder.append("(");
        }
        boolean first = true;
        for(Geometry geometry:collection){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(geometry.sql(true, true));
        }
        if(bracket) {
            builder.append(")");
        }
        return builder.toString();
    }
}
