package org.anyline.entity.geometry;

public class LineSegment extends Geometry{
    private Point p1;
    private Point p2;
    public LineSegment(){}
    public LineSegment(Point p1, Point p2){
        this.p1 = p1;
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
