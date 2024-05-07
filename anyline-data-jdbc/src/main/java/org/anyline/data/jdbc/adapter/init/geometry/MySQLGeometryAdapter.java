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



package org.anyline.data.jdbc.adapter.init.geometry;

import org.anyline.entity.geometry.*;
import org.anyline.util.ByteBuffer;
import org.anyline.util.NumberUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class MySQLGeometryAdapter {
    private static Map<Integer, Geometry.Type> types = new Hashtable<>();
    static {
        types.put(1, Geometry.Type.Point);
        types.put(2, Geometry.Type.LineString);
        types.put(3, Geometry.Type.Polygon);
        types.put(4, Geometry.Type.MultiPoint);
        types.put(5, Geometry.Type.MultiLine);
        types.put(6, Geometry.Type.MultiPolygon);
        types.put(7, Geometry.Type.GeometryCollection);
    }
    public static void init(Geometry geometry){
        if(null != geometry){
            if(null == geometry.srid()){
                geometry.srid(0);
            }
            Byte endian = geometry.endian();
            if(null == endian){
                geometry.endian(1);
            }
            Integer type = geometry.type();
            if(null == type){
                if(geometry instanceof Point){
                    type = 1;
                }else if(geometry instanceof LineString){
                    type = 2;
                }else if(geometry instanceof Polygon){
                    type = 3;
                }else if(geometry instanceof MultiPoint){
                    type = 4;
                }else if(geometry instanceof MultiLine){
                    type = 5;
                }else if(geometry instanceof MultiPolygon){
                    type = 6;
                }else if(geometry instanceof GeometryCollection){
                    type = 7;
                }
                geometry.type(type);
            }
        }
    }
    public static Geometry.Type type(Integer type){
        return types.get(type);
    }
    public static String sql(Geometry geometry){
        return null;
    }
    public static String sql(Point point){
        return "Point(" + point.x() + " " + point.y() + ")";
    }
    public static Geometry parse(byte[] bytes){
        Geometry geometry = null;
        //取字节数组的前4个来解析srid
        byte[] srid_bytes = new byte[4];
        System.arraycopy(bytes, 0, srid_bytes, 0, 4);
        //是否大端格式
        byte endian = bytes[4];
        // 解析SRID
        int srid = NumberUtil.byte2int(bytes, 0, 4, endian==0);
        int type = NumberUtil.byte2int(bytes, 5, 4, endian==0);
        if(type == 1){
            geometry = parsePoint(bytes);
        }else if(type == 2){
            geometry = parseLine(bytes);
        }else if(type == 3){
            geometry = parsePolygon(bytes);
        }else if(type == 4){
            geometry = parseMultiPoint(bytes);
        }else if(type == 5){
            geometry = parseMultiLine(bytes);
        }else if(type == 6){
            geometry = parseMultiPolygon(bytes);
        }else if(type == 7){
            geometry = parseGeometryCollection(bytes);
        }
        geometry.endian(endian);
        geometry.srid(srid);
        geometry.type(type);
        geometry.origin(bytes);
        return geometry;
    }
    /*
        POINT(120 36.1)
        bytes[25]:
        00 00 00 00, 01, 01 00 00 00, 00 00 00 00 00 00 5E 40, CD CC CC CC CC 0C 42 40
        component	    size(起-止) decimal hex
        SRID            4(0-3)      0       00 00 00 00
        Endian	        1(4-4)  	1       01(1:小端, 0:大端)
        WKB type	    4(5-8)  	1       01 00 00 00
        X(经度)	        8(9-16) 	120.0   00 00 00 00 00 00 5e 40
        Y(纬度)	        8(17-24)	36.1    cd cc cc cc cc 0c 42 40
    */

    /**
     * 解析 Point
     * @param bytes bytes
     * @return Point
     */
    public static Point parsePoint(byte[] bytes){
        Point point = point(bytes, 9);
        return point;
    }

    public static Point point(byte[] bytes, int offset){
        ByteBuffer buffer = new ByteBuffer(bytes, bytes[4], offset);
        return point(buffer);
    }
    public static Point point(ByteBuffer buffer){
        Point point = new Point(buffer.readDouble(), buffer.readDouble());
        point.tag("Point");
        point.type(1);
        point.endian(1);
        return point;
    }

    /*
        LINESTRING(1 2, 15 15, 11 22)
        bytes[61]:
        00 00 00 00, 01, 02 00 00 00, 03 00 00 00, 00 00 00 00 00 00 F0 3F, 00 00 00 00 00 00 00 40, 00 00 00 00 00 00 2E 40, 00 00 00 00 00 00 2E 40, 00 00 00 00 00 00 26 40 00 00 00 00 00 00 36 40
        component	    size(起-止) decimal  hex
        SRID            4(0-3)      0       00 00 00 00
        Endian	        1(4-4)      1       01
        WKB type	    4(5-8)      1       01 00 00 00
        point count     4(9-12)     3       03 00 00 00
        X(经度)          8(13-20)    1 	    00 00 00 00 00 00 f0 3f
        Y(纬度)          8(21-28)    2 	    00 00 00 00 00 00 00 40
        X(经度)          8(29-36)   15 	    00 00 00 00 00 00 2e 40
        Y(纬度)          8(37-44)   15       00 00 00 00 00 00 2e 40
        X(经度)          8(45-52)   11       00 00 00 00 00 00 2e 40
        Y(纬度)          8(53-60)   22       00 00 00 00 00 00 2e 40
   */

    /**
     * 解析Line
     * @param bytes bytes
     * @return LineString
     */
    public static LineString parseLine(byte[] bytes){
        LineString line = line(bytes, 9);
        return line;
    }

    /**
     *
     * @param bytes bytes
     * @param offset point count的开始位置
     * @return LineString
     */
    public static LineString line(byte[] bytes, int offset){
        ByteBuffer buffer = new ByteBuffer(bytes, bytes[4], offset);
        return line(buffer);
    }
    public static LineString line(ByteBuffer buffer){
        List<Point> points = new ArrayList<>();
        int count = buffer.readInt();
        for(int i=0; i<count; i++){
            Point point = point(buffer);
            points.add(point);
        }
        LineString line = new LineString(points);
        line.tag("LineString");
        line.type(2);
        line.endian(1);
        return line;
    }

    /*

        头部（Header）：
            SRID
            字节顺序（Endian）：表示二进制数据的字节顺序，通常为大端序（Big Endian）或小端序（Little Endian）。
            类型标识符（Type Identifier）：标识几何对象的类型，对于多边形（Polygon）来说，它的值是十六进制的0103。
            环的数量（Number of Rings）：表示多边形中环的数量，包括外部环和内部环（孔）。
        外部环（Exterior Ring）：
            点的数量（Number of Points）：表示构成外部环的点的数量。
            点的坐标（Coordinates）：按照顺序列出外部环中每个点的坐标，每个点的坐标由X和Y值组成。
        内部环（Interior Rings）（可选）：
            点的数量（Number of Points）：表示每个内部环中点的数量。
            点的坐标（Coordinates）：按照顺序列出每个内部环中每个点的坐标，每个点的坐标由X和Y值组成。
        单个环
        POLYGON((121.415703 31.172893, 121.415805 31.172664, 121.416127 31.172751, 121.41603 31.172976, 121.415703 31.172893)
        bytes[97]:
        00 00 00 00, 01, 03 00 00 00, 01 00 00 00, 05 00 00 00, 57 76 C1 E0 9A 5A 5E 40, 13 B5 34 B7 42 2C 3F 40, DA 20 93 8C 9C 5A 5E 40, 51 32 39 B5 33 2C 3F 40, E3 FE 23 D3 A1 5A 5E 40, EF 59 D7 68 39 2C 3F 40, EA 09 4B 3C A0 5A 5E 40, 2E FE B6 27 48 2C 3F 40, 57 76 C1 E0 9A 5A 5E 40, 13 B5 34 B7 42 2C 3F 40
        component        size(起-止) decimal      hex
        SRID            4(0-3)       0            00 00 00 00
        Endian          1(4-4)       1            01
        WKB type        4(5-8)       3            03 00 00 00
        rings count     4(9-12)      1            01 00 00 00
        外部环(注意这里的外部环只能有一个，如果有多个就是MultiPolygon了)
        points count    4(13-16)     5            05 00 00 00
        X(经度)          8(17-24)    121.415703   57 76 C1 E0 9A 5A 5E 40
        Y(纬度)          8(25-32)    31.172893    13 B5 34 B7 42 2C 3F 40
        X(经度)          8(33-40)    121.415805   DA 20 93 8C 9C 5A 5E 40
        Y(纬度)          8(41-48)    31.172664    51 32 39 B5 33 2C 3F 40
        X(经度)          8(49-56)    121.416127   E3 FE 23 D3 A1 5A 5E 40
        Y(纬度)          8(57-64)    31.172751    EF 59 D7 68 39 2C 3F 40
        X(经度)          8(65-72)    121.41603    EA 09 4B 3C A0 5A 5E 40
        Y(纬度)          8(73-80)    31.172976    2E FE B6 27 48 2C 3F 40
        X(经度)          8(81-88)    121.415703   57 76 C1 E0 9A 5A 5E 40
        Y(纬度)          8(89-96)    31.172893    13 B5 34 B7 42 2C 3F 40
*/
    /*
        多个环(含内环)
        POLYGON ((30 20, 45 40, 10 40, 30 20), (20 30, 35 35, 30 20, 20 30), (25 25, 30 35, 15 30, 25 25))
        bytes[217]
        00 00 00 00, 01, 03 00 00 00, 03 00 00 00,
        04 00 00 00, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 00 34 40, 00 00 00 00 00 80 46 40, 00 00 00 00 00 00 44 40, 00 00 00 00 00 00 24 40, 00 00 00 00 00 00 44 40, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 00 34 40,
        04 00 00 00, 00 00 00 00 00 00 34 40, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 80 41 40, 00 00 00 00 00 80 41 40, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 00 34 40, 00 00 00 00 00 00 34 40, 00 00 00 00 00 00 3E 40,
        04 00 00 00, 00 00 00 00 00 00 39 40, 00 00 00 00 00 00 39 40, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 80 41 40, 00 00 00 00 00 00 2E 40, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 00 39 40, 00 00 00 00 00 00 39 40
        component        size(起-止)   decimal      hex
        SRID             4(0-3)       0            00 00 00 00
        Endian           1(4-4)       1            01
        WKB type         4(5-8)       3            03 00 00 00
        rings count      4(9-12)      3            03 00 00 00
        外环(注意这里的外环只能有一个，如果有多个就是MultiPolygon了)
        外环points数量    4(13-16)      4            04 00 00 00
        X(经度)          8(17-24)     30            00 00 00 00 00 00 3E 40
        Y(纬度)          8(25-32)     20            00 00 00 00 00 00 34 40
        X(经度)          8(33-40)     45            00 00 00 00 00 80 46 40
        Y(纬度)          8(41-48)     40            00 00 00 00 00 00 44 40
        X(经度)          8(49-56)     10            00 00 00 00 00 00 24 40
        Y(纬度)          8(57-64)     40            00 00 00 00 00 00 44 40
        X(经度)          8(65-72)     30            00 00 00 00 00 00 3E 40
        Y(纬度)          8(73-80)     20            00 00 00 00 00 00 34 40
        内环
        points count    4(81-84)      4            04 00 00 00
        X(经度)          8(85-92)     20            00 00 00 00 00 00 34 40
        Y(纬度)          8(93-100)    30            00 00 00 00 00 00 3E 40
        X(经度)          8(101-108)   35            00 00 00 00 00 80 41 40
        Y(纬度)          8(109-116)   35            00 00 00 00 00 80 41 40
        X(经度)          8(117-124)   30            00 00 00 00 00 00 3E 40
        Y(纬度)          8(125-132)   20            00 00 00 00 00 00 34 40
        X(经度)          8(133-140)   20            00 00 00 00 00 00 34 40
        Y(纬度)          8(141-148)   30            00 00 00 00 00 00 3E 40
        points count    4(149-152)    4            04 00 00 00
        X(经度)          8(153-160)   25            00 00 00 00 00 00 39 40
        Y(纬度)          8(161-168)   25            00 00 00 00 00 00 39 40
        X(经度)          8(169-176)   30            00 00 00 00 00 00 3E 40
        Y(纬度)          8(177-184)   35            00 00 00 00 00 80 41 40
        X(经度)          8(185-192)   15            00 00 00 00 00 00 2E 40
        Y(纬度)          8(193-200)   30            00 00 00 00 00 00 3E 40
        X(经度)          8(201-208)   25            00 00 00 00 00 00 39 40
        X(经度)          8(209-216)   25            00 00 00 00 00 00 39 40

   */
    /**
     * 解析Polygon
     * @param bytes bytes
     * @return Polygon
     */
    public static Polygon parsePolygon(byte[] bytes){
        Polygon polygon = polygon(bytes, 9);
        return polygon;
    }

    /**
     *
     * @param bytes byte
     * @param offset 环数量开始位置
     * @return Polygon
     */
    public static Polygon polygon(byte[] bytes, int offset){
        ByteBuffer buffer = new ByteBuffer(bytes, bytes[4], offset);
        return polygon(buffer);
    }
    public static Polygon polygon(ByteBuffer buffer){
        Polygon polygon = new Polygon();
        polygon.tag("Polygon");
        polygon.type(3);
        polygon.endian(1);
        int ring_count = buffer.readInt();
        //外环(只有一个)
        //外环中Point数量
        Ring out = ring(buffer);
        out.clockwise(true);
        polygon.add(out);
        if(ring_count > 1){
            //内环(可能有多个)
            for(int r=1; r<ring_count; r++){
                //内环中Point数量
                Ring in = ring(buffer);
                in.clockwise(false);
                polygon.add(in);
            }
        }
        return polygon;
    }
    public static Ring ring(ByteBuffer buffer){
        List<Point> points = new ArrayList<>();
        int point_count = buffer.readInt();
        for(int p=0; p<point_count; p++){
            points.add(point(buffer));
        }
        Ring ring = new Ring(points);
        return ring;
    }
    /*
    MULTIPOINT(30 20, 25 25, 55 85)
    byte[76]
    00 00 00 00, 01, 04 00 00 00, 03 00 00 00,
    (01, 01 00 00 00, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 00 34 40),
    (01, 01 00 00 00, 00 00 00 00 00 00 39 40, 00 00 00 00 00 00 39 40),
    (01, 01 00 00 00, 00 00 00 00 00 80 4B 40, 00 00 00 00 00 40 55 40)

    component        size(起-止)   decimal       hex
    SRID              4(0-3)       0            00 00 00 00
    Endian            1(4-4)       1            01
    WKB type          4(5-8)       4(MultiPoint)04 00 00 00
    points count     4(9-12)       3            03 00 00 00
    Endian          1(13-13)       1            01
    WKB type        4(14-17)       1(point)     01 00 00 00(好像也没别的值可选，有点多余)
    X(经度)          8(18-25)      30            00 00 00 00 00 00 3E 40
    Y(纬度)          8(26-33)      20            00 00 00 00 00 00 34 40
    Endian          1(34-34)       1            01
    WKB type        4(35-38)       1(point)     01 00 00 00
    X(经度)          8(39-46)      25            00 00 00 00 00 00 39 40
    Y(纬度)          8(47-54)      25            00 00 00 00 00 00 39 40
    Endian          1(55-55)       1            01
    WKB type        4(56-59)       1(point)     01 00 00 00
    X(经度)          8(60-67)      55            00 00 00 00 00 80 4B 40
    Y(纬度)          8(68-75)      85            00 00 00 00 00 40 55 40
     */
    /**
     * 解析MultiPoint
     * @param bytes bytes
     * @return MultiPoint
     */
    public static MultiPoint parseMultiPoint(byte[] bytes){
        ByteBuffer buffer = new ByteBuffer(bytes, bytes[4], 9);
        return multiPoint(buffer);
    }
    public static MultiPoint multiPoint(ByteBuffer buffer){
        //点数量
        int count = buffer.readInt();
        List<Point> points = new ArrayList<>();
        for(int i=0; i<count; i++){
            //跳过 Endian(1位)和 WKB type(4位)
            buffer.step(5);
            points.add(point(buffer));
        }
        MultiPoint multiPoint = new MultiPoint(points);
        multiPoint.tag("MultiPoint");
        multiPoint.type(4);
        multiPoint.endian(1);
        return multiPoint;
    }
    /*
    两条线段，每条线段3个点
    MULTILINESTRING((120 36.1, 120 36.2, 120 36.3), (121 36.1, 121 36.2, 121 36.3))
    byte[127]
    00 00 00 00, 01, 05 00 00 00, 02 00 00 00,
    01, 02 00 00 00, 03 00 00 00, 00 00 00 00 00 00 5E 40(x), CD CC CC CC CC 0C 42 40(y), 00 00 00 00 00 00 5E 40, 9A 99 99 99 99 19 42 40, 00 00 00 00 00 00 5E 40, 66 66 66 66 66 26 42 40,
    01, 02 00 00 00, 03 00 00 00, 00 00 00 00 00 40 5E 40(x), CD CC CC CC CC 0C 42 40(y), 00 00 00 00 00 40 5E 40, 9A 99 99 99 99 19 42 40, 00 00 00 00 00 40 5E 40, 66 66 66 66 66 26 42 40

    component        size(起-止)   decimal       hex
    SRID              4(0-3)       0            00 00 00 00
    Endian            1(4-4)       1            01
    WKB type          4(5-8)       5            05 00 00 00
    line count       4(9-12)       2            02 00 00 00
    Endian          1(13-13)       1            01(第0条)
    WKB type        4(14-17)       1(line)      02 00 00 00(好像也没别的值可选，有点多余)
    point count     4(18-21)       3            03 00 00 00(第0条线段3个点)
    X(经度)          8(22-29)      120           00 00 00 00 00 00 5E 40
    Y(纬度)          8(30-37)      36.1          CD CC CC CC CC 0C 42 40
    X(经度)          8(38-45)      120           00 00 00 00 00 00 5E 40
    Y(纬度)          8(46-53)      36.2          9A 99 99 99 99 19 42 40
    X(经度)          8(54-61)      120           00 00 00 00 00 00 5E 40
    Y(纬度)          8(62-69)      36.3          66 66 66 66 66 26 42 40
    Endian          1(70-70)       1            01(第1条)
    WKB type        4(71-74)       1(line)      02 00 00 00(好像也没别的值可选，有点多余)
    point count     4(75-79)       3            03 00 00 00(第0条线段3个点)
    X(经度)          8(80-87)      121           00 00 00 00 00 40 5E 40
    Y(纬度)          8(88-95)      36.1          CD CC CC CC CC 0C 42 40
    X(经度)          8(96-103)     121           00 00 00 00 00 40 5E 40
    Y(纬度)          8(104-111)    36.2          9A 99 99 99 99 19 42 40
    X(经度)          8(112-119)    121           00 00 00 00 00 40 5E 40
    Y(纬度)          8(120-127)    36.3          66 66 66 66 66 26 42 40

    * */
    /**
     * 解析MultiLine
     * @param bytes bytes
     * @return MultiPoint
     */
    public static MultiLine parseMultiLine(byte[] bytes){
        ByteBuffer buffer = new ByteBuffer(bytes, bytes[4], 9);
        return multiLine(buffer);
    }
    public static MultiLine multiLine(ByteBuffer buffer){
        //线段数量
        int line_count = buffer.readInt();
        List<LineString> lines = new ArrayList<>();
        for(int l=0; l<line_count; l++){
            //跳过 Endian(1位)和 WKB type(4位)
            buffer.step(5);
            lines.add(line(buffer));
        }
        MultiLine multiLine = new MultiLine(lines);
        multiLine.tag("MultiLine");
        multiLine.type(5);
        multiLine.endian(1);
        return multiLine;
    }
    /*
    SRID(4 byte) + Endian (1 byte) + WKB type (4 bytes) + Number of polygons (4 bytes) + Polygon 1 + Polygon 2 + ... + Polygon n
    其中，Endian指定字节顺序（大端或小端），WKB type表示几何类型（0x0606），Number of polygons表示该MultiPolygon包含的多边形数量，后面跟着每个多边形的WKB结构。

    每个多边形的WKB结构如下：
    Endian (1 byte) + WKB type (4 bytes) + Number of rings (4 bytes) + Exterior ring + Interior ring 1 + Interior ring 2 + ... + Interior ring n
    其中，Endian和WKB type与MultiPolygon相同，Number of rings表示该多边形包含的环数（通常为1个外环和若干个内环），后面跟着每个环的WKB结构。

    每个环的WKB结构如下：
    Endian (1 byte) + WKB type (4 bytes) + Number of points (4 bytes) + Point 1 + Point 2+ ...+ Point n
    其中，Endian和WKB type与前两者相同，Number of points表示该环包含的点数，后面跟着每个点的坐标信息（通常为二维平面坐标或三维空间坐标）。
    需要注意的是，MySQL中的WKB结构采用了标准的OGC格式，但字节顺序与大多数数据库和编程语言不同。因此，在使用MySQL WKB时需要进行字节顺序转换。

    带有内环的MultiPolygon, 由两个多边形组成的集合，其中第一个多边形包含了一个内环。
    MULTIPOLYGON (((0 0, 0 10, 10 10, 10 0, 0 0), (2 2, 2 8, 8 8, 8 2, 2 2)), ((15 15, 15 20, 20 20, 20 15, 15 15)))
    byte[283]
    00 00 00 00, 01, 06 00 00 00, 02 00 00 00,
    面0
    01, 03 00 00 00, 02 00 00 00,
    外环
    05 00 00 00, 00 00 00 00 00 00 00 00(x1), 00 00 00 00 00 00 00 00(y1), 00 00 00 00 00 00 00 00(x2), 00 00 00 00 00 00 24 40(y2), 00 00 00 00 00 00 24 40(x3), 00 00 00 00 00 00 24 40(y3), 00 00 00 00 00 00 24 40(x4), 00 00 00 00 00 00 00 00(y4), 00 00 00 00 00 00 00 00(x5), 00 00 00 00 00 00 00 00(y5)
    内环
    05 00 00 00, 00 00 00 00 00 00 00 40(x1), 00 00 00 00 00 00 00 40(y1), 00 00 00 00 00 00 00 40(x2), 00 00 00 00 00 00 20 40(y2), 00 00 00 00 00 00 20 40(y3), 00 00 00 00 00 00 20 40(y3), 00 00 00 00 00 00 20 40(x4), 00 00 00 00 00 00 00 40(y4), 00 00 00 00 00 00 00 40(x5), 00 00 00 00 00 00 00 40(y5),
    面1
    01, 03 00 00 00, 01 00 00 00, 05 00 00 00,
    00 00 00 00 00 00 2E 40, 00 00 00 00 00 00 2E 40, 00 00 00 00 00 00 2E 40, 00 00 00 00 00 00 34 40, 00 00 00 00 00 00 34 40, 00 00 00 00 00 00 34 40, 00 00 00 00 00 00 34 40, 00 00 00 00 00 00 2E 40, 00 00 00 00 00 00 2E 40 00 00 00 00 00 00 2E 40

    component        size(起-止)   decimal         hex                          comment
    SRID              4(0-3)       0              00 00 00 00
    Endian            1(4-4)       1              01
    WKB type          4(5-8)       6              06 00 00 00                  MultiPolygon
    polygon count    4(9-12)       2              02 00 00 00                  共2个直接子元素(Polygon)

    面0
    Endian          1(13-13)       1              01                           针对第0个直接子元素(Polygon)
    WKB type        4(14-17)       3              03 00 00 00                  第0个直接子元素(Polygon)类型:Polygon
    ring count      4(18-21)       2              02 00 00 00                  第0个Polygon中共2个环

    面0外环
    point count     4(22-25)      5              05 00 00 00                  第0个Polygon外环点数量
    X1(经度)         8(26-33)      0              00 00 00 00 00 00 00 00
    Y1(纬度)         8(34-41)      0              00 00 00 00 00 00 00 00
    X2(经度)         8(42-49)      0              00 00 00 00 00 00 00 00
    Y2(纬度)         8(50-57)      10             00 00 00 00 00 00 24 40
    X3(经度)         8(58-65)      10             00 00 00 00 00 00 24 40
    Y3(纬度)         8(66-73)      10             00 00 00 00 00 00 24 40
    X4(经度)         8(74-81)      10             00 00 00 00 00 00 24 40
    Y4(纬度)         8(82-89)      0              00 00 00 00 00 00 24 40
    X5(经度)         8(90-97)      0              00 00 00 00 00 00 00 00
    Y6(纬度)         8(98-105)     0              00 00 00 00 00 00 00 00
    面0内环0
    point count     4(106-109)    5              05 00 00 00                  第0个Polygon第0个内环点数量
    X1(经度)         8(110-117)    2              00 00 00 00 00 00 00 40
    Y1(纬度)         8(118-125)    2              00 00 00 00 00 00 00 40
    X2(经度)         8(126-133)    2              00 00 00 00 00 00 00 40
    Y2(纬度)         8(134-141)    8              00 00 00 00 00 00 20 40
    X3(经度)         8(142-149)    8              00 00 00 00 00 00 20 40
    Y3(纬度)         8(150-157)    8              00 00 00 00 00 00 20 40
    X4(经度)         8(158-165)    8              00 00 00 00 00 00 20 40
    Y4(纬度)         8(166-173)    2              00 00 00 00 00 00 00 40
    X5(经度)         8(174-181)    2              00 00 00 00 00 00 00 40
    Y5(纬度)         8(182-189)    2              00 00 00 00 00 00 00 40
    面1
    Endian          1(190-190)    1              01                           针对第1个直接子元素(Polygon)
    WKB type        4(191-194)    3              03 00 00 00                  第1个直接子元素(Polygon)类型:Polygon
    ring count      4(195-198)    1              01 00 00 00                  第1个Polygon中共2个环
    面1外环
    point count     4(199-202)    5              05 00 00 00                  第0个Polygon外环点数量
    X1(经度)         8(203-210)   15              00 00 00 00 00 00 2E 40
    Y1(纬度)         8(211-218)   15              00 00 00 00 00 00 2E 40
    X2(经度)         8(219-226)   15              00 00 00 00 00 00 2E 40
    Y2(纬度)         8(227-234)   20              00 00 00 00 00 00 34 40
    X3(经度)         8(235-242)   20              00 00 00 00 00 00 34 40
    Y3(纬度)         8(243-250)   20              00 00 00 00 00 00 34 40
    X4(经度)         8(251-258)   20              00 00 00 00 00 00 34 40
    Y4(纬度)         8(259-266)   15              00 00 00 00 00 00 2E 40
    X5(经度)         8(267-274)   15              00 00 00 00 00 00 2E 40
    Y5(纬度)         8(275-282)   15              00 00 00 00 00 00 2E 40
    */
    /**
     * 解析MultiPolygon
     * @param bytes bytes
     * @return MultiPolygon
     */
    public static MultiPolygon parseMultiPolygon(byte[] bytes){
        ByteBuffer buffer = new ByteBuffer(bytes, bytes[4], 9);
        return multiPolygon(buffer);
    }
    public static MultiPolygon multiPolygon(ByteBuffer buffer){
        //面数量
        int polygon_count = buffer.readInt();
        List<Polygon> polygons = new ArrayList<>();
        for(int py=0; py<polygon_count; py++){
            //跳过Endian(1)+WKB type(4)
            buffer.step(5);
            Polygon polygon = polygon(buffer);
            polygons.add(polygon);
        }
        MultiPolygon multiPolygon = new MultiPolygon(polygons);
        multiPolygon.tag("MultiPolygon");
        multiPolygon.type(6);
        multiPolygon.endian(1);
        return multiPolygon;
    }
/*
GEOMETRYCOLLECTION(
    POINT(120 36.1),
    LINESTRING(120 36.1, 120 36.2, 120 36.3),
    MULTIPOLYGON(
        ((0 0, 0 10, 10 10, 10 0, 0 0), (2 2, 2 8, 8 8, 8 2, 2 2)),
        ((15 15, 15 20, 20 20, 20 15, 15 15))
    )
)

byte[370]
00 00 00 00, 01, 07 00 00 00, 03 00 00 00,
01, 01 00 00 00, 00 00 00 00 00 00 5E 40, CD CC CC CC CC 0C 42 40,
01, 02 00 00 00, 03 00 00 00, 00 00 00 00 00 00 5E 40, CD CC CC CC CC 0C 42 40, 00 00 00 00 00 00 5E 40, 9A 99 99 99 99 19 42 40, 00 00 00 00 00 00 5E 40, 66 66 66 66 66 26 42 40,
01, 06 00 00 00, 02 00 00 00,
    01, 03 00 00 00, 02 00 00 00, 05 00 00 00,
    00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 24 40 00 00 00 00 00 00 24 40 00 00 00 00 00 00 24 40 00 00 00 00 00 00 24 40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 00 00 00 40 00 00 00 00 00 00 00 40 00 00 00 00 00 00 00 40 00 00 00 00 00 00 20 40 00 00 00 00 00 00 20 40 00 00 00 00 00 00 20 40 00 00 00 00 00 00 20 40 00 00 00 00 00 00 00 40 00 00 00 00 00 00 00 40 00 00 00 00 00 00 00 40 01 03 00 00 00 01 00 00 00 05 00 00 00 00 00 00 00 00 00 2E 40 00 00 00 00 00 00 2E 40 00 00 00 00 00 00 2E 40 00 00 00 00 00 00 34 40 00 00 00 00 00 00 34 40 00 00 00 00 00 00 34 40 00 00 00 00 00 00 34 40 00 00 00 00 00 00 2E 40 00 00 00 00 00 00 2E 40 00 00 00 00 00 00 2E 40
Byte Order (1 byte): 用于指示字节顺序的字节，常见的取值为 0 表示大端字节序，1 表示小端字节序。
Geometry Type (4 bytes): 用于表示 GeometryCollection 对象的类型，对应于几何类型代码。在 WKB 格式中，GeometryCollection 对象的类型代码为 7。
Num Geometries (4 bytes): 用于表示 GeometryCollection 中包含的几何对象的数量。
Geometry 1, Geometry 2, ..., Geometry N: 表示 GeometryCollection 中的每个几何对象的 WKB 表示。

    component        size(起-止)   decimal         hex                          comment
    SRID              4(0-3)       0              00 00 00 00
    Endian            1(4-4)       1              01
    WKB type          4(5-8)       7              07 00 00 00                  GeometryCollection
    Geometry count    4(9-12)      2              03 00 00 00                  包含3个直接子元素:1个点, 一条线, 一个多面(多个多边形)

    第0个直接子元素:Point
    Endian          1(13-13)       1              01
    WKB type        4(14-17)       1              01 00 00 00                  Point
    X               8(18-25)       120            00 00 00 00 00 00 5E 40
    Y               8(26-33)       36.1           CD CC CC CC CC 0C 42 40

    第1个直接子元素:LINESTRING
    Endian          1(34-34)       1              01
    WKB type        4(35-38)       2              02 00 00 00                  LINESTRING
    Point count     4(39-42)       3              03 00 00 00                  包含3个点
    X               8(43-50)       120            00 00 00 00 00 00 5E 40
    Y               8(51-58)       36.1           CD CC CC CC CC 0C 42 40
    X               8(59-66)       120            00 00 00 00 00 00 5E 40
    Y               8(67-74)       36.2           9A 99 99 99 99 19 42 40
    X               8(75-82)       120            00 00 00 00 00 00 5E 40
    Y               8(83-90)       36.3           66 66 66 66 66 26 42 40

    第2个直接子元素:MULTIPOLYGON
    Endian            1(91-91)     1              01
    WKB type          4(92-95)     6              06 00 00 00                  MultiPolygon
    polygon count     4(96-99)     2              02 00 00 00                  共2个直接子元素(Polygon)

    面0
    Endian          1(100-100)     1              01                           针对第0个直接子元素(Polygon)
    WKB type        4(101-104)     3              03 00 00 00                  第0个直接子元素(Polygon)类型:Polygon
    ring count      4(105-108)     2              02 00 00 00                  第0个Polygon中共2个环

    面0外环
    point count     4(109-112)    5              05 00 00 00                  第0个Polygon外环点数量
    X1(经度)         8(113-120)    0              00 00 00 00 00 00 00 00
    Y1(纬度)         8(121-128)    0              00 00 00 00 00 00 00 00
    X2(经度)         8(129-136)    0              00 00 00 00 00 00 00 00
    Y2(纬度)         8(137-144)    10             00 00 00 00 00 00 24 40
    X3(经度)         8(145-152)    10             00 00 00 00 00 00 24 40
    Y3(纬度)         8(153-160)    10             00 00 00 00 00 00 24 40
    X4(经度)         8(161-168)    10             00 00 00 00 00 00 24 40
    Y4(纬度)         8(169-176)    0              00 00 00 00 00 00 24 40
    X5(经度)         8(177-184)    0              00 00 00 00 00 00 00 00
    Y6(纬度)         8(185-192)    0              00 00 00 00 00 00 00 00
    面0内环0
    point count     4(193-196)    5              05 00 00 00                  第0个Polygon第0个内环点数量
    X1(经度)         8(197-204)    2              00 00 00 00 00 00 00 40
    Y1(纬度)         8(205-212)    2              00 00 00 00 00 00 00 40
    X2(经度)         8(213-220)    2              00 00 00 00 00 00 00 40
    Y2(纬度)         8(221-228)    8              00 00 00 00 00 00 20 40
    X3(经度)         8(229-236)    8              00 00 00 00 00 00 20 40
    Y3(纬度)         8(235-244)    8              00 00 00 00 00 00 20 40
    X4(经度)         8(245-252)    8              00 00 00 00 00 00 20 40
    Y4(纬度)         8(253-260)    2              00 00 00 00 00 00 00 40
    X5(经度)         8(261-268)    2              00 00 00 00 00 00 00 40
    Y5(纬度)         8(269-276)    2              00 00 00 00 00 00 00 40
    面1
    Endian          1(277-277)    1              01                           针对第1个直接子元素(Polygon)
    WKB type        4(278-281)    3              03 00 00 00                  第1个直接子元素(Polygon)类型:Polygon
    ring count      4(282-285)    1              01 00 00 00                  第1个Polygon中共2个环
    面1外环
    point count     4(286-289)    5              05 00 00 00                  第0个Polygon外环点数量
    X1(经度)         8(290-297)   15              00 00 00 00 00 00 2E 40
    Y1(纬度)         8(298-305)   15              00 00 00 00 00 00 2E 40
    X2(经度)         8(306-313)   15              00 00 00 00 00 00 2E 40
    Y2(纬度)         8(314-321)   20              00 00 00 00 00 00 34 40
    X3(经度)         8(322-329)   20              00 00 00 00 00 00 34 40
    Y3(纬度)         8(330-337)   20              00 00 00 00 00 00 34 40
    X4(经度)         8(338-345)   20              00 00 00 00 00 00 34 40
    Y4(纬度)         8(346-353)   15              00 00 00 00 00 00 2E 40
    X5(经度)         8(354-361)   15              00 00 00 00 00 00 2E 40
    Y5(纬度)         8(362-369)   15              00 00 00 00 00 00 2E 40

 */

    /**
     * 解析MGeometryCollection
     * @param bytes bytes
     * @return GeometryCollection
     */
    public static GeometryCollection parseGeometryCollection(byte[] bytes){
        GeometryCollection collection = new GeometryCollection();
        ByteBuffer buffer = new ByteBuffer(bytes, bytes[4], 9);
        //Geometry count
        int geometryCount = buffer.readInt();
        for(int g=0; g<geometryCount; g++){
            //Endian
            buffer.step(1);
            //WKB type
            int type = buffer.readInt();
            Geometry geometry = null;
            if(type == 1){
                geometry = point(buffer);
            }else if(type == 2){
                geometry = line(buffer);
            }else if(type == 3){
                geometry = polygon(buffer);
            }else if(type == 4){
                geometry = multiPoint(buffer);
            }else if(type == 5){
                geometry = multiLine(buffer);
            }else if(type == 6){
                geometry = multiPolygon(buffer);
            }
            if(null != geometry) {
                collection.add(geometry);
            }
        }
        collection.tag("GeometryCollection");
        collection.type(7);
        collection.endian(1);

        return collection;
    }

    /**
     * 生成wkb格式要
     * @param geometry geometry
     * @return bytes
     */
    public static byte[] wkb(Geometry geometry){
        if(geometry instanceof Point){
            return wkb((Point)geometry);
        }else if(geometry instanceof LineString){
            return wkb((LineString)geometry);
        }else if(geometry instanceof Polygon){
            return wkb((Polygon)geometry);
        }else if(geometry instanceof MultiPoint){
            return wkb((MultiPoint)geometry);
        }else if(geometry instanceof MultiLine){
            return wkb((MultiLine)geometry);
        }else if(geometry instanceof MultiPolygon){
            return wkb((MultiPolygon)geometry);
        }else if(geometry instanceof GeometryCollection){
            return wkb((GeometryCollection)geometry);
        }
        return null;
    }

    public static void wkb(ByteBuffer buffer, Geometry geometry, boolean head){
        if(geometry instanceof Point){
            wkb(buffer, (Point)geometry, head);
        }else if(geometry instanceof LineString){
            wkb(buffer, (LineString)geometry, head);
        }else if(geometry instanceof Polygon){
            wkb(buffer, (Polygon)geometry, head);
        }else if(geometry instanceof MultiPoint){
            wkb(buffer, (MultiPoint)geometry, head);
        }else if(geometry instanceof MultiLine){
            wkb(buffer, (MultiLine)geometry, head);
        }else if(geometry instanceof MultiPolygon){
            wkb(buffer, (MultiPolygon)geometry, head);
        }
    }
    /*public static void head(ByteBuffer buffer, Geometry geometry){
        buffer.put(geometry.srid());
        buffer.put((byte) geometry.endian());
        buffer.put(geometry.type());
    }*/

    public static byte[] wkb(Point point){
        init(point);
        ByteBuffer buffer = new ByteBuffer(25, point.endian());
        buffer.put(point.srid());
        wkb(buffer, point, true);
        byte[] bytes = buffer.bytes();
        return bytes;
    }
    public static void wkb(ByteBuffer buffer, Point point, boolean head){
        if(head){
            buffer.put((byte) point.endian());
            buffer.put(point.type());
        }
        buffer.put(point.x());
        buffer.put(point.y());
    }
    public static byte[] wkb(LineString line){
        List<Point> points = line.points();
        ByteBuffer buffer = new ByteBuffer(points.size()*16+13, line.endian());
        buffer.put(line.srid());
        wkb(buffer, line, true);
        byte[] bytes = buffer.bytes();
        return bytes;
    }
    public static void wkb(ByteBuffer buffer, LineString line, boolean head){
        if(head){
            buffer.put((byte)line.endian());
            buffer.put(line.type());
        }
        List<Point> points = line.points();
        buffer.put(points.size());
        for(Point point:points){
            wkb(buffer, point, false);
        }
    }

    public static byte[] wkb(Polygon polygon){
        init(polygon);
        List<Ring> rings = polygon.rings();
        int len  = 13;
        for(Ring ring:rings){
            len += ring.points().size()*16 + 4;
        }
        ByteBuffer buffer = new ByteBuffer(len, polygon.endian());
        buffer.put(polygon.srid());
        wkb(buffer, polygon, true);
        return buffer.bytes();
    }

    public static void wkb(ByteBuffer buffer, Polygon polygon, boolean head){
        if(head){
            buffer.put((byte)polygon.endian());
            buffer.put(polygon.type());
        }
        List<Ring> rings = polygon.rings();
        buffer.put(rings.size());
        for(Ring ring:rings){
            wkb(buffer, ring);
        }
    }

    public static void wkb(ByteBuffer buffer, Ring ring){
        List<Point> points = ring.points();
        buffer.put(points.size());
        for(Point point:points){
            wkb(buffer, point, false);
        }
    }
    public static byte[] wkb(MultiPoint multiPoint){
        init(multiPoint);
        List<Point> points = multiPoint.points();
        int len = 13 + points.size()*(16+1+4); //xy + endian + type
        ByteBuffer buffer = new ByteBuffer(len, multiPoint.endian());
        buffer.put(multiPoint.srid());
        wkb(buffer, multiPoint, true);
        return buffer.bytes();
    }

    public static void  wkb(ByteBuffer buffer, MultiPoint multiPoint, boolean head){
        if(head){
            buffer.put((byte)multiPoint.endian());
            buffer.put(multiPoint.type());
        }
        List<Point> points = multiPoint.points();
        buffer.put(points.size());
        for(Point point:points){
            buffer.put((byte)multiPoint.endian());
            buffer.put(point.type());
            wkb(buffer, point, false);
        }
    }
    public static byte[] wkb(MultiLine multiLine){
        init(multiLine);
        int len = 13;
        List<LineString> lines = multiLine.lines();
        for(LineString line:lines){
            len += (1 + 4 + 4);
            List<Point> points = line.points();
            len += points.size()*16;
        }
        ByteBuffer buffer = new ByteBuffer(len, multiLine.endian());
        buffer.put(multiLine.srid());
        wkb(buffer, multiLine, true);
        return buffer.bytes();
    }
    public static void wkb(ByteBuffer buffer, MultiLine multiLine, boolean head){
        if(head){
            buffer.put((byte)multiLine.endian());
            buffer.put(multiLine.type());
        }
        List<LineString> lines = multiLine.lines();
        buffer.put(lines.size());
        for(LineString line:lines){
            wkb(buffer, line, true);
        }
    }
    public static byte[] wkb(MultiPolygon multiPolygon){
        init(multiPolygon);
        int len = 13;
        List<Polygon> polygons = multiPolygon.polygons();
        for(Polygon polygon:polygons){
            len += 9;
            List<Ring> rings = polygon.rings();
            for(Ring ring:rings){
                len += 4;
                len += ring.points().size()*16;
            }
        }
        ByteBuffer buffer = new ByteBuffer(len, multiPolygon.endian());
        buffer.put(multiPolygon.srid());
        wkb(buffer, multiPolygon, true);
        return buffer.bytes();
    }
    public static void wkb(ByteBuffer buffer, MultiPolygon multiPolygon, boolean head){
        if(head){
            buffer.put((byte)multiPolygon.endian());
            buffer.put(multiPolygon.type());
        }
        List<Polygon> polygons = multiPolygon.polygons();
        buffer.put(polygons.size());
        for(Polygon polygon:polygons){
            wkb(buffer, polygon, true);
        }
    }
    /**
     * 生成wkb格式要
     * @param collection GeometryCollection
     * @return bytes
     */
    public static byte[] wkb(GeometryCollection collection){
        init(collection);
        ByteBuffer buffer = new ByteBuffer(collection.endian());
        List<Geometry> list = collection.collection();
        buffer.put(collection.srid());
        buffer.put((byte) collection.endian());
        buffer.put(collection.type());
        buffer.put(list.size());
        for(Geometry geometry:list){
            wkb(buffer, geometry, true);
        }
        byte[] bytes = buffer.bytes();
        return bytes;
    }
}
