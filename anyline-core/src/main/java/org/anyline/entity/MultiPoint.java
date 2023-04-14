package org.anyline.entity;

import java.util.ArrayList;
import java.util.List;

public class MultiPoint {
    private List<Point> points = new ArrayList<>();

    public MultiPoint add(Point point){
        points.add(point);
        return this;
    }
    public MultiPoint add(MultiPoint points){
        if(null != points){
            points.add(points.getPoints());
        }
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
    public List<Point> getPoints(){
        return points;
    }

    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     *             false:((1 1),(2 2))<br/>
     *             true: MULTIPOINT((1 1),(2 2))
     * @return String
     */
    public String sql(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("MULTIPOINT");
        }
        builder.append("(");
        for(Point point:points){
            builder.append(point.sql(false));
        }
        builder.append(")");
        return builder.toString();
    }
    public String sql(){
        return sql(true);
    }
}
