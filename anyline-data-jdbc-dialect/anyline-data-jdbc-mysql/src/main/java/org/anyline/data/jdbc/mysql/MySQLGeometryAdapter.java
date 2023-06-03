package org.anyline.data.jdbc.mysql;

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
    public static byte[] bytes(Geometry geometry){
        return null;
    }
    public static String sql(Geometry geometry){
        return null;
    }
    public static byte[] bytes(Point point){
        byte[] bx= NumberUtil.double2bytes(point.getX());
        byte[] by= NumberUtil.double2bytes(point.getY());
        byte[] bytes =new byte[25];
        bytes[4]=0x01;
        bytes[5]=0x01;
        for(int i=0;i<8;++i){
            bytes[9+i]=bx[i];
            bytes[17+i]=by[i];
        }
        return bytes;
    }
    public static String sql(Point point){
        return "Point(" + point.getX() + " " + point.getY() + ")";
    }
    public static Geometry parse(byte[] bytes){
        Geometry geometry = null;
        //取字节数组的前4个来解析srid
        byte[] srid_bytes = new byte[4];
        System.arraycopy(bytes, 0, srid_bytes, 0, 4);
        //是否大端格式
        boolean bigEndian = (bytes[4] == 0x00);
        // 解析SRID
        int srid = NumberUtil.byte2int(bytes, 0, 4, bigEndian);
        int type = NumberUtil.byte2int(bytes, 5, 4, bigEndian);
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
        }
        geometry.setSrid(srid);
        return geometry;
    }
    /*
        POINT(120 36.1)
        bytes[25]:
        00 00 00 00, 01, 01 00 00 00, 00 00 00 00 00 00 5e 40, cd cc cc cc cc 0c 42 40
        component	    size(起-止) decimal hex
        SRID            4(0-3)      0       00 00 00 00
        Byte order	    1(4-4)  	1       01(1:小端,0:大端)
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
        return new Point(buffer.readDouble(), buffer.readDouble());
    }
    /*
        LINESTRING(1 2, 15 15, 11 22)
        bytes[61]:
        00 00 00 00, 01, 02 00 00 00 ,03 00 00 00, 00 00 00 00 00 00 f0 3f, 00 00 00 00 00 00 00 40, 00 00 00 00 00 00 2e 40, 00 00 00 00 00 00 2e 40, 00 00 00 00 00 00 26 40 00 00 00 00 00 00 36 40
        component	    size(起-止) decimal  hex
        SRID            4(0-3)     0        00 00 00 00
        Byte order	    1(4-4)     1        01
        WKB type	    4(5-8)     1        01 00 00 00
        point count     4(9-12)    3        03 00 00 00
        X(经度)          8(13-20)   1 	    00 00 00 00 00 00 f0 3f
        Y(纬度)          8(21-28)   2 	    00 00 00 00 00 00 00 40
        X(经度)          8(29-36)   15 	    00 00 00 00 00 00 2e 40
        Y(纬度)          8(37-44)   15       00 00 00 00 00 00 2e 40
        X(经度)          8(45-52)   11       00 00 00 00 00 00 2e 40
        Y(纬度)          8(53-60)   22       00 00 00 00 00 00 2e 40
   */
    /**
     * 解析Line
     * @param bytes bytes
     * @return Line
     */
    public static Line parseLine(byte[] bytes){
        Line line = line(bytes, 9);
        return line;
    }

    /**
     *
     * @param bytes bytes
     * @param offset point count的开始位置
     * @return Line
     */
    public static Line line(byte[] bytes, int offset){
        ByteBuffer buffer = new ByteBuffer(bytes, bytes[4], offset);
        return line(buffer);
    }
    public static Line line(ByteBuffer buffer){
        List<Point> points = new ArrayList<>();
        int count = buffer.readInt();
        for(int i=0; i<count; i++){
            Point point = point(buffer);
            points.add(point);
        }
        Line line = new Line(points);
        return line;
    }
    /*

        头部（Header）：
            SRID
            字节顺序（Byte Order）：表示二进制数据的字节顺序，通常为大端序（Big Endian）或小端序（Little Endian）。
            类型标识符（Type Identifier）：标识几何对象的类型，对于多边形（Polygon）来说，它的值是十六进制的0103。
            环的数量（Number of Rings）：表示多边形中环的数量，包括外部环和内部环（孔）。
        外部环（Exterior Ring）：
            点的数量（Number of Points）：表示构成外部环的点的数量。
            点的坐标（Coordinates）：按照顺序列出外部环中每个点的坐标，每个点的坐标由X和Y值组成。
        内部环（Interior Rings）（可选）：
            点的数量（Number of Points）：表示每个内部环中点的数量。
            点的坐标（Coordinates）：按照顺序列出每个内部环中每个点的坐标，每个点的坐标由X和Y值组成。
        单个环
        POLYGON((121.415703 31.172893,121.415805 31.172664,121.416127 31.172751,121.41603 31.172976,121.415703 31.172893)
        bytes[97]:
        00 00 00 00, 01, 03 00 00 00, 01 00 00 00, 05 00 00 00, 57 76 C1 E0 9A 5A 5E 40, 13 B5 34 B7 42 2C 3F 40, DA 20 93 8C 9C 5A 5E 40, 51 32 39 B5 33 2C 3F 40, E3 FE 23 D3 A1 5A 5E 40, EF 59 D7 68 39 2C 3F 40, EA 09 4B 3C A0 5A 5E 40, 2E FE B6 27 48 2C 3F 40, 57 76 C1 E0 9A 5A 5E 40, 13 B5 34 B7 42 2C 3F 40
        component        size(起-止) decimal      hex
        SRID            4(0-3)       0            00 00 00 00
        Byte order      1(4-4)       1            01
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
        04 00 00 00, 00 00 00 00 00 00 39 40, 00 00 00 00 00 00 39 40, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 80 41 40, 00 00 00 00 00 00 2E 40, 00 00 00 00 00 00 3E 40, 00 00 00 00 00 00 39 40 ,00 00 00 00 00 00 39 40
        component        size(起-止)   decimal      hex
        SRID             4(0-3)       0            00 00 00 00
        Byte order       1(4-4)       1            01
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
    Byte order        1(4-4)       1            01
    WKB type          4(5-8)       4(MultiPoint)04 00 00 00
    points count     4(9-12)       3            03 00 00 00
    Byte order      1(13-13)       1            01
    WKB type        4(14-17)       1(point)     01 00 00 00(好像也没别的值可选，有点多余)
    X(经度)          8(18-25)      30            00 00 00 00 00 00 3E 40
    Y(纬度)          8(26-33)      20            00 00 00 00 00 00 34 40
    Byte order      1(34-34)       1            01
    WKB type        4(35-38)       1(point)     01 00 00 00
    X(经度)          8(39-46)      25            00 00 00 00 00 00 39 40
    Y(纬度)          8(47-54)      25            00 00 00 00 00 00 39 40
    Byte order      1(55-55)       1            01
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
        //点数量
        int count = buffer.readInt();
        List<Point> points = new ArrayList<>();
        for(int i=0; i<count; i++){
            //跳过 byte order(1位)和 WKB type(4位)
            buffer.step(5);
            points.add(point(buffer));
        }
        MultiPoint multiPoint = new MultiPoint(points);
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
    Byte order        1(4-4)       1            01
    WKB type          4(5-8)       5            05 00 00 00
    line count       4(9-12)       2            02 00 00 00
    Byte order      1(13-13)       1            01(第0条)
    WKB type        4(14-17)       1(line)      02 00 00 00(好像也没别的值可选，有点多余)
    point count     4(18-21)       3            03 00 00 00(第0条线段3个点)
    X(经度)          8(22-29)      120           00 00 00 00 00 00 5E 40
    Y(纬度)          8(30-37)      36.1          CD CC CC CC CC 0C 42 40
    X(经度)          8(38-45)      120           00 00 00 00 00 00 5E 40
    Y(纬度)          8(46-53)      36.2          9A 99 99 99 99 19 42 40
    X(经度)          8(54-61)      120           00 00 00 00 00 00 5E 40
    Y(纬度)          8(62-69)      36.3          66 66 66 66 66 26 42 40
    Byte order      1(70-70)       1            01(第1条)
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
        //线段数量
        int line_count = buffer.readInt();
        List<Line> lines = new ArrayList<>();
        for(int l=0; l<line_count; l++){
            //跳过 byte order(1位)和 WKB type(4位)
            buffer.step(5);
            lines.add(line(buffer));
        }
        MultiLine multiLine = new MultiLine(lines);
        return multiLine;
    }
}
