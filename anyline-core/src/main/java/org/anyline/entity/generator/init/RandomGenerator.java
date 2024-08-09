/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        if(null == columns) {
            if(entity instanceof DataRow) {
                columns = ((DataRow)entity).getPrimaryKeys();
            }else{
                columns = EntityAdapterProxy.primaryKeys(entity.getClass(), true);
            }
        }
        for(String column:columns) {
            if(null != BeanUtil.getFieldValue(entity, column)) {
                continue;
            }
            create(entity, type, table, column, other);
        }
        return true;
    }

    @Override
    public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column> columns, String other) {
        if(null == columns) {
            if(entity instanceof DataRow) {
                columns = ((DataRow)entity).getPrimaryColumns();
            }else{
                columns = EntityAdapterProxy.primaryKeys(entity.getClass());
            }
        }
        for(Column column:columns.values()) {
            if(null != BeanUtil.getFieldValue(entity, column.getName())) {
                continue;
            }
            create(entity, type, table, column.getName(), other);
        }
        return true;
    }
    private void create(Object entity, DatabaseType type, String table, String column, String other) {

        String prefix = ConfigTable.PRIMARY_GENERATOR_PREFIX;
        int len = ConfigTable.PRIMARY_GENERATOR_RANDOM_LENGTH - prefix.length();
        String value = prefix + BasicUtil.getRandomString(len);
        if(ConfigTable.PRIMARY_GENERATOR_UPPER) {
            value = value.toUpperCase();
        }else if(ConfigTable.PRIMARY_GENERATOR_LOWER) {
            value = value.toLowerCase();
        }
        BeanUtil.setFieldValue(entity, column, value, true);
    }
}
