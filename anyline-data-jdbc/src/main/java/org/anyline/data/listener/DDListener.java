package org.anyline.data.listener;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.entity.data.*;
import org.anyline.service.AnylineService;

import java.util.Collection;

public interface DDListener {

    public boolean beforeAdd(Column column);

    public void afterAdd(Column column, boolean result);

    /**
     * 修改列之前触发
     * @param column 列
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeAlter(Column column);
    public void afterAlter(Column column, boolean result);

    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(如String to Number) 修改表内容
     * @param table 表
     * @param column 列
     * @param exception exception
     * @return boolean  如果返回false则中断执行
     */
    public boolean afterAlterColumnException(Table table, Column column, Exception exception);
    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(如String to Number) 修改表内容
     * @param table 表
     * @param column 列
     * @param rows rows 整个表中行数(超出ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION时调用)
     * @param exception exception
     * @return boolean  如果返回false则中断执行
     */
    public boolean afterAlterColumnException(Table table, Column column, int rows, Exception exception);

    public boolean beforeDrop(Column column);
    public void afterDrop(Column column, boolean result);

    public boolean beforeRename(Column column);
    public void afterRename(Column column, boolean result);


    /**
     * 创建 table 之前触发
     * @param table
     * @return
     */
    public boolean beforeCreate(Table table);
    public void afterCreate(Table table, boolean result);
    public boolean beforeAlter(Table table);
    public boolean beforeAlter(Table table, Collection<Column> columns);
    public void afterAlter(Table table, boolean result);
    public void afterAlter(Table table, Collection<Column> columns, boolean result);

    public boolean beforeDrop(Table table);
    public void afterDrop(Table table, boolean result);
    public boolean beforeRename(Table table);
    public void afterRename(Table table, boolean result);


    /**
     * 创建 view 之前调用
     * @param view view
     * @return boolean
     */
    public boolean beforeCreate(View view);
    public void afterCreate(View view, boolean result);
    public boolean beforeAlter(View view);
    public void afterAlter(View view, boolean result);
    public boolean beforeDrop(View view);
    public void afterDrop(View view, boolean result);
    public boolean beforeRename(View view);
    public void afterRename(View view, boolean result);


    /**
     * 创建 MasterTable 之前调用
     * @param table table
     * @return boolean
     */
    public boolean beforeCreate(MasterTable table);
    public void afterCreate(MasterTable table, boolean result);
    public boolean beforeAlter(MasterTable table);
    public void afterAlter(MasterTable table, boolean result);
    public boolean beforeDrop(MasterTable table);
    public void afterDrop(MasterTable table, boolean result);
    public boolean beforeRename(MasterTable table);
    public void afterRename(MasterTable table, boolean result);

    /**
     * 创建 PartitionTable 之前调用
     * @param table table
     * @return boolean
     */
    public boolean beforeCreate(PartitionTable table);
    public void afterCreate(PartitionTable table, boolean result);
    public boolean beforeAlter(PartitionTable table);
    public void afterAlter(PartitionTable table, boolean result);
    public boolean beforeDrop(PartitionTable table);
    public void afterDrop(PartitionTable table, boolean result);
    public boolean beforeRename(PartitionTable table);
    public void afterRename(PartitionTable table, boolean result);

    /**
     * 创建 index 之前触发
     * @param index index
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeAdd(Index index);
    public void afterAdd(Index index, boolean result);

    public boolean beforeAlter(Index index);
    public void afterAlter(Index index, boolean result);

    public boolean beforeDrop(Index index);
    public void afterDrop(Index index, boolean result);
    public boolean beforeRename(Index index);
    public void afterRename(Index index, boolean result);

    /**
     * 创建 primary 之前触发
     * @param primary primary
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeAdd(PrimaryKey primary);
    public void afterAdd(PrimaryKey primary, boolean result);

    public boolean beforeAlter(PrimaryKey primary);
    public void afterAlter(PrimaryKey primary, boolean result);

    public boolean beforeDrop(PrimaryKey primary);
    public void afterDrop(PrimaryKey primary, boolean result);
    public boolean beforeRename(PrimaryKey primary);
    public void afterRename(PrimaryKey primary, boolean result);
    /**
     * 创建 foreign 之前触发
     * @param foreign foreign
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeAdd(ForeignKey foreign);
    public void afterAdd(ForeignKey foreign, boolean result);

    public boolean beforeAlter(ForeignKey foreign);
    public void afterAlter(ForeignKey foreign, boolean result);

    public boolean beforeDrop(ForeignKey foreign);
    public void afterDrop(ForeignKey foreign, boolean result);
    public boolean beforeRename(ForeignKey foreign);
    public void afterRename(ForeignKey foreign, boolean result);


    /**
     * 创建constraint之前触发
     * @param constraint constraint
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeAdd(Constraint constraint);
    public void afterAdd(Constraint constraint, boolean result);
    public boolean beforeAlter(Constraint constraint);
    public void afterAlter(Constraint constraint, boolean result);
    public boolean beforeDrop(Constraint constraint);
    public void afterDrop(Constraint constraint, boolean result);
    public boolean beforeRename(Constraint constraint);
    public void afterRename(Constraint constraint, boolean result);

    /**
     * 创建procedure之前触发
     * @param procedure procedure
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeCreate(Procedure procedure);
    public void afterCreate(Procedure procedure, boolean result);
    public boolean beforeAlter(Procedure procedure);
    public void afterAlter(Procedure procedure, boolean result);
    public boolean beforeDrop(Procedure procedure);
    public void afterDrop(Procedure procedure, boolean result);
    public boolean beforeRename(Procedure procedure);
    public void afterRename(Procedure procedure, boolean result);
    /**
     * 创建 function 之前触发
     * @param function function
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeCreate(Function function);
    public void afterCreate(Function function, boolean result);
    public boolean beforeAlter(Function function);
    public void afterAlter(Function function, boolean result);
    public boolean beforeDrop(Function function);
    public void afterDrop(Function function, boolean result);
    public boolean beforeRename(Function function);
    public void afterRename(Function function, boolean result);


    /**
     * 创建 trigger 之前触发
     * @param trigger trigger
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeCreate(Trigger trigger);
    public void afterCreate(Trigger trigger, boolean result);
    public boolean beforeAlter(Trigger trigger);
    public void afterAlter(Trigger trigger, boolean result);
    public boolean beforeDrop(Trigger trigger);
    public void afterDrop(Trigger trigger, boolean result);
    public boolean beforeRename(Trigger trigger);
    public void afterRename(Trigger trigger, boolean result);

    public void setService(AnylineService srvice);
    public AnylineService getService();
    public void setAdapter(JDBCAdapter adapter);



}
