package org.anyline.entity.geometry;

import java.util.ArrayList;
import java.util.List;

public class GeometryCollection extends Geometry{
    private List<Geometry> list = new ArrayList<>();
    public List<Geometry> list(){
        return list;
    }
    public GeometryCollection add(Geometry geometry){
        list.add(geometry);
        return this;
    }
}
