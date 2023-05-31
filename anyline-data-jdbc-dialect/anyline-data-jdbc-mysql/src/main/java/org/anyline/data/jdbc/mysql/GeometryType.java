package org.anyline.data.jdbc.mysql;

import org.anyline.entity.geometry.*;

public enum GeometryType {
    point( Point.class),
    LineString( Line.class),
    Polygon(Polygon.class),
    MultiPoint(MultiPoint.class),
    MultiLineString( MultiLine.class),
    MultiPolygon(MultiPolygon.class),
    GeometryCollection(GeometryCollection.class);
    private Class clazz;
    GeometryType(Class clazz){
        this.clazz = clazz;
    }
}
