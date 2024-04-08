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

public class LineString extends Geometry {
    private List<Point> points = new ArrayList<>();
    public LineString add(Point point){
        points.add(point);
        return this;
    }
    public LineString add(double x, double y){
        return add(new Point(x, y));
    }
    public LineString add(int x, int y){
        return add(new Point(x, y));
    }
    public List<Point> points(){
        return points;
    }
    public LineString(){
        type = 2;
    }
    public LineString(List<Point> points) {
        this();
        this.points = points;
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
        boolean first = true;
        for(Point point:points){
            if(!first){
                builder.append(",");
            }
            builder.append(point.toString(false));
            first = false;
        }
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

        boolean first = true;
        for(Point point:points){
            if(!first){
                builder.append(",");
            }
            builder.append(point.sql(false, false));
            first = false;
        }
        if(bracket){
            builder.append(")");
        }
        return builder.toString();
    }
    public String sql(){
        return sql(true, true);
    }

    public List<Point> getPoints() {
        return points;
    }
}

