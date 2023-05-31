package org.anyline.entity.geometry;

import org.anyline.util.NumberUtil;

import java.math.BigDecimal;
import java.util.List;

public class Point extends Geometry{
    boolean isInt = false;
    private Double x;
    private Double y;
    public Point(){}
    public Point(Double x, Double y){
        this.x = x ;
        this.y = y;
    }
    public Point(Integer[] xy){
        this.x = new Double(x) ;
        this.y = new Double(y);
        isInt = true;
    }
    public Point(int[] xy){
        this.x = new Double(x) ;
        this.y = new Double(y);
        isInt = true;
    }
    public Point(Double[] xy){
        this.x = xy[0] ;
        this.y = xy[1];
    }
    public Point(double[] xy){
        this.x = xy[0] ;
        this.y = xy[1];
    }
    public Point(int x, int y){
        this.x = new Double(x) ;
        this.y = new Double(y);
        isInt = true;
    }
    public Point(Integer x, Integer y){
        this.x = new Double(x) ;
        this.y = new Double(y);
        isInt = true;
    }
    public Point(byte[] bytes){
        x = NumberUtil.byte2double(bytes,9);
        y = NumberUtil.byte2double(bytes,17);
    }

    public byte[] bytes(){
        byte[] bx= NumberUtil.double2bytes(x);
        byte[] by= NumberUtil.double2bytes(y);
        byte[] bytes =new byte[25];
        bytes[4]=0x01;
        bytes[5]=0x01;
        for(int i=0;i<8;++i){
            bytes[9+i]=bx[i];
            bytes[17+i]=by[i];
        }
        return bytes;
    }
    public Byte[] Bytes(){
        Byte[] bx= NumberUtil.double2Bytes(x);
        Byte[] by= NumberUtil.double2Bytes(y);
        Byte[] bytes =new Byte[25];
        bytes[4]=0x01;
        bytes[5]=0x01;
        for(int i=0;i<8;++i){
            bytes[9+i]=bx[i];
            bytes[17+i]=by[i];
        }
        return bytes;
    }
    public Double[] getArray(){
        return new Double[]{x,y};
    }
    public Double[] getDoubles(){
        return new Double[]{x,y};
    }
    public Float[] getFloats(){
        return new Float[]{x.floatValue(),y.floatValue()};
    }
    public BigDecimal[] getDecimals(){
        return new BigDecimal[]{BigDecimal.valueOf(x),BigDecimal.valueOf(y)};
    }
    public float[] floats(){
        return new float[]{x.floatValue(),y.floatValue()};
    }
    public double[] doubles() throws Exception{
        return new double[]{x, y};
    }
    public Long[] getLongs(){
        return new Long[]{x.longValue(),y.longValue()};
    }
    public long[] longs() throws Exception{
        return new long[]{x.longValue(), y.longValue()};
    }
    public Integer[] getIntegers(){
        return new Integer[]{x.intValue(),y.intValue()};
    }
    public int[] ints() throws Exception{
        return new int[]{x.intValue(), y.intValue()};
    }
    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public String toString(){
        return toString(true);
    }
    public String toString(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag){
            builder.append("Point");
        }
        builder.append("(");
        builder.append(NumberUtil.format(x, "0.###########"));
        builder.append(",");
        builder.append(NumberUtil.format(y, "0.###########"));
        builder.append(")");
        return builder.toString();
    }

    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     *             false:(120 36)<br/>
     *             true: Point(120 36)
     * @param bracket 是否包含()
     * @return String
     */
    public String sql(boolean tag, boolean bracket){
        StringBuilder builder = new StringBuilder();
        if(tag){
            builder.append("Point");
        }
        if(bracket){
            builder.append("(");
        }
        builder.append(NumberUtil.format(x, "0.###########"))
                .append(" ")
                .append(NumberUtil.format(y, "0.###########"));
        if(bracket){
            builder.append(")");
        }
        return builder.toString();
    }
    public String sql(){
        return sql(true, true);
    }
}
