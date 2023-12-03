package org.anyline.metadata;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.util.*;

public class BaseMetadata<T extends BaseMetadata> {

    protected Catalog catalog                      ; // 数据库 catalog与schema 不同有数据库实现方式不一样
    protected Schema schema                       ; // dbo mysql中相当于数据库名  查数据库列表 是用SHOW SCHEMAS 但JDBC con.getCatalog()返回数据库名 而con.getSchema()返回null
    protected String name                         ; // 名称
    protected String comment                      ; // 备注
    protected boolean execute = true              ; // DDL是否立即执行, false:只创建SQL不执行可以通过ddls()返回生成的SQL
    protected Long objectId;

    protected Table table;
    protected String definition;

    protected T origin;
    protected T update;
    protected boolean setmap = false              ;  //执行了upate()操作后set操作是否映射到update上(除了table,catalog, schema,name,drop,action)
    protected boolean getmap = false              ;  //执行了upate()操作后get操作是否映射到update上(除了table,catalog, schema,name,drop,action)

    protected boolean drop = false                ;
    protected ACTION.DDL action = null            ; //ddl命令 add drop alter
    protected List<String> ddls                   ;
    protected String identity                     ;
    protected Object extend                       ; //扩展属性
    protected Date checkSchemaTime                ;
    public String getIdentity(){
        if(null == identity){
            identity = BasicUtil.nvl(catalog,"")+"_"+BasicUtil.nvl(schema,"")+"_"+BasicUtil.nvl(getTableName(false),"")+"_"+BasicUtil.nvl(getName(),"") ;
            identity = identity.toUpperCase();
            //identity = MD5Util.crypto(identity.toUpperCase());
        }
        return identity;
    }

    public static <T extends BaseMetadata> List<String> names(LinkedHashMap<String, T> columns){
        return names(columns, false);
    }
    public static <T extends BaseMetadata> List<String> names(LinkedHashMap<String, T> columns, boolean upper){
        List<String> names = new ArrayList<>();
        if(null != columns) {
            for (T column : columns.values()) {
                String name = column.getName();
                if (upper && null != name) {
                    name = name.toUpperCase();
                }
                names.add(name);
            }
        }
        return names;
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
        this.catalog = new Catalog(catalog);
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
        this.schema = new Schema(schema);
        return (T)this;
    }
    public T setSchema(Schema schema) {
        this.schema = schema;
        return (T)this;
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

    public T setName(String name) {
        this.name = name;
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
        this.drop = true;
        return (T)this;
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

    public void drop() {
        this.drop = true;
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
     * @param update 是否检测upate
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

    public String getDefinition() {
        if(getmap && null != update){
            return  update.definition;
        }
        return definition;
    }

    public T setDefinition(String definition) {
        if(setmap && null != update){
            ((Trigger)update).definition = definition;
            return (T)this;
        }
        this.definition = definition;
        return (T)this;
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

    public static  <T extends BaseMetadata> T search(List<T> list, String catalog, String schema, String name){
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
    public static  <T extends BaseMetadata> T search(List<T> list, String catalog, String name){
        for(T item:list){
            if(BasicUtil.equalsIgnoreCase(item.getCatalogName(), catalog)
                    && BasicUtil.equalsIgnoreCase(item.getName(), name)
            ){
                return item;
            }
        }
        return null;
    }
    public static  <T extends BaseMetadata> T search(List<T> list, String name){
        for(T item:list){
            if(BasicUtil.equalsIgnoreCase(item.getName(), name)){
                return item;
            }
        }
        return null;
    }

}
