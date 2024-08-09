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

import org.anyline.metadata.Table;

import java.io.Serializable;

public class Join  implements Serializable {
    public static enum TYPE{
        INNER               {public String getCode() {return "INNER JOIN";} 	public String getName() {return "内连接";}},
        LEFT				{public String getCode() {return "LEFT JOIN";} 	public String getName() {return "左连接";}},
        RIGHT			    {public String getCode() {return "RIGHT JOIN";} 	public String getName() {return "右连接";}},
        FULL				{public String getCode() {return "FULL JOIN";} 	public String getName() {return "全连接";}};
        public abstract String getName();
        public abstract String getCode();
    }
    private Table table;
    private TYPE type = TYPE.INNER;
    private String condition;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
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

}
