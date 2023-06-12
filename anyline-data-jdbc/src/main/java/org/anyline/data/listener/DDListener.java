package org.anyline.data.listener;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.entity.data.*;
import org.anyline.service.AnylineService;

import java.util.Collection;

public interface DDListener {

    public boolean beforeCreate(Table table);
    public void afterCreate(Table table, boolean result);
    public boolean beforeCreate(View view);
    public void afterCreate(View view, boolean result);
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



    public boolean beforeAlter(Table table);
    public boolean beforeAlter(Table table, Collection<Column> columns);
    public void afterAlter(Table table, boolean result);
    public void afterAlter(Table table, Collection<Column> columns, boolean result);

    public boolean beforeAlter(View view);
    public void afterAlter(View view, boolean result);
    public boolean beforeDrop(Table table);
    public void afterDrop(Table table, boolean result);
    public boolean beforeDrop(View view);
    public void afterDrop(View view, boolean result);
    public boolean beforeRename(Table table);
    public void afterRename(Table table, boolean result);



    public boolean beforeAdd(Index index);
    public void afterAdd(Index index, boolean result);

    /**
     * 修改index之前触发
     * @param index index
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeAlter(Index index);
    public void afterAlter(Index index, boolean result);

    public boolean beforeDrop(Index index);
    public void afterDrop(Index index, boolean result);

    public boolean beforeAdd(Constraint constraint);
    public void afterAdd(Constraint constraint, boolean result);

    /**
     * 修改index之前触发
     * @param constraint constraint
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeAlter(Constraint constraint);
    public void afterAlter(Constraint constraint, boolean result);

    public boolean beforeDrop(Constraint constraint);
    public void afterDrop(Constraint constraint, boolean result);

    public void setService(AnylineService srvice);
    public AnylineService getService();
    public void setAdapter(JDBCAdapter adapter);

}
