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



package org.anyline.data.jdbc.util;

import org.anyline.adapter.KeyAdapter;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.handler.*;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;

public class JDBCUtil {
    private static Logger log = LoggerFactory.getLogger(JDBCUtil.class);

    /**
     *
     * column[结果集封装-子流程](方法2)<br/>
     * 方法(2)表头内部遍历
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column column
     * @param rsm ResultSetMetaData
     * @param index 第几列
     * @return Column
     */
    
    public static Column column(DriverAdapter adapter, DataRuntime runtime, Column column, ResultSetMetaData rsm, int index){
        if(null == column){
            column = new Column();
        }
        String catalog = null;
        String schema = null;
        try{
            catalog = BasicUtil.evl(rsm.getCatalogName(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getCatalogName]");
        }
        try{
            schema = BasicUtil.evl(rsm.getSchemaName(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getSchemaName]");
        }
        adapter.correctSchemaFromJDBC(runtime, column, catalog, schema);
        try{
            column.setClassName(rsm.getColumnClassName(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getColumnClassName]");
        }
        try{
            column.caseSensitive(rsm.isCaseSensitive(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:isCaseSensitive]");
        }
        try{
            column.currency(rsm.isCurrency(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:isCurrency]");
        }
        try{
            column.setOriginName(rsm.getColumnName(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getColumnName]");
        }
        try{
            column.setName(rsm.getColumnLabel(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getColumnLabel]");
        }
        try{
            column.setPrecision(rsm.getPrecision(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getPrecision]");
        }
        try{
            column.setScale(rsm.getScale(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getScale]");
        }
        try{
            column.setDisplaySize(rsm.getColumnDisplaySize(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getColumnDisplaySize]");
        }
        try{
            column.setSigned(rsm.isSigned(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:isSigned]");
        }
        try{
            column.setTable(rsm.getTableName(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getTableName]");
        }
        try {
            column.setType(rsm.getColumnType(index));
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getColumnType]");
        }
        try {
            //不准确 POINT 返回 GEOMETRY
            String jdbcType = rsm.getColumnTypeName(index);
            column.setJdbcType(jdbcType);
            if(BasicUtil.isEmpty(column.getTypeName())) {
                column.setTypeName(jdbcType);
            }
        }catch (Exception e){
            log.debug("[获取MetaData失败][驱动未实现:getColumnTypeName]");
        }
        adapter.typeMetadata(runtime, column);
        return column;
    }

    /**
     * column[结果集封装]<br/>(方法3)<br/>
     * 有表名的情况下可用<br/>
     * 根据jdbc.datasource.connection.DatabaseMetaData获取指定表的列数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param columns columns
     * @param dbmd DatabaseMetaData
     * @param table 表
     * @param pattern 列名称通配符
     * @return LinkedHashMap
     * @param <T> Column
     * @throws Exception 异常
     */
    
    public static <T extends Column> LinkedHashMap<String, T> metadata(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception {
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        Catalog catalog = table.getCatalog();
        Schema schema = table.getSchema();
        if(BasicUtil.isEmpty(table.getName())){
            return columns;
        }
        String catalogName = null;
        String schemaName = null;
        if(null != catalog){
            catalogName = catalog.getName();
        }
        if(null != schema){
            schemaName = schema.getName();
        }
        ResultSet set = dbmd.getColumns(catalogName, schemaName, table.getName(), pattern);
        Map<String,Integer> keys = keys(set);
        while (set.next()){
            String name = set.getString("COLUMN_NAME");
            if(null == name){
                continue;
            }
            String columnCatalog = string(keys,"TABLE_CAT", set, null);
            if(null != columnCatalog){
                columnCatalog = columnCatalog.trim();
            }
            String columnSchema = string(keys,"TABLE_SCHEM", set, null);
            if(null != columnSchema){
                columnSchema = columnSchema.trim();
            }

            T column = columns.get(name.toUpperCase());
            if(null == column){
                if(create) {
                    column = (T)new Column(name);
                    columns.put(name.toUpperCase(), column);
                }else {
                    continue;
                }
            }

            adapter.correctSchemaFromJDBC(runtime, column, columnCatalog, columnSchema);
            if(!BasicUtil.equalsIgnoreCase(catalog, column.getCatalogName())){
                continue;
            }
            if(!BasicUtil.equalsIgnoreCase(schema, column.getSchemaName())){
                continue;
            }

            String remark = string(keys, "REMARKS", set, column.getComment());
            if("TAG".equals(remark)){
                column = (T)new Tag();
            }
            column.setComment(remark);
            column.setTable(BasicUtil.evl(string(keys,"TABLE_NAME", set, table.getName()), column.getTableName(true)));
            column.setType(integer(keys, "DATA_TYPE", set, column.getType()));
            column.setType(integer(keys, "SQL_DATA_TYPE", set, column.getType()));
            String jdbcType = string(keys, "TYPE_NAME", set, column.getTypeName());
            if(BasicUtil.isEmpty(column.getTypeName())) {
                //数据库中 有jdbc是支持的类型 如果数据库中有了就不用jdbc的了
                column.setTypeName(jdbcType);
            }
            column.setJdbcType(jdbcType);
            column.setPrecision(integer(keys, "COLUMN_SIZE", set, column.getPrecision()));
            column.setScale(integer(keys, "DECIMAL_DIGITS", set, column.getScale()));
            column.nullable(bool(keys, "NULLABLE", set, column.isNullable()));
            column.setDefaultValue(value(keys, "COLUMN_DEF", set, column.getDefaultValue()));
            column.setPosition(integer(keys, "ORDINAL_POSITION", set, column.getPosition()));
            column.autoIncrement(bool(keys,"IS_AUTOINCREMENT", set, column.isAutoIncrement()));
            adapter.typeMetadata(runtime, column);
            column(adapter, runtime, column, set);
            column.setName(name);
        }

        // 主键
        ResultSet rs = dbmd.getPrimaryKeys(catalogName, schemaName, table.getName());
        while (rs.next()) {
            String name = rs.getString(4);
            Column column = columns.get(name.toUpperCase());
            if (null == column) {
                continue;
            }
            column.primary(true);
        }
        return columns;
    }

    /**
     * column[结果集封装-子流程](方法3)<br/>
     * 方法(3)内部遍历
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column column
     * @param rs ResultSet
     * @return Column
     */
    
    public static Column column(DriverAdapter adapter, DataRuntime runtime, Column column, ResultSet rs){
        if(null == column){
            column = new Column();
        }
        try {
            Map<String,Integer> keys = keys(rs);
            if(null == column.getName()){
                column.setName(string(keys, "COLUMN_NAME", rs));
            }
            if(null == column.getType()){
                column.setType(BasicUtil.parseInt(string(keys, "DATA_TYPE", rs), null));
            }
            if(null == column.getType()){
                column.setType(BasicUtil.parseInt(string(keys, "SQL_DATA_TYPE", rs), null));
            }
            if(null == column.getTypeName()){
                String jdbcType = string(keys, "TYPE_NAME", rs);
                column.setJdbcType(jdbcType);
                if(BasicUtil.isEmpty(column.getTypeName())) {
                    column.setTypeName(jdbcType);
                }
            }
            if(null == column.getPrecision()) {
                column.setPrecision(integer(keys, "COLUMN_SIZE", rs, null));
            }
            if(null == column.getScale()) {
                column.setScale(BasicUtil.parseInt(string(keys, "DECIMAL_DIGITS", rs), null));
            }
            if(null == column.getPosition()) {
                column.setPosition(BasicUtil.parseInt(string(keys, "ORDINAL_POSITION", rs), 0));
            }
            if(-1 == column.isAutoIncrement()) {
                column.autoIncrement(BasicUtil.parseBoolean(string(keys, "IS_AUTOINCREMENT", rs), false));
            }
            if(-1 == column.isGenerated()) {
                column.generated(BasicUtil.parseBoolean(string(keys, "IS_GENERATEDCOLUMN", rs), false));
            }
            if(null == column.getComment()) {
                column.setComment(string(keys, "REMARKS", rs));
            }
            if(null == column.getPosition()){
                column.setPosition(BasicUtil.parseInt(string(keys, "ORDINAL_POSITION", rs), 0));
            }
            if (BasicUtil.isEmpty(column.getDefaultValue())) {
                column.setDefaultValue(string(keys, "COLUMN_DEF", rs));
            }
            adapter.typeMetadata(runtime, column);
        }catch (Exception e){
            e.printStackTrace();
        }
        return column;
    }

    /**
     * query[结果集封装-子流程]
     * 封装查询结果行,在外层遍历中修改rs下标
     * @param system 系统表不检测列属性
     * @param runtime  runtime
     * @param metadatas metadatas
     * @param rs jdbc返回结果
     * @return DataRow
     */
    public static DataRow row(DriverAdapter adapter, boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas, ConfigStore configs, ResultSet rs){
        DataRow row = null;
        KeyAdapter.KEY_CASE kc = null;
        if(null != configs){
            kc = configs.keyCase();
        }
        if(null == kc){
            if(!ConfigTable.IS_UPPER_KEY && !ConfigTable.IS_LOWER_KEY){
                kc = KeyAdapter.KEY_CASE.SRC;
            }
        }
        boolean upper = false;
        if(KeyAdapter.KEY_CASE.SRC == kc){
            row = new OriginRow();
        }else if(KeyAdapter.KEY_CASE.PUT_UPPER == kc){
            //put时大写,DataRow按SRC处理
            upper = true;
            row = new DataRow(KeyAdapter.KEY_CASE.SRC);
        }else if(null != kc){
            row = new DataRow(kc);
        }else{
            row = new DataRow();
        }
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int qty = rsmd.getColumnCount();
            if (!system && (null == metadatas || metadatas.isEmpty())) {
                for (int i = 1; i <= qty; i++) {
                    String name = rsmd.getColumnLabel(i);
                    if(null == name){
                        name = rsmd.getColumnName(i);
                    }
                    if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
                        continue;
                    }
                    Column column = metadatas.get(name) ;
                    column = column(adapter, runtime, (Column) column, rsmd, i);
                    metadatas.put(name.toUpperCase(), column);
                }
            }
            for (int i = 1; i <= qty; i++) {
                String name = rsmd.getColumnLabel(i);
                if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
                    continue;
                }
                try {
                    Column column = metadatas.get(name.toUpperCase());
                    //Object v = BeanUtil.value(column.getTypeName(), rs.getObject(name));
                    Object origin = rs.getObject(name);
                    row.putOrigin(name, origin);
                    Object value = adapter.read(runtime, column, origin, null);
                    if (upper) {
                        name = name.toUpperCase();
                    }
                    row.put(false, name, value);
                }catch (Exception e){
                    if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                        e.printStackTrace();
                    }else{
                        log.error("[结果集封装][result:fail][msg:{}]", e.toString());
                    }
                }
            }
            row.setMetadata(metadatas);
        }catch (Exception e){
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                e.printStackTrace();
            }else{
                log.error("[结果集封装][result:fail][msg:{}]", e.toString());
            }
        }
        return row;
    }

    public static LinkedHashMap<String, Object> map(DriverAdapter adapter, boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas, ConfigStore configs, ResultSet rs){
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int qty = rsmd.getColumnCount();
            if (!system && (null == metadatas || metadatas.isEmpty())) {
                for (int i = 1; i <= qty; i++) {
                    String name = rsmd.getColumnLabel(i);
                    if(null == name){
                        name = rsmd.getColumnName(i);
                    }
                    if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
                        continue;
                    }
                    Column column = metadatas.get(name) ;
                    column = column(adapter, runtime, (Column) column, rsmd, i);
                    metadatas.put(name.toUpperCase(), column);
                }
            }
            for (int i = 1; i <= qty; i++) {
                String name = rsmd.getColumnLabel(i);
                if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
                    continue;
                }
                try {
                    Column column = metadatas.get(name.toUpperCase());
                    //Object v = BeanUtil.value(column.getTypeName(), rs.getObject(name));
                    Object value = adapter.read(runtime, column, rs.getObject(name), null);
                    map.put(name, value);
                }catch (Exception e){
                    if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                        e.printStackTrace();
                    }else{
                        log.error("[结果集封装][result:fail][msg:{}]", e.toString());
                    }
                }
            }
        }catch (Exception e){
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                e.printStackTrace();
            }else{
                log.error("[结果集封装][result:fail][msg:{}]", e.toString());
            }
        }
        return map;
    }
    public static boolean stream(DriverAdapter adapter, StreamHandler handler, ResultSet rs, ConfigStore configs, boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas) {
        try {
            if (handler instanceof ResultSetHandler) {
                return ((ResultSetHandler) handler).read(rs);
            } else {
                if (handler instanceof DataRowHandler) {
                    DataRowHandler dataRowHandler = (DataRowHandler) handler;
                    DataRow row = JDBCUtil.row(adapter, system, runtime, metadatas, configs, rs);
                    if (!dataRowHandler.read(row)) {
                        return false;
                    }
                } else if (handler instanceof EntityHandler) {
                    Class clazz = configs.entityClass();
                    if (null != clazz) {
                        EntityHandler entityHandler = (EntityHandler) handler;
                        DataRow row = JDBCUtil.row(adapter, system, runtime, metadatas, configs, rs);
                        if (!entityHandler.read(row.entity(clazz))) {
                            return false;
                        }
                    }
                } else if (handler instanceof MapHandler) {
                    MapHandler mh = (MapHandler) handler;
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int cols = rsmd.getColumnCount();
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        String name = rsmd.getColumnLabel(i);
                        if(null == name){
                            name = rsmd.getColumnName(i);
                        }
                        map.put(name, rs.getObject(i));
                    }
                    if (!mh.read(map)) {
                        return false;
                    }
                }
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }
    /**
	 * 先检测rs中是否包含当前key 如果包含再取值, 取值时按keys中的大小写为准
	 * @param keys keys
	 * @param key key
	 * @param set ResultSet
	 * @return String
	 * @throws Exception 异常
	 */
    public static String string(Map<String, Integer> keys, String key, ResultSet set, String def) throws Exception {
        Object value = value(keys, key, set);
        if(null != value){
            return value.toString();
        }
        return def;
    }
    public static String string(Map<String, Integer> keys, String key, ResultSet set) throws Exception {
        return string(keys, key, set, null);
    }
    public static Integer integer(Map<String, Integer> keys, String key, ResultSet set, Integer def) throws Exception {
        Object value = value(keys, key, set);
        if(null != value){
            return BasicUtil.parseInt(value, def);
        }
        return null;
    }
    public static Long longs(Map<String, Integer> keys, String key, ResultSet set, Long def) throws Exception {
        Object value = value(keys, key, set);
        if(null != value){
            return BasicUtil.parseLong(value, def);
        }
        return null;
    }
    public static Boolean bool(Map<String, Integer> keys, String key, ResultSet set, Boolean def) throws Exception {
        Object value = value(keys, key, set);
        if(null != value){
            return BasicUtil.parseBoolean(value, def);
        }
        return null;
    }
    public static Boolean bool(Map<String, Integer> keys, String key, ResultSet set, int def) throws Exception {
        Boolean defaultValue = null;
        if(def == 0){
            defaultValue = false;
        }else if(def == 1){
            defaultValue = true;
        }
        return bool(keys, key, set, defaultValue);
    }

    /**
     * 从resultset中根据名列取值
     * @param keys 列名位置
     * @param key 列名 多个以,分隔
     * @param set result
     * @param def 默认值
     * @return Object
     * @throws Exception Exception
     */
    public static Object value(Map<String, Integer> keys, String key, ResultSet set, Object def) throws Exception {
        String[] ks = key.split(",");
        Object result = null;
        for(String k:ks){
            Integer index = keys.get(k);
            if(null != index && index >= 0){
                try {
                    // db2 直接用 set.getObject(String) 可能发生 参数无效：未知列名 String
                    result =  set.getObject(index);
                    if(null != result){
                        return result;
                    }
                }catch (Exception e){

                }
            }
        }
        return def;
    }
    public static Object value(Map<String, Integer> keys, String key, ResultSet set) throws Exception {
        return value(keys, key, set, null);
    }

    public static <T extends Table> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, ResultSet set) throws Exception{
        if(null == tables){
            tables = new LinkedHashMap<>();
        }
        Map<String,Integer> keys = JDBCUtil.keys(set);
        while(set.next()) {
            String tableName = JDBCUtil.string(keys, "TABLE_NAME", set);

            if (BasicUtil.isEmpty(tableName)) {
                tableName = JDBCUtil.string(keys, "NAME", set);
            }
            if (BasicUtil.isEmpty(tableName)) {
                continue;
            }
            T table = tables.get(tableName.toUpperCase());
            if (null == table) {
                if (create) {
                    table = (T) new Table();
                    tables.put(tableName.toUpperCase(), table);
                } else {
                    continue;
                }
            }
            String catalogName = JDBCUtil.string(keys, "TABLE_CAT", set);
            String schemaName = JDBCUtil.string(keys, "TABLE_SCHEM", set);
            adapter.correctSchemaFromJDBC(runtime, table, catalogName, schemaName);
            table.setName(tableName);
            JDBCUtil.init(table, set, keys);
            tables.put(tableName.toUpperCase(), table);
        }
        return tables;
    }

    public static <T extends Table> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> tables, ResultSet set) throws Exception{
        if(null == tables){
            tables = new ArrayList<>();
        }
        Map<String,Integer> keys = keys(set);
        while(set.next()) {
            String tableName = string(keys, "TABLE_NAME", set);

            if(BasicUtil.isEmpty(tableName)){
                tableName = string(keys, "NAME", set);
            }
            if(BasicUtil.isEmpty(tableName)){
                continue;
            }
            String catalogName = BasicUtil.evl(string(keys, "TABLE_CATALOG", set), string(keys, "TABLE_CAT", set));
            String schemaName = BasicUtil.evl(string(keys, "TABLE_SCHEMA", set), string(keys, "TABLE_SCHEM", set));
            Table chk = new Table();
            adapter.correctSchemaFromJDBC(runtime, chk, catalogName, schemaName);
            T table = adapter.search(tables, chk.getCatalog(), chk.getSchema(), tableName);
            boolean contains = true;
            if(null == table){
                if(create){
                    table = (T)new Table();
                    contains = false;
                }else{
                    continue;
                }
            }
            adapter.correctSchemaFromJDBC(runtime, table, catalogName, schemaName);
            table.setSchema(schemaName);
            table.setName(tableName);
            init(table, set, keys);
            if(!contains) {
                tables.add(table);
            }
        }
        return tables;
    }
    public static <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, ResultSet set) throws Exception{

        if (null == views) {
            views = new LinkedHashMap<>();
        }
        Map<String, Integer> keys = keys(set);
        while (set.next()) {
            String viewName = string(keys, "VIEW_NAME", set);

            if (BasicUtil.isEmpty(viewName)) {
                viewName = string(keys, "NAME", set);
            }
            if (BasicUtil.isEmpty(viewName)) {
                viewName = string(keys, "TABLE_NAME", set);
            }
            if (BasicUtil.isEmpty(viewName)) {
                continue;
            }
            T view = views.get(viewName.toUpperCase());
            if (null == view) {
                if (create) {
                    view = (T) new View();
                    views.put(viewName.toUpperCase(), view);
                } else {
                    continue;
                }
            }
            String catalogName = BasicUtil.evl(string(keys, "TABLE_CATALOG", set), string(keys, "TABLE_CAT", set));
            String schemaName = BasicUtil.evl(string(keys, "TABLE_SCHEMA", set), string(keys, "TABLE_SCHEM", set));
            adapter.correctSchemaFromJDBC(runtime, view, catalogName, schemaName);
            view.setName(viewName);
            init(view, set, keys);
            views.put(viewName.toUpperCase(), view);
        }
        return views;
    }

    /**
     * 获取ResultSet中的列
     * @param set ResultSet
     * @return list
     * @throws Exception 异常 Exception
     */
    public static Map<String, Integer> keys(ResultSet set) throws Exception {
        ResultSetMetaData rsmd = set.getMetaData();
        Map<String, Integer> keys = new HashMap<>();
        if(null != rsmd){
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String name = rsmd.getColumnLabel(i);
                if(null == name){
                    name = rsmd.getColumnName(i);
                }
                keys.put(name.toUpperCase(), i);
            }
        }
        return keys;
    }

    public static void init(Table table, ResultSet set, Map<String,Integer> keys){
        try {
            table.setType(BasicUtil.evl(JDBCUtil.string(keys, "TABLE_TYPE", set), table.getType()));
        }catch (Exception ignored){}
        try {
            table.setComment(BasicUtil.evl(JDBCUtil.string(keys, "REMARKS", set), table.getComment()));
        }catch (Exception ignored){}
        try {
            table.setTypeCat(BasicUtil.evl(JDBCUtil.string(keys, "TYPE_CAT", set), table.getTypeCat()));
        }catch (Exception ignored){}
        try {
            table.setTypeName(BasicUtil.evl(JDBCUtil.string(keys, "TYPE_NAME", set), table.getTypeName()));
        }catch (Exception ignored){}
        try {
            table.setSelfReferencingColumn(BasicUtil.evl(JDBCUtil.string(keys, "SELF_REFERENCING_COL_NAME", set), table.getSelfReferencingColumn()));
        }catch (Exception ignored){}
        try {
            table.setRefGeneration(BasicUtil.evl(JDBCUtil.string(keys, "REF_GENERATION", set), table.getRefGeneration()));
        }catch (Exception ignored){}
    }

    public static void init(View view, ResultSet set, Map<String, Integer> keys){
        try {
            view.setType(BasicUtil.evl(string(keys, "TABLE_TYPE", set), view.getType()));
        }catch (Exception ignored){}
        try {
            view.setComment(BasicUtil.evl(string(keys, "REMARKS", set), view.getComment()));
        }catch (Exception ignored){}
        try {
            view.setTypeCat(BasicUtil.evl(string(keys, "TYPE_CAT", set), view.getTypeCat()));
        }catch (Exception ignored){}
        try {
            view.setTypeName(BasicUtil.evl(string(keys, "TYPE_NAME", set), view.getTypeName()));
        }catch (Exception ignored){}
        try {
            view.setSelfReferencingColumn(BasicUtil.evl(string(keys, "SELF_REFERENCING_COL_NAME", set), view.getSelfReferencingColumn()));
        }catch (Exception ignored){}
        try {
            view.setRefGeneration(BasicUtil.evl(string(keys, "REF_GENERATION", set), view.getRefGeneration()));
        }catch (Exception ignored){}
    }
    public static void queryTimeout(Statement statement, ConfigStore configs){
        int timeout = ConfigStore.SQL_QUERY_TIMEOUT(configs);
        if(timeout > 0){
            try {
                statement.setQueryTimeout(timeout);
            }catch (Exception e){
                log.warn("设置超时时间异常:{}", e.toString());
            }
        }
    }
    public static void updateTimeout(Statement statement, ConfigStore configs){
        int timeout = ConfigStore.SQL_QUERY_TIMEOUT(configs);
        if(timeout > 0){
            try {
                statement.setQueryTimeout(timeout);
            }catch (Exception e){
                log.warn("设置超时时间异常:{}", e.toString());
            }
        }
    }
}
