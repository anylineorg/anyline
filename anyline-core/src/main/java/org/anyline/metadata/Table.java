/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.metadata;

import org.anyline.exception.AnylineException;
import org.anyline.metadata.differ.TableDiffer;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.Serializable;
import java.util.*;

public class Table<E extends Table> extends BaseMetadata<E> implements Serializable {
    public enum TYPE implements Type{
        NORMAL(1),
        VIEW(2);
        public final int value;
        TYPE(int vaule){
            this.value = vaule;
        }
        public int value(){
            return value;
        }
    }
    private static Map<Integer, Type> types = new HashMap<>();
    static {
        for(TYPE type: TYPE.values()){
            types.put(type.value, type);
        }
    }
    public static Map<Integer, Type> types(){
        return types;
    }
    //struct 是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
    enum STRUCT{
        COLUMN(4),
        PRIMARY(8),
        FOREIGN(16),
        INDEX(32),
        CONSTRAINT(64),
        DDL(32768);
        public final int value;
        STRUCT(int value){
            this.value = value;
        }
    }
    protected String keyword = "TABLE"            ;
    /**
     * 继承自
     */
    protected Table inherit;
    /**
     * 主表(相对于分区表)
     */
    protected Table master;
    /**
     * 分区
     * partition by :分区方式(LIST, RANGE, HASH)及 依据列
     * partition of :主表
     * partition for:分区依据值
     */
    protected Partition partition ;

    /**
     * 表类型 不同数据库有所区别
     */
    protected String type                         ;
    /**
     * 地理坐标系
     */
    protected int srid                            ;

    protected String typeCat                      ;
    protected String typeSchema                   ;
    protected String typeName                     ;
    /**
     * 指定 "identifier" 列的名称
     */
    protected String selfReferencingColumn        ;
    /**
     * 指定在 SELF_REFERENCING_COL_NAME 中创建值的方式。如 SYSTEM USER DERIVED
     */
    protected String refGeneration                ;

    /**
     * 数据库引擎
     */
    protected String engine                       ;

    private String engineParameters               ;

    /**
     * 编码
     */
    protected String charset                      ;
    /**
     * 排序规则
     */
    protected String collate                      ;
    /**
     * 数据的过期时间
     */
    protected Long ttl                            ;

    /**
     * 创建时间
     */
    protected Date createTime;
    /**
     * 修改结构时间
     */
    protected Date updateTime;
    /**
     * 数据行数
     */
    protected Long dataRows                     ;
    /**
     * 数据长度
     */
    protected Long dataLength                   ;
    /**
     * 下一个自增值
     */
    protected Long increment                    ;
    /**
     * 占用未用空间
     */
    protected Long dataFree                     ;
    /**
     * 索引长度
     */
    protected Long indexLength                  ;
    /**
     * 是否临时表
     */
    protected int temporary                     ;

    /**
     * 主键是否需要更新
     */
    protected int changePrimary = -1             ;
    /**
     * 物化视图
     */
    protected LinkedHashMap<String, View> materializes;
    protected List<Key> keys;
    protected Distribution distribution;
    protected PrimaryKey primaryKey;
    protected LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
    protected LinkedHashMap<String, Tag> tags       = new LinkedHashMap<>();
    protected LinkedHashMap<String, Index> indexes = new LinkedHashMap<>();
    protected LinkedHashMap<String, Constraint> constraints = new LinkedHashMap<>();
    protected boolean sort = false; //列是否排序

    protected boolean autoDropColumn = ConfigTable.IS_DDL_AUTO_DROP_COLUMN;     //执行alter时是否删除 数据库中存在 但table 中不存在的列(属性)


    public Table(){
    }
    public Table(String name){
        if(null != name){
            if(name.contains(":") || name.contains(" ")){
                //自定义XML或sql
                this.name = name;
            }else {
                if (name.contains(".")) {
                    String[] tmps = name.split("\\.");
                    if (tmps.length == 2) {
                        this.schema = new Schema(tmps[0]);
                        this.name = tmps[1];
                    } else if (tmps.length == 3) {
                        this.catalog = new Catalog(tmps[0]);
                        this.schema = new Schema(tmps[1]);
                        this.name = tmps[2];
                    }
                } else {
                    this.name = name;
                }
            }
        }else {
            this.name = name;
        }
    }

    public Table(String schema, String table){
        this(null, schema, table);
    }
    public Table(Schema schema, String table){
        this(null, schema, table);
    }
    public Table(String catalog, String schema, String name){
        if(BasicUtil.isNotEmpty(catalog)) {
            this.catalog = new Catalog(catalog);
        }
        if(BasicUtil.isNotEmpty(schema)) {
            this.schema = new Schema(schema);
        }
        this.name = name;
    }
    public Table(Catalog catalog, Schema schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }

    public Table setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public Distribution getDistribution() {
        if(getmap && null != update){
            return update.getDistribution();
        }
        return distribution;
    }

    public Table setDistribution(Distribution distribution) {
        if(getmap && null != update){
            return update.setDistribution(distribution);
        }
        this.distribution = distribution;
        return this;
    }

    /**
     * 设置分桶方式
     * @param type 分桶方式
     * @param buckets 分桶数量
     * @param columns 分桶依据列
     * @return this
     */
    public Table setDistribution(Distribution.TYPE type, int buckets, String ... columns){
        setDistribution(new Distribution(type, buckets, columns));
        return this;
    }
    /**
     * 设置分桶方式
     * @param type 分桶方式
     * @param columns 分桶依据列
     * @return this
     */
    public Table setDistribution(Distribution.TYPE type, String ... columns){
        setDistribution(new Distribution(type, columns));
        return this;
    }
    public E drop(){
        this.action = ACTION.DDL.TABLE_DROP;
        return super.drop();
    }
    public int getPrimaryKeySize(){
        PrimaryKey pk = getPrimaryKey();
        if(null != pk){
            return pk.getColumns().size();
        }
        return 0;
    }
    public List<Key> getKeys() {
        if(getmap && null != update){
            return update.getKeys();
        }
        return keys;
    }

    public Table setKeys(List<Key> keys) {
        if(getmap && null != update){
            return update.setKeys(keys);
        }
        this.keys = keys;
        return this;
    }
    public Table addKey(Key key){
        if(getmap && null != update){
            return update.addKey(key);
        }
        if(null == keys){
            keys = new ArrayList<>();
        }
        keys.add(key);
        return this;
    }
    public Table addKey(Key.TYPE type, String ... columns){
        if(getmap && null != update){
            return update.addKey(type, columns);
        }
        if(null == keys){
            keys = new ArrayList<>();
        }
        Key key = new Key();
        key.setType(type);
        key.setColumns(columns);
        keys.add(key);
        return this;
    }

    public Partition getPartition() {
        if(getmap && null != update){
            return update.partition;
        }
        return partition;
    }

    /**
     * 分区依据值
     * @param type 分区方式
     * @param values 分区依据值
     * @return Table
     */
    public Table partitionFor(Partition.TYPE type, Object ... values){
        Partition partition = new Partition();
        partition.setType(type);
        partition.addValues(values);
        return setPartition(partition);
    }
    public Table setPartition(Partition partition) {
        if(setmap && null != update){
            update.setPartition(partition);
            return this;
        }
        this.partition = partition;
        return this;
    }

    public Table partitionBy(Partition.TYPE type, String ... columns){
        Partition partition = new Partition();
        partition.setType(type);
        partition.setColumns(columns);
        return setPartition(partition);
    }

    public String getMasterName() {
        if(null != master){
            return master.getName();
        }
        return null;
    }

    public Table setMaster(String master) {
        this.master = new MasterTable(master);
        return this;
    }

    public Table getMaster() {
        return master;
    }

    public Table setPartitionOf(Table master) {
        this.master = master;
        return this;
    }
    public Table setMaster(Table master) {
        this.master = master;
        return this;
    }

    public LinkedHashMap<String, View> getMaterializes() {
        return materializes;
    }

    public Table setMaterializes(LinkedHashMap<String, View> materializes) {
        this.materializes = materializes;
        return this;
    }
    public Table addMaterializes(View view) {
        if(null == this.materializes){
            this.materializes = new LinkedHashMap<>();
        }
        this.materializes.put(view.getName().toUpperCase(), view);
        return this;
    }

    public LinkedHashMap<String, Column> primarys(){
        LinkedHashMap<String, Column> pks = new LinkedHashMap<>();
        for(Map.Entry<String, Column> item:columns.entrySet()){
            Column column = item.getValue();
            String key = item.getKey();
            if(column.isPrimaryKey() == 1){
                pks.put(key, column);
            }
        }
        return pks;
    }
    public Column primary(){
        for(Column column:columns.values()){
            if(column.isPrimaryKey() == 1){
                return column;
            }
        }
        return null;
    }
    public E clone(){
        E copy = super.clone();
        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()){
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;
        return copy;
    }

    public Table addColumn(Column column){
        if(setmap && null != update){
            update.addColumn(column);
            return this;
        }
        column.setTable(this);
        if (null == columns) {
            columns = new LinkedHashMap<>();
        }
        columns.put(column.getName().toUpperCase(), column);

        return this;
    }
    public Long getTtl() {
        if(getmap && null != update){
            return update.ttl;
        }
        return ttl;
    }

    public Table setTtl(Long ttl) {
        if(setmap && null != update){
            update.setTtl(ttl);
            return this;
        }
        this.ttl = ttl;
        return this;
    }
    public Column addColumn(String name, String type, int precision, int scale){
        Column column = new Column(name, type, precision, scale);
        addColumn(column);
        return column;
    }
    public Column addColumn(String name, String type, int precision){
        Column column = new Column(name, type, precision);
        addColumn(column);
        return column;
    }
    public Column addColumn(String name, String type){
        return addColumn(name, type, true, null);
    }
    public Column addColumn(String name, TypeMetadata type){
        return addColumn(name, type, true, null);
    }
    public Column addColumn(String name, String type, boolean nullable, Object def){
        Column column = new Column();
        column.setName(name);
        column.nullable(nullable);
        column.setDefaultValue(def);
        column.setTypeName(type);
        addColumn(column);
        return column;
    }
    public Column addColumn(String name, TypeMetadata type, boolean nullable, Object def){
        Column column = new Column();
        column.setName(name);
        column.nullable(nullable);
        column.setDefaultValue(def);
        column.setTypeMetadata(type);
        addColumn(column);
        return column;
    }
    public Table setPrimaryKey(String ... keys){
        if(setmap && null != update){
            update.setPrimaryKey(keys);
            return this;
        }
        if(null != primaryKey){
            //取消原主键中的列标记
            for(Column column:primaryKey.getColumns().values()){
                column.primary(false);
            }
        }
        primaryKey = new PrimaryKey();
        primaryKey.setTable(this);
        if (null != columns) {
            for (String key : keys) {
                Column column = columns.get(key.toUpperCase());
                if (null != column) {
                    column.primary(true);
                    primaryKey.addColumn(column);
                } else {
                    throw new AnylineException("未匹配到" + key + ", 请诜添加到columns");
                }
            }
        } else {
            throw new AnylineException("请先设置columns");
        }

        return this;
    }

    public Table setPrimaryKey(PrimaryKey primaryKey){
        if(setmap && null != update){
            update.setPrimaryKey(primaryKey);
            return this;
        }

        if(null != this.primaryKey){
            //取消原主键中的列标记
            for(Column column:this.primaryKey.getColumns().values()){
                column.primary(false);
            }
        }
        if(null != columns){
            for(Column column:columns.values()){
                column.primary(false);
            }
        }
        this.primaryKey = primaryKey;
        if (null != primaryKey) {
            primaryKey.setTable(this);
        }
        checkColumnPrimary();
        return this;
    }

    /**
     * 检测主键<br/>
     * 根据主键对象，设置列主键标识<br/>
     * @return this
     */
    public Table checkColumnPrimary(){
        if(null != primaryKey) {
            LinkedHashMap<String, Column> pcs = primaryKey.getColumns();
            if (null != pcs) {
                for (Column column : pcs.values()) {
                    column.primary(true);
                    if(null != columns) {
                        Column col = columns.get(column.getName().toUpperCase());
                        if (null != col) {
                            col.setPrimary(true);
                        }
                    }
                }
            }
        }
        return this;
    }

    /**
     * 根据列主键标识创建主键
     *
     * @return this
     */
    public Table createPrimaryKey(){
        if(null == primaryKey && null != columns) {
            for(Column column:columns.values()){
                if(column.isPrimaryKey() == 1){
                    if(null == primaryKey){
                        primaryKey = new PrimaryKey();
                    }
                    primaryKey.addColumn(column);
                }
            }
        }
        return this;
    }


    public Table addTag(Tag tag){
        if(setmap && null != update){
            update.addTag(tag);
            return this;
        }
        tag.setTable(this);
        if(null == tags){
            tags = new LinkedHashMap<>();
        }
        tags.put(tag.getName(), tag);
        return this;
    }
    public Tag addTag(String name, String type){
        return addTag(name, type, true, null);
    }
    public Tag addTag(String name, String type, Object value){
        Tag tag = new Tag(name, type, value);
        addTag(tag);
        return tag;
    }
    public Tag addTag(String name, String type, boolean nullable, Object def){
        Tag tag = new Tag();
        tag.setName(name);
        tag.nullable(nullable);
        tag.setDefaultValue(def);
        tag.setTypeName(type);
        addTag(tag);
        return tag;
    }
    public String getName(boolean greedy){
        String result = "";
        if(greedy){
            if(BasicUtil.isNotEmpty(catalog)){
                result = catalog+".";
            }
            if(BasicUtil.isNotEmpty(schema)){
                result = result + schema + ".";
            }
            result = result + name;
        }else{
            result = name;
        }
        return result;
    }
    public String getType() {
        if(getmap && null != update){
            return update.type;
        }
        return type;
    }

    public Table setType(String type) {
        if(setmap && null != update){
            update.setType(type);
            return this;
        }
        this.type = type;
        return this;
    }

    public String getTypeCat() {
        if(getmap && null != update){
            return update.typeCat;
        }
        return typeCat;
    }

    public Table setTypeCat(String typeCat) {
        if(setmap && null != update){
            update.setTypeCat(typeCat);
            return this;
        }
        this.typeCat = typeCat;
        return this;
    }

    public String getTypeSchema() {
        if(getmap && null != update){
            return update.typeSchema;
        }
        return typeSchema;
    }

    public Table setTypeSchema(String typeSchema) {
        if(setmap && null != update){
            update.setTypeSchema(typeSchema);
            return this;
        }
        this.typeSchema = typeSchema;
        return this;
    }

    public String getTypeName() {
        if(getmap && null != update){
            return update.typeName;
        }
        return typeName;
    }

    public Table setTypeName(String typeName) {
        if(setmap && null != update){
            update.setTypeName(typeName);
            return this;
        }
        this.typeName = typeName;
        return this;
    }

    public String getSelfReferencingColumn() {
        if(getmap && null != update){
            return update.selfReferencingColumn;
        }
        return selfReferencingColumn;
    }

    public Table setSelfReferencingColumn(String selfReferencingColumn) {
        if(setmap && null != update){
            update.setSelfReferencingColumn(selfReferencingColumn);
            return this;
        }
        this.selfReferencingColumn = selfReferencingColumn;
        return this;
    }

    public String getRefGeneration() {
        if(getmap && null != update){
            return update.refGeneration;
        }
        return refGeneration;
    }

    public Table setRefGeneration(String refGeneration) {
        if(setmap && null != update){
            update.setRefGeneration(refGeneration);
            return this;
        }
        this.refGeneration = refGeneration;
        return this;
    }

    public List<Column> columns(){
        List<Column> list = new ArrayList<>();
        LinkedHashMap<String, Column> columns = getColumns();
        for (Column column:columns.values()){
            list.add(column);
        }
        return list;
    }
    public LinkedHashMap<String, Column> getColumns() {
        if(getmap && null != update){
            return update.getColumns();
        }
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        return columns;
    }

    /**
     * 列名s
     * @param name 是否只获取列表
     * @return List
     */
    public List<String> getColumns(boolean name) {
        LinkedHashMap<String, Column> columns = getColumns();
        List<String> names = new ArrayList<>();
        if(null != columns){
            for(Column column:columns.values()){
                names.add(column.getName());
            }
        }
        return names;
    }

    public <T extends Column> Table setColumns(LinkedHashMap<String, T> columns) {
        if(setmap && null != update){
            update.setColumns(columns);
            return this;
        }
        this.columns = (LinkedHashMap<String, Column>)columns;
        if(null != columns) {
            for (Column column : columns.values()) {
                column.setTable(this);
            }
        }
        return this;
    }
    public List<Tag> tags(){
        List<Tag> list = new ArrayList<>();
        LinkedHashMap<String, Tag> tags = getTags();
        for(Tag tag:tags.values()){
            list.add(tag);
        }
        return list;
    }

    public LinkedHashMap<String, Tag> getTags() {
        if(getmap && null != update){
            return update.getTags();
        }
        if(null == tags){
            tags = new LinkedHashMap<>();
        }
        return tags;
    }

    public Table setTags(LinkedHashMap<String, Tag> tags) {
        if(setmap && null != update){
            update.setTags(tags);
            return this;
        }
        this.tags = tags;
        if(null != tags) {
            for (Column tag : tags.values()) {
                tag.setTable(this);
            }
        }
        return this;
    }
    public Index getIndex(String name){
        if(null != indexes && null != name){
            return indexes.get(name.toUpperCase());
        }
        return null;
    }
    public <T extends Index> LinkedHashMap<String, T> getIndexes() {
        if(getmap && null != update){
            return update.getIndexes();
        }
        if(null == indexes){
            indexes = new LinkedHashMap<>();
        }
        return (LinkedHashMap<String, T>) indexes;
    }
    public <T extends Index> LinkedHashMap<String, T> getIndexs() {
        return getIndexes();
    }
    public LinkedHashMap<String, Column> getPrimaryKeyColumns(){
        PrimaryKey pk = getPrimaryKey();
        if(null != pk){
            return pk.getColumns();
        }
        return new LinkedHashMap<>();
    }
    public PrimaryKey getPrimaryKey(){
        if(getmap && null != update){
            return update.getPrimaryKey();
        }
        if(null == primaryKey){
            for(Column column: columns.values()){
                if(column.isPrimaryKey() ==1){
                    if(null == primaryKey){
                        primaryKey = new PrimaryKey();
                        primaryKey.setName("pk_"+getName());
                        primaryKey.setTable(this);
                    }
                    primaryKey.addColumn(column);
                }
            }
        }
        if(null == primaryKey){
            for(Index index: indexes.values()){
                if(index.isPrimary()){
                    primaryKey = new PrimaryKey();
                    primaryKey.setName(index.getName());
                    primaryKey.setTable(this);
                    primaryKey.setColumns(index.getColumns());
                }
            }
        }
        return primaryKey;
    }

    public int getChangePrimary() {
        return changePrimary;
    }

    public void setChangePrimary(int changePrimary) {
        this.changePrimary = changePrimary;
    }

    public <T extends Index> Table setIndexes(LinkedHashMap<String, T> indexes) {
        if(setmap && null != update){
            update.setIndexes(indexes);
            return this;
        }

        this.indexes = (LinkedHashMap<String, Index>) indexes;
        for(Index index: indexes.values()){
            index.setTable(this);
        }
        return this;
    }
    public Table add(Index index){
        if(null == indexes){
            indexes = new LinkedHashMap<>();
        }
        index.setTable(this);
        indexes.put(index.getName().toUpperCase(), index);
        return this;
    }
    public Table add(Constraint constraint){
        if(null == constraints){
            constraints = new LinkedHashMap<>();
        }
        constraint.setTable(this);
        constraints.put(constraint.getName().toUpperCase(), constraint);
        return this;
    }
    public <T extends Constraint> LinkedHashMap<String, T> getConstraints() {
        if(getmap && null != update){
            return update.getConstraints();
        }
        if(null == constraints){
            constraints = new LinkedHashMap<>();
        }
        return (LinkedHashMap<String, T>) constraints;
    }

    public Table setConstraints(LinkedHashMap<String, Constraint> constraints) {
        if(setmap && null != update){
            update.setConstraints(constraints);
            return this;
        }
        this.constraints = constraints;
        return this;
    }

    public Column getColumn(String name){
        if(getmap && null != update){
            return update.getColumn(name);
        }
        if(null == columns || null == name){
            return null;
        }
        return columns.get(name.toUpperCase());
    }
    public Column getTag(String name){
        if(getmap && null != update){
            return update.getTag(name);
        }
        return tags.get(name.toUpperCase());
    }

    public String getEngine() {
        if(getmap && null != update){
            return update.engine;
        }
        return engine;
    }

    public Table setEngine(String engine) {
        if(setmap && null != update){
            update.setEngine(engine);
            return this;
        }
        this.engine = engine;
        return this;
    }

    public String getCharset() {
        if(getmap && null != update){
            return update.charset;
        }
        return charset;
    }

    public Table setCharset(String charset) {
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

    public Table setCollate(String collate) {
        if(setmap && null != update){
            update.setCollate(collate);
            return this;
        }
        this.collate = collate;
        return this;
    }

    public int getSrid() {
        if(getmap && null != update){
            return update.srid;
        }
        return srid;
    }

    public Table setSrid(int srid) {
        if(setmap && null != update){
            update.setSrid(srid);
            return this;
        }
        this.srid = srid;
        return this;
    }

    public Long getDataFree() {
        return dataFree;
    }

    public Table setDataFree(Long dataFree) {
        this.dataFree = dataFree;
        return this;
    }

    public String getEngineParameters() {
        return engineParameters;
    }

    public Table setEngineParameters(String engineParameters) {
        this.engineParameters = engineParameters;
        return this;
    }

    public Table getInherit() {
        return inherit;
    }

    public Table setInherit(Table inherit) {
        this.inherit = inherit;
        return this;
    }

    public Table setInherit(String setInherit) {
        this.inherit = new Table(setInherit);
        return this;
    }
    public String getKeyword() {
        return keyword;
    }


    public boolean isAutoDropColumn() {
        return autoDropColumn;
    }

    public Table setAutoDropColumn(boolean autoDropColumn) {
        this.autoDropColumn = autoDropColumn;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Table setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Table setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Long getDataRows() {
        return dataRows;
    }

    public Table setDataRows(Long dataRows) {
        this.dataRows = dataRows;
        return this;
    }

    public Long getDataLength() {
        return dataLength;
    }

    public int getTemporary() {
        return temporary;
    }
    public boolean isTemporary() {
        return (temporary == 1);
    }

    public Table setTemporary(int temporary) {
        this.temporary = temporary;
        return this;
    }

    public Table setTemporary(boolean temporary) {
        if(temporary){
            this.temporary = 1;
        }else{
            this.temporary = 0;
        }
        return this;
    }

    public Table setDataLength(Long dataLength) {
        this.dataLength = dataLength;
        return this;
    }

    public Long getIncrement() {
        return increment;
    }

    public Table setIncrement(Long increment) {
        this.increment = increment;
        return this;
    }

    public Long getIndexLength() {
        return indexLength;
    }

    public Table setIndexLength(Long indexLength) {
        this.indexLength = indexLength;
        return this;
    }

    public boolean isSort() {
        return sort;
    }

    public Table setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    /**
     * 列排序
     * @param nullFirst 未设置位置(setPosition)的列是否排在最前
     * @return Table
     */
    public Table sort(boolean nullFirst){
        sort = true;
        if(null != columns){
            Column.sort(columns, nullFirst);
        }
        return this;
    }
    public Table sort(){
        return sort(false);
    }
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(keyword).append(":");
        if(null != catalog && BasicUtil.isNotEmpty(catalog.getName())){
            builder.append(catalog.getName()).append(".");
        }
        if(null != schema && BasicUtil.isNotEmpty(schema.getName())){
            builder.append(schema.getName()).append(".");
        }
        builder.append(name);
        return builder.toString();
    }

    public boolean equals(Table table) {
        return equals(table, true);
    }
    public boolean equals(Table table, boolean ignoreCase) {
        if(null == table){
            return false;
        }
        boolean catalog_equals = BasicUtil.equals(this.catalog, table.getCatalog(), ignoreCase);
        if(!catalog_equals){
            return false;
        }
        boolean schema_equals = BasicUtil.equals(this.schema, table.getSchema(), ignoreCase) ;
        if(!schema_equals){
            return false;
        }
        boolean name_equals = BasicUtil.equals(this.name, table.getName());
        if(!name_equals){
            return false;
        }
        return true;
    }

    /**
     * 主键相同
     * @param table table
     * @return boolean
     */
    public boolean primaryEquals(Table table){
        if(null == table){
            return false;
        }
        PrimaryKey pks = getPrimaryKey();
        PrimaryKey tpks = table.getPrimaryKey();
        if(null == pks){
            if(null == tpks){
                return true;
            }else{
                return false;
            }
        }
        return pks.equals(tpks);
    }
    public TableDiffer compare(Table table){
        return TableDiffer.compare(this, table);
    }
    /**
     * 分桶方式及数量
     */
    public static class Distribution{
        public enum TYPE{
            HASH 			("HASH"  			, "哈希分桶"),
            RANDOM 			("RANDOM"  			, "随机分桶");
            final String code;
            final String name;
            TYPE(String code, String name){
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
        /**
         * 分桶方式
         */
        private TYPE type;
        /**
         * 分桶数量
         */
        private int buckets = -1;
        /**
         * 分桶依据列
         */
        private LinkedHashMap<String, Column> columns;
        public Distribution(){

        }
        public Distribution(TYPE type, int buckets, String ... columns){
            setBuckets(buckets);
            setType(type);
            setColumns(columns);
        }
        public Distribution(TYPE type, String ... columns){
            setType(type);
            setColumns(columns);
        }
        public int getBuckets() {
            return buckets;
        }

        public Distribution setBuckets(int buckets) {
            this.buckets = buckets;
            return this;
        }

        public TYPE getType() {
            return type;
        }

        public Distribution setType(TYPE type) {
            this.type = type;
            return this;
        }

        public LinkedHashMap<String, Column> getColumns() {
            return columns;
        }

        public Distribution setColumns(LinkedHashMap<String, Column> columns) {
            this.columns = columns;
            return this;
        }
        public Distribution setColumns(String ... columns) {
            if(null == this.columns){
                this.columns = new LinkedHashMap<>();
            }
            if(null != columns){
                for (String column:columns){
                    this.columns.put(column.toUpperCase(), new Column(column));
                }
            }
            return this;
        }
    }
    public static class Key{
        public enum TYPE {
            DUPLICATE 			("DUPLICATE"  		, "排序列"),
            AGGREGATE 			("AGGREGATE"  		, "维度列"),
            UNIQUE 			    ("UNIQUE"  			, "主键列");
            final String code;
            final String name;
            TYPE(String code, String name){
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
        private TYPE type;
        private LinkedHashMap<String, Column> columns;

        public TYPE getType() {
            return type;
        }

        public Key setType(TYPE type) {
            this.type = type;
            return this;
        }

        public LinkedHashMap<String, Column> getColumns() {
            return columns;
        }

        public Key setColumns(LinkedHashMap<String, Column> columns) {
            this.columns = columns;
            return this;
        }
        public Key setColumns(String ... columns) {
            if(null == this.columns){
                this.columns = new LinkedHashMap<>();
            }
            if(null != columns){
                for (String column:columns){
                    this.columns.put(column.toUpperCase(), new Column(column));
                }
            }
            return this;
        }
    }
    /**
     * 分区
     * partition by :分区方式(LIST, RANGE, HASH)及 依据列
     * partition of :主表
     * partition for:分区依据值
     */
    public static class Partition implements Serializable {
        public enum TYPE{LIST, RANGE, HASH}
        //主表中设置分区
        private List<Slice> slices = new ArrayList<>();
        //RANGE
        private Object min;
        private Object max;
        //LIST
        private List<Object> values;
        //HASH
        private int modulus;
        private int remainder;

        private Partition.TYPE type;
        private LinkedHashMap<String, Column> columns;

        public Partition(){

        }
        public Partition(Partition.TYPE type){
            this.type = type;
        }
        public Partition(Partition.TYPE type, String ... columns){
            this.type = type;
            this.columns = new LinkedHashMap<>();
            for(String column:columns){
                this.columns.put(column.toUpperCase(), new Column(column));
            }
        }
        public Partition addSlice(Slice slice){
            slices.add(slice);
            return this;
        }
        public List<Slice> getSlices(){
            return this.slices;
        }
        public Partition.TYPE getType() {
            return type;
        }

        public Partition setType(Partition.TYPE type) {
            this.type = type;
            return this;
        }

        public LinkedHashMap<String, Column> getColumns() {
            return columns;
        }

        public Partition setColumns(LinkedHashMap<String, Column> columns) {
            this.columns = columns;
            return this;
        }

        public Partition setColumns(String ... columns){
            this.columns = new LinkedHashMap<>();
            for(String column:columns){
                this.columns.put(column.toUpperCase(), new Column(column));
            }
            return this;
        }
        public Partition addColumn(Column column){
            if(null == columns){
                columns = new LinkedHashMap<>();
            }
            columns.put(column.getName().toUpperCase(), column);

            return this;
        }
        public Partition addColumn(String column){
            return addColumn(new Column(column));
        }

        public Partition setRange(Object min, Object max){
            this.min = min;
            this.max = max;
            return this;
        }
        public Object getMin() {
            return min;
        }

        public Partition setSlices(List<Slice> slices) {
            this.slices = slices;
            return this;
        }

        public Partition setMin(Object min) {
            this.min = min;
            return this;
        }

        public Object getMax() {
            return max;
        }

        public Partition setMax(Object max) {
            this.max = max;
            return this;
        }

        public List<Object> getValues() {
            return values;
        }

        public Partition setValues(List<Object> values) {
            this.values = values;
            return this;
        }
        public Partition addValues(Object ... items) {
            if(null == values) {
                this.values = new ArrayList<>();
            }
            for(Object item:items) {
                if (item instanceof Collection) {
                    Collection cons = (Collection) item;
                    for(Object con:cons){
                        addValues(con);
                    }
                }else if(item instanceof Object[]){
                    Object[] objs = (Object[]) item;
                    for(Object obj:objs){
                        addValues(obj);
                    }
                }else {
                    values.add(item);
                }
            }
            return this;
        }

        public int getModulus() {
            return modulus;
        }

        public Partition setModulus(int modulus) {
            this.modulus = modulus;
            return this;
        }
        public Partition setHash(int modulus, int remainder) {
            this.modulus = modulus;
            this.remainder = remainder;
            return this;
        }

        public int getRemainder() {
            return remainder;
        }

        public Partition setRemainder(int remainder) {
            this.remainder = remainder;
            return this;
        }

        /**
         * 分片(分区依据值)
         */
        public static class Slice implements Serializable{
            private String name;
            private Object min;
            private Object max;
            private List<Object> values;
            private LinkedHashMap<String,Object> less;
            private int interval;
            private String unit;
            public String getName() {
                return name;
            }

            public Slice setName(String name) {
                this.name = name;
                return this;
            }

            public Object getMin() {
                return min;
            }

            public Slice setMin(Object min) {
                this.min = min;
                return this;
            }

            public List<Object> getValues() {
                return values;
            }

            public Slice setValues(List<Object> values) {
                this.values = values;
                return this;
            }
            public Slice setValues(Object ... values) {
                if(null == this.values){
                    this.values = new ArrayList<>();
                }
                if(null != values){
                    for(Object value:values){
                        this.values.add(value);
                    }
                }
                return this;
            }
            public Slice addValue(Object value) {
                if(null == value){
                    return this;
                }
                if(null == this.values){
                    this.values = new ArrayList<>();
                }
                if(value instanceof Collection){
                    this.values.addAll((Collection<?>) value);
                }else {
                    this.values.add(value);
                }
                return this;
            }

            public Slice addValues(Object ... values) {
                if(null != values){
                    for(Object value:values){
                        addValue(value);
                    }
                }
                return this;
            }

            public Object getMax() {
                return max;
            }

            public Slice setMax(Object max) {
                this.max = max;
                return this;
            }

            public int getInterval() {
                return interval;
            }

            public Slice setInterval(int interval) {
                this.interval = interval;
                return this;
            }

            public LinkedHashMap<String,Object> getLess() {
                return less;
            }

            public Slice setLess(String column, Object less) {
                if(null == this.less){
                    this.less = new LinkedHashMap<>();
                }
                this.less.put(column.toUpperCase(), less);
                return this;
            }

            public String getUnit() {
                return unit;
            }

            public Slice setUnit(String unit) {
                this.unit = unit;
                return this;
            }
        }
    }
}
