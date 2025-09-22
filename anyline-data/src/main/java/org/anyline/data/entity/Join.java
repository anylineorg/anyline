/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.entity;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;

import java.io.Serializable;
import java.util.List;

public class Join  implements Serializable {
    private static final long serialVersionUID = 1L;
    public static enum TYPE{
        INNER               {public String getCode() {return "INNER JOIN";} 	public String getName() {return "内连接";}},
        LEFT				{public String getCode() {return "LEFT JOIN";} 	public String getName() {return "左连接";}},
        LEFT_LATERAL		{public String getCode() {return "LEFT JOIN LATERAL";} 	public String getName() {return "左横向连接";}},
        RIGHT			    {public String getCode() {return "RIGHT JOIN";} 	public String getName() {return "右连接";}},
        RIGHT_LATERAL	    {public String getCode() {return "RIGHT JOIN LATERAL";} 	public String getName() {return "右横向连接";}},
        FULL				{public String getCode() {return "FULL JOIN";} 	public String getName() {return "全连接";}};
        public abstract String getName();
        public abstract String getCode();
    }
    private TYPE type = TYPE.INNER;
    private ConfigStore conditions = new DefaultConfigStore();


    public ConfigStore getConditions() {
        return conditions;
    }

    public void setConditions(ConfigStore configs) {
        this.conditions = configs;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public Join addConditions(String ... conditions) {
        for(String condition : conditions) {
            this.conditions.and(condition);
        }
        return this;
    }
    public Join addConditions(List<String> conditions) {
        for(String condition : conditions) {
            this.conditions.and(condition);
        }
        return this;
    }

    public Join setConditions(String ... conditions) {
        this.conditions = new DefaultConfigStore();
        for(String condition:conditions) {
            this.conditions.and(condition);
        }
        return this;
    }
    public Join setConditions(List<String> conditions) {
        this.conditions = new DefaultConfigStore();
        for(String condition:conditions) {
            this.conditions.and(condition);
        }
        return this;
    }
    public DataRow map(boolean empty) {
        DataRow row = new OriginRow();
        if(empty || null != type) {
            row.put("type", type.getCode());
        }
        row.put("conditions", conditions.getConfigChain().map(empty));
        return row;
    }
    public DataRow map() {
        return map(false);
    }
    public String json(boolean empty) {
        return map(empty).json();
    }
    public String json() {
        return json(false);
    }
}
