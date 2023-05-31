package org.anyline.entity.geometry;

import org.anyline.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public class Line extends Geometry {
    private List<Point> points = new ArrayList<>();
    public Line add(Point point){
        points.add(point);
        return this;
    }
    public List<Point> points(){
        return points;
    }
    public Line(){}
    public Line(List<Point> points) {
        this.points = points;
    }
}

