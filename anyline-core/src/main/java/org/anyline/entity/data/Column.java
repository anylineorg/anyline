package org.anyline.entity.data;

import org.anyline.entity.metadata.ColumnType;
import org.anyline.entity.metadata.JavaType;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.util.*;

public class Column {

    public static <T extends Column> List<String> names(LinkedHashMap<String, T> columns){
        List<String> names = new ArrayList<>();
        for(T column:columns.values()){
            names.add(column.getName());
        }
        return names;
    }

    public static  <T extends Column>  void sort(Map<String,T> columns){
        List<T> list = new ArrayList<>();
        list.addAll(columns.values());
        sort(list);
        columns.clear();
        for(T column:list){
            columns.put(column.getName().toUpperCase(), column);
        }
    }
    public static  <T extends Column>  void sort(List<T> columns){
        Collections.sort(columns, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                Integer p1 = o1.getPosition();
                Integer p2 = o2.getPosition();
                if(p1 == p2){
                    return 0;
                }
                if(null == p1){
                    return -1;
                }
                if(null == p2){
                    return 1;
                }
                return p1 > p2 ? 1:-1;
            }
        });
    }
    protected String keyword = "COLUMN"           ;

    protected String name                         ; // 名称
    protected String originalName                 ; // 原名 SELECT ID AS USER_ID FROM USER; originalName=ID, name=USER_ID
    protected String catalog                      ; // 数据库 catalog与schema 不同有数据库实现方式不一样
    protected String schema                       ; // dbo mysql中相当于数据库名  查数据库列表 是用SHOW SCHEMAS 但JDBC con.getCatalog()返回数据库名 而con.getSchema()返回null
    protected String className                    ; // 对应的Java数据类型 java.lang.Long
    protected String tableName                    ; // 表名
    protected Table table                         ; // 表
    protected Integer displaySize                 ; // display size
    protected String comment                      ; // 备注
    protected Integer type                        ; // 类型
    protected String typeName                     ; // 类型名称 varchar完整类型调用getFullType > varchar(10)
    protected ColumnType columnType               ;
    protected JavaType javaType                   ;
    protected String jdbcType                     ; // 有可能与typeName不一致 可能多个typeName对应一个jdbcType 如point>
    protected Integer precision                   ; // 整个字段的长度(包含小数部分)  123.45：precision = 5 ,scale = 2 对于SQL Server 中 varchar(max)设置成 -1 null:表示未设置
    protected Integer scale                       ; // 小数部分的长度
    protected String dateScale                    ; // 日期类型 精度
    protected int nullable                   = -1 ; // 是否可以为NULL -1:未配置 1:是  0:否
    protected int caseSensitive              = -1 ; // 是否区分大小写
    protected int isCurrency                 = -1 ; // 是否是货币
    protected int isSigned                   = -1 ; // 是否可以带正负号
    protected int isAutoIncrement            = -1 ; // 是否自增
    protected Integer incrementSeed          =  1 ; // 自增起始值
    protected Integer incrementStep          =  1 ; // 自增增量
    protected int isPrimaryKey               = -1 ; // 是否主键
    protected int isGenerated                = -1 ; // 是否generated
    protected Object defaultValue                 ; // 默认值
    protected String charset                      ; // 编码
    protected String collate                      ; // 排序编码
    protected String reference                    ; // 外键依赖列
    protected int srid                            ; // SRID


    protected Integer position                    ; // 在表或索引中的位置,如果需要在第一列 设置成0
    protected String order                        ; // 在索引中的排序方式ASC | DESC

    protected String after                        ; // 修改列时 在表中的位置
    protected String before                       ; // 修改列时 在表中的位置
    protected int isOnUpdate                 = -1 ; // 是否在更新行时 更新这一列数据
    protected Object value                        ;


    protected boolean drop = false                ;
    protected String action = null                ; //ddl命令 add drop alter

    protected Column update                       ;
    protected boolean setmap = false              ;  //执行了upate()操作后set操作是否映射到update上(除了table,catalog,schema,name,drop,action)
    protected boolean getmap = false              ;  //执行了upate()操作后get操作是否映射到update上(除了table,catalog,schema,name,drop,action)



    public Column(){
        this(null);
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
    }




    public Column update(){
        return update(true, true);
    }

    public Column update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }

    public Column getUpdate() {
        return update;
    }

    public Column setUpdate(Column update, boolean setmap, boolean getmap) {
        BeanUtil.copyFieldValueNvl(update, this);
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        update.update = null;
        return this;
    }

    public String getDateScale() {
        if(getmap && null != update){
            return update.getDateScale();
        }
        return dateScale;
    }

    public Column setDateScale(String dateScale) {
        if(setmap && null != update){
            update.setDateScale(dateScale);
            return this;
        }
        this.dateScale = dateScale;
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public Column setCatalog(String catalog) {
        this.catalog = catalog;
        if(null != table){
            table.setCatalog(catalog);
        }
        return this;
    }

    public String getClassName() {
        if(getmap && null != update){
            return update.getClassName();
        }
        return className;
    }

    public Column setClassName(String className) {
        if(setmap && null != update){
            update.setClassName(className);
            return this;
        }
        this.className = className;
        return this;
    }

    public Integer getDisplaySize() {
        if(getmap && null != update){
            return update.getDisplaySize();
        }
        return displaySize;
    }

    public Column setDisplaySize(Integer displaySize) {
        if(setmap && null != update){
            update.setDisplaySize(displaySize);
            return this;
        }
        this.displaySize = displaySize;
        return this;
    }

    public String getComment() {
        if(getmap && null != update){
            return update.comment;
        }
        return comment;
    }

    public Column setComment(String comment) {
        if(setmap && null != update){
            update.setComment(comment);
            return this;
        }
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
        if(getmap && null != update){
            return update.type;
        }
        return type;
    }

    /**
     * 设置数据类型 根据jdbc定义的类型ID
     * @param type type
     * @return Column
     */
    public Column setType(Integer type) {
        if(setmap && null != update){
            update.setType(type);
            return this;
        }
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
        if(setmap && null != update){
            update.setType(type);
            return this;
        }
        return setTypeName(type);
    }

    public Table getTable() {
        return table;
    }

    /**
     * 相关表
     * @param update 是否检测upate
     * @return table
     */
    public Table getTable(boolean update) {
        if(update){
            if(null != table && null != table.getUpdate()){
                return table.getUpdate();
            }
        }
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setTable(String table) {
       setTableName(table);
    }

    public String getTypeName() {
        if(getmap && null != update){
            return update.typeName;
        }
        return typeName;
    }

    public String getJdbcType() {
        if(getmap && null != update){
            return update.jdbcType;
        }
        return jdbcType;
    }

    public Column setJdbcType(String jdbcType) {
        if(setmap && null != update){
            update.setJdbcType(jdbcType);
            return this;
        }
        this.jdbcType = jdbcType;
        return this;
    }

    /**
     * 设置数据类型 根据数据库定义的数据类型
     * @param typeName 数据类型 如 int  varchar(10) decimal(18,6)
     * @return Column
     */
    public Column setTypeName(String typeName) {
        if(setmap && null != update){
            update.setTypeName(typeName);
            return this;
        }
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
                this.precision = 0;
                this.scale = 0;
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
        if(getmap && null != update){
            return update.getPrecision();
        }
        return precision;
    }

    public Column setPrecision(Integer precision) {
        if(setmap && null != update){
            update.setPrecision(precision);
            return this;
        }
        this.precision = precision;
        return this;
    }
    public Column setPrecision(Integer precision, Integer scale) {
        if(setmap && null != update){
            update.setPrecision(precision, scale);
            return this;
        }
        this.precision = precision;
        this.scale = scale;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public Column setSchema(String schema) {
        this.schema = schema;
        if(null != table){
            table.setSchema(schema);
        }
        return this;
    }

    public String getTableName() {
        if(null != table){
            Table update = table.getUpdate();
            if(null != update){
                return update.getName();
            }
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
        if(getmap && null != update){
            return update.value;
        }
        return value;
    }

    public Column setValue(Object value) {
        if(setmap && null != update){
            update.setValue(value);
            return this;
        }
        this.value = value;
        return this;
    }

    public int isCaseSensitive() {
        if(getmap && null != update){
            return update.caseSensitive;
        }
        return caseSensitive;
    }

    public Column setCaseSensitive(int caseSensitive) {
        if(setmap && null != update){
            update.setCaseSensitive(caseSensitive);
            return this;
        }
        this.caseSensitive = caseSensitive;
        return this;
    }
    public Column setCaseSensitive(Boolean caseSensitive) {
        if(setmap && null != update){
            update.setCaseSensitive(caseSensitive);
            return this;
        }
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
        if(getmap && null != update){
            return update.isCurrency;
        }
        return isCurrency;
    }

    public Column setCurrency(int currency) {
        if(setmap && null != update){
            update.setCurrency(currency);
            return this;
        }
        this.isCurrency = currency;
        return this;
    }
    public Column setCurrency(Boolean currency) {
        if(setmap && null != update){
            update.setCurrency(currency);
            return this;
        }
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
        if(getmap && null != update){
            return update.isSigned;
        }
        return isSigned;
    }

    public Column setSigned(int signed) {
        if(setmap && null != update){
            update.setSigned(signed);
            return this;
        }
        this.isSigned = signed;
        return this;
    }
    public Column setSigned(Boolean signed) {
        if(setmap && null != update){
            update.setSigned(signed);
            return this;
        }
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
        if(getmap && null != update){
            return update.getScale();
        }
        return scale;
    }

    public Column setScale(Integer scale) {
        if(setmap && null != update){
            update.setScale(scale);
            return this;
        }
        this.scale = scale;
        return this;
    }

    public int isNullable() {
        if(getmap && null != update){
            return update.nullable;
        }
        return nullable;
    }

    public Column setNullable(int nullable) {
        if(setmap && null != update){
            update.setNullable(nullable);
            return this;
        }
        this.nullable = nullable;
        return this;
    }
    public Column setNullable(Boolean nullable) {
        if(setmap && null != update){
            update.setNullable(nullable);
            return this;
        }
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
        if(getmap && null != update){
            return update.isAutoIncrement;
        }
        return isAutoIncrement;
    }

    public Column setAutoIncrement(int autoIncrement) {
        if(setmap && null != update){
            update.setAutoIncrement(autoIncrement);
            return this;
        }
        this.isAutoIncrement = autoIncrement;
        if(autoIncrement == 1){
            setNullable(false);
        }
        return this;
    }

    public Column setAutoIncrement(Boolean autoIncrement) {
        if(setmap && null != update){
            update.setAutoIncrement(autoIncrement);
            return this;
        }
        if(null != autoIncrement) {
            if(autoIncrement){
                this.isAutoIncrement = 1;
                setNullable(false);
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
        if(setmap && null != update){
            update.setAutoIncrement(seed, step);
            return this;
        }
        setAutoIncrement(1);
        this.incrementSeed= seed;
        this.incrementStep = step;
        return this;
    }

    public int isPrimaryKey() {
        if(getmap && null != update){
            return update.isPrimaryKey;
        }
        return isPrimaryKey;
    }

    public Column setPrimaryKey(int primaryKey) {
        if(setmap && null != update){
            update.setPrimaryKey(primaryKey);
            return this;
        }
        this.isPrimaryKey = primaryKey;
        return this;
    }
    public Column setPrimaryKey(Boolean primaryKey) {
        if(setmap && null != update){
            update.setPrimaryKey(primaryKey);
            return this;
        }
        if(null != primaryKey){
            if(primaryKey){
                this.isPrimaryKey = 1 ;
                setNullable(false);
            }else{
                this.isPrimaryKey = 0 ;
            }
        }
        return this;
    }

    public int isGenerated() {
        if(getmap && null != update){
            return update.isGenerated;
        }
        return isGenerated;
    }

    public Column setGenerated(int generated) {
        if(setmap && null != update){
            update.setGenerated(generated);
            return this;
        }
        this.isGenerated = generated;
        return this;
    }
    public Column setGenerated(Boolean generated) {
        if(setmap && null != update){
            update.setGenerated(generated);
            return this;
        }
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
        if(getmap && null != update){
            return update.defaultValue;
        }
        return defaultValue;
    }

    public Column setDefaultValue(Object defaultValue) {
        if(setmap && null != update){
            update.setDefaultValue(defaultValue);
            return this;
        }
        this.defaultValue = defaultValue;
        return this;
    }

    public Integer getPosition() {
        if(getmap && null != update){
            return update.position;
        }
        return position;
    }

    public String getOrder() {
        if(getmap && null != update){
            return update.order;
        }
        return order;
    }

    public Column setOrder(String order) {
        if(setmap && null != update){
            update.setOrder(order);
            return this;
        }
        this.order = order;
        return this;
    }

    public Column setPosition(Integer position) {
        if(setmap && null != update){
            update.setPosition(position);
            return this;
        }
        this.position = position;
        return this;
    }

    public String getAfter() {
        if(getmap && null != update){
            return update.after;
        }
        return after;
    }

    public Integer getIncrementSeed() {
        if(getmap && null != update){
            return update.incrementSeed;
        }
        return incrementSeed;
    }

    public Column setIncrementSeed(Integer incrementSeed) {
        if(setmap && null != update){
            update.setIncrementSeed(incrementSeed);
            return this;
        }
        this.incrementSeed = incrementSeed;
        return this;
    }

    public Integer getIncrementStep() {
        if(getmap && null != update){
            return update.incrementStep;
        }
        return incrementStep;
    }

    public Column setIncrementStep(Integer incrementStep) {
        if(setmap && null != update){
            update.setIncrementStep(incrementStep);
            return this;
        }
        this.incrementStep = incrementStep;
        return this;
    }

    public int isOnUpdate() {
        if(getmap && null != update){
            return update.isOnUpdate;
        }
        return isOnUpdate;
    }

    public Column setOnUpdate(int onUpdate) {
        if(setmap && null != update){
            update.setOnUpdate(onUpdate);
            return this;
        }
        this.isOnUpdate = onUpdate;
        return this;
    }
    public Column setOnUpdate(Boolean onUpdate) {
        if(setmap && null != update){
            update.setOnUpdate(onUpdate);
            return this;
        }
        if(null != onUpdate){
            if(onUpdate){
                this.isOnUpdate = 1;
            }else{
                this.isOnUpdate = 0;
            }
        }
        return this;
    }
  

    public Column setAfter(String after) {
        if(setmap && null != update){
            update.setAfter(after);
            return this;
        }
        this.after = after;
        return this;
    }

    public String getOriginalName() {
        if(getmap && null != update){
            return update.originalName;
        }
        return originalName;
    }

    public Column setOriginalName(String originalName) {
        if(setmap && null != update){
            update.setOriginalName(originalName);
            return this;
        }
        this.originalName = originalName;
        return this;
    }

    public String getBefore() {
        if(getmap && null != update){
            return update.before;
        }
        return before;
    }

    public String getCharset() {
        if(getmap && null != update){
            return update.charset;
        }
        return charset;
    }

    public Column setCharset(String charset) {
        if(setmap && null != update){
            update.setCharset(charset);
            return this;
        }
        this.charset = charset;
        return this;
    }

    public String getCollate() {
        if(getmap && null != update){
            return update.collate;
        }
        return collate;
    }

    public Column setCollate(String collate) {
        if(setmap && null != update){
            update.setCollate(collate);
            return this;
        }
        this.collate = collate;
        return this;
    }
    public Column setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public Column setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }
    public Column setBefore(String before) {
        if(setmap && null != update){
            update.setBefore(before);
            return this;
        }
        this.before = before;
        return this;
    } 
    public String getFullType(){
        if(getmap && null != update){
            return update.getFullType();
        }
        return getFullType(typeName);
    }
    public String getFullType(String typeName){
        if(getmap && null != update){
            return update.getFullType(typeName);
        }
        return getFullType(typeName, ignorePrecision());
    }
    public String getFullType(String typeName, boolean ignorePrecision){
        if(getmap && null != update){
            return update.getFullType(typeName, ignorePrecision);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(typeName);
        if(!ignorePrecision) {
            if (null != precision) {
                if (precision > 0) {
                    builder.append("(").append(precision);
                    if (null != scale && scale > 0) {
                        builder.append(",").append(scale);
                    }
                    builder.append(")");
                } else if (precision == -1) {
                    builder.append("(max)");
                }
            }
        }
        return builder.toString();
    }

    
    public void delete() {
        this.drop = true;
    }

    
    public boolean isDelete() {
        return drop;
    }

    
    public void setDelete(boolean drop) {
        this.drop = drop;
    }

    
    public void drop() {
        this.drop = true;
    }

    
    public boolean isDrop() {
        return drop;
    }

    
    public void setDrop(boolean drop) {
        this.drop = drop;
    }

    
    public boolean equals(Column column) {
        if(null == column){
            return false;
        }
        if(!BasicUtil.equals(typeName, column.getTypeName())){
            return false;
        }
        if(!BasicUtil.equals(precision, column.getPrecision())){
            return false;
        }
        if(!BasicUtil.equals(scale, column.getScale())){
            return false;
        }
        if(!BasicUtil.equals(defaultValue, column.getDefaultValue())){
            return false;
        }
        if(!BasicUtil.equals(comment, column.getComment())){
            return false;
        }
        if(!BasicUtil.equals(nullable, column.isNullable())){
            return false;
        }
        if(!BasicUtil.equals(isAutoIncrement, column.isAutoIncrement())){
            return false;
        }
        if(!BasicUtil.equals(charset, column.getCharset())){
            return false;
        }
        if(!BasicUtil.equals(isPrimaryKey, column.isPrimaryKey())){
            return false;
        }

        return true;
    }

    
    public ColumnType getColumnType() {
        if(getmap && null != update){
            return update.columnType;
        }
        return columnType;
    }

    
    public Column setColumnType(ColumnType columnType) {
        if(setmap && null != update){
            update.setColumnType(columnType);
            return this;
        }
        this.columnType = columnType;
        return this;
    }

    
    public JavaType getJavaType() {
        if(getmap && null != update){
            return update.javaType;
        }
        return javaType;
    }

    
    public Column setJavaType(JavaType javaType) {
        if(setmap && null != update){
            update.setJavaType(javaType);
            return this;
        }
        this.javaType = javaType;
        return this;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getSrid() {
        if(getmap && null != update){
            return update.srid;
        }
        return srid;
    }

    public Column setSrid(int srid) {
        if(setmap && null != update){
            update.setSrid(srid);
            return this;
        }
        this.srid = srid;
        return this;
    }

    public String getReference() {
        if(getmap && null != update){
            return update.reference;
        }
        return reference;
    }

    public Column setReference(String reference) {
        if(setmap && null != update){
            update.setReference(reference);
            return this;
        }
        this.reference = reference;
        return this;
    }

    /**
     * 是否需要指定精度 主要用来识别能取出精度，但DDL不需要精度的类型
     * 精确判断通过adapter
     * @return boolean
     */
    public boolean ignorePrecision(){
        if(null != typeName) {
            String chk = typeName.toLowerCase();
            if (chk.contains("date")) {
                return true;
            }
            if (chk.contains("time")) {
                return true;
            }
            if (chk.contains("year")) {
                return true;
            }
            if (chk.contains("text")) {
                return true;
            }
            if (chk.contains("blob")) {
                return true;
            }
            if (chk.contains("json")) {
                return true;
            }
            if (chk.contains("point")) {
                return true;
            }
            if (chk.contains("line")) {
                return true;
            }
            if (chk.contains("polygon")) {
                return true;
            }
            if (chk.contains("geometry")) {
                return true;
            }
        }
        return false;
    }
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(" ");
        builder.append(getFullType());
        if(BasicUtil.isNotEmpty(defaultValue)){
            builder.append(" default ").append(defaultValue);
        }
        return builder.toString();
    }
    public Column clone(){
        Column copy = new Column();
        BeanUtil.copyFieldValueNvl(copy, this);

        copy.update = null;
        copy.setmap = false;
        copy.getmap = false;
        return copy;
    }
    public String getKeyword() {
        return this.keyword;
    }

}

