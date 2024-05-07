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

public class GeometryCollection extends Geometry{
    private List<Geometry> collection = new ArrayList<>();
    public GeometryCollection(){
        type = 7;
    }
    public List<Geometry> collection(){
        return collection;
    }
    public GeometryCollection add(Geometry geometry){
        collection.add(geometry);
        return this;
    }
    public String toString(boolean tag){
        StringBuilder builder = new StringBuilder();
        if(tag) {
            builder.append(tag());
        }
        builder.append("(");
        boolean first = true;
        for(Geometry geometry:collection){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(geometry.toString(true));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public String sql() {
        return sql(true, true);
    }

    public List<Geometry> getCollection() {
        return collection;
    }

    /**
     * sql格式
     * @param tag 是否包含tag<br/>
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
        for(Geometry geometry:collection){
            if(!first){
                builder.append(",");
            }
            first = false;
            builder.append(geometry.sql(true, true));
        }
        if(bracket) {
            builder.append(")");
        }
        return builder.toString();
    }
}
