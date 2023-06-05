package org.anyline.entity.geometry;

public class Circle extends Geometry{

    private Point center;
    private Double radius;
    public Circle(){}
    public Circle(Point center, double radius){
        this.center = center;
        this.radius = radius;
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
        builder.append(center.toString(false));
        builder.append(",");
        builder.append(radius);
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
        builder.append(center.sql(false, false));
        builder.append(",");
        builder.append(radius);
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
