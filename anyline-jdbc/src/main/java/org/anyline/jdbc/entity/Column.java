package org.anyline.jdbc.entity;

public class Column {

    private String catalogName;
    private String className;
    private String schema                   ;
    private String table                    ;
    private int displaySize                 ;
    private String label                    ;
    private String name                     ;
    private int type                        ;
    private String typeName                 ;
    private int precision                   ; //整个字段的长度  123.45：precision = 5 ，scale = 2
    private int scale                       ; //小数部分的长度
    private boolean nullable                ; //是否可以为NULL
    private boolean caseSensitive           ; //是否区分大小写
    private boolean isCurrency              ; //是否是货币
    private boolean isSigned                ; //是否可以带正负号

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
}
