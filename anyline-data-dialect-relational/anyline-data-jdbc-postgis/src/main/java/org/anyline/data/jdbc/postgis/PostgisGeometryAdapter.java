/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.data.jdbc.postgis;

import org.postgis.*;

public class PostgisGeometryAdapter {

    public static Object parsePGgeometry(PGgeometry input) {
        Object result = input;
        /*
        1"POINT",
        2"LINESTRING",
        3"POLYGON",
        4"MULTIPOINT",
        5"MULTILINESTRING",
        6"MULTIPOLYGON",
        7"GEOMETRYCOLLECTION"
        */
        if(null != input) {
            Geometry geometry = input.getGeometry();
            int type = input.getGeoType();
            if(type == 1) {
                result = parsePoint((Point) geometry);
            }else if(type == 2) {
                result = parseLineString((LineString) geometry);
            }else if(type == 3) {
                result = parsePolygon((Polygon) geometry);
            }else if(type == 4) {
                result = parseMultiPoint((MultiPoint)geometry);
            }else if(type == 5) {
                result = parseMultiLineString((MultiLineString) geometry);
            }else if(type == 6) {
                result = parseMultiPolygon((MultiPolygon) geometry);
            }else if(type == 7) {
                result = parseGeometryCollection((GeometryCollection) geometry);
            }
        }
        return result;
    }
    /**
     * 点
     * @param input PG原生point
     * @return Point
     */
    public static Object parsePolygon(Polygon input) {
        return input;
    }

    public static Object parseMultiLineString(MultiLineString input) {
        return input;
    }

    public static Object parsePoint(Point input) {
        org.anyline.entity.geometry.Point result = new org.anyline.entity.geometry.Point();
        result.tag("Point");
        try {
            result.srid(input.srid);
            result.x(input.x);
            result.y(input.y);
        }catch (Exception e) {
            return input;
        }
        return result;
    }

    public static Object parsePGgeometryLW(PGgeometryLW input) {
        return input;
    }


    public static Object parsePGbox3d(PGbox3d input) {
        return input;
    }

    public static Object parsePGbox2d(PGbox2d input) {
        return input;
    }

    public static Object parseMultiPolygon(MultiPolygon input) {
        return input;
    }

    public static Object parseMultiPoint(MultiPoint input) {
        org.anyline.entity.geometry.MultiPoint result = new org.anyline.entity.geometry.MultiPoint();
        result.tag("MultiPoint");
        try {
            result.srid(input.srid);
            Point[] points = input.getPoints();
            for(Point point:points) {
                result.add(point.getX(), point.getY());
            }
        }catch (Exception e) {
            return input;
        }
        return result;
    }

    public static Object parseLineString(LineString input) {
        return input;
    }

    public static Object parseGeometryCollection(GeometryCollection input) {
        return input;
    }
}
