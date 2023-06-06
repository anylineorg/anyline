package org.anyline.entity.geometry;

public abstract class Geometry {
    public enum Type{
        Point(Point.class),
        LineString(LineString.class),
        Line(Line.class),
        LineSegment(LineSegment.class),
        Polygon(Polygon.class),
        Box(Box.class),
        Circle(Circle.class),
        MultiPoint(MultiPoint.class),
        MultiLine(MultiLine.class),
        MultiPolygon(MultiPolygon.class),
        GeometryCollection(GeometryCollection.class);

        private Class clazz;
        Type(Class clazz){
             this.clazz = clazz;
        }
    }
    protected String tag;
    protected Integer srid = 0;
    protected Integer type;
    protected Byte endian;
    protected Object origin;
    public Integer srid() {
        return srid;
    }

    public void srid(int srid) {
        this.srid = srid;
    }

    public Integer type() {
        return type;
    }

    public void type(int type) {
        this.type = type;
    }

    public Byte endian() {
        return endian;
    }

    public void endian(byte endian) {
        this.endian = endian;
    }
    public void endian(int endian) {
        this.endian = (byte) endian;
    }

    public Object origin() {
        return origin;
    }

    public void origin(Object origin) {
        this.origin = origin;
    }

    public String tag() {
        if(null == tag){
            return this.getClass().getSimpleName();
        }
        return tag;
    }

    public void tag(String tag) {
        this.tag = tag;
    }

    public abstract String toString();
    public abstract String toString(boolean tag);
    public abstract String sql(boolean tag, boolean bracket);
    public abstract String sql();
/*    public abstract String tag(String tag);
    public abstract String tag();*/
}