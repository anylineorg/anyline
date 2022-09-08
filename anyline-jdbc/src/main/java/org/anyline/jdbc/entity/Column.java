package org.anyline.jdbc.entity;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

public class Column {

    private String catalog                  ; //数据库
    private String className                ; //java.lang.Long
    private String schema                   ; //dbo
    private String table                    ; //表名
    private int displaySize                 ; //display size
    private String label                    ; //备注
    private String name                     ; //名称
    private int type                        ; //类型
    private String typeName                 ; //类型名称
    private int precision                   ; //整个字段的长度  123.45：precision = 5 ，scale = 2
    private Integer scale                   ; //小数部分的长度
    private boolean nullable                ; //是否可以为NULL
    private boolean caseSensitive           ; //是否区分大小写
    private boolean isCurrency              ; //是否是货币
    private boolean isSigned                ; //是否可以带正负号
    private boolean isAutoIncrement         ; //是否自增
    private boolean isPrimaryKey            ; //是否主键
    private boolean isGenerated             ; //是否generated
    private Object defaultValue             ; //默认值
    private int position                    ; //在表或索引中的位置
    private String order                    ; //在索引中的排序方式ASC | DESC

    private String after                    ; //修改列时 在表中的位置
    private String before                   ; //修改列时 在表中的位置

    private Column update;

    public Column update(){
        update = new Column();
        return update;
    }

    public Column getUpdate() {
        return update;
    }

    public Column setUpdate(Column update) {
        this.update = update;
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public Column setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public Column setClassName(String className) {
        this.className = className;
        return this;
    }

    public int getDisplaySize() {
        return displaySize;
    }

    public Column setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Column setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getName() {
        return name;
    }

    public Column setName(String name) {
        this.name = name;
        return this;
    }

    public int getType() {
        return type;
    }

    public Column setType(int type) {
        this.type = type;
        return this;
    }

    public String getTypeName() {
        return typeName;
    }

    public Column setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    public int getPrecision() {
        return precision;
    }

    public Column setPrecision(int precision) {
        this.precision = precision;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public Column setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getTable() {
        return table;
    }

    public Column setTable(String table) {
        this.table = table;
        return this;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public Column setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public boolean isCurrency() {
        return isCurrency;
    }

    public Column setCurrency(boolean currency) {
        isCurrency = currency;
        return this;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public Column setSigned(boolean signed) {
        isSigned = signed;
        return this;
    }

    public int getScale() {
        return scale;
    }

    public Column setScale(Integer scale) {
        this.scale = scale;
        return this;
    }

    public boolean isNullable() {
        return nullable;
    }

    public Column setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getPosition() {
        return position;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(" ").append(typeName);
        if(precision > 0){
            builder.append("(").append(precision);
            if(null != scale){
                builder.append(",").append(scale);
            }
            builder.append(")");
        }
        if(BasicUtil.isNotEmpty(defaultValue)){
            builder.append(" default ").append(defaultValue);
        }
        return builder.toString();
    }
    public Object clone(){
        Column copy = new Column();
        copy.setName(name);
        copy.setCatalog(catalog);
        copy.setClassName(className);
        copy.setSchema(schema);
        copy.setTable(table);
        copy.setDisplaySize(displaySize);
        copy.setLabel(label);
        copy.setType(type);
        copy.setTypeName(typeName);
        copy.setPrecision(precision);
        copy.setScale(scale);
        copy.setNullable(nullable);
        copy.setCaseSensitive(caseSensitive);
        copy.setCurrency(isCurrency);
        copy.setSigned(isSigned);
        copy.setPrimaryKey(isPrimaryKey);
        copy.setGenerated(isGenerated);
        copy.setDefaultValue(defaultValue);
        copy.setPosition(position);
        copy.setOrder(order);
        copy.setBefore(before);
        copy.setAfter(after);
        return copy;
    }
}

