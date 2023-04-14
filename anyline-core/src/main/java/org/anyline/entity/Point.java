package org.anyline.entity;

import org.anyline.util.NumberUtil;

public class Point {
    private Double x;
    private Double y;
    public Point(){}
    public Point(Double x, Double y){
        this.x = x ;
        this.y = y;
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
        return "Point(" + x + "," + y + ")";
    }
    public String toSql(){
        return "Point(" + x + " " + y + ")";
    }
}
