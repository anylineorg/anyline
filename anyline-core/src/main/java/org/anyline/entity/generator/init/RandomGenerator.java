package org.anyline.entity.generator.init;

import org.anyline.entity.DataRow;
import org.anyline.entity.data.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.List;

public class RandomGenerator implements PrimaryGenerator {
    @Override
    public boolean create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
        if(null == columns){
            if(entity instanceof DataRow){
                columns = ((DataRow)entity).getPrimaryKeys();
            }else{
                columns = EntityAdapterProxy.primaryKeys(entity.getClass(), true);
            }
        }
        for(String column:columns){
            if(null == BeanUtil.getFieldValue(entity, column)) {
                continue;
            }
            String prefix = ConfigTable.PRIMARY_GENERATOR_PREFIX;
            int len = ConfigTable.PRIMARY_GENERATOR_RANDOM_LENGTH - prefix.length();
            String value = prefix + BasicUtil.getRandomString(len);
            if(ConfigTable.PRIMARY_GENERATOR_UPPER){
                value = value.toUpperCase();
            }else if(ConfigTable.PRIMARY_GENERATOR_LOWER){
                value = value.toLowerCase();
            }
            BeanUtil.setFieldValue(entity, column, value, true);

        }
        return true;
    }
}
