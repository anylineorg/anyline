package org.anyline.entity.geometry;

public class Geometry {
    public enum Type{
        point(1, Point.class),
        Line(2, Line.class),
        Polygon(3, Polygon.class),
        MultiPoint(4, MultiPoint.class),
        MultiLine(5, MultiLine.class),
        MultiPolygon(6, MultiPolygon.class),
        GeometryCollection(7, GeometryCollection.class);
        private Integer header;
        private Class clazz;
        Type(Integer header, Class clazz){
            this.header = header;
            this.clazz = clazz;
        }
        public int getHeader(){
            return header;
        }
    }
}