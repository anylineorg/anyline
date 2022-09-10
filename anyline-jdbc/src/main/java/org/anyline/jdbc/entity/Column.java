package org.anyline.jdbc.entity;

import org.anyline.listener.DDListener;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

public class Column {

    private String catalog                  ; //数据库
    private String className                ; //java.lang.Long
    private String schema                   ; //dbo
    private String table                    ; //表名
    private int displaySize                 ; //display size
    private String comment                  ; //备注
    private String name                     ; //名称
    private int type                        ; //类型
    private String typeName                 ; //类型名称
    private int precision                   ; //整个字段的长度(包含小数部分)  123.45：precision = 5 ，scale = 2 对于SQL Server 中 varchar(max)设置成 -1
    private Integer scale                   ; //小数部分的长度
    private boolean nullable                ; //是否可以为NULL
    private boolean caseSensitive           ; //是否区分大小写
    private boolean isCurrency              ; //是否是货币
    private boolean isSigned                ; //是否可以带正负号
    private boolean isAutoIncrement         ; //是否自增
    private int incrementSeed = 1           ; //自增起始值
    private int incrementStep = 1           ; //自增增量
    private boolean isPrimaryKey            ; //是否主键
    private boolean isGenerated             ; //是否generated
    private Object defaultValue             ; //默认值
    private String charset                  ; //编码
    private String collate                  ; //排序编码

    private Integer position                ; //在表或索引中的位置,如果需要在第一列 设置成0
    private String order                    ; //在索引中的排序方式ASC | DESC

    private String after                    ; //修改列时 在表中的位置
    private String before                   ; //修改列时 在表中的位置
    private boolean isOnUpdate              ; //是否在更新行时 更新这一列数据

    private Column update                   ;

    private DDListener listener               ; //修改事件


    public Column(){
    }
    public Column(String name){
        this(null, name);
    }
    public Column(String table, String name){
        this(null, table, name);
    }
    public Column(String catalog, String schema, String table, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.name = name;
    }
    public Column(String schema, String table, String name){
        this(null, schema, table, name);
    }
    public Column update(){
        update = (Column) this.clone();
        return update;
    }

    public Column getUpdate() {
        return update;
    }

    public Column setUpdate(Column update) {
        BeanUtil.copyFieldValueNvl(update, this);
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

    public String getComment() {
        return comment;
    }

    public Column setComment(String comment) {
        this.comment = comment;
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
    public Column setPrecision(int precision, int scale) {
        this.precision = precision;
        this.scale = scale;
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

    public Integer getScale() {
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

    public Integer getPosition() {
        return position;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getAfter() {
        return after;
    }

    public int getIncrementSeed() {
        return incrementSeed;
    }

    public void setIncrementSeed(int incrementSeed) {
        this.incrementSeed = incrementSeed;
    }

    public int getIncrementStep() {
        return incrementStep;
    }

    public void setIncrementStep(int incrementStep) {
        this.incrementStep = incrementStep;
    }

    public boolean isOnUpdate() {
        return isOnUpdate;
    }

    public void setOnUpdate(boolean onUpdate) {
        isOnUpdate = onUpdate;
    }

    public DDListener getListener() {
        return listener;
    }

    public void setListener(DDListener listener) {
        this.listener = listener;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
    }

    public String getNewName() {
        if(null != update){
            return update.getName();
        }
        return null;
    }

    public void setNewName(String newName) {
        if(null == update){
            update();
        }
        update.setName(newName);
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
        copy.setComment(comment);
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
        copy.setCharset(charset);
        copy.setCollate(collate);
        return copy;
    }
}

