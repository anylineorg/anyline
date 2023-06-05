package org.anyline.entity.geometry;

import java.util.ArrayList;
import java.util.List;

public class MultiPolygon extends Geometry{

    private List<Polygon> polygons = new ArrayList<>();

    public MultiPolygon(){

    }
    public MultiPolygon(List<Polygon> polygons){
        this.polygons = polygons;
    }
    public MultiPolygon add(Polygon polygon){
        polygons.add(polygon);
        return this;
    }

    public MultiPolygon add(List<Polygon> polygons){
        if(null != polygons) {
            polygons.addAll(polygons);
        }
        return this;
    }
    public MultiPolygon clear(){
        //polygons.clear();
        polygons = new ArrayList<>();
        return this;
    }
    public List<Polygon> polygons(){
        return polygons;
    }
    public String toString(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("MultiPolygon");
        }
        builder.append("(");
        boolean first = true;
        for(Polygon polygon:polygons){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(polygon.toString(false));
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

    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     * @param bracket 是否包含()
     * @return String
     */
    public String sql(boolean tag, boolean bracket){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("MultiPolygon");
        }
        if(bracket) {
            builder.append("(");
        }
        boolean first = true;
        for(Polygon polygon:polygons){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(polygon.sql(false, false));
        }
        if(bracket) {
            builder.append(")");
        }
        return builder.toString();
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }
}
