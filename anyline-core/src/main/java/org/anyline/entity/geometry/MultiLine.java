package org.anyline.entity.geometry;

import java.util.ArrayList;
import java.util.List;

public class MultiLine extends Geometry{
    private List<Line> lines = new ArrayList<>();

    public MultiLine(){

    }
    public MultiLine(List<Line> lines){
        this.lines = lines;
    }
    public MultiLine add(Line line){
        lines.add(line);
        return this;
    }

    public MultiLine add(List<Line> lines){
        if(null != lines) {
            lines.addAll(lines);
        }
        return this;
    }
    public MultiLine clear(){
        //lines.clear();
        lines = new ArrayList<>();
        return this;
    }
    public List<Line> getLines(){
        return lines;
    }
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("MultiLine");
        builder.append("(");
        boolean first = true;
        for(Line line:lines){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(line.toString(false));
        }
        builder.append(")");
        return builder.toString();
    }
    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     *             false:((120 36.1, 120 36.2, 120 36.3), (121 36.1, 121 36.2, 121 36.3))<br/>
     *             true: MultiLine((120 36.1, 120 36.2, 120 36.3), (121 36.1, 121 36.2, 121 36.3))
     * @return String
     */
    public String sql(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("MultiLine");
        }
        builder.append("(");
        boolean first = true;
        for(Line line:lines){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(line.sql(false, false));
        }
        builder.append(")");
        return builder.toString();
    }
    public String sql(){
        return sql(true);
    }
}
