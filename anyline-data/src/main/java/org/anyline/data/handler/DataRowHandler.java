package org.anyline.data.handler;

import org.anyline.entity.DataRow;

public interface DataRowHandler extends StreamHandler{

    /**
     *
     * @param row 一行
     * @return 返回false中断遍历
     */
    boolean read(DataRow row);
}
