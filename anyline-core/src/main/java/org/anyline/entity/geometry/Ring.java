package org.anyline.entity.geometry;

import java.util.ArrayList;
import java.util.List;

public class Ring {
    private Boolean clockwise = null; //是否顺时针
    private List<Point> points = new ArrayList<>();
    public Ring add(Point point){
        points.add(point);
        return this;
    }
    public List<Point> points(){
        return points;
    }
    public Ring(){}
    public Ring(List<Point> points) {
        this.points = points;
    }

    public Boolean clockwise() {
        return clockwise;
    }

    public void clockwise(Boolean clockwise) {
        this.clockwise = clockwise;
    }

    public void points(List<Point> points) {
        this.points = points;
    }
}
