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

import org.anyline.entity.geometry.*;
import org.postgresql.geometric.*;

import java.util.List;

public class PostgresqlGeometryAdapter {
    /**
     * 点
     * @param pg PG原生point
     * @return Point
     */
    public static Point parsePoint(PGpoint pg) {
        Point point = new Point(pg.x, pg.y);
        point.origin(pg);
        point.tag("Point");
        return point;
    }

    /**
     * 线段(两个点)
     * @param pg PG原生lseg
     * @return LineSegment
     */
    public static LineSegment parseLineSegment(PGlseg pg) {
        PGpoint[] points = pg.point;
        LineSegment segment = new LineSegment(parsePoint(points[0]), parsePoint(points[1]));
        segment.origin(pg);
        segment.tag("Lseg");
        return segment;
    }

    /**
     * 线(多个点)
     * @param pg PG原生path
     * @return LineString
     */
    public static LineString parsePath(PGpath pg) {
        LineString string = new LineString();
        PGpoint[] points = pg.points;
        for(PGpoint point:points) {
            string.add(parsePoint(point));
        }
        string.origin(pg);
        string.tag("Path");
        return string;
    }

    /**
     * 多边形
     * @param pg PG原生polygon
     * @return Polygon
     */
    public static Polygon parsePolygon(PGpolygon pg) {
        Polygon polygon = new Polygon();
        PGpoint[] points = pg.points;
        //只有一个外环
        Ring ring = new Ring();
        ring.clockwise(false);
        for(PGpoint point:points) {
            ring.add(parsePoint(point));
        }
        polygon.add(ring);
        polygon.origin(pg);
        polygon.tag("Polygon");
        return polygon;
    }

    /**
     * 圆
     * @param pg PG原生circle
     * @return Circle
     */
    public static Circle parseCircle(PGcircle pg) {
        Circle circle = new Circle(parsePoint(pg.center), pg.radius);
        circle.origin(pg);
        circle.tag("Circle");
        return circle;
    }

    /**
     * 直线
     * @param pg PG原生line
     * @return Line
     */
    public static Line parseLine(PGline pg) {
        Line line = new Line(pg.a, pg.b, pg.c);
        line.origin(pg);
        line.tag("Line");
        return line;
    }

    /**
     * 长方形
     * @param pg PG原生box
     * @return Line
     */
    public static Box parseBox(PGbox pg) {
        PGpoint[] points = pg.point;
        Box box = new Box(parsePoint(points[0]), parsePoint(points[1]));
        box.origin(pg);
        box.tag("Box");
        return box;
    }

    public static PGpoint convert(Point point) {
        PGpoint pg = new PGpoint(point.x(), point.y());
        return pg;
    }
    public static PGlseg convert(LineSegment segment) {
        PGlseg  pg = new PGlseg(convert(segment.p1()), convert(segment.p2()));
        return pg;
    }
    public static PGpath convert(LineString string) {
        List<Point> points = string.points();
        if(!points.isEmpty()) {
            int size = points.size();
            Point first = points.get(0);
            Point last = points.get(size - 1);
            boolean open = true;
            if(first.x() == last.x() && first.y() == last.y()) {
                open = false;
            }
            PGpoint[] pgs = new PGpoint[size];
            int index = 0;
            for(Point point:points) {
                pgs[index++] = convert(point);
            }
            PGpath pg = new PGpath(pgs, open);
            return pg;
        }
        return new PGpath();
    }
    public static PGline convert(Line line) {
        PGline pg = new PGline(line.a(), line.b(), line.c());
        return pg;
    }
    public static PGbox convert(Box box) {
        PGbox pg = new PGbox(convert(box.p1()), convert(box.p2()));
        return pg;
    }
    public static PGcircle convert(Circle circle) {
        PGcircle pg = new PGcircle(convert(circle.center()), circle.radius());
        return pg;
    }
}
