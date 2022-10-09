package org.anyline.data.entity;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.init.DefaultDDListener;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

public class Column implements org.anyline.entity.data.Column{

    protected String keyword = "COLUMN"            ;

    protected String catalog                      ; // 数据库
    protected String className                    ; // java.lang.Long
    protected String schema                       ; // dbo
    protected String tableName                    ; // 表名
    protected Table table                         ; // 表
    protected Integer displaySize                 ; // display size
    protected String comment                      ; // 备注
    protected String name                         ; // 名称
    protected Integer type                        ; // 类型
    protected String typeName                     ; // 类型名称
    protected Integer precision                   ; // 整个字段的长度(包含小数部分)  123.45：precision = 5 ，scale = 2 对于SQL Server 中 varchar(max)设置成 -1
    protected Integer scale                       ; // 小数部分的长度
    protected int nullable                   = -1 ; // 是否可以为NULL
    protected int caseSensitive              = -1 ; // 是否区分大小写
    protected int isCurrency                 = -1 ; // 是否是货币
    protected int isSigned                   = -1 ; // 是否可以带正负号
    protected int isAutoIncrement            = -1 ; // 是否自增
    protected Integer incrementSeed          = 1  ; // 自增起始值
    protected Integer incrementStep          = 1  ; // 自增增量
    protected int isPrimaryKey               = -1 ; // 是否主键
    protected int isGenerated                = -1 ; // 是否generated
    protected Object defaultValue                 ; // 默认值
    protected String charset                      ; // 编码
    protected String collate                      ; // 排序编码

    protected Integer position                    ; // 在表或索引中的位置,如果需要在第一列 设置成0
    protected String order                        ; // 在索引中的排序方式ASC | DESC

    protected String after                        ; // 修改列时 在表中的位置
    protected String before                       ; // 修改列时 在表中的位置
    protected int isOnUpdate                 = -1 ; // 是否在更新行时 更新这一列数据
    protected Object value                        ;

    protected Column update                       ;

    protected DDListener listener                 ;


    public Column(){
        this.listener = new DefaultDDListener();
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
        setCatalog(catalog);
        setSchema(schema);
        setName(name);
        setTable(table);
        this.listener = new DefaultDDListener();
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
        if(null != comment){
            comment = comment.trim().replace("'","");
        }
        this.comment = comment;
        return this;
    }

    public String getName() {
        return name;
    }

    public Column setName(String name) {
        if(null != name){
            name = name.trim().replace("'","");
        }
        this.name = name;
        return this;
    }

    public Integer getType() {
        return type;
    }

    /**
     * 设置数据类型 根据jdbc定义的类型ID
     * @param type type
     * @return Column
     */
    public Column setType(Integer type) {
        if(this.type != type) {
            this.className = null;
        }
        this.type = type;
        return this;
    }
    /**
     * 设置数据类型 根据数据库定义的数据类型 实际调用了setTypeName(String)
     * @param type  数据类型 如 int  varchar(10) decimal(18,6)
     * @return Column
     */
    public Column setType(String type) {
        return setTypeName(type);
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


    /**
     * 设置数据类型 根据数据库定义的数据类型
     * @param typeName 数据类型 如 int  varchar(10) decimal(18,6)
     * @return Column
     */
    public Column setTypeName(String typeName) {
        this.precision = 0;
        this.scale = 0;
        if(null != typeName){
            typeName = typeName.trim().replace("'","");
            if(typeName.toUpperCase().contains("IDENTITY")){
                setAutoIncrement(true);
            }
            if(typeName.contains(" ")) {
                // TYPE_NAME=int identity
                typeName = typeName.split(" ")[0];
            }
            if(typeName.contains("(")){
                String len = typeName.substring(typeName.indexOf("(")+1, typeName.indexOf(")"));
                if(len.contains(",")){
                    String[] lens = len.split("\\,");
                    setPrecision(BasicUtil.parseInt(lens[0], null));
                    setScale(BasicUtil.parseInt(lens[1], null));
                }else{
                    setPrecision(BasicUtil.parseInt(len,null));
                }
                typeName = typeName.substring(0,typeName.indexOf("(") );
            }
        }
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int isCaseSensitive() {
        return caseSensitive;
    }

    public Column setCaseSensitive(int caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }
    public Column setCaseSensitive(Boolean caseSensitive) {
        if(null != caseSensitive) {
            if(caseSensitive) {
                this.caseSensitive = 1;
            }else {
                this.caseSensitive = 0;
            }
        }
        return this;
    }

    public int isCurrency() {
        return isCurrency;
    }

    public Column setCurrency(int currency) {
        this.isCurrency = currency;
        return this;
    }
    public Column setCurrency(Boolean currency) {
        if(null != currency){
            if(currency){
                this.isCurrency = 1;
            }else{
                this.isCurrency = 0;
            }
        }
        return this;
    }

    public int isSigned() {
        return isSigned;
    }

    public Column setSigned(int signed) {
        this.isSigned = signed;
        return this;
    }
    public Column setSigned(Boolean signed) {
        if(null != signed){
            if(signed){
                this.isSigned = 1;
            }else{
                this.isSigned = 0;
            }
        }
        return this;
    }

    public Integer getScale() {
        return scale;
    }

    public Column setScale(Integer scale) {
        this.scale = scale;
        return this;
    }

    public int isNullable() {
        return nullable;
    }

    public Column setNullable(int nullable) {
        this.nullable = nullable;
        return this;
    }
    public Column setNullable(Boolean nullable) {
        if(null != nullable){
            if(nullable){
                this.nullable = 1;
            }else{
                this.nullable = 0;
            }
        }
        return this;
    }

    public int isAutoIncrement() {
        return isAutoIncrement;
    }

    public Column setAutoIncrement(int autoIncrement) {
        this.isAutoIncrement = autoIncrement;
        return this;
    }

    public Column setAutoIncrement(Boolean autoIncrement) {
        if(null != autoIncrement) {
            if(autoIncrement){
                this.isAutoIncrement = 1;
            }else{
                this.isAutoIncrement = 0;
            }
        }
        return this;
    }

    /**
     * 递增列
     * @param seed 起始值
     * @param step 增量
     * @return  Column
     */
    public Column setAutoIncrement(int seed, int step) {
        setAutoIncrement(1);
        this.incrementSeed= seed;
        this.incrementStep = step;
        return this;
    }

    public int isPrimaryKey() {
        return isPrimaryKey;
    }

    public Column setPrimaryKey(int primaryKey) {
        this.isPrimaryKey = primaryKey;
        return this;
    }
    public Column setPrimaryKey(Boolean primaryKey) {
        if(null != primaryKey){
            if(primaryKey){
                this.isPrimaryKey = 1 ;
            }else{
                this.isPrimaryKey = 0 ;
            }
        }
        return this;
    }

    public int isGenerated() {
        return isGenerated;
    }

    public Column setGenerated(int generated) {
        this.isGenerated = generated;
        return this;
    }
    public Column setGenerated(Boolean generated) {
        if(null != generated){
            if(generated){
                this.isGenerated = 1;
            }else{
                this.isGenerated = 0;
            }
        }
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

    public int isOnUpdate() {
        return isOnUpdate;
    }

    public Column setOnUpdate(int onUpdate) {
        this.isOnUpdate = onUpdate;
        return this;
    }
    public Column setOnUpdate(Boolean onUpdate) {
        if(null != onUpdate){
            if(onUpdate){
                this.isOnUpdate = 1;
            }else{
                this.isOnUpdate = 0;
            }
        }
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
    public Column setCreater(JDBCAdapter adapter){
        if(null != listener){
            listener.setAdapter(adapter);
        }
        return this;
    }
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(" ").append(typeName);
        if(null != precision && precision > 0){
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
        copy.setTypeName(typeName);
        copy.setCatalog(catalog);
        copy.setClassName(className);
        copy.setSchema(schema);
        copy.setTableName(tableName);
        copy.setDisplaySize(displaySize);
        copy.setComment(comment);
        copy.setType(type);
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
    public String getKeyword() {
        return this.keyword;
    }
}

