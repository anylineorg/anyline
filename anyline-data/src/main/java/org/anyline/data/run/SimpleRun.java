package org.anyline.data.run;

import java.util.ArrayList;

public class SimpleRun extends TextRun implements Run {
    public SimpleRun(){}
    public SimpleRun(StringBuilder builder){
        this.builder = builder;
    }
    public SimpleRun(String sql){
        this.builder.append(sql);
    }
    public String getFinalQuery() {
        return builder.toString();
    }

    public String getFinalUpdate() {
        return builder.toString();
    }

    public SimpleRun addValue(Object value) {
        RunValue runValue = new RunValue();
        runValue.setValue(value);
        if(null == values){
            values = new ArrayList<>();
        }
        values.add(runValue);
        return this;
    }
}
