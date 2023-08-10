package org.anyline.entity.geometry;

import org.anyline.util.NumberUtil;

public class Point extends Geometry{
    private Double x;
    private Double y;
    public Point(){
        type = 1;
    }
    public Point(Double x, Double y){
        this();
        this.x = x ;
        this.y = y;
    }
    public Point(Integer[] xy){
        this(new Double(xy[0]), new Double(xy[1]));
    }
    public Point(int[] xy){
        this(xy[0], xy[1]);
    }
    public Point(Double[] xy){
        this(xy[0], xy[1]);
    }
    public Point(double[] xy){
        this(xy[0], xy[1]);
    }
    public Point(int x, int y){
        this(new Double(x), new Double(y));
    }
    public Point(Integer x, Integer y){
        this(new Double(x), new Double(y));
    }

    public double[] doubles() {
        return new double[]{x, y};
    }

    public Double x() {
        return x;
    }

    public void x(Double x) {
        this.x = x;
    }

    public Double y() {
        return y;
    }

    public void y(Double y) {
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public String toString(){
        return toString(true);
    }
    public String toString(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag){
            builder.append(tag());
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
            builder.append(tag());
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
