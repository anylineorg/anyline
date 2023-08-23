package org.anyline.data.handler;

import java.sql.ResultSet;

public interface ResultSetHandler extends StreamHandler{
    void read(ResultSet result);
}
