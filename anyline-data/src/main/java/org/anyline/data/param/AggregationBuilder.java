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
            groups.group(column);
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
