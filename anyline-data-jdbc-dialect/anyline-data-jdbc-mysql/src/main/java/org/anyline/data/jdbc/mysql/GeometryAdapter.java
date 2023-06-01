package org.anyline.data.jdbc.mysql;

import org.anyline.entity.geometry.Geometry;
import org.anyline.entity.geometry.Line;
import org.anyline.entity.geometry.Point;
import org.anyline.util.NumberUtil;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class GeometryAdapter {

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
        X coordinate	8(9-16) 	120.0   00 00 00 00 00 00 5e 40
        Y coordinate	8(17-24)	36.1    cd cc cc cc cc 0c 42 40
    */
    public static Point parsePoint(byte[] bytes){
        double x = NumberUtil.byte2double(bytes, 9);
        double y = NumberUtil.byte2double(bytes, 17);
        Point point = new Point(x, y);
        return point;
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
        X coordinate	8(13-20)   1 	    00 00 00 00 00 00 f0 3f
        Y coordinate	8(21-28)   2 	    00 00 00 00 00 00 00 40
        X coordinate	8(29-36)   15 	    00 00 00 00 00 00 2e 40
        Y coordinate	8(37-44)   15       00 00 00 00 00 00 2e 40
        X coordinate	8(45-52)   11       00 00 00 00 00 00 2e 40
        Y coordinate	8(53-60)   22       00 00 00 00 00 00 2e 40
   */
    /**
     *
     * @param bytes
     * @return
     */
    public static Line parseLine(byte[] bytes){
        boolean bigEndian = (bytes[4] == 0x00);
        int count = NumberUtil.byte2int(bytes, 9, 4, bigEndian);
        List<Point> points = new ArrayList<>();
        for(int i=0; i<count; i++){
            double x = NumberUtil.byte2double(bytes, 13+8*i*2);
            double y = NumberUtil.byte2double(bytes, 21+8*i*2);
            Point point = new Point(x, y);
            points.add(point);
        }
        Line line = new Line(points);
        return line;
    }
}
