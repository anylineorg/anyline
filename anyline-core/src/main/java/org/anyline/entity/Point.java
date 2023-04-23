package org.anyline.entity;

import org.anyline.util.NumberUtil;

public class Point {
    boolean isInt = false;
    private Double x;
    private Double y;
    public Point(){}
    public Point(Double x, Double y){
        this.x = x ;
        this.y = y;
    }
    public Point(int x, int y){
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
    public Double[] getArray(){
        return new Double[]{x,y};
    }
    public Double[] getDoubles(){
        return new Double[]{x,y};
    }
    public Long[] getLongs(){
        return new Long[]{x.longValue(),y.longValue()};
    }
    public Integer[] getInteger(){
        return new Integer[]{x.intValue(),y.intValue()};
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
            builder.append("point");
        }
        builder.append("(");
        if (isInt) {
            builder.append(x.intValue());
        }else{
            builder.append(x);
        }
        builder.append(",");
        if (isInt) {
            builder.append(y.intValue());
        }else{
            builder.append(y);
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     *             false:(120 36)<br/>
     *             true: Point(120 36)
     * @return String
     */
    public String sql(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag){
            builder.append("Point");
        }
        builder.append("(").append(x).append(" ").append(y).append(")");
        return builder.toString();
    }
    public String sql(){
        return sql(true);
    }
}
