package org.anyline.entity.generator.init;

import org.anyline.entity.data.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.util.BeanUtil;

import java.util.List;
import java.util.UUID;

public class UUIDGenerator implements PrimaryGenerator {
    @Override
    public Object create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
        for(String column:columns){
            String value = UUID.randomUUID().toString();
            BeanUtil.setFieldValue(entity, column, value, false);
        }
        return entity;
    }
}
