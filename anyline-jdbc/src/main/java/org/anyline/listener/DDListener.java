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
    public boolean beforeDrop(Column column);
    public void afterDrop(Column column, boolean result);



    public boolean beforeAlter(Table table);
    public void afterAlter(Table table, boolean result);
    public boolean beforeDrop(Table table);
    public void afterDrop(Table table, boolean result);

    public void setService(AnylineService service);
}
