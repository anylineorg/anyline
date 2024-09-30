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

package org.anyline.data.param;

import org.anyline.data.prepare.RunPrepare;
import org.anyline.entity.*;
import org.anyline.metadata.Table;
import org.anyline.service.AnylineService;

import java.util.ArrayList;
import java.util.List;

public class AggregationBuilder {
    private AnylineService service;
    private Table table;
    private RunPrepare prepare;
    private ConfigStore configs;
    private List<AggregationConfig> aggregations = new ArrayList<>();
    private GroupStore groups = new DefaultGroupStore();
    public AggregationBuilder(AnylineService service) {
        this.service = service;
    }
    public AggregationBuilder group(String ... columns) {
        for(String column:columns){
            groups.add(column);
        }
        return this;
    }
    public GroupStore groups(){
        return this.groups;
    }
    public List<AggregationConfig> aggregations() {
        return this.aggregations;
    }
    public AggregationBuilder aggregation(Aggregation aggregation, String column, String result){
        AggregationConfig config = new AggregationConfig(aggregation, column, result);
        aggregations.add(config);
        return this;
    }
    public AggregationBuilder table(Table table){
        this.table = table;
        return this;
    }
    public AggregationBuilder table(RunPrepare prepare){
        this.prepare = prepare;
        return this;
    }
    public AggregationBuilder configs(ConfigStore configs){
        this.configs = configs;
        return this;
    }
    public DataSet querys(){
        configs.setGroups(groups);
        configs.aggregations(aggregations);
        if(null != prepare){
            return service.querys(prepare, configs);
        }else if(null != table){
            return service.querys(table, configs);
        }
        throw new RuntimeException("未提供Table或RunPrepare");
    }
    public DataRow query(){
        configs.setGroups(groups);
        configs.aggregations(aggregations);
        if(null != prepare){
            return service.query(prepare, configs);
        }else if(null != table){
            return service.query(table, configs);
        }
        throw new RuntimeException("未提供Table或RunPrepare");
    }

}
