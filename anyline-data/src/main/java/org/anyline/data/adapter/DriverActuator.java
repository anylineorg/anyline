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



package org.anyline.data.adapter;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface DriverActuator {
    Logger log = LoggerFactory.getLogger(DriverActuator.class);
    /**
     * 根据类型注入到DriverAdapter中
     * @return Class
     */
    Class<? extends DriverAdapter> supportAdapterType();

    /**
     * 返回值越高 优先级越高
     * 支持相同DriverAdapter的worker只有一个生效，以优先级最高的为准
     * @return int
     */
    default int priority() {
        return 0;
    }
    DataSource getDataSource(DriverAdapter adapter, DataRuntime runtime);
    Connection getConnection(DriverAdapter adapter, DataRuntime runtime, DataSource datasource);
    void releaseConnection(DriverAdapter adapter, DataRuntime runtime, Connection connection, DataSource datasource);
    <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, DataSource datasource, T meta);
    <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, T meta);
    <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, Connection con, T meta);

    /**
     * database[结果集封装]<br/>
     * 根据JDBC内置接口 product
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param product 上一步查询结果
     * @return product
     * @throws Exception 异常
     */
    String product(DriverAdapter adapter, DataRuntime runtime, boolean create, String product);

    /**
     * database[结果集封装]<br/>
     * 根据JDBC内置接口 version
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param version 上一步查询结果
     * @return version
     * @throws Exception 异常
     */
    String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version);

    /**
     * 数据库列表
     * @param adapter adapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return List
     */
    default List<Database> databases(DriverAdapter adapter, DataRuntime runtime) {
        return new ArrayList<>();
    }
    default List<Catalog> catalogs(DriverAdapter adapter, DataRuntime runtime) {
        return new ArrayList<>();
    }
    default List<Schema> schemas(DriverAdapter adapter, DataRuntime runtime) {
        return new ArrayList<>();
    }
    DataSet select(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String cmd, List<Object> values, LinkedHashMap<String,Column> columns) throws Exception;
    /**
     * query procedure [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param procedure 存储过程
     * @param navi 分页
     * @return DataSet
     */
    default DataSet querys(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, PageNavi navi) throws Exception{
        return null;
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return maps
     */
    List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception;

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return map
     */
    Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception;

    /**
     * 执行insert
     * @param adapter DriverAdapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param data 插入数量
     * @param configs ConfigStore
     * @param run Run
     * @param generatedKey 执行insert后返回自增主键的key
     * @param pks
     * @return long
     * @throws Exception Exception
     */
    long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception;

    /**
     * update [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return 影响行数
     */
    long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception;

    /**
     * procedure [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param procedure 存储过程
     * @param random  random
     * @return 输出参数
     */
    default List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception{
        return null;
    }
    /**
     * execute [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return 影响行数
     */
    long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception;

    /**
     * 根据结果集对象获取列结构,如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @param comment 是否需要查询列注释
     * @return LinkedHashMap
     */
    default LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime, String random, Run run, boolean comment) {
        return new LinkedHashMap<>();
    }


    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param tables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return tables
     * @throws Exception 异常
     */
    default <T extends Table> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create,  LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return tables;
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param tables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return tables
     * @throws Exception 异常
     */
    default <T extends Table> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> tables,  Catalog catalog, Schema schema, String pattern, int types) throws Exception{
        return tables;
    }
    /**
     * view[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param views 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return tables
     * @throws Exception 异常
     */
    default <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime, boolean create,  LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return views;
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param views 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return tables
     * @throws Exception 异常
     */
    default <T extends Table> List<T> views(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> views, Catalog catalog, Schema schema, String pattern, int types) throws Exception{
        return views;
    }

    /**
     * 根据结果集解析列结构
     * @param adapter DriverAdapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param columns 上一步查询结果
     * @param table 表
     * @param cmd sql
     * @return columns
     * @param <T> Column
     */
    default <T extends Column> LinkedHashMap<String, T> columns(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String cmd) throws Exception{
        return columns;
    }
    /**
     * 根方法(3)根据根据驱动内置元数据接口补充表结构
     * @param adapter DriverAdapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param columns 上一步查询结果
     * @param table 表
     * @return columns
     * @param <T> Column
     */
    default <T extends Column> LinkedHashMap<String, T> metadata(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception{
        return new LinkedHashMap<>();
    }

    /**
     * index[结果集封装]<br/>
     * 根据驱动内置元数据接口查询索引
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param unique 是否唯一
     * @param approximate 索引允许结果反映近似值
     * @return indexes indexes
     * @throws Exception 异常
     */
    default <T extends Index> LinkedHashMap<String, T> indexes(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate) throws Exception{
        return new LinkedHashMap<>();
    }
}
