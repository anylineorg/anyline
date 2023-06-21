package org.anyline.data.listener;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.run.Run;
import org.anyline.entity.data.*;

import java.util.Collection;
import java.util.List;

public interface DDListener {

    default boolean prepareAdd(JDBCRuntime runtime, String random,  Column column){
        return true;
    }
    default boolean beforeAdd(JDBCRuntime runtime, String random,  Column column, List<Run> runs){
        return true;
    }
    default void afterAdd(JDBCRuntime runtime, String random,  Column column, List<Run> runs, boolean result, long millis){}


    default boolean prepareAlter(JDBCRuntime runtime, String random,  Column column){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  Column column, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  Column column, List<Run> runs, boolean result, long millis){}

    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(JDBCRuntime runtime, String random,  如String to Number) 修改表内容
     * @param table 表
     * @param column 列
     * @param exception exception
     * @return default boolean  如果返回false则中断执行
     */
    default boolean afterAlterColumnException(JDBCRuntime runtime, String random,  Table table, Column column, Exception exception){return true;}
    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(JDBCRuntime runtime, String random,  如String to Number) 修改表内容
     * @param table 表
     * @param column 列
     * @param rows rows 整个表中行数(JDBCRuntime runtime, String random,  超出ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION时调用)
     * @param exception exception
     * @return default boolean  如果返回false则中断执行
     */
    default boolean afterAlterColumnException(JDBCRuntime runtime, String random,  Table table, Column column, int rows, Exception exception){return true;}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  Column column){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  Column column, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  Column column, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  Column column){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  Column column, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  Column column, List<Run> runs, boolean result, long millis){}


    /**
     * 创建 table 之前触发
     * @param table
     * @return
     */
    default boolean prepareCreate(JDBCRuntime runtime, String random,  Table table){return true;}
    default boolean beforeCreate(JDBCRuntime runtime, String random,  Table table, List<Run> runs){return true;}
    default void afterCreate(JDBCRuntime runtime, String random,  Table table, List<Run> runs, boolean result, long millis){}

    default boolean parepareAlter(JDBCRuntime runtime, String random,  Table table){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  Table table, List<Run> runs){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  Table table, Collection<Column> columns){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  Table table, List<Run> runs, boolean result, long millis){}
    default void afterAlter(JDBCRuntime runtime, String random,  Table table, Collection<Column> columns, List<Run> runs, boolean result, long millis){}


    default boolean prepareDrop(JDBCRuntime runtime, String random,  Table table){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  Table table, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  Table table, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  Table table){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  Table table, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  Table table, List<Run> runs, boolean result, long millis){}


    /**
     * 创建 view 之前调用
     * @param view view
     * @return boolean
     */
    default boolean prepareCreate(JDBCRuntime runtime, String random,  View view){return true;}
    default boolean beforeCreate(JDBCRuntime runtime, String random,  View view, List<Run> runs){return true;}
    default void afterCreate(JDBCRuntime runtime, String random,  View view, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  View view){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  View view, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  View view, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  View view){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  View view, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  View view, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  View view){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  View view, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  View view, List<Run> runs, boolean result, long millis){}


    /**
     * 创建 MasterTable 之前调用
     * @param table table
     * @return boolean
     */
    default boolean prepareCreate(JDBCRuntime runtime, String random,  MasterTable table){return true;}
    default boolean beforeCreate(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs){return true;}
    default void afterCreate(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  MasterTable table){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  MasterTable table){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  MasterTable table){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs, boolean result, long millis){}

    /**
     * 创建 PartitionTable 之前调用
     * @param table table
     * @return boolean
     */
    default boolean prepareCreate(JDBCRuntime runtime, String random,  PartitionTable table){return true;}
    default boolean beforeCreate(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs){return true;}
    default void afterCreate(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  PartitionTable table){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  PartitionTable table){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  PartitionTable table){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs, boolean result, long millis){}

    /**
     * 创建 index 之前触发
     * @param index index
     * @return default boolean  如果返回false则中断执行
     */
    default boolean prepareAdd(JDBCRuntime runtime, String random,  Index index){return true;}
    default boolean beforeAdd(JDBCRuntime runtime, String random,  Index index, List<Run> runs){return true;}
    default void afterAdd(JDBCRuntime runtime, String random,  Index index, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  Index index){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  Index index, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  Index index, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  Index index){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  Index index, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  Index index, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  Index index){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  Index index, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  Index index, List<Run> runs, boolean result, long millis){}

    /**
     * 创建 primary 之前触发
     * @param primary primary
     * @return default boolean  如果返回false则中断执行
     */
    default boolean prepareAdd(JDBCRuntime runtime, String random,  PrimaryKey primary){return true;}
    default boolean beforeAdd(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs){return true;}
    default void afterAdd(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  PrimaryKey primary){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  PrimaryKey primary){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  PrimaryKey primary){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs, boolean result, long millis){}
    /**
     * 创建 foreign 之前触发
     * @param foreign foreign
     * @return default boolean  如果返回false则中断执行
     */
    default boolean prepareAdd(JDBCRuntime runtime, String random,  ForeignKey foreign){return true;}
    default boolean beforeAdd(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs){return true;}
    default void afterAdd(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  ForeignKey foreign){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  ForeignKey foreign){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  ForeignKey foreign){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs, boolean result, long millis){}


    /**
     * 创建constraint之前触发
     * @param constraint constraint
     * @return default boolean  如果返回false则中断执行
     */
    default boolean prepareAdd(JDBCRuntime runtime, String random,  Constraint constraint){return true;}
    default boolean beforeAdd(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs){return true;}
    default void afterAdd(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  Constraint constraint){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  Constraint constraint){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  Constraint constraint){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs, boolean result, long millis){}

    /**
     * 创建procedure之前触发
     * @param procedure procedure
     * @return default boolean  如果返回false则中断执行
     */
    default boolean prepareCreate(JDBCRuntime runtime, String random,  Procedure procedure){return true;}
    default boolean beforeCreate(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs){return true;}
    default void afterCreate(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  Procedure procedure){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  Procedure procedure){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  Procedure procedure){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs, boolean result, long millis){}
    /**
     * 创建 function 之前触发
     * @param function function
     * @return default boolean  如果返回false则中断执行
     */
    default boolean prepareCreate(JDBCRuntime runtime, String random,  Function function){return true;}
    default boolean beforeCreate(JDBCRuntime runtime, String random,  Function function, List<Run> runs){return true;}
    default void afterCreate(JDBCRuntime runtime, String random,  Function function, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  Function function){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  Function function, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  Function function, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  Function function){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  Function function, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  Function function, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  Function function){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  Function function, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  Function function, List<Run> runs, boolean result, long millis){}


    /**
     * 创建 trigger 之前触发
     * @param trigger trigger
     * @return default boolean  如果返回false则中断执行
     */
    default boolean prepareCreate(JDBCRuntime runtime, String random,  Trigger trigger){return true;}
    default boolean beforeCreate(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs){return true;}
    default void afterCreate(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs, boolean result, long millis){}

    default boolean prepareAlter(JDBCRuntime runtime, String random,  Trigger trigger){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs){return true;}
    default void afterAlter(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs, boolean result, long millis){}

    default boolean prepareDrop(JDBCRuntime runtime, String random,  Trigger trigger){return true;}
    default boolean beforeDrop(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs){return true;}
    default void afterDrop(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs, boolean result, long millis){}

    default boolean prepareRename(JDBCRuntime runtime, String random,  Trigger trigger){return true;}
    default boolean beforeRename(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs){return true;}
    default void afterRename(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs, boolean result, long millis){}

}
