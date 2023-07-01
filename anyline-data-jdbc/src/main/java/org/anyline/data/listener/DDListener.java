package org.anyline.data.listener;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.run.Run;
import org.anyline.entity.data.*;
import org.anyline.entity.data.ACTION.SWITCH;


import java.util.Collection;
import java.util.List;

public interface DDListener {
    /**
     * 准备添加列
     * @param runtime 包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param column 列
     * @return SWITCH
     */
    default SWITCH prepareAdd(JDBCRuntime runtime, String random, Column column){ return SWITCH.CONTINUE;}
    default SWITCH beforeAdd(JDBCRuntime runtime, String random, Column column, List<Run> runs){ return SWITCH.CONTINUE;}
    default SWITCH afterAdd(JDBCRuntime runtime, String random,  Column column, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}


    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  Column column){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  Column column, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  Column column, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(JDBCRuntime runtime, String random,  如String to Number) 修改表内容
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param table 表
     * @param column 列
     * @param exception exception
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH afterAlterColumnException(JDBCRuntime runtime, String random,  Table table, Column column, Exception exception){return SWITCH.CONTINUE;}
    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(JDBCRuntime runtime, String random,  如String to Number) 修改表内容
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param table 表
     * @param column 列
     * @param rows rows 整个表中行数(JDBCRuntime runtime, String random,  超出ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION时调用)
     * @param exception exception
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH afterAlterColumnException(JDBCRuntime runtime, String random,  Table table, Column column, int rows, Exception exception){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  Column column){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  Column column, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  Column column, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  Column column){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  Column column, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  Column column, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}


    /**
     * 创建 table 之前触发
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param table 表
     * @return SWITCH
     */
    default SWITCH prepareCreate(JDBCRuntime runtime, String random,  Table table){return SWITCH.CONTINUE;}
    default SWITCH beforeCreate(JDBCRuntime runtime, String random,  Table table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterCreate(JDBCRuntime runtime, String random,  Table table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH parepareAlter(JDBCRuntime runtime, String random,  Table table){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  Table table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  Table table, Collection<Column> columns){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  Table table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  Table table, Collection<Column> columns, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}


    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  Table table){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  Table table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  Table table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  Table table){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  Table table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  Table table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}


    /**
     * 创建 view 之前调用
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param view view
     * @return SWITCH
     */
    default SWITCH prepareCreate(JDBCRuntime runtime, String random,  View view){return SWITCH.CONTINUE;}
    default SWITCH beforeCreate(JDBCRuntime runtime, String random,  View view, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterCreate(JDBCRuntime runtime, String random,  View view, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  View view){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  View view, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  View view, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  View view){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  View view, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  View view, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  View view){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  View view, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  View view, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}


    /**
     * 创建 MasterTable 之前调用
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param table table
     * @return SWITCH
     */
    default SWITCH prepareCreate(JDBCRuntime runtime, String random,  MasterTable table){return SWITCH.CONTINUE;}
    default SWITCH beforeCreate(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterCreate(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  MasterTable table){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  MasterTable table){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  MasterTable table){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  MasterTable table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    /**
     * 创建 PartitionTable 之前调用
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param table table
     * @return SWITCH
     */
    default SWITCH prepareCreate(JDBCRuntime runtime, String random,  PartitionTable table){return SWITCH.CONTINUE;}
    default SWITCH beforeCreate(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterCreate(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  PartitionTable table){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  PartitionTable table){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  PartitionTable table){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  PartitionTable table, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    /**
     * 创建 index 之前触发
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param index index
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH prepareAdd(JDBCRuntime runtime, String random,  Index index){return SWITCH.CONTINUE;}
    default SWITCH beforeAdd(JDBCRuntime runtime, String random,  Index index, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAdd(JDBCRuntime runtime, String random,  Index index, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  Index index){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  Index index, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  Index index, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  Index index){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  Index index, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  Index index, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  Index index){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  Index index, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  Index index, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    /**
     * 创建 primary 之前触发
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param primary primary
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH prepareAdd(JDBCRuntime runtime, String random,  PrimaryKey primary){return SWITCH.CONTINUE;}
    default SWITCH beforeAdd(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAdd(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  PrimaryKey primary){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  PrimaryKey primary){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  PrimaryKey primary){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  PrimaryKey primary, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}
    /**
     * 创建 foreign 之前触发
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param foreign foreign
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH prepareAdd(JDBCRuntime runtime, String random,  ForeignKey foreign){return SWITCH.CONTINUE;}
    default SWITCH beforeAdd(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAdd(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  ForeignKey foreign){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  ForeignKey foreign){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  ForeignKey foreign){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  ForeignKey foreign, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}


    /**
     * 创建constraint之前触发
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param constraint constraint
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH prepareAdd(JDBCRuntime runtime, String random,  Constraint constraint){return SWITCH.CONTINUE;}
    default SWITCH beforeAdd(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAdd(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  Constraint constraint){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  Constraint constraint){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  Constraint constraint){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  Constraint constraint, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    /**
     * 创建procedure之前触发
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param procedure procedure
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH prepareCreate(JDBCRuntime runtime, String random,  Procedure procedure){return SWITCH.CONTINUE;}
    default SWITCH beforeCreate(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterCreate(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  Procedure procedure){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  Procedure procedure){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  Procedure procedure){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  Procedure procedure, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}
    /**
     * 创建 function 之前触发
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param function function
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH prepareCreate(JDBCRuntime runtime, String random,  Function function){return SWITCH.CONTINUE;}
    default SWITCH beforeCreate(JDBCRuntime runtime, String random,  Function function, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterCreate(JDBCRuntime runtime, String random,  Function function, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  Function function){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  Function function, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  Function function, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  Function function){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  Function function, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  Function function, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  Function function){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  Function function, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  Function function, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}


    /**
     * 创建 trigger 之前触发
     * @param runtime  包含数据源(key)、适配器、JDBCTemplate、dao
     * @param random 用来标记同一组SQL、执行结构、参数等
     * @param trigger trigger
     * @return default SWITCH  如果返回false则中断执行
     */
    default SWITCH prepareCreate(JDBCRuntime runtime, String random,  Trigger trigger){return SWITCH.CONTINUE;}
    default SWITCH beforeCreate(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterCreate(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareAlter(JDBCRuntime runtime, String random,  Trigger trigger){return SWITCH.CONTINUE;}
    default SWITCH beforeAlter(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterAlter(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareDrop(JDBCRuntime runtime, String random,  Trigger trigger){return SWITCH.CONTINUE;}
    default SWITCH beforeDrop(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterDrop(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

    default SWITCH prepareRename(JDBCRuntime runtime, String random,  Trigger trigger){return SWITCH.CONTINUE;}
    default SWITCH beforeRename(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs){return SWITCH.CONTINUE;}
    default SWITCH afterRename(JDBCRuntime runtime, String random,  Trigger trigger, List<Run> runs, boolean result, long millis){return SWITCH.CONTINUE;}

}
