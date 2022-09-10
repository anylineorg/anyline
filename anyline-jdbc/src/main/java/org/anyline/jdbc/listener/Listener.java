package org.anyline.jdbc.listener;

import org.anyline.jdbc.entity.Column;

public interface Listener {
    /**
     * 修改列之前触发
     * @param column 列
     * @return boolean  如果返回false则中断执行
     */
    public boolean beforeAlter(Column column);
    public void afterAlter(Column column, boolean result);
}
