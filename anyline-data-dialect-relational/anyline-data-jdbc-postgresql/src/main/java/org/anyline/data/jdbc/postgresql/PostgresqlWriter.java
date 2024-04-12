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

package org.anyline.data.jdbc.postgresql;

import org.anyline.adapter.DataWriter;
import org.anyline.proxy.ConvertProxy;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.entity.geometry.*;
import org.anyline.util.DateUtil;
import org.postgresql.geometric.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public enum PostgresqlWriter {
    DateWriter(new Object[]{java.sql.Date.class, LocalDate.class}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(!placeholder && null != value) {
                Date date = (Date) ConvertProxy.convert(value, Date.class, false);
                value = " to_date( '"+ DateUtil.format(date)+"', 'YYYY-MM-DD')";
            }
            return value;
        }
    }),
    DateTimeWriter(new Object[]{Timestamp.class, java.util.Date.class, LocalDateTime.class}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(!placeholder && null != value) {
                Date date = (Date) ConvertProxy.convert(value, Date.class, false);
                value = " to_timestamp( '"+ DateUtil.format(date)+"', 'YYYY-MM-DD HH24:MI:SS')";
            }
            return value;
        }
    }),
    PointWriter(new Object[]{Point.class, StandardTypeMetadata.POINT}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
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
        public Object write(Object value, boolean placeholder) {
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
        public Object write(Object value, boolean placeholder) {
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
        public Object write(Object value, boolean placeholder) {
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
    }),
    BoxWriter(new Object[]{Box.class, StandardTypeMetadata.BOX}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Box) {
                Box box = (Box) value;
                PGbox pg = PostgresqlGeometryAdapter.convert(box);
                if (placeholder) {
                    return pg;
                } else {
                    return box.toString(true);
                }
            }
            return value;
        }
    }),
    CircleWriter(new Object[]{Circle.class, StandardTypeMetadata.CIRCLE}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(value instanceof Circle) {
                Circle circle = (Circle) value;
                PGcircle pg = PostgresqlGeometryAdapter.convert(circle);
                if (placeholder) {
                    return pg;
                } else {
                    return circle.toString(true);
                }
            }
            return value;
        }
    })
    ;
    public Object[] supports(){
        return supports;
    }
    public DataWriter writer(){
        return writer;
    }
    private final Object[] supports;
    private final DataWriter writer;
    PostgresqlWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
