package org.anyline.data.jdbc.starrocks;

import org.anyline.annotation.Component;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.MySQLGenusAdapter;
import org.anyline.metadata.type.DatabaseType;

@Component("anyline.data.jdbc.adapter.starrocks")
public class StarRocksAdapter extends MySQLGenusAdapter implements JDBCAdapter {
    public DatabaseType type() {
        return DatabaseType.StarRocks;
    }

    public StarRocksAdapter() {
        super();
        delimiterFr = "`";
        delimiterTo = "`";
    }
}
