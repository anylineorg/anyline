package org.anyline.data.generator.init;

import org.anyline.data.generator.PrimaryGenerator;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.List;

public class RandomGenerator implements PrimaryGenerator {
    @Override
    public Object create(Object entity, JDBCAdapter.DB_TYPE type, String table, List<String> columns, String other) {
        for(String column:columns){
            String prefix = ConfigTable.PRIMARY_GENERATOR_PREFIX;
            int len = ConfigTable.PRIMARY_GENERATOR_RANDOM_LENGTH - prefix.length();
            String value = prefix + BasicUtil.getRandomString(len);
            if(ConfigTable.PRIMARY_GENERATOR_UPPER){
                value = value.toUpperCase();
            }else if(ConfigTable.PRIMARY_GENERATOR_LOWER){
                value = value.toLowerCase();
            }
            BeanUtil.setFieldValue(entity, column, value, false);
        }
        return entity;
    }
}
