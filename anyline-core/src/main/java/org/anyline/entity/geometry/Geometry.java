/*
 * Copyright 2006-2025 www.anyline.org
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
        Type(Class clazz) {
             this.clazz = clazz;
        }
    }
    protected String tag;
    protected Integer srid = 0;
    protected Integer type = 0;
    protected Byte endian = 1;
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
        if(null == tag) {
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