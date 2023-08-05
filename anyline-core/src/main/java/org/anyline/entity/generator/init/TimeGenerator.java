package org.anyline.entity.generator.init;

import org.anyline.entity.DataRow;
import org.anyline.metadata.Column;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;

import java.util.LinkedHashMap;
import java.util.List;

public class TimeGenerator implements PrimaryGenerator {
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
            if(null != BeanUtil.getFieldValue(entity, column)) {
                continue;
            }
            create(entity, type, table, column, other);
        }
        return true;
    }
    @Override
    public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column> columns, String other) {
        if(null == columns){
            if(entity instanceof DataRow){
                columns = ((DataRow)entity).getPrimaryColumns();
            }else{
                columns = EntityAdapterProxy.primaryKeys(entity.getClass());
            }
        }
        for(Column column:columns.values()){
            if(null != BeanUtil.getFieldValue(entity, column.getName())) {
                continue;
            }
            create(entity, type, table, column.getName(), other);
        }
        return true;
    }
    public boolean create(Object entity, DatabaseType type, String table, String column, String other) {
        String format = ConfigTable.PRIMARY_GENERATOR_TIME_FORMAT;
        if(null == format){
            format = "yyyyMMddHHmmssSSS";
        }
        String value = DateUtil.format(format);
        if(ConfigTable.PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH > 0){
            value += BasicUtil.getRandomNumberString(ConfigTable.PRIMARY_GENERATOR_TIME_SUFFIX_LENGTH);
        }
        BeanUtil.setFieldValue(entity, column, value, true);
        return true;
    }
}
