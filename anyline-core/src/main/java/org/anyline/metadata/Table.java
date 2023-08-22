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
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class Table   implements Serializable {
    public static <T extends Table> List<String> names(LinkedHashMap<String, T> tables) {
        List<String> names = new ArrayList<>();
        for (T table : tables.values()) {
            names.add(table.getName());
        }
        return names;
    }

    protected String keyword = "TABLE"            ;
    protected String catalog                      ;
    protected String schema                       ;
    protected String name                         ;
    protected String type                         ;
    protected String comment                      ;
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
    protected Date checkSchemaTime                ;
    protected List<String> ddls                   ;


    protected PrimaryKey primaryKey;
    protected LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
    protected LinkedHashMap<String, Tag> tags       = new LinkedHashMap<>();
    protected LinkedHashMap<String, Index> indexs   = new LinkedHashMap<>();
    protected LinkedHashMap<String, Constraint> constraints = new LinkedHashMap<>();
    protected boolean autoDropColumn = ConfigTable.IS_DDL_AUTO_DROP_COLUMN;     //执行alter时是否删除 数据库中存在 但table 中不存在的列

    protected Table origin;
    protected Table update;
    protected boolean setmap = false              ;  //执行了upate()操作后set操作是否映射到update上(除了catalog, schema,name)
    protected boolean getmap = false              ;  //执行了upate()操作后get操作是否映射到update上(除了catalog, schema,name)

    public Table(){
        this(null);
    }
    public Table(String name){
        this(null, name);
    }
    public Table(String schema, String table){
        this(null, schema, table);
    }
    public Table(String catalog, String schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }

    public List<Column> primarys(){
        List<Column> pks = new ArrayList<>();
        for(Column column:columns.values()){
            if(column.isPrimaryKey() == 1){
                pks.add(column);
            }
        }
        return pks;
    }
    public Column primary(){
        List<Column> pks = primarys();
        if(pks.isEmpty()){
            return null;
        }
        return pks.get(0);
    }
    public Table clone(){
        Table copy = new Table();
        BeanUtil.copyFieldValue(copy, this);

        LinkedHashMap<String,Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()){
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;
        copy.origin = this;
        copy.update = null;
        copy.setmap = false;
        copy.getmap = false;
        return copy;
    }

    public Table update(){
        return update(true, true);
    }
    public Table update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        update.origin = this;
        return update;
    }


    public Table getUpdate() {
        return update;
    }

    public Table setUpdate(Table update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
            update.origin = this;
        }
        return this;
    }

    public Table setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public Table setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
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
    public Column addColumn(String name, String type){
        return addColumn(name, type, true, null);
    }
    public Column addColumn(String name, String type, boolean nullable, Object def){
        Column column = new Column();
        column.setName(name);
        column.setNullable(nullable);
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
                column.setPrimaryKey(false);
            }
        }
        primaryKey = new PrimaryKey();
        primaryKey.setTable(this);
        if (null != columns) {
            for (String key : keys) {
                Column column = columns.get(key.toUpperCase());
                if (null != column) {
                    column.setPrimaryKey(true);
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
                column.setPrimaryKey(false);
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
        tag.setNullable(nullable);
        tag.setDefaultValue(def);
        tag.setTypeName(type);
        addTag(tag);
        return tag;
    }
    public String getCatalog() {
        return catalog;
    }

    public Table setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public Table setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getName() {
        return name;
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
    public Table setName(String name){
        this.name = name;
        return this;
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

    public String getComment() {
        if(getmap && null != update){
            return update.comment;
        }
        return comment;
    }

    public Table setComment(String comment) {
        if(setmap && null != update){
            update.setComment(comment);
            return this;
        }
        this.comment = comment;
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

    public <T extends Column> LinkedHashMap<String, T> getColumns() {
        if(getmap && null != update){
            return update.getColumns();
        }
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        return (LinkedHashMap<String, T>) columns;
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

    public <T extends Tag> LinkedHashMap<String, T> getTags() {
        if(getmap && null != update){
            return update.getTags();
        }
        if(null == tags){
            tags = new LinkedHashMap<>();
        }
        return (LinkedHashMap<String, T>) tags;
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

    public Date getCheckSchemaTime() {
        return checkSchemaTime;
    }

    public Table setCheckSchemaTime(Date checkSchemaTime) {
        if(setmap && null != update){
            update.setCheckSchemaTime(checkSchemaTime);
            return this;
        }
        this.checkSchemaTime = checkSchemaTime;
        return this;
    }

    public Table getOrigin() {
        return origin;
    }

    public void setOrigin(Table origin) {
        this.origin = origin;
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

    public List<String> getDdls() {
        return ddls;
    }

    public void setDdls(List<String> ddl) {
        this.ddls = ddl;
    }
    public List<String> ddls() {
        return ddls;
    }
    public List<String> ddls(boolean init) {
        return ddls;
    }
    public List<String> getDdls(boolean init) {
        return ddls;
    }

    public String ddl() {
        if(null != ddls && ddls.size()>0){
            return ddls.get(0);
        }
        return null;
    }
    public String ddl(boolean init) {
        if(null != ddls && ddls.size()>0){
            return ddls.get(0);
        }
        return null;
    }
    public String getDdl(boolean init) {
        if(null != ddls && ddls.size()>0){
            return ddls.get(0);
        }
        return null;
    }
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(keyword).append(":");
        if(BasicUtil.isNotEmpty(catalog)){
            builder.append(catalog).append(".");
        }
        if(BasicUtil.isNotEmpty(schema)){
            builder.append(schema).append(".");
        }
        builder.append(name);
        return builder.toString();
    }
}
