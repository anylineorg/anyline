package org.anyline.entity.geometry;

import java.util.ArrayList;
import java.util.List;

public class MultiPoint extends Geometry{
    private List<Point> points = new ArrayList<>();
    public MultiPoint(){

    }
    public MultiPoint(List<Point> points){
        this.points = points;
    }
    public MultiPoint add(Point point){
        points.add(point);
        return this;
    }

    public MultiPoint add(List<Point> points){
        if(null != points) {
            points.addAll(points);
        }
        return this;
    }
    public MultiPoint clear(){
        //points.clear();
        points = new ArrayList<>();
        return this;
    }
    public List<Point> points(){
        return points;
    }
    public String toString(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("MULTIPOINT");
        }
        builder.append("(");
        boolean first = true;
        for(Point point:points){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(point.toString(false));
        }
        builder.append(")");
        return builder.toString();
    }

    public String toString(){
        return toString(true);
    }
    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     *             false:((1 1),(2 2))<br/>
     *             true: MULTIPOINT((1 1),(2 2))
     * @param bracket 是否包含()
     * @return String
     */
    public String sql(boolean tag, boolean bracket){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("MULTIPOINT");
        }
        if(bracket) {
            builder.append("(");
        }
        boolean first = true;
        for(Point point:points){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(point.sql(false, false));
        }
        if(bracket) {
            builder.append(")");
        }
        return builder.toString();
    }
    public String sql(){
        return sql(true, true);
    }
}
