/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.entity.geometry;

import java.util.ArrayList;
import java.util.List;

public class MultiLine extends Geometry{
    private List<LineString> lines = new ArrayList<>();

    public MultiLine(){
        type = 5;
    }
    public MultiLine(List<LineString> lines){
        this();
        this.lines = lines;
    }
    public MultiLine add(LineString line){
        lines.add(line);
        return this;
    }

    public MultiLine add(List<LineString> lines){
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
    public List<LineString> lines(){
        return lines;
    }
    public String toString(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append(tag());
        }
        builder.append("(");
        boolean first = true;
        for(LineString line:lines){
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
            builder.append(tag());
        }
        if(bracket) {
            builder.append("(");
        }
        boolean first = true;
        for(LineString line:lines){
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

    public List<LineString> getLines() {
        return lines;
    }
}
