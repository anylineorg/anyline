package org.anyline.listener;

import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;
import org.anyline.service.AnylineService;

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
     * @param table table
     * @param column column
     * @param exception
     * @return boolean  如果返回false则中断执行
     */
    public boolean afterAlterException(Table table, Column column, Exception exception);
    /**
     * 修改列之后触发
     * 触发之后如果返回true dao将再执行一次 alter column
     * 一般在此事件中处理 发生类型转换时(如String to Number) 修改表内容
     * @param table table
     * @param column column
     * @param rows rows 整个表中行数(超出ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION时调用)
     * @param exception
     * @return boolean  如果返回false则中断执行
     */
    public boolean afterAlterException(Table table, Column column, int rows, Exception exception);

    public boolean beforeDrop(Column column);
    public void afterDrop(Column column, boolean result);



    public boolean beforeAlter(Table table);
    public void afterAlter(Table table, boolean result);
    public boolean beforeDrop(Table table);
    public void afterDrop(Table table, boolean result);
    public boolean beforeRename(Table table);
    public void afterRename(Table table, boolean result);

    public void setService(AnylineService srvice);
    public AnylineService getService();
    public void setCreater(SQLCreater creater);
}
