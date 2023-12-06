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
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.io.Serializable;
import java.util.*;

public class Table<E extends Table> extends BaseMetadata<E> implements Serializable {

    protected String keyword = "TABLE"            ;

    protected String masterName;
    protected Table master;
    protected Partition partitionBy ; // 分区方式
    protected Partition partitionFor ; // 分区值

    protected String type                         ;
    protected Table inherits                      ;
    protected int srid                            ;

    protected String typeCat                      ;
    protected String typeSchema                   ;
    protected String typeName                     ;
    protected String selfReferencingColumn        ;
    protected String refGeneration                ;

    protected String engine                       ;
    protected String charset                      ;
    protected String collate                      ;
    protected Long ttl                            ;

    protected Date createTime;
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
     * 索引长度
     */
    protected Long indexLength                  ;


    protected PrimaryKey primaryKey;
    protected LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
    protected LinkedHashMap<String, Tag> tags       = new LinkedHashMap<>();
    protected LinkedHashMap<String, Index> indexs   = new LinkedHashMap<>();
    protected LinkedHashMap<String, Constraint> constraints = new LinkedHashMap<>();
    protected boolean autoDropColumn = ConfigTable.IS_DDL_AUTO_DROP_COLUMN;     //执行alter时是否删除 数据库中存在 但table 中不存在的列


    public Table(){
    }
    public Table(String name){
        this.name = name;
    }
    public Table(String schema, String table){
        this(null, schema, table);
    }
    public Table(Schema schema, String table){
        this(null, schema, table);
    }
    public Table(String catalog, String schema, String name){
        this.catalog = new Catalog(catalog);
        this.schema = new Schema(schema);
        this.name = name;
    }
    public Table(Catalog catalog, Schema schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }



    public Partition getPartitionFor() {
        if(getmap && null != update){
            return update.partitionFor;
        }
        return partitionFor;
    }
    public Partition getPartitionBy() {
        if(getmap && null != update){
            return update.partitionBy;
        }
        return partitionBy;
    }
    public Table partitionOf(Table master){
        this.master = master;
        return this;
    }
    public Table partitionFor(Partition partition){
        return setPartitionFor(partition);
    }
    public Table partitionFor(Partition.TYPE type, Object ... values){
        Partition partition = new Partition();
        partition.setType(type);
        partition.addList(values);
        return setPartitionFor(partition);
    }
    public Table setPartitionFor(Partition partition) {
        if(setmap && null != update){
            update.setPartitionFor(partition);
            return this;
        }
        this.partitionFor = partition;
        return this;
    }
    public Table partitionBy(Partition partition){
        return setPartitionBy(partition);
    }
    public Table partitionBy(Partition.TYPE type, String ... columns){
        Partition partition = new Partition();
        partition.setType(type);
        partition.setColumns(columns);
        return setPartitionBy(partition);
    }
    public Table setPartitionBy(Partition partition) {
        if(setmap && null != update){
            update.setPartitionBy(partition);
            return this;
        }
        this.partitionBy = partition;
        return this;
    }

    public String getMasterName() {
        if(null == masterName && null != master){
            masterName = master.getName();
        }
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }
    public void setMaster(String masterName) {
        this.masterName = masterName;
    }

    public Table getMaster() {
        if(null == master && null != masterName){
            master = new Table(masterName);
        }
        return master;
    }

    public void setPartitionOf(Table master) {
        this.master = master;
    }
    public void setMaster(Table master) {
        this.master = master;
    }


    public LinkedHashMap<String, Column> primarys(){
        LinkedHashMap<String, Column> pks = new LinkedHashMap<>();
        for(Map.Entry<String,Column> item:columns.entrySet()){
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
        LinkedHashMap<String,Column> cols = new LinkedHashMap<>();
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
    public Column addColumn(String name, String type, boolean nullable, Object def){
        Column column = new Column();
        column.setName(name);
        column.nullable(nullable);
        column.setDefaultValue(def);
        column.setTypeName(type);
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
                    throw new AnylineException("未匹配到" + key + ",请诜添加到columns");
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
        this.primaryKey = primaryKey;
        if (null != primaryKey) {
            primaryKey.setTable(this);
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
        if(null != indexs && null != name){
            return indexs.get(name.toUpperCase());
        }
        return null;
    }
    public <T extends Index> LinkedHashMap<String, T> getIndexs() {
        if(getmap && null != update){
            return update.getIndexs();
        }
        if(null == indexs){
            indexs = new LinkedHashMap<>();
        }
        return (LinkedHashMap<String, T>) indexs;
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
                        primaryKey.setName(getName()+"_PK");
                        primaryKey.setTable(this);
                    }
                    primaryKey.addColumn(column);
                }
            }
        }
        if(null == primaryKey){
            for(Index index: indexs.values()){
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

    public <T extends Index> Table setIndexs(LinkedHashMap<String, T> indexs) {
        if(setmap && null != update){
            update.setIndexs(indexs);
            return this;
        }

        this.indexs = (LinkedHashMap<String, Index>) indexs;
        return this;
    }
    public Table add(Index index){
        if(null == indexs){
            indexs = new LinkedHashMap<>();
        }
        index.setTable(this);
        indexs.put(index.getName().toUpperCase(), index);
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


    public Table getInherits() {
        return inherits;
    }

    public void setInherits(Table inherits) {
        this.inherits = inherits;
    }

    public void setInherits(String inherits) {
        this.inherits = new Table(inherits);
    }

    public String getKeyword() {
        return keyword;
    }


    public boolean isAutoDropColumn() {
        return autoDropColumn;
    }

    public void setAutoDropColumn(boolean autoDropColumn) {
        this.autoDropColumn = autoDropColumn;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getDataRows() {
        return dataRows;
    }

    public void setDataRows(Long dataRows) {
        this.dataRows = dataRows;
    }

    public Long getDataLength() {
        return dataLength;
    }

    public void setDataLength(Long dataLength) {
        this.dataLength = dataLength;
    }

    public Long getIndexLength() {
        return indexLength;
    }

    public void setIndexLength(Long indexLength) {
        this.indexLength = indexLength;
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
}
