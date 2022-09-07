package org.anyline.jdbc.entity;

public class Column {

    private String catalogName              ; //数据库
    private String className                ; //java.lang.Long
    private String schema                   ; //dbo
    private String table                    ; //表名
    private int displaySize                 ; //display size
    private String label                    ; //备注
    private String name                     ; //名称
    private int type                        ; //类型
    private String typeName                 ; //类型名称
    private int precision                   ; //整个字段的长度  123.45：precision = 5 ，scale = 2
    private int scale                       ; //小数部分的长度
    private boolean nullable                ; //是否可以为NULL
    private boolean caseSensitive           ; //是否区分大小写
    private boolean isCurrency              ; //是否是货币
    private boolean isSigned                ; //是否可以带正负号
    private boolean isAutoIncrement         ; //是否自增
    private boolean isPrimaryKey            ; //是否主键
    private String primaryKeyName           ; //主键名称
    private int primaryKeyIndex             ; //主键顺序
    private boolean isGenerated             ; //是否generated
    private Object defaultValue             ; //默认值

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

    public String getCatalogName() {
        return catalogName;
    }

    public Column setCatalogName(String catalogName) {
        this.catalogName = catalogName;
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

    public Column setScale(int scale) {
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

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public int getPrimaryKeyIndex() {
        return primaryKeyIndex;
    }

    public void setPrimaryKeyIndex(int primaryKeyIndex) {
        this.primaryKeyIndex = primaryKeyIndex;
    }
}
