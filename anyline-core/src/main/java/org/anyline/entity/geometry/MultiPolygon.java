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

    public MultiPolygon add(List<Line> polygons){
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
    public List<Polygon> getLines(){
        return polygons;
    }
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("MultiPolygon");
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
    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     * @return String
     */
    public String sql(boolean tag){
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
            builder.append(polygon.sql(false, false));
        }
        builder.append(")");
        return builder.toString();
    }
    public String sql(){
        return sql(true);
    }
}
