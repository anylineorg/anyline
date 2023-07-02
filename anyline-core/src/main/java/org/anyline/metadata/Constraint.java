package org.anyline.metadata;

import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Constraint  implements Serializable {

    protected String catalog      ;
    protected String schema       ;
    protected Table table         ;
    protected String name         ;
    protected boolean unique      ; // 是否唯一
    protected String type         ; //
    protected String comment    ; // 备注
    protected LinkedHashMap<String,Column> columns = new LinkedHashMap<>();

    protected Constraint update;
    protected boolean setmap = false              ;  //执行了upate()操作后set操作是否映射到update上(除了table,catalog,schema,name,drop,action)
    protected boolean getmap = false              ;  //执行了upate()操作后get操作是否映射到update上(除了table,catalog,schema,name,drop,action)
    public Constraint(){
    }

    public String getCatalog() {
        return catalog;
    }

    public Constraint setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public Constraint setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getTableName(boolean update) {
       Table table = getTable(update);
       if(null != table) {
           return table.getName();
       }
       return null;
    }

    public Table getTable(boolean update) {
        if(update){
            if(null != table && null != table.getUpdate()){
                return table.getUpdate();
            }
        }
        return table;
    }

    public Constraint setTable(Table table) {
        this.table = table;
        return this;
    }
    public Constraint setTable(String table) {
        this.table = new Table(table);
        return this;
    }


    public String getName() {
        return name;
    }

    public Constraint setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isUnique() {
        if(getmap && null != update){
            return update.unique;
        }
        return unique;
    }

    public Constraint setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public String getType() {
        if(getmap && null != update){
            return update.type;
        }
        return type;
    }

    public Constraint setType(String type) {
        this.type = type;
        return this;
    }

    public LinkedHashMap<String, Column> getColumns() {
        if(getmap && null != update){
            return update.columns;
        }
        return columns;
    }
    public Column getColumn(String name) {
        if(getmap && null != update){
            return update.getColumn(name);
        }
        if(null != columns && null != name){
            return columns.get(name.toUpperCase());
        }
        return null;
    }

    public Constraint setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
        return this;
    }
    public Constraint addColumn(Column column){
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        columns.put(column.getName().toUpperCase(), column);
        return this;
    }

    public Constraint addColumn(String column){
        return addColumn(new Column(column));
    }

    public Constraint addColumn(String column, String order){
        return addColumn(new Column(column).setOrder(order));
    }
    public Constraint addColumn(String column, String order, int position){
        return addColumn(new Column(column).setOrder(order).setPosition(position));
    }


    public Constraint setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public Constraint setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }
    public Constraint getUpdate() {
        return update;
    }

    public Constraint update(){
        return update(true, true);
    }

    public Constraint update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }

    public Constraint setUpdate(Constraint update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
        }
        return this;
    }

    public String getComment() {
        if(getmap && null != update){
            return update.comment;
        }
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Constraint clone(){
        Constraint copy = new Constraint();
        BeanUtil.copyFieldValue(copy, this);


        LinkedHashMap<String,Column> cols = new LinkedHashMap<>();
        for(Column column:this.columns.values()){
            Column col = column.clone();
            cols.put(col.getName().toUpperCase(), col);
        }
        copy.columns = cols;
        copy.update = null;
        copy.setmap = false;
        copy.getmap = false;
        return copy;
    }
}
