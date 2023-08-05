package org.anyline.entity.generator.init;

import org.anyline.entity.DataRow;
import org.anyline.metadata.Column;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.LinkedHashMap;
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
    private void create(Object entity, DatabaseType type, String table, String column, String other){

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
}
