package org.anyline.data.param;

import org.anyline.entity.Aggregation;

public class AggregationConfig {
    private Aggregation agg;
    private String column;
    private String result;
    public AggregationConfig(Aggregation agg, String column, String result) {
        this.agg = agg;
        this.column = column;
        this.result = result;
    }
}
