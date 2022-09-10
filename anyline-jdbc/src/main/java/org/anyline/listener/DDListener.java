package org.anyline.listener;

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
     * @param column column
     * @param exception
     * @return boolean  如果返回false则中断执行
     */
    public boolean afterAlterException(Column column, Exception exception);

    public boolean beforeDrop(Column column);
    public void afterDrop(Column column, boolean result);



    public boolean beforeAlter(Table table);
    public void afterAlter(Table table, boolean result);
    public boolean beforeDrop(Table table);
    public void afterDrop(Table table, boolean result);

    public void setService(AnylineService service);
}
