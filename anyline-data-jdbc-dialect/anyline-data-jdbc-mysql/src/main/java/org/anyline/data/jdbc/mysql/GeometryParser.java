package org.anyline.data.jdbc.mysql;

import org.anyline.entity.geometry.*;

import java.util.Hashtable;
import java.util.Map;

public class GeometryParser {

    private static Map<Integer, Geometry.Type> types = new Hashtable<>();
    static {
        types.put(1, Geometry.Type.Point);
        types.put(2, Geometry.Type.Line);
        types.put(3, Geometry.Type.Polygon);
        types.put(4, Geometry.Type.MultiPoint);
        types.put(5, Geometry.Type.MultiLine);
        types.put(6, Geometry.Type.MultiPolygon);
        types.put(7, Geometry.Type.GeometryCollection);
    }
    public static Geometry.Type type(Integer type){
        return types.get(type);
    }
}
