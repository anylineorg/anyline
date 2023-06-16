package org.anyline.entity.data;


import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class ForeignKey extends Constraint implements Serializable {
    public boolean isForeign(){
        return true;
    }

    private Table reference;

    private ForeignKey update;

    public ForeignKey(){}
    public ForeignKey(String name){
        this.setName(name);
    }

    /**
     * 外键
     * @param table 表
     * @param column 列
     * @param rtable 依赖表
     * @param rcolumn 依赖列
     */
    public ForeignKey(String table, String column, String rtable, String rcolumn){
        setTable(table);
        setReference(rtable);
        addColumn(column, rcolumn);
    }

    public ForeignKey getUpdate() {
        return update;
    }

    public ForeignKey setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public ForeignKey setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }

    public ForeignKey update(){
        return update(true, true);
    }

    public ForeignKey update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }

    public ForeignKey setUpdate(ForeignKey update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        update.update = null;
        return this;
    }

    public ForeignKey setReference(Table reference){
        if(setmap && null != update){
            update.setReference(reference);
            return this;
        }
        this.reference = reference;
        return this;
    }
    /**
     * 添加依赖表
     * @param reference 依赖表
     * @return ForeignKey
     */
    public ForeignKey setReference(String reference){
        if(setmap && null != update){
            update.setReference(reference);
            return this;
        }
        this.reference = new Table(reference);
        return this;
    }

    public Table getReference() {
        if(getmap && null != update){
            return update.reference;
        }
        return reference;
    }

    /**
     * 添加列
     * @param column 列 需要设置reference属性
     * @return ForeignKey
     */
    public ForeignKey addColumn(Column column){
        if(setmap && null != update){
            update.addColumn(column);
            return this;
        }
        super.addColumn(column);
        return this;
    }

    /**
     * 添加列
     * @param column 列
     * @param table 依赖表
     * @param reference 依赖列
     * @return ForeignKey
     */
    public ForeignKey addColumn(String column, String table, String reference){
        if(setmap && null != update){
            update.addColumn(column, table, reference);
            return this;
        }
        this.reference = new Table(table);
        addColumn(new Column(column).setReference(reference));
        return this;
    }
    /**
     * 添加列
     * @param column 列
     * @param reference 依赖列
     * @return ForeignKey
     */
    public ForeignKey addColumn(String column,  String reference){
        if(setmap && null != update){
            update.addColumn(column, reference);
            return this;
        }
        addColumn(new Column(column).setReference(reference));
        return this;
    }

    public ForeignKey clone(){
        ForeignKey copy = new ForeignKey();

        BeanUtil.copyFieldValueNvl(copy, this);

        copy.reference = this.reference.clone();
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
