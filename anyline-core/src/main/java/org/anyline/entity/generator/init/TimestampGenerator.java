package org.anyline.entity.generator.init;

import org.anyline.entity.DataRow;
import org.anyline.entity.data.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.List;

public class TimestampGenerator implements PrimaryGenerator {
    @Override
    public boolean create(Object entity, DatabaseType type, String table, List<String> columns, List<String> inserts, String other) {
        if(null == columns){
            if(entity instanceof DataRow){
                columns = ((DataRow)entity).getPrimaryKeys();
            }else{
                columns = EntityAdapterProxy.primaryKeys(entity.getClass(), true);
            }
        }
        for(String column:columns){
            String value = System.currentTimeMillis()+"";
            if(ConfigTable.PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH > 0){
                value += BasicUtil.getRandomNumberString(ConfigTable.PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH);
            }
            BeanUtil.setFieldValue(entity, column, value, false);
            if(!inserts.contains(column)){
                inserts.add(column);
            }
        }
        return true;
    }
}
