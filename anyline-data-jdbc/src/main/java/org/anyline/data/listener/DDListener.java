package org.anyline.data.listener;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.entity.data.*;
import org.anyline.service.AnylineService;

import java.util.Collection;

public interface DDListener {

    boolean beforeAdd(Column column);

    void afterAdd(Column column, boolean result);

    /**
     * 修改列之前触发
     * @param column 列
     * @return boolean  如果返回false则中断执行
     */
    boolean beforeAlter(Column column);
    void afterAlter(Column column, boolean result);

    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(如String to Number) 修改表内容
     * @param table 表
     * @param column 列
     * @param exception exception
     * @return boolean  如果返回false则中断执行
     */
    boolean afterAlterColumnException(Table table, Column column, Exception exception);
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
    boolean afterAlterColumnException(Table table, Column column, int rows, Exception exception);

    boolean beforeDrop(Column column);
    void afterDrop(Column column, boolean result);

    boolean beforeRename(Column column);
    void afterRename(Column column, boolean result);


    /**
     * 创建 table 之前触发
     * @param table
     * @return
     */
    boolean beforeCreate(Table table);
    void afterCreate(Table table, boolean result);
    boolean beforeAlter(Table table);
    boolean beforeAlter(Table table, Collection<Column> columns);
    void afterAlter(Table table, boolean result);
    void afterAlter(Table table, Collection<Column> columns, boolean result);

    boolean beforeDrop(Table table);
    void afterDrop(Table table, boolean result);
    boolean beforeRename(Table table);
    void afterRename(Table table, boolean result);


    /**
     * 创建 view 之前调用
     * @param view view
     * @return boolean
     */
    boolean beforeCreate(View view);
    void afterCreate(View view, boolean result);
    boolean beforeAlter(View view);
    void afterAlter(View view, boolean result);
    boolean beforeDrop(View view);
    void afterDrop(View view, boolean result);
    boolean beforeRename(View view);
    void afterRename(View view, boolean result);


    /**
     * 创建 MasterTable 之前调用
     * @param table table
     * @return boolean
     */
    boolean beforeCreate(MasterTable table);
    void afterCreate(MasterTable table, boolean result);
    boolean beforeAlter(MasterTable table);
    void afterAlter(MasterTable table, boolean result);
    boolean beforeDrop(MasterTable table);
    void afterDrop(MasterTable table, boolean result);
    boolean beforeRename(MasterTable table);
    void afterRename(MasterTable table, boolean result);

    /**
     * 创建 PartitionTable 之前调用
     * @param table table
     * @return boolean
     */
    boolean beforeCreate(PartitionTable table);
    void afterCreate(PartitionTable table, boolean result);
    boolean beforeAlter(PartitionTable table);
    void afterAlter(PartitionTable table, boolean result);
    boolean beforeDrop(PartitionTable table);
    void afterDrop(PartitionTable table, boolean result);
    boolean beforeRename(PartitionTable table);
    void afterRename(PartitionTable table, boolean result);

    /**
     * 创建 index 之前触发
     * @param index index
     * @return boolean  如果返回false则中断执行
     */
    boolean beforeAdd(Index index);
    void afterAdd(Index index, boolean result);

    boolean beforeAlter(Index index);
    void afterAlter(Index index, boolean result);

    boolean beforeDrop(Index index);
    void afterDrop(Index index, boolean result);
    boolean beforeRename(Index index);
    void afterRename(Index index, boolean result);

    /**
     * 创建 primary 之前触发
     * @param primary primary
     * @return boolean  如果返回false则中断执行
     */
    boolean beforeAdd(PrimaryKey primary);
    void afterAdd(PrimaryKey primary, boolean result);

    boolean beforeAlter(PrimaryKey primary);
    void afterAlter(PrimaryKey primary, boolean result);

    boolean beforeDrop(PrimaryKey primary);
    void afterDrop(PrimaryKey primary, boolean result);
    boolean beforeRename(PrimaryKey primary);
    void afterRename(PrimaryKey primary, boolean result);
    /**
     * 创建 foreign 之前触发
     * @param foreign foreign
     * @return boolean  如果返回false则中断执行
     */
    boolean beforeAdd(ForeignKey foreign);
    void afterAdd(ForeignKey foreign, boolean result);

    boolean beforeAlter(ForeignKey foreign);
    void afterAlter(ForeignKey foreign, boolean result);

    boolean beforeDrop(ForeignKey foreign);
    void afterDrop(ForeignKey foreign, boolean result);
    boolean beforeRename(ForeignKey foreign);
    void afterRename(ForeignKey foreign, boolean result);


    /**
     * 创建constraint之前触发
     * @param constraint constraint
     * @return boolean  如果返回false则中断执行
     */
    boolean beforeAdd(Constraint constraint);
    void afterAdd(Constraint constraint, boolean result);
    boolean beforeAlter(Constraint constraint);
    void afterAlter(Constraint constraint, boolean result);
    boolean beforeDrop(Constraint constraint);
    void afterDrop(Constraint constraint, boolean result);
    boolean beforeRename(Constraint constraint);
    void afterRename(Constraint constraint, boolean result);

    /**
     * 创建procedure之前触发
     * @param procedure procedure
     * @return boolean  如果返回false则中断执行
     */
    boolean beforeCreate(Procedure procedure);
    void afterCreate(Procedure procedure, boolean result);
    boolean beforeAlter(Procedure procedure);
    void afterAlter(Procedure procedure, boolean result);
    boolean beforeDrop(Procedure procedure);
    void afterDrop(Procedure procedure, boolean result);
    boolean beforeRename(Procedure procedure);
    void afterRename(Procedure procedure, boolean result);
    /**
     * 创建 function 之前触发
     * @param function function
     * @return boolean  如果返回false则中断执行
     */
    boolean beforeCreate(Function function);
    void afterCreate(Function function, boolean result);
    boolean beforeAlter(Function function);
    void afterAlter(Function function, boolean result);
    boolean beforeDrop(Function function);
    void afterDrop(Function function, boolean result);
    boolean beforeRename(Function function);
    void afterRename(Function function, boolean result);


    /**
     * 创建 trigger 之前触发
     * @param trigger trigger
     * @return boolean  如果返回false则中断执行
     */
    boolean beforeCreate(Trigger trigger);
    void afterCreate(Trigger trigger, boolean result);
    boolean beforeAlter(Trigger trigger);
    void afterAlter(Trigger trigger, boolean result);
    boolean beforeDrop(Trigger trigger);
    void afterDrop(Trigger trigger, boolean result);
    boolean beforeRename(Trigger trigger);
    void afterRename(Trigger trigger, boolean result);

    void setService(AnylineService srvice);
    AnylineService getService();
    void setAdapter(JDBCAdapter adapter);



}
