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
    /**
     * 点
     * @param pg PG原生point
     * @return Point
     */
    public static Object parsePolygon(Polygon pg){
        return pg;
    }

    public static Object parseMultiLineString(MultiLineString pg) {
        return pg;
    }

    public static Object parsePoint(Point pg) {
        return pg;
    }

    public static Object parsePGgeometryLW(PGgeometryLW pg) {
        return pg;
    }

    public static Object parsePGgeometry(PGgeometry pg) {
        return pg;
    }

    public static Object parsePGbox3d(PGbox3d pg) {
        return pg;
    }

    public static Object parsePGbox2d(PGbox2d pg) {
        return pg;
    }

    public static Object parseMultiPolygon(MultiPolygon pg) {
        return pg;
    }

    public static Object parseMultiPoint(MultiPoint pg) {
        return pg;
    }

    public static Object parseLineString(LineString pg) {
        return pg;
    }

    public static Object parseGeometryCollection(GeometryCollection pg) {
        return pg;
    }

    public static org.postgis.Point convert(org.anyline.entity.geometry.Point point){
        org.postgis.Point pg = new org.postgis.Point(point.x(), point.y());
        return pg;
    }
}
