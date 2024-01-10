package org.anyline.metadata.type;

public enum AggregationType {
    MIN			    ("MIN"  			, "最小"),
    MAX			    ("MAX"  			, "最大"),
    SUM			    ("SUM"  			, "求和"),
    REPLACE			    ("REPLACE"  			, "替换"), //对于维度列相同的行，指标列会按照导入的先后顺序，后导入的替换先导入的。
    REPLACE_IF_NOT_NULL			    ("REPLACE_IF_NOT_NULL"  			, "非空值替换"), //和 REPLACE 的区别在于对于null值，不做替换。这里要注意的是字段默认值要给NULL，而不能是空字符串，如果是空字符串，会给你替换成空字符串。
    HLL_UNION			    ("HLL_UNION"  			, "HLL 类型的列的聚合方式"),//通过 HyperLogLog 算法聚合
    BITMAP_UNION			    ("BITMAP_UNION"  			, "BIMTAP 类型的列的聚合方式，");//进行位图的并集聚合
    final String code;
    final String name;
    AggregationType(String code, String name){
        this.code = code;
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public String getCode(){
        return code;
    }
}
