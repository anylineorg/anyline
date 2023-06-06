package org.anyline.entity.geometry;

public class Box extends Geometry{

    private Point p1;
    private Point p2;
    public Box(){}
    public Box(Point p1, Point p2){
        this.p1 = p1;
        this.p2 = p2;
    }

    public Point p1() {
        return p1;
    }

    public void p1(Point p1) {
        this.p1 = p1;
    }

    public Point p2() {
        return p2;
    }

    public void p2(Point p2) {
        this.p2 = p2;
    }

    public Point getP1() {
        return p1;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
    }

    public Point getP2() {
        return p2;
    }

    public void setP2(Point p2) {
        this.p2 = p2;
    }
    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public String toString(boolean tag) {
        StringBuilder builder = new StringBuilder();
        if (tag) {
            builder.append(tag());
        }
        builder.append("(");
        builder.append(p1.toString(false));
        builder.append(",");
        builder.append(p2.toString(false));
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String sql(boolean tag, boolean bracket) {

        StringBuilder builder = new StringBuilder();
        if(tag){
            builder.append(tag());
        }
        if(bracket){
            builder.append("(");
        }
        builder.append(p1.sql(false, false));
        builder.append(",");
        builder.append(p2.sql(false, false));
        if(bracket){
            builder.append(")");
        }
        return builder.toString();
    }

    @Override
    public String sql() {
        return sql(true, true);
    }
}
