package org.anyline.data.handler;

import java.sql.ResultSet;

public interface ResultSetHandler extends StreamHandler{
    /**
     *
     * @param result ResultSet
     * @return boolean
     */
    boolean read(ResultSet result);
}
