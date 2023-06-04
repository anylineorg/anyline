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
    protected int type;
    protected int endian;

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getEndian() {
        return endian;
    }

    public void setEndian(int endian) {
        this.endian = endian;
    }
}