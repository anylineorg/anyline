package org.anyline.entity;

import java.util.ArrayList;
import java.util.List;

public class MultiPoint {
    private List<Point> points = new ArrayList<>();

    public MultiPoint add(Point point){
        points.add(point);
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
}
