package org.anyline.entity.geometry;

public class Line extends Geometry{
    private double a;
    private double b;
    private double c;

    private Point p1;
    private Point p2;
    public Line(){}
    public Line(Point p1, Point p2){
        this.p1 = p1;
        this.p2 = p2;
        slope();
    }
    public Line(double a, double b, double c){
        this.a = a;
        this.b = b;
        this.c = c;
        point();
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

    public void slope(){
        double x1 = p1.x();
        double x2 = p2.x();
        double y1 = p1.y();
        double y2 = p2.y();
        if (x1 == x2) {
            this.a = -1.0;
            this.b = 0.0;
        } else {
            this.a = (y2 - y1) / (x2 - x1);
            this.b = -1.0;
        }

        this.c = y1 - this.a * x1;
    }
    public void point() {

        if (b != 0) {
            double x1 = 0;
            double y1 = (-a * x1 - c) / b;
            p1 = new Point(x1, y1);

            double x2 = 1;
            double y2 = (-a * x2 - c) / b;
            p2 = new Point(x2, y2);
        } else {
            double y1 = 0;
            double x1 = -c / a;
            p1 = new Point(x1, y1);

            double y2 = 1;
            double x2 = -c / a;
            p2 = new Point(x2, y2);
        }
    }


}
