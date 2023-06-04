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
    protected int srid;
    protected byte type;

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}