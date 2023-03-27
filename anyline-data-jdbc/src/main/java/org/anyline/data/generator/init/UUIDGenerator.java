package org.anyline.data.generator.init;

import org.anyline.data.generator.PrimaryGenerator;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.util.BeanUtil;

import java.util.List;
import java.util.UUID;

public class UUIDGenerator implements PrimaryGenerator {
    @Override
    public Object create(Object entity, JDBCAdapter.DB_TYPE type, String table, List<String> columns, String other) {
        for(String column:columns){
            String value = UUID.randomUUID().toString();
            BeanUtil.setFieldValue(entity, column, value, false);
        }
        return entity;
    }
}
