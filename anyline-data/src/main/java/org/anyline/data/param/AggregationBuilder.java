package org.anyline.data.param;

import org.anyline.data.prepare.RunPrepare;
import org.anyline.entity.Aggregation;
import org.anyline.entity.DataSet;
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
    private List<String> groups = new ArrayList<>();
    public AggregationBuilder(AnylineService service) {
        this.service = service;
    }
    public AggregationBuilder group(String ... columns) {
        for(String column:columns){
            groups.add(column);
        }
        return this;
    }
    public AggregationBuilder agg(Aggregation agg, String column, String result){
        AggregationConfig config = new AggregationConfig(agg, column, result);
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
        for(String group:groups){
            configs.group(group);
        }
        configs.aggregations(aggregations);
        if(null != prepare){
            return service.querys(prepare, configs);
        }else if(null != table){
            return service.querys(table, configs);
        }
        throw new RuntimeException("未提供Table或RunPrepare");
    }

}
