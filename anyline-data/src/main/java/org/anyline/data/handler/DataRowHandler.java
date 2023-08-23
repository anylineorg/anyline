package org.anyline.data.handler;

import org.anyline.entity.DataRow;

public interface DataRowHandler extends StreamHandler{
    boolean read(DataRow row);
}
