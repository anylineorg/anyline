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
    public List<Line> lines(){
        return lines;
    }
    public String toString(boolean tag){
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
            builder.append(line.toString(false));
        }
        builder.append(")");
        return builder.toString();
    }
    public String toString(){
        return toString(true);
    }
    /**
     * sql格式
     * @param tag 是否包含tag<br/>
     *             false:((120 36.1, 120 36.2, 120 36.3), (121 36.1, 121 36.2, 121 36.3))<br/>
     *             true: MultiLine((120 36.1, 120 36.2, 120 36.3), (121 36.1, 121 36.2, 121 36.3))
     * @param bracket 是否包含()
     * @return String
     */
    public String sql(boolean tag, boolean bracket){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append("MultiLine");
        }
        if(bracket) {
            builder.append("(");
        }
        boolean first = true;
        for(Line line:lines){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(line.sql(false, false));
        }
        if(bracket) {
            builder.append(")");
        }
        return builder.toString();
    }
    public String sql(){
        return sql(true, true);
    }

    public List<Line> getLines() {
        return lines;
    }
}
