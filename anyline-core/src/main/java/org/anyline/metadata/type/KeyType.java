package org.anyline.metadata.type;

public enum KeyType {
    DUPLICATE 			("DUPLICATE"  			, "排序列"),
    AGGREGATE 			("AGGREGATE"  			, "维度列"),
    UNIQUE 			    ("UNIQUE"  			, "主键列");
    final String code;
    final String name;
    KeyType(String code, String name){
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
