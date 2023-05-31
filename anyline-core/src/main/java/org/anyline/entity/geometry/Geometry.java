package org.anyline.entity.geometry;

public class Geometry {
    public enum Type{
        Point(Point.class),
        Line(Line.class),
        Polygon(Polygon.class),
        MultiPoint(MultiPoint.class),
        MultiLine(MultiLine.class),
        MultiPolygon(MultiPolygon.class),
        GeometryCollection(GeometryCollection.class);

        private Class clazz;
        Type(Class clazz){
             this.clazz = clazz;
        }
    }
}