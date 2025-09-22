package org.anyline.metadata;

import org.anyline.metadata.type.JavaType;
import org.anyline.metadata.type.TypeMetadata;

public class DataType {

    protected String name                         ; // 类型名称 varchar完整类型调用getFullType > varchar(10)
    protected String originName                   ; // 原名,只有查询时才会区分,添加列时用name即可 SELECT ID AS USER_ID FROM USER; originName=ID, name=USER_ID
    protected String qualifier                    ; // 数据类型限定符 DATETIME YEAR TO MINUTE(6) 中的YEAR TO MINUTE部分 表达式中以{Q}表示
    protected String originType                   ; // 原始类型(未解析,交给具体的adapter解析)
    protected TypeMetadata metadata               ;
    protected String fullType                     ; // 完整类型名称
    protected String finalType                    ; // 如果设置了finalType 生成SQL时 name finalType 其他属性
    protected Integer type                        ; // 类型
    protected DataType child                      ;
    protected JavaType javaType                   ;
    protected String jdbcType                     ; // 有可能与typeName不一致 可能多个typeName对应一个jdbcType 如point>

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getOriginType() {
        return originType;
    }

    public void setOriginType(String originType) {
        this.originType = originType;
    }

    public TypeMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TypeMetadata metadata) {
        this.metadata = metadata;
    }

    public String getFullType() {
        return fullType;
    }

    public void setFullType(String fullType) {
        this.fullType = fullType;
    }

    public String getFinalType() {
        return finalType;
    }

    public void setFinalType(String finalType) {
        this.finalType = finalType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public DataType getChild() {
        return child;
    }

    public void setChild(DataType child) {
        this.child = child;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public void setJavaType(JavaType javaType) {
        this.javaType = javaType;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }
}
