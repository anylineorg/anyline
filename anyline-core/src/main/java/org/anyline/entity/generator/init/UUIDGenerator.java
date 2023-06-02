package org.anyline.entity.generator.init;

import org.anyline.entity.DataRow;
import org.anyline.entity.data.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BeanUtil;

import java.util.List;
import java.util.UUID;

public class UUIDGenerator implements PrimaryGenerator {
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
            String value = UUID.randomUUID().toString();
            BeanUtil.setFieldValue(entity, column, value, true);
            if(!inserts.contains(column) && !inserts.contains("+"+column)){
                inserts.add("+"+column);
            }
        }
        return true;
    }
}
