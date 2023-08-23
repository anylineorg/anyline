package org.anyline.data.handler;

import java.sql.ResultSet;

public interface ResultSetHandler extends StreamHandler{
    /**
     *
     * @param result ResultSet
     * @return 总行数
     */
    long read(ResultSet result);
}
