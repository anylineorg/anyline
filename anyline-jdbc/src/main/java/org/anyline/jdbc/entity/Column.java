package org.anyline.jdbc.entity;

import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.listener.DDListener;
import org.anyline.listener.impl.DefaulDDtListener;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

public class Column {

    private String catalog                      ; //数据库
    private String className                    ; //java.lang.Long
    private String schema                       ; //dbo
    private String tableName                    ; //表名
    private Table table                         ; //表
    private Integer displaySize                 ; //display size
    private String comment                      ; //备注
    private String name                         ; //名称
    private Integer type                        ; //类型
    private String typeName                     ; //类型名称
    private Integer precision                   ; //整个字段的长度(包含小数部分)  123.45：precision = 5 ，scale = 2 对于SQL Server 中 varchar(max)设置成 -1
    private Integer scale                       ; //小数部分的长度
    private Boolean nullable                    ; //是否可以为NULL
    private Boolean caseSensitive               ; //是否区分大小写
    private Boolean isCurrency                  ; //是否是货币
    private Boolean isSigned                    ; //是否可以带正负号
    private Boolean isAutoIncrement             ; //是否自增
    private Integer incrementSeed       = 1     ; //自增起始值
    private Integer incrementStep       = 1     ; //自增增量
    private Boolean isPrimaryKey                ; //是否主键
    private Boolean isGenerated                 ; //是否generated
    private Object defaultValue                 ; //默认值
    private String charset                      ; //编码
    private String collate                      ; //排序编码

    private Integer position                    ; //在表或索引中的位置,如果需要在第一列 设置成0
    private String order                        ; //在索引中的排序方式ASC | DESC

    private String after                        ; //修改列时 在表中的位置
    private String before                       ; //修改列时 在表中的位置
    private Boolean isOnUpdate                  ; //是否在更新行时 更新这一列数据

    private Column update                       ;

    private DDListener listener                 ;


    public Column(){
        this.listener = new DefaulDDtListener();
    }
    public Column(String name){
        this(null, name);
    }
    public Column(String table, String name){
        this(null, table, name);
    }
    public Column(String schema, String table, String name){
        this(null, schema, table, name);
    }
    public Column(String catalog, String schema, String table, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.tableName = table;
        this.name = name;
        this.listener = new DefaulDDtListener();
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

    public Integer getDisplaySize() {
        return displaySize;
    }

    public Column setDisplaySize(Integer displaySize) {
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

    public Integer getType() {
        return type;
    }

    public Column setType(Integer type) {
        if(this.type != type) {
            this.className = null;
        }
        this.type = type;
        return this;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setTable(String table) {
       setTableName(table);
    }

    public String getTypeName() {
        return typeName;
    }

    public Column setTypeName(String typeName) {
        if(!BasicUtil.equalsIgnoreCase(typeName, this.typeName)) {
            this.className = null;
        }
        this.typeName = typeName;
        return this;
    }

    public Integer getPrecision() {
        return precision;
    }

    public Column setPrecision(Integer precision) {
        this.precision = precision;
        return this;
    }
    public Column setPrecision(Integer precision, Integer scale) {
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

    public String getTableName() {
        if(null != table){
            return table.getName();
        }
        return tableName;
    }

    public Column setTableName(String tableName) {
        this.tableName = tableName;
        this.table = new Table(tableName);
        return this;
    }

    public Boolean isCaseSensitive() {
        return caseSensitive;
    }

    public Column setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public Boolean isCurrency() {
        return isCurrency;
    }

    public Column setCurrency(Boolean currency) {
        isCurrency = currency;
        return this;
    }

    public Boolean isSigned() {
        return isSigned;
    }

    public Column setSigned(Boolean signed) {
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

    public Boolean isNullable() {
        return nullable;
    }

    public Column setNullable(Boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public Boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    public Column setAutoIncrement(Boolean autoIncrement) {
        isAutoIncrement = autoIncrement;
        return this;
    }

    public Boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public Column setPrimaryKey(Boolean primaryKey) {
        isPrimaryKey = primaryKey;
        return this;
    }

    public Boolean isGenerated() {
        return isGenerated;
    }

    public Column setGenerated(Boolean generated) {
        isGenerated = generated;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Column setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Integer getPosition() {
        return position;
    }

    public String getOrder() {
        return order;
    }

    public Column setOrder(String order) {
        this.order = order;
        return this;
    }

    public Column setPosition(Integer position) {
        this.position = position;
        return this;
    }

    public String getAfter() {
        return after;
    }

    public Integer getIncrementSeed() {
        return incrementSeed;
    }

    public Column setIncrementSeed(Integer incrementSeed) {
        this.incrementSeed = incrementSeed;
        return this;
    }

    public Integer getIncrementStep() {
        return incrementStep;
    }

    public Column setIncrementStep(Integer incrementStep) {
        this.incrementStep = incrementStep;
        return this;
    }

    public Boolean isOnUpdate() {
        return isOnUpdate;
    }

    public Column setOnUpdate(Boolean onUpdate) {
        isOnUpdate = onUpdate;
        return this;
    }

    public DDListener getListener() {
        return listener;
    }

    public Column setListener(DDListener listener) {
        this.listener = listener;
        return this;
    }

    public Column setAfter(String after) {
        this.after = after;
        return this;
    }

    public String getBefore() {
        return before;
    }

    public String getCharset() {
        return charset;
    }

    public Column setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public String getCollate() {
        return collate;
    }

    public Column setCollate(String collate) {
        this.collate = collate;
        return this;
    }

    public String getNewName() {
        if(null != update){
            return update.getName();
        }
        return null;
    }

    public Column setNewName(String newName) {
        if(null == update){
            update();
        }
        update.setName(newName);
        return update;
    }
    public Column setBefore(String before) {
        this.before = before;
        return this;
    }
    public Column setService(AnylineService service){
        if(null != listener){
            listener.setService(service);
        }
        return this;
    }
    public Column setCreater(SQLCreater creater){
        if(null != listener){
            listener.setCreater(creater);
        }
        return this;
    }
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(" ").append(typeName);
        if(precision > 0){
            builder.append("(").append(precision);
            if(null != scale && scale > 0){
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
        copy.setTableName(tableName);
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

