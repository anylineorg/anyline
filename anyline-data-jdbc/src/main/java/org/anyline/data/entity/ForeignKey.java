package org.anyline.data.entity;


public class ForeignKey extends Constraint{
    public boolean isForeign(){
        return true;
    }

    private Table reference;
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

    public void setReference(Table reference){
        this.reference = reference;
    }
    /**
     * 添加依赖表
     * @param reference 依赖表
     * @return
     */
    public void setReference(String reference){
        this.reference = new Table(reference);
    }

    public Table getReference() {
        return reference;
    }

    /**
     * 添加列
     * @param column 列 需要设置reference属性
     * @return
     */
    public ForeignKey addColumn(Column column){
        super.addColumn(column);
        return this;
    }

    /**
     * 添加列
     * @param column 列
     * @param table 依赖表
     * @param reference 依赖列
     * @return
     */
    public ForeignKey addColumn(String column, String table, String reference){
        this.reference = new Table(table);
        addColumn(new Column(column).setReference(reference));
        return this;
    }
    /**
     * 添加列
     * @param column 列
     * @param reference 依赖列
     * @return
     */
    public ForeignKey addColumn(String column,  String reference){
        addColumn(new Column(column).setReference(reference));
        return this;
    }
}
