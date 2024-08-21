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

package org.anyline.data.entity;

import org.anyline.data.Run;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultAutoCondition;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.metadata.Table;
import org.anyline.util.BasicUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Join  implements Serializable {
    public static enum TYPE{
        INNER               {public String getCode() {return "INNER JOIN";} 	public String getName() {return "内连接";}},
        LEFT				{public String getCode() {return "LEFT JOIN";} 	public String getName() {return "左连接";}},
        RIGHT			    {public String getCode() {return "RIGHT JOIN";} 	public String getName() {return "右连接";}},
        FULL				{public String getCode() {return "FULL JOIN";} 	public String getName() {return "全连接";}};
        public abstract String getName();
        public abstract String getCode();
    }
    private RunPrepare prepare;
    private Run run;
    private TYPE type = TYPE.INNER;
    private ConfigStore configs = new DefaultConfigStore();

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public void setConditions(List<Condition> conditions) {
        for(Condition condition:conditions) {
            conditions.add(condition);
        }
    }

    public ConfigStore getConfigs() {
        return configs;
    }

    public void setConfigs(ConfigStore configs) {
        this.configs = configs;
    }

    public RunPrepare getPrepare(){
        return prepare;
    }
    public void setPrepare(RunPrepare prepare){
        this.prepare = prepare;
    }
    public void setTable(Table table){
        DefaultTablePrepare prepare = new DefaultTablePrepare();
        prepare.setTable(table);
        this.prepare = prepare;
    }
    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public Join addCondition(String condition){
        configs.and(condition);
        return this;
    }

    public Join setConditions(String ... conditions) {
        for(String condition:conditions){
            configs.and(condition);
        }
        return this;
    }
}
