package org.anyline.data.jdbc.adapter.init.alias;

import org.anyline.data.metadata.PropertyAlias;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;

public enum MySQLGenusPropertyAlias implements PropertyAlias {
    MergeTree ("engine", "MergeTree", "InnoDB", Table.class);

    private String group                        ; // 属性分组 如 ENGINE
    private String input                        ; // 输入属性名称
    private String value                        ; // 兼容当前数据库的类型
    private Class<? extends Metadata> metadata  ; // 适用类型 如Table.class


    MySQLGenusPropertyAlias(String group, String input, String value, Class<? extends Metadata> metadata) {
        this.input = input;
        this.value = value;
        this.group = group;
        this.metadata = metadata;
    }
    @Override
    public String input(){
        return input;
    }
    @Override
    public String group(){
        return group;
    }
    @Override
    public String value(){
        return value;
    }
    @Override
    public Class<? extends Metadata> metadata(){
        return metadata;
    }
}
