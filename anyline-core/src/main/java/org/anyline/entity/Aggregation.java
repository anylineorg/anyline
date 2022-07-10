package org.anyline.entity;

public enum Aggregation {
    MIN			{public String getCode(){return "MIN";}  			public String getName(){return "最小";}},
    MAX			{public String getCode(){return "MAX";} 			public String getName(){return "最大";}},
    AVG		    {public String getCode(){return "AVG";}  			public String getName(){return "平均";}},
    SUM			{public String getCode(){return "SUM";}  			public String getName(){return "合计";}},
    COUNT		{public String getCode(){return "COUNT";}  			public String getName(){return "数量";}},
    STDEV 		{public String getCode(){return "STDEV";}  			public String getName(){return "标准偏差";}},
    STDEVA 		{public String getCode(){return "STDEVA";} 		    public String getName(){return "标准偏差";}},
    STDEVP		{public String getCode(){return "STDEVP";}      	public String getName(){return "标准偏差";}},
    STDEVPA		{public String getCode(){return "STDEVPA";}      	public String getName(){return "标准偏差";}},
    VAR		    {public String getCode(){return "VAR";}             public String getName(){return "方差";}},
    VARA		{public String getCode(){return "VARA";}   	        public String getName(){return "方差";}},
    VARP		{public String getCode(){return "VARP";}  		    public String getName(){return "方差";}},
    VARPA		{public String getCode(){return "VARPA";}  			public String getName(){return "方差";}};
    public abstract String getName();
    public abstract String getCode();
}
