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

package org.anyline.data.jdbc.postgis;

import org.anyline.adapter.DataWriter;
import org.anyline.data.jdbc.postgresql.PostgresqlGeometryAdapter;
import org.anyline.entity.geometry.Line;
import org.anyline.entity.geometry.LineSegment;
import org.anyline.entity.geometry.LineString;
import org.anyline.entity.geometry.Point;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.postgresql.geometric.PGline;
import org.postgresql.geometric.PGlseg;
import org.postgresql.geometric.PGpath;
import org.postgresql.geometric.PGpoint;

public enum PostgisWriter {
    PointWriter(new Object[]{Point.class, StandardTypeMetadata.POINT}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            if(value instanceof Point) {
                Point point = (Point) value;
                PGpoint pg = PostgresqlGeometryAdapter.convert(point);
                if (placeholder) {
                    return pg;
                } else {
                    return point.toString(true);
                }
            }
            return value;
        }
    }),
    LineSegmentWriter(new Object[]{LineSegment.class, StandardTypeMetadata.LSEG}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            if(value instanceof LineSegment) {
                LineSegment segment = (LineSegment) value;
                PGlseg pg = PostgresqlGeometryAdapter.convert(segment);
                if (placeholder) {
                    return pg;
                } else {
                    return segment.toString(true);
                }
            }
            return value;
        }
    }),
    PathWriter(new Object[]{LineString.class, StandardTypeMetadata.PATH}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            if(value instanceof LineString) {
                LineString string = (LineString) value;
                PGpath pg = PostgresqlGeometryAdapter.convert(string);
                if (placeholder) {
                    return pg;
                } else {
                    return string.toString(true);
                }
            }
            return value;
        }
    }),
    LineWriter(new Object[]{Line.class, StandardTypeMetadata.LINE}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            if(value instanceof Line) {
                Line line = (Line) value;
                PGline pg = PostgresqlGeometryAdapter.convert(line);
                if (placeholder) {
                    return pg;
                } else {
                    return line.toString(true);
                }
            }
            return value;
        }
    })
    ;
    public Object[] supports() {
        return supports;
    }
    public DataWriter writer() {
        return writer;
    }
    private final Object[] supports;
    private final DataWriter writer;
    PostgisWriter(Object[] supports, DataWriter writer) {
        this.supports = supports;
        this.writer = writer;
    }
}
