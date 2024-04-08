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

public class Polygon extends Geometry{
    private List<Ring> rings = new ArrayList<>();
    public Polygon add(Ring ring){
        rings.add(ring);
        return this;
    }
    public List<Ring> rings(){
        return rings;
    }
    public Polygon(){
        type = 3;
    }
    public Polygon(List<Ring> rings) {
        this();
        this.rings = rings;
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
        //顺时针(外部环)
        boolean first = true;
        for(Ring ring:rings){
            if(ring.clockwise() == true){
                if(!first){
                    builder.append(",");
                }
                builder.append(ring.toString(false));
                first = false;
            }
        }
        //逆时针(内部环)(可选)
        for(Ring ring:rings){
            if(ring.clockwise() == false){
                if(!first){
                    builder.append(",");
                }
                builder.append(ring.toString(false));
                first = false;
            }
        }

        builder.append(")");
        return builder.toString();
    }

    /**
     * sql格式
     * POLYGON((121.415703 31.172893, 121.415805 31.172664, 121.416127 31.172751, 121.41603 31.172976, 121.415703 31.172893)<br/>
     * POLYGON ((30 20, 45 40, 10 40, 30 20), (20 30, 35 35, 30 20, 20 30), (25 25, 30 35, 15 30, 25 25))
     * @param tag 是否包含tag
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
        for(Ring ring:rings){
            if(ring.clockwise() == true){
                if(!first){
                    builder.append(",");
                }
                builder.append(ring.sql(false, true));
                first = false;
            }
        }
        //逆时针(内部环)(可选)
        for(Ring ring:rings){
            if(ring.clockwise() == false){
                if(!first){
                    builder.append(",");
                }
                builder.append(ring.sql(false, true));
                first = false;
            }
        }
        if(bracket){
            builder.append(")");
        }
        return builder.toString();
    }
    public String sql(){
        return sql(true, true);
    }

    public List<Ring> getRings() {
        return rings;
    }
}
