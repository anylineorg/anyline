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

import org.anyline.entity.DataRow;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.util.*;

public class Metadata<T extends Metadata> {

    public enum TYPE implements Type{
        TABLE(1)            , // 继承子表、父表、分区表、主表、点类型、边类型
        VIEW(2)             , // 视图
        COLUMN(4)           , // 列
        PRIMARY(8)          , // 主键
        FOREIGN(16)         , // 外键
        INDEX(32)           , // 索引
        CONSTRAINT(64)      , // 约束
        SCHEMA(128)         , // SCHEMA
        CATALOG(256)        , // CATALOG

        FUNCTION(512)       , // 函数
        PROCEDURE(1024)     , // 存储过程
        TRIGGER(2048)       , // 触发器
        SEQUENCE(4096)      , // 序列
        SYNONYM(8192)       , // 同义词
        DDL(16384)            // DDL
        ;
        public final int value;
        TYPE(int value){
            this.value = value;
        }
        public int value(){
            return value;
        }
    }
    public static boolean check(int strut, Type type){
        int tp = type.value();
        return ((strut & tp) == tp);
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
    public static Type type(int type){
        return types().get(type);
    }
    public static List<Type> types(int types){
        List<Type> list = new ArrayList<>();
        int count = 0;
        while (types >= 1) {
            int temp = types % 2;
            types = (types - temp) / 2;
            if (temp == 1) {
                Type t = null;
                if (count == 0){
                    t = type(1);
                }else{
                    t = type((2 << (count - 1)));
                }
                if(null != t){
                    list.add(t);
                }
            }
            count++;
        }
        return list;
    }
    protected DatabaseType database = DatabaseType.NONE;
    protected String datasource                   ; // 数据源
    protected Catalog catalog                     ; // 数据库 catalog与schema 不同有数据库实现方式不一样
    protected Schema schema                       ; // dbo mysql中相当于数据库名  查数据库列表 是用SHOW SCHEMAS 但JDBC con.getCatalog()返回数据库名 而con.getSchema()返回null
    protected String name                         ; // 名称
    protected String alias                        ; // 别名
    protected String comment                      ; // 备注
    protected boolean execute = true              ; // DDL是否立即执行, false:只创建SQL不执行可以通过ddls()返回生成的SQL
    protected String text;
    protected String id;
    protected String user                         ; // 所属用户
    protected Long objectId;

    protected Table<?> table;
    protected String definition;

    protected T origin;
    protected T update;
    protected boolean setmap = false              ;  //执行了update()操作后set操作是否映射到update上(除了table, catalog, schema, name, drop, action)
    protected boolean getmap = false              ;  //执行了update()操作后get操作是否映射到update上(除了table, catalog, schema, name, drop, action)

    protected boolean drop = false                ;
    protected ACTION.DDL action = null            ; //ddl命令 add drop alter
    protected List<String> ddls                   ;
    protected String identity                     ;
    protected Object extend                       ; //扩展属性
    protected Date checkSchemaTime                ;
    protected LinkedHashMap<String, Object> property;
    protected DataRow metadata = null             ; //驱动返回的全部属性

    public DataRow getMetadata() {
        return metadata;
    }

    public void setMetadata(DataRow metadata) {
        this.metadata = metadata;
    }

    public String getIdentity(){
        if(null == identity){
            identity = BasicUtil.nvl(getCatalogName(), "") + "_" + BasicUtil.nvl(getSchemaName(), "") + "_" + BasicUtil.nvl(getTableName(false), "") + "_" + BasicUtil.nvl(getName(), "") ;
            identity = identity.toUpperCase();
            //identity = MD5Util.crypto(identity.toUpperCase());
        }
        return identity;
    }

    public static <T extends Metadata> List<String> names(LinkedHashMap<String, T> metas){
        return names(metas, false);
    }
    public static <T extends Metadata> List<String> names(LinkedHashMap<String, T> metas, boolean upper){
        List<String> names = new ArrayList<>();
        if(null != metas) {
            for (T meta : metas.values()) {
                String name = meta.getName();
                if (upper && null != name) {
                    name = name.toUpperCase();
                }
                names.add(name);
            }
        }
        return names;
    }

    public static <T extends Metadata> List<String> names(List<T> metas){
        return names(metas, false);
    }
    public static <T extends Metadata> List<String> names(List<T> metas, boolean upper){
        List<String> names = new ArrayList<>();
        if(null != metas) {
            for (T meta : metas) {
                String name = meta.getName();
                if (upper && null != name) {
                    name = name.toUpperCase();
                }
                names.add(name);
            }
        }
        return names;
    }

    /**
     * 排序
     * @param positions 列名,排序...
     * @param columns 列
     * @param <T> T
     */
    public static <T extends Metadata> void sort(LinkedHashMap<String, Integer> positions, LinkedHashMap<String, T> columns){
        if(null == positions || positions.isEmpty()){
            return;
        }
        List<T> list = new ArrayList<>();
        list.addAll(columns.values());

        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                Integer p1 = positions.get(o1.getName().toUpperCase());
                Integer p2 = positions.get(o2.getName().toUpperCase());
                if(p1 == p2){
                    return 0;
                }
                if (null == p1) {
                    return 1;
                }
                if (null == p2) {
                    return -1;
                }
                return p1 > p2 ? 1:-1;
            }
        });

        columns.clear();
        for(T column:list){
            columns.put(column.getName().toUpperCase(), column);
        }
    }

    public DatabaseType getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseType database) {
        this.database = database;
    }
    public String getDataSource() {
        return datasource;
    }

    public void setDataSource(String datasource) {
        this.datasource = datasource;
    }

    public Catalog getCatalog() {
        return catalog;
    }
    public String getCatalogName() {
        if(null == catalog){
            return null;
        }
        return catalog.getName();
    }

    public T setCatalog(String catalog) {
        if(BasicUtil.isEmpty(catalog)){
            this.catalog = null;
        }else {
            this.catalog = new Catalog(catalog);
        }
        return (T)this;
    }

    public T setCatalog(Catalog catalog) {
        this.catalog = catalog;
        return (T)this;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getSchemaName() {
        if(null == schema){
            return null;
        }
        return schema.getName();
    }

    public T setSchema(String schema) {
        if(null == schema){
            this.schema = null;
        }else {
            this.schema = new Schema(schema);
        }
        return (T)this;
    }
    public T setSchema(Schema schema) {
        this.schema = schema;
        return (T)this;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getCheckSchemaTime() {
        return checkSchemaTime;
    }

    public T setCheckSchemaTime(Date checkSchemaTime) {
        if(setmap && null != update){
            update.setCheckSchemaTime(checkSchemaTime);
            return (T)this;
        }
        this.checkSchemaTime = checkSchemaTime;
        return (T)this;
    }
    public String getName() {
        return name;
    }
    public String getFullName(){
        String dest = null;
        String catalogName = getCatalogName();
        String schemaName = getSchemaName();
        String tableName = name;
        if(BasicUtil.isNotEmpty(catalogName)){
            dest = catalogName;
        }
        if(BasicUtil.isNotEmpty(schemaName)){
            if(null == dest){
                dest = schemaName;
            }else{
                dest += "." + schemaName;
            }
        }
        if(BasicUtil.isNotEmpty(tableName)){
            if(null == dest){
                dest = tableName;
            }else{
                dest += "." + tableName;
            }
        }
        return dest;

    }

    public T setName(String name) {
        this.name = name;
        return (T)this;
    }

    public String getAlias() {
        return alias;
    }

    public T setAlias(String alias) {
        this.alias = alias;
        return (T)this;
    }

    public T setComment(String comment) {
        if(setmap && null != update){
            update.comment = comment;
            return (T)this;
        }
        this.comment = comment;
        return (T)this;
    }

    public String getComment() {
        if(getmap && null != update){
            return update.comment;
        }
        return comment;
    }

    public T delete() {
        return drop();
    }

    public boolean isDelete() {
        return drop;
    }

    public T setDelete(boolean drop) {
        this.drop = drop;
        return (T)this;
    }

    public ACTION.DDL getAction() {
        return action;
    }

    public T setAction(ACTION.DDL action) {
        this.action = action;
        return (T)this;
    }

    public T drop() {
        this.drop = true;
        return (T)this;
    }

    public boolean isDrop() {
        return drop;
    }

    public T setDrop(boolean drop) {
        this.drop = drop;
        return (T)this;
    }

    public T setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public T setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return (T)update;
    }

    /**
     * 相关表
     * @param update 是否检测update
     * @return table
     */
    public Table getTable(boolean update) {
        if(update){
            if(null != table && null != table.getUpdate()){
                return (Table) table.getUpdate();
            }
        }
        return table;
    }

    public Table getTable() {
        return getTable(false);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getTableName(boolean update) {
        Table table = getTable(update);
        if(null != table){
            return table.getName();
        }
        return null;
    }

    public String getTableName() {
        return getTableName(false);
    }
    public T setTable(String table) {
        this.table = new Table(table);
        return (T)this;
    }

    public LinkedHashMap<String, Object> getProperty() {
        if(getmap && null != update){
            return update.getProperty();
        }
        return property;
    }

    public T setProperty(String key, Object value) {
        if(getmap && null != update){
            return (T)update.setProperty(key, value);
        }
        if(null == this.property){
            this.property = new LinkedHashMap<>();
        }
        this.property.put(key, value);
        return (T)this;
    }
    public T setProperty(LinkedHashMap<String, Object> property) {
        if(getmap && null != update){
            return (T)update.setProperty(property);
        }
        this.property = property;
        return (T)this;
    }
    public String getDefinition() {
        if(getmap && null != update){
            return  update.definition;
        }
        return definition;
    }

    public T setDefinition(String definition) {
        if(setmap && null != update){
            update.definition = definition;
            return (T)this;
        }
        this.definition = definition;
        return (T)this;
    }

    public boolean isRename(){
        if(null != update){
            return !BasicUtil.equalsIgnoreCase(name, update.getName());
        }
        return false;
    }
    public String getDdl() {
        if(null != ddls && ddls.size()>0){
            return ddls.get(0);
        }
        return null;
    }

    public List<String> getDdls() {
        return ddls;
    }

    public void setDdls(List<String> ddl) {
        this.ddls = ddl;
    }
    public void addDdl(String ddl) {
        if(this.ddls == null){
            this.ddls = new ArrayList<>();
        }
        ddls.add(ddl);
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

    public Object getExtend() {
        return extend;
    }

    public void setExtend(Object extend) {
        this.extend = extend;
    }

    public boolean execute() {
        return execute;
    }

    /**
     * DDL是否立即执行
     * @param execute  默认:true, false:只生成SQL不支持，可以通过ddls()返回生成的SQL
     */

    public void execute(boolean execute) {
        this.execute = execute;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public T getUpdate() {
        return update;
    }
    public T setUpdate(T update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
        }
        return (T)this;
    }
    public T update(){
        return update(true, true);
    }
    public T update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        update.origin = this;
        return update;
    }

    public T clone(){
        T copy = null;
        try {
            copy = (T)getClass().newInstance();
            BeanUtil.copyFieldValue(copy, this);
            copy.update = null;
            copy.setmap = false;
            copy.getmap = false;
        } catch (Exception e) {
        }
        return copy;
    }

    public static <T extends Metadata> T search(List<T> list, String catalog, String schema, String name){
        for(T item:list){
            if(BasicUtil.equalsIgnoreCase(item.getCatalogName(), catalog)
                    && BasicUtil.equalsIgnoreCase(item.getSchemaName(), schema)
                    && BasicUtil.equalsIgnoreCase(item.getName(), name)
            ){
                return item;
            }
        }
        return null;
    }
    public static <T extends Metadata> T search(List<T> list, Catalog catalog, Schema schema, String name){
        for(T item:list){
            if(BasicUtil.equalsIgnoreCase(item.getCatalogName(), catalog)
                    && BasicUtil.equalsIgnoreCase(item.getSchemaName(), schema)
                    && BasicUtil.equalsIgnoreCase(item.getName(), name)
            ){
                return item;
            }
        }
        return null;
    }
    public static <T extends Metadata> T search(List<T> list, String catalog, String name){
        for(T item:list){
            if(BasicUtil.equalsIgnoreCase(item.getName(), name)){
                if(BasicUtil.equalsIgnoreCase(item.getCatalogName(), catalog)){
                    return item;
                }
            }
        }
        return null;
    }
    public static <T extends Metadata> T search(List<T> list, String name){
        for(T item:list){
            if(BasicUtil.equalsIgnoreCase(item.getName(), name)){
                return item;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getKeyword(){
        return "object";
    }
    public String toString(){
        return getKeyword() + ":" + getName();
    }
}
