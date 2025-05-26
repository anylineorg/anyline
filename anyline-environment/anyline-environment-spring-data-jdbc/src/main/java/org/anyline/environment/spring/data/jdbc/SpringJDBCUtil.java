/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.environment.spring.data.jdbc;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.util.BasicUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.util.LinkedHashMap;

public class SpringJDBCUtil {
    private static Log log = LogProxy.get(SpringJDBCUtil.class);

    /**
     * column[结果集封装]<br/>(方法4)<br/>
     * 解析查询结果metadata(0=1)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param columns columns
     * @param table 表
     * @param set SqlRowSet由spring封装过的结果集ResultSet
     * @return LinkedHashMap
     * @param <T> Column
     * @throws Exception Exception
     */
    public static <T extends Column> LinkedHashMap<String, T> columns(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception {
        if(null == columns) {
            columns = new LinkedHashMap<>();
        }
        SqlRowSetMetaData rsm = set.getMetaData();
        for (int i = 1; i <= rsm.getColumnCount(); i++) {
            String name = rsm.getColumnName(i);
            if(BasicUtil.isEmpty(name)) {
                continue;
            }
            T column = columns.get(name.toUpperCase());
            if(null == column) {
                if(create) {
                    column = (T)column(adapter, runtime, column, rsm, i);
                    if(BasicUtil.isEmpty(column.getName())) {
                        column.setName(name);
                    }
                    columns.put(column.getName().toUpperCase(), column);
                }
            }
        }
        return columns;
    }

    /**
     * column[结果集封装-子流程](方法4)<br/>
     * 内部遍历<br/>
     * columns(DataRuntime runtime, boolean create, LinkedHashMap columns, Table table, SqlRowSet set)遍历内部<br/>
     * 根据SqlRowSetMetaData获取列属性 jdbc.queryForRowSet(where 1=0)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column 获取的数据赋值给column如果为空则新创建一个
     * @param rsm 通过spring封装过的SqlRowSet获取的SqlRowSetMetaData
     * @param index 第几列
     * @return Column
     */
    
    public static Column column(DriverAdapter adapter, DataRuntime runtime, Column column, SqlRowSetMetaData rsm, int index) {
        if(null == column) {
            column = new Column();
            String catalog = null;
            String schema = null;
            try {
                catalog = BasicUtil.evl(rsm.getCatalogName(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getCatalogName]");
            }
            try {
                schema = BasicUtil.evl(rsm.getSchemaName(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getSchemaName]");
            }
            adapter.correctSchemaFromJDBC(runtime, column, catalog, schema);
            try {
                column.setClassName(rsm.getColumnClassName(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getColumnClassName]");
            }
            try {
                column.currency(rsm.isCurrency(index));
            } catch (Exception e) {
                column.caseSensitive(rsm.isCaseSensitive(index));
                log.debug("[获取MetaData失败][驱动未实现:isCurrency]");
            }
            try {
                column.setOriginName(rsm.getColumnName(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getColumnName]");
            }
            try {
                column.setName(rsm.getColumnLabel(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getColumnLabel]");
            }
            try {
                column.setPrecision(rsm.getPrecision(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getPrecision]");
            }
            try {
                column.setScale(rsm.getScale(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getScale]");
            }
            try {
                column.setDisplaySize(rsm.getColumnDisplaySize(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getColumnDisplaySize]");
            }
            try {
                column.setSigned(rsm.isSigned(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:isSigned]");
            }
            try {
                column.setTable(rsm.getTableName(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getTableName]");
            }
            try {
                column.setType(rsm.getColumnType(index));
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getColumnType]");
            }
            try {
                String jdbcType = rsm.getColumnTypeName(index);
                column.setJdbcType(jdbcType);
                if(BasicUtil.isEmpty(column.getTypeName())) {
                    column.setTypeName(jdbcType);
                }
            } catch (Exception e) {
                log.debug("[获取MetaData失败][驱动未实现:getColumnTypeName]");
            }

            adapter.typeMetadata(runtime, column);
        }
        return column;
    }

}
