package org.anyline.data.jdbc.postgresql;

import org.anyline.entity.geometry.*;
import org.postgresql.geometric.*;

public class PostgresqlGeometryAdapter {
    /**
     * 点
     * @param pg PG原生point
     * @return Point
     */
    public static Point parsePoint(PGpoint pg){
        Point point = new Point(pg.x, pg.y);
        point.origin(pg);
        return point;
    }
    /**
     * 线段(两个点)
     * @param pg PG原生lseg
     * @return LineSegment
     */
    public static LineSegment parseLineSegment(PGlseg pg){
        PGpoint[] points = pg.point;
        LineSegment segment = new LineSegment(parsePoint(points[0]), parsePoint(points[1]));
        segment.origin(pg);
        return segment;
    }
    /**
     * 线(多个点)
     * @param pg PG原生path
     * @return LineString
     */
    public static LineString parsePath(PGpath pg){
        LineString string = new LineString();
        PGpoint[] points = pg.points;
        for(PGpoint point:points){
            string.add(parsePoint(point));
        }
        string.origin(pg);
        return string;
    }
    /**
     * 多边形
     * @param pg PG原生polygon
     * @return Polygon
     */
    public static Polygon parsePolygon(PGpolygon pg){
        Polygon polygon = new Polygon();
        PGpoint[] points = pg.points;
        //只有一个外环
        Ring ring = new Ring();
        ring.clockwise(false);
        for(PGpoint point:points){
            ring.add(parsePoint(point));
        }
        polygon.add(ring);
        polygon.origin(pg);
        return polygon;
    }
    /**
     * 圆
     * @param pg PG原生circle
     * @return Circle
     */
    public static Circle parseCircle(PGcircle pg){
        Circle circle = new Circle(parsePoint(pg.center), pg.radius);
        circle.origin(pg);
        return circle;
    }

    /**
     * 直线
     * @param pg PG原生line
     * @return Line
     */
    public static Line parseLine(PGline pg){
        Line line = new Line(pg.a, pg.b, pg.c);
        line.origin(pg);
        return line;
    }
    /**
     * 长方形
     * @param pg PG原生box
     * @return Line
     */
    public static Box parseBox(PGbox pg){
        PGpoint[] points = pg.point;
        Box box = new Box(parsePoint(points[0]), parsePoint(points[1]));
        box.origin(pg);
        return box;
    }
}
