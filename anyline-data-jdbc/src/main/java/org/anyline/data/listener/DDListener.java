package org.anyline.data.listener;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.entity.data.*;

import java.util.Collection;

public interface DDListener {

    default boolean beforeAdd(JDBCRuntime runtime, Column column){
        return true;
    }
    
    default void afterAdd(JDBCRuntime runtime, Column column, boolean result){}
    
    /**
     * 修改列之前触发
     * @param column 列
     * @return default boolean  如果返回false则中断执行
     */
    default boolean beforeAlter(JDBCRuntime runtime, Column column){return true;}
    default void afterAlter(JDBCRuntime runtime, Column column, boolean result){}

    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(JDBCRuntime runtime, 如String to Number) 修改表内容
     * @param table 表
     * @param column 列
     * @param exception exception
     * @return default boolean  如果返回false则中断执行
     */
    default boolean afterAlterColumnException(JDBCRuntime runtime, Table table, Column column, Exception exception){return true;}
    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(JDBCRuntime runtime, 如String to Number) 修改表内容
     * @param table 表
     * @param column 列
     * @param rows rows 整个表中行数(JDBCRuntime runtime, 超出ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION时调用)
     * @param exception exception
     * @return default boolean  如果返回false则中断执行
     */
    default boolean afterAlterColumnException(JDBCRuntime runtime, Table table, Column column, int rows, Exception exception){return true;}

    default boolean beforeDrop(JDBCRuntime runtime, Column column){return true;}
    default void afterDrop(JDBCRuntime runtime, Column column, boolean result){}

    default boolean beforeRename(JDBCRuntime runtime, Column column){return true;}
    default void afterRename(JDBCRuntime runtime, Column column, boolean result){}


    /**
     * 创建 table 之前触发
     * @param table
     * @return
     */
    default boolean beforeCreate(JDBCRuntime runtime, Table table){return true;}
    default void afterCreate(JDBCRuntime runtime, Table table, boolean result){}
    default boolean beforeAlter(JDBCRuntime runtime, Table table){return true;}
    default boolean beforeAlter(JDBCRuntime runtime, Table table, Collection<Column> columns){return true;}
    default void afterAlter(JDBCRuntime runtime, Table table, boolean result){}
    default void afterAlter(JDBCRuntime runtime, Table table, Collection<Column> columns, boolean result){}

    default boolean beforeDrop(JDBCRuntime runtime, Table table){return true;}
    default void afterDrop(JDBCRuntime runtime, Table table, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, Table table){return true;}
    default void afterRename(JDBCRuntime runtime, Table table, boolean result){}


    /**
     * 创建 view 之前调用
     * @param view view
     * @return boolean
     */
    default boolean beforeCreate(JDBCRuntime runtime, View view){return true;}
    default void afterCreate(JDBCRuntime runtime, View view, boolean result){}
    default boolean beforeAlter(JDBCRuntime runtime, View view){return true;}
    default void afterAlter(JDBCRuntime runtime, View view, boolean result){}
    default boolean beforeDrop(JDBCRuntime runtime, View view){return true;}
    default void afterDrop(JDBCRuntime runtime, View view, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, View view){return true;}
    default void afterRename(JDBCRuntime runtime, View view, boolean result){}


    /**
     * 创建 MasterTable 之前调用
     * @param table table
     * @return boolean
     */
    default boolean beforeCreate(JDBCRuntime runtime, MasterTable table){return true;}
    default void afterCreate(JDBCRuntime runtime, MasterTable table, boolean result){}
    default boolean beforeAlter(JDBCRuntime runtime, MasterTable table){return true;}
    default void afterAlter(JDBCRuntime runtime, MasterTable table, boolean result){}
    default boolean beforeDrop(JDBCRuntime runtime, MasterTable table){return true;}
    default void afterDrop(JDBCRuntime runtime, MasterTable table, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, MasterTable table){return true;}
    default void afterRename(JDBCRuntime runtime, MasterTable table, boolean result){}

    /**
     * 创建 PartitionTable 之前调用
     * @param table table
     * @return boolean
     */
    default boolean beforeCreate(JDBCRuntime runtime, PartitionTable table){return true;}
    default void afterCreate(JDBCRuntime runtime, PartitionTable table, boolean result){}
    default boolean beforeAlter(JDBCRuntime runtime, PartitionTable table){return true;}
    default void afterAlter(JDBCRuntime runtime, PartitionTable table, boolean result){}
    default boolean beforeDrop(JDBCRuntime runtime, PartitionTable table){return true;}
    default void afterDrop(JDBCRuntime runtime, PartitionTable table, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, PartitionTable table){return true;}
    default void afterRename(JDBCRuntime runtime, PartitionTable table, boolean result){}

    /**
     * 创建 index 之前触发
     * @param index index
     * @return default boolean  如果返回false则中断执行
     */
    default boolean beforeAdd(JDBCRuntime runtime, Index index){return true;}
    default void afterAdd(JDBCRuntime runtime, Index index, boolean result){}

    default boolean beforeAlter(JDBCRuntime runtime, Index index){return true;}
    default void afterAlter(JDBCRuntime runtime, Index index, boolean result){}

    default boolean beforeDrop(JDBCRuntime runtime, Index index){return true;}
    default void afterDrop(JDBCRuntime runtime, Index index, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, Index index){return true;}
    default void afterRename(JDBCRuntime runtime, Index index, boolean result){}

    /**
     * 创建 primary 之前触发
     * @param primary primary
     * @return default boolean  如果返回false则中断执行
     */
    default boolean beforeAdd(JDBCRuntime runtime, PrimaryKey primary){return true;}
    default void afterAdd(JDBCRuntime runtime, PrimaryKey primary, boolean result){}

    default boolean beforeAlter(JDBCRuntime runtime, PrimaryKey primary){return true;}
    default void afterAlter(JDBCRuntime runtime, PrimaryKey primary, boolean result){}

    default boolean beforeDrop(JDBCRuntime runtime, PrimaryKey primary){return true;}
    default void afterDrop(JDBCRuntime runtime, PrimaryKey primary, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, PrimaryKey primary){return true;}
    default void afterRename(JDBCRuntime runtime, PrimaryKey primary, boolean result){}
    /**
     * 创建 foreign 之前触发
     * @param foreign foreign
     * @return default boolean  如果返回false则中断执行
     */
    default boolean beforeAdd(JDBCRuntime runtime, ForeignKey foreign){return true;}
    default void afterAdd(JDBCRuntime runtime, ForeignKey foreign, boolean result){}

    default boolean beforeAlter(JDBCRuntime runtime, ForeignKey foreign){return true;}
    default void afterAlter(JDBCRuntime runtime, ForeignKey foreign, boolean result){}

    default boolean beforeDrop(JDBCRuntime runtime, ForeignKey foreign){return true;}
    default void afterDrop(JDBCRuntime runtime, ForeignKey foreign, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, ForeignKey foreign){return true;}
    default void afterRename(JDBCRuntime runtime, ForeignKey foreign, boolean result){}


    /**
     * 创建constraint之前触发
     * @param constraint constraint
     * @return default boolean  如果返回false则中断执行
     */
    default boolean beforeAdd(JDBCRuntime runtime, Constraint constraint){return true;}
    default void afterAdd(JDBCRuntime runtime, Constraint constraint, boolean result){}
    default boolean beforeAlter(JDBCRuntime runtime, Constraint constraint){return true;}
    default void afterAlter(JDBCRuntime runtime, Constraint constraint, boolean result){}
    default boolean beforeDrop(JDBCRuntime runtime, Constraint constraint){return true;}
    default void afterDrop(JDBCRuntime runtime, Constraint constraint, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, Constraint constraint){return true;}
    default void afterRename(JDBCRuntime runtime, Constraint constraint, boolean result){}

    /**
     * 创建procedure之前触发
     * @param procedure procedure
     * @return default boolean  如果返回false则中断执行
     */
    default boolean beforeCreate(JDBCRuntime runtime, Procedure procedure){return true;}
    default void afterCreate(JDBCRuntime runtime, Procedure procedure, boolean result){}
    default boolean beforeAlter(JDBCRuntime runtime, Procedure procedure){return true;}
    default void afterAlter(JDBCRuntime runtime, Procedure procedure, boolean result){}
    default boolean beforeDrop(JDBCRuntime runtime, Procedure procedure){return true;}
    default void afterDrop(JDBCRuntime runtime, Procedure procedure, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, Procedure procedure){return true;}
    default void afterRename(JDBCRuntime runtime, Procedure procedure, boolean result){}
    /**
     * 创建 function 之前触发
     * @param function function
     * @return default boolean  如果返回false则中断执行
     */
    default boolean beforeCreate(JDBCRuntime runtime, Function function){return true;}
    default void afterCreate(JDBCRuntime runtime, Function function, boolean result){}
    default boolean beforeAlter(JDBCRuntime runtime, Function function){return true;}
    default void afterAlter(JDBCRuntime runtime, Function function, boolean result){}
    default boolean beforeDrop(JDBCRuntime runtime, Function function){return true;}
    default void afterDrop(JDBCRuntime runtime, Function function, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, Function function){return true;}
    default void afterRename(JDBCRuntime runtime, Function function, boolean result){}


    /**
     * 创建 trigger 之前触发
     * @param trigger trigger
     * @return default boolean  如果返回false则中断执行
     */
    default boolean beforeCreate(JDBCRuntime runtime, Trigger trigger){return true;}
    default void afterCreate(JDBCRuntime runtime, Trigger trigger, boolean result){}
    default boolean beforeAlter(JDBCRuntime runtime, Trigger trigger){return true;}
    default void afterAlter(JDBCRuntime runtime, Trigger trigger, boolean result){}
    default boolean beforeDrop(JDBCRuntime runtime, Trigger trigger){return true;}
    default void afterDrop(JDBCRuntime runtime, Trigger trigger, boolean result){}
    default boolean beforeRename(JDBCRuntime runtime, Trigger trigger){return true;}
    default void afterRename(JDBCRuntime runtime, Trigger trigger, boolean result){}

}
