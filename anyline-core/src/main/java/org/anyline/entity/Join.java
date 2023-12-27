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


package org.anyline.entity;

import java.io.Serializable;

public class Join  implements Serializable {
    public static enum TYPE{
        INNER               {public String getCode(){return "INNER JOIN";} 	public String getName(){return "内连接";}},
        LEFT				{public String getCode(){return "LEFT JOIN";} 	public String getName(){return "左连接";}},
        RIGHT			    {public String getCode(){return "RIGHT JOIN";} 	public String getName(){return "右连接";}},
        FULL				{public String getCode(){return "FULL JOIN";} 	public String getName(){return "全连接";}};
        public abstract String getName();
        public abstract String getCode();
    }
    private String schema;
    private String name;
    private String alias;
    private TYPE type = TYPE.INNER;
    private String condition;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        parseName();
    }
    private void parseName(){
        if(null != name){
            if(null != name && name.contains(".")){
                String[] tbs = name.split("\\.");
                name = tbs[1];
                schema = tbs[0];
            }
            String tag = " as ";
            String lower = name.toLowerCase();
            int tagIdx = lower.indexOf(tag);
            if(tagIdx > 0){
                alias = name.substring(tagIdx+tag.length()).trim();
                name = name.substring(0, tagIdx).trim();
            }
            if(name.contains(" ")){
                String[] tmps = name.split(" ");
                name = tmps[0];
                alias = tmps[1];
            }
        }
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
