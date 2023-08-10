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
 *
 *          
 */


package org.anyline.dao;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AnylineDao<E>{
	void setRuntime(DataRuntime runtime);
	DataRuntime runtime();
	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/

	/**
	 * 查询row列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare RunPrepare
	 * @param configs 查询条件
	 * @param conditions 查询条件
	 * @return mpas
	 */
	DataSet querys(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return querys(runtime(), null, false, prepare, configs, conditions);
	}
	/**
	 * 查询entity列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare RunPrepare
	 * @param configs 查询条件
	 * @param conditions 查询条件
	 * @return mpas
	 */
	<T> EntitySet<T> selects(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions);
	default <T> EntitySet<T> selects(RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions){
		return selects(runtime(), null, false, prepare, clazz, configs, conditions);
	}

	/**
	 * 查询map列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare RunPrepare
	 * @param configs 查询条件
	 * @param conditions 查询条件
	 * @return mpas
	 */
	List<Map<String,Object>> maps(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default List<Map<String,Object>> maps(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return maps(runtime(), null, false, prepare, configs, conditions);
	}

	/**
	 * 合计总行数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare RunPrepare
	 * @param configs 查询条件
	 * @param conditions 查询条件
	 * @return long
	 */
	long count(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default long count(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return count(runtime(), null, false, prepare, configs, conditions);
	}

	/**
	 * 创建查询序列SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 可以是多个序列
	 * @return DataRow
	 */
	DataRow sequence(DataRuntime runtime, String random, boolean recover, boolean next, String ... names);
	default DataRow sequence(boolean next, String ... names){
		return sequence(runtime(), null, false, next, names);
	}
	/**
	 * 是否存在
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare RunPrepare
	 * @param configs 查询条件
	 * @param conditions 查询条件
	 * @return boolean
	 */
	boolean exists(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default boolean exists(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return exists(runtime(), null, false, prepare, configs, conditions);
	}

	/**
	 * 更新记录
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param data		需要更新的数据
	 * @param dest		需要更新的表,如果没有提供则根据data解析
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @return int 影响行数
	 */
	int update(DataRuntime runtime, String random, boolean recover, String dest, Object data, ConfigStore configs, List<String> columns);
	default int update(String dest, Object data, ConfigStore configs, List<String> columns){
		return update(runtime(), null, false, dest, data, configs, columns);
	}
	default int update(String dest, Object data, ConfigStore configs, String ... columns){
		return update(dest, data, configs, BeanUtil.array2list(columns));
	}
	default int update(Object data, ConfigStore configs, String ... columns){
		return update(null, data, configs, BeanUtil.array2list(columns));
	}
	default int update(Object data, ConfigStore configs, List<String> columns){
		return update(null, data, configs, columns);
	}
	default int update(String dest, Object data, String ... columns){
		return update(dest, data, null, BeanUtil.array2list(columns));
	}
	default int update(String dest, Object data, List<String> columns){
		return update(dest, data, null, columns);
	}
	default int update(Object data, List<String> columns){
		return update(null, data, null, columns);
	}

	/** 
	 * 添加
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param data 需要插入的数据 
	 * @param checkPrimary   是否需要检查重复主键,默认不检查 
	 * @param columns  需要插入的列 
	 * @param dest 表 
	 * @return int
	 */
	int insert(DataRuntime runtime, String random, boolean recover, String dest, Object data, boolean checkPrimary, List<String> columns);
	default int insert(String dest, Object data, boolean checkPrimary, List<String> columns){
		return insert(runtime(), null, false, dest, data, checkPrimary, columns);
	}
	default int insert(String dest, Object data, boolean checkPrimary, String ... columns){
		return insert(dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default int insert(Object data, boolean checkPrimary, String ... columns){
		return insert(null, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default int insert(String dest, Object data, String ... columns){
		return insert(dest, data, false, BeanUtil.array2list(columns));
	}
	default int insert(Object data, String ... columns){
		return insert(null, data, false, BeanUtil.array2list(columns));
	}
	default int insert(Object data, boolean checkPrimary, List<String> columns){
		return insert(null, data, checkPrimary, columns);
	}
	default int insert(String dest, Object data, List<String> columns){
		return insert(dest, data, false, columns);
	}
	default int insert(Object data, List<String> columns){
		return insert(null, data, false, columns);
	}


	/** 
	 * 保存(insert|update)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param dest  表
	 * @param data  data
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns  columns
	 * @return int
	 */
	int save(DataRuntime runtime, String random, boolean recover, String dest, Object data, boolean checkPrimary, List<String>  columns);
	default int save(String dest, Object data, boolean checkPrimary, List<String>  columns){
		return save(runtime(), null, false, dest, data, checkPrimary, columns);
	}
	default int save(String dest, Object data, boolean checkPrimary, String ... columns){
		return save(dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default int save(Object data, boolean checkPrimary, String ... columns){
		return save(null, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default int save(String dest, Object data, String ... columns){
		return save(dest, data, false, BeanUtil.array2list(columns));
	}
	default int save(Object data, String ... columns){
		return save(null, data, false, BeanUtil.array2list(columns));
	}


	/**
	 * 执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param prepare 包含表或自定义SQL
	 * @param configs configs
	 * @param conditions conditions
	 * @return 影响行数
	 */
	int execute(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default int execute(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return execute(runtime(), null, false, prepare, configs, conditions);
	}
	default int execute(RunPrepare prepare,  String ... conditions){
		return execute(prepare, null, conditions);
	}
 
	/** 
	 * 执行存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param recover 执行完后是否还原回执行前数据源
	 * @param procedure  存储过程
	 * @return boolean
	 */
	boolean execute(DataRuntime runtime, String random, boolean recover, Procedure procedure);
	default boolean execute(Procedure procedure){
		return execute(runtime(), null, false, procedure);
	}
	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @return DataSet
	 */
	DataSet querys(DataRuntime runtime, String random, boolean recover, Procedure procedure, PageNavi navi);
	default DataSet querys(Procedure procedure, PageNavi navi){
		return querys(runtime(), null, false, procedure, navi);
	}
	int delete(DataRuntime runtime, String random, boolean recover, String dest, Object obj, String ... columns);
	default int delete(String dest, Object obj, String ... columns){
		return delete(runtime(), null, false, dest, obj, columns);
	}
	int delete(DataRuntime runtime, String random, boolean recover, String table, ConfigStore configs, String ... conditions);
	default int delete(String table, ConfigStore configs, String ... conditions){
		return delete(runtime(), null, false, table, configs, conditions);
	}
	/**
	 * 删除多行
	 * @param table 表
	 * @param key 列
	 * @param values 值集合
	 * @return 影响行数
	 */
	<T> int deletes(DataRuntime runtime, String random, boolean recover, String table, String key, Collection<T> values);
	default <T> int deletes(String table, String key, Collection<T> values){
		return deletes(runtime(), null, false, table, key, values);
	}
	default <T> int deletes(String table, String key, T ... values){
		return deletes(table, key, BeanUtil.array2list(values));
	}
	int truncate(DataRuntime runtime, String random, boolean recover, String table);
	default int truncate(String table){
		return truncate(runtime(), null, false, table);
	}

	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													database
	 ******************************************************************************************************************/

	LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, boolean recover);
	default LinkedHashMap<String, Database> databases(){
		return databases(runtime(), null, false);
	}
	Database database(DataRuntime runtime, String random, boolean recover, String name);
	default Database database(String name){
		return database(runtime(), null, false, name);
	}


	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String name, String types);
	default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String catalog, String schema, String name, String types){
		return tables(runtime(), null, false, greedy, catalog, schema, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String schema, String name, String types){
		return tables(greedy, null, schema, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String name, String types){
		return tables(greedy, null, null, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String types){
		return tables(greedy, null, null, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(boolean greedy){
		return tables(greedy, null, null, null, "TABLE");
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String catalog, String schema, String name, String types){
		return tables(false, catalog, schema, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String schema, String name, String types){
		return tables(false, null, schema, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String name, String types){
		return tables(false, null, null, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String types){
		return tables(false, null, null, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(){
		return tables(false, null, null, null, "TABLE");
	}

	/**
	 *
	 * 查询表的创建SQL
	 * @param runtime
	 * @param random
	 * @param recover
	 * @param table
	 * @param init 是否还原初始状态(自增ID)
	 * @return
	 */
	List<String> ddl(DataRuntime runtime, String random, boolean recover, Table table, boolean init);
	default List<String> ddl(Table table, boolean init){
		return ddl(runtime(), null, false, table, init);
	}


	/* *****************************************************************************************************************
	 * 													views
	 ******************************************************************************************************************/

	/**
	 * 查询视图
	 * @param runtime
	 * @param random
	 * @param recover
	 * @param greedy
	 * @param catalog
	 * @param schema
	 * @param pattern
	 * @param types
	 * @return
	 * @param <T>
	 */
	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String pattern, String types);
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy, String catalog, String schema, String name, String types){
		return views(runtime(), null, false, greedy, catalog, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy, String schema, String name, String types){
		return views(greedy, null, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy, String name, String types){
		return views(greedy, null, null, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy, String types){
		return views(greedy, null, null, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(boolean greedy){
		return views(greedy, null, null, null, "TABLE");
	}
	default <T extends View> LinkedHashMap<String, T> views(String catalog, String schema, String name, String types){
		return views(false, catalog, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(String schema, String name, String types){
		return views(false, null, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(String name, String types){
		return views(false, null, null, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(String types){
		return views(false, null, null, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(){
		return views(false, null, null, null, "TABLE");
	}
	List<String> ddl(DataRuntime runtime, String random, boolean recover, View view);
	default List<String> ddl(View view){
		return ddl(runtime(), null, false, view);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	<T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String name, String types);
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String catalog, String schema, String name, String types){
		return mtables(runtime(), null, false, greedy, catalog, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String schema, String name, String types){
		return mtables(greedy, null, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String name, String types){
		return mtables(greedy, null, null, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String types){
		return mtables(greedy, null, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy){
		return mtables(greedy, "STABLE");
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(String catalog, String schema, String name, String types){
		return mtables(false, catalog, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(String schema, String name, String types){
		return mtables(false, null, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(String name, String types){
		return mtables(false, null, null, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(String types){
		return mtables(false, null, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> mtables(){
		return mtables(false, "STABLE");
	}
	List<String> ddl(DataRuntime runtime, String random, boolean recover, MasterTable table);
	default List<String> ddl(MasterTable table){
		return ddl(runtime(), null, false, table);
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/
	<T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, String random, boolean recover, boolean greedy, MasterTable master, Map<String, Object> tags, String name);

	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String catalog, String schema, String master, String name){
		MasterTable mtable = new MasterTable(catalog, schema, master);
		return ptables(greedy,mtable, null, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String schema, String master, String name){
		return ptables(greedy,null, schema, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master, String name){
		return ptables(greedy,null, null, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master){
		return ptables(greedy,null, null, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master){
		return ptables(greedy,master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String,Object> tags, String name){
		return ptables(greedy, master, tags, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String,Object> tags){
		return ptables(runtime(), null, false, greedy,master, tags, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(String catalog, String schema, String master, String name){
		MasterTable mtable = new MasterTable(catalog, schema, master);
		return ptables(false,mtable, null, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(String schema, String master, String name){
		return ptables(false,null, schema, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(String master, String name){
		return ptables(false,null, null, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(String master){
		return ptables(false,null, null, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master){
		return ptables(false,master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String,Object> tags, String name){
		return ptables(false, master, tags, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String,Object> tags){
		return ptables(false,master, tags, null);
	}
	List<String> ddl(DataRuntime runtime, String random, boolean recover, PartitionTable table);
	default List<String> ddl(PartitionTable table){
		return ddl(runtime(), null, false,  table);
	}

	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/

	/**
	 * 查询表结构
	 * @param runtime
	 * @param random
	 * @param recover
	 * @param greedy
	 * @param table
	 * @param primary 是否检测列的主键标识
	 * @return
	 * @param <T>
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table, boolean primary);
	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table){
		return columns(runtime(), null, false, greedy, table, ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table){
		return columns(greedy, new Table(table));
	}

	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String catalog, String schema, String table){
		return columns(greedy, new Table(catalog, schema, table));
	}
	default <T extends Column> LinkedHashMap<String, T> columns(Table table){
		return columns(false, table);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(String table){
		return columns(false, table);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(String catalog, String schema, String table){
		return columns(new Table(catalog, schema, table));
	}

	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table);
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table){
		return tags(runtime(), null, false, greedy, new Table(table));
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String catalog, String schema, String table){
		return tags(runtime(),null, false, greedy, new Table(catalog,schema,table));
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table){
		return tags(runtime(), null, false,greedy, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(Table table){
		return tags(runtime(), null, false,false, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(String table){
		return tags(false, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(String catalog, String schema, String table){
		return tags(false, catalog, schema, table);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/
	PrimaryKey primary(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table);
	default PrimaryKey primary(boolean greedy, Table table){
		return primary(runtime(), null, false, greedy, table);
	}
	default PrimaryKey primary(boolean greedy, String table){
		return primary(greedy,new Table(table));
	}
	default PrimaryKey primary(boolean greedy, String catalog, String schema, String table){
		return primary(greedy, new Table(catalog, schema, table));
	}
	default PrimaryKey primary(Table table){
		return primary(false, table);
	}
	default PrimaryKey primary(String table){
		return primary(false, table);
	}
	default PrimaryKey primary(String catalog, String schema, String table){
		return primary(false, catalog, schema, table);
	}


	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table);
	default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table){
		return foreigns(runtime(), null, false, greedy, table);
	}
	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/
	<T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table, String name);
	default <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, Table table, String name){
		return indexs(runtime(), null, false, greedy, table, name);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String table, String name){
		return indexs(greedy, new Table(table), name);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, Table table){
		return indexs(greedy, table, null);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String table){
		return indexs(greedy, new Table(table), null);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String catalog, String schema, String table){
		return indexs(greedy, new Table(catalog, schema, table), null);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(Table table, String name){
		return indexs(false, table, name);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(String table, String name){
		return indexs(false, table, name);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(Table table){
		return indexs(false, table);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(String table){
		return indexs(false, table);
	}
	default <T extends Index> LinkedHashMap<String, T> indexs(String catalog, String schema, String table){
		return indexs(false, catalog, schema, table);
	}

	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table, String name);
	default <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, Table table, String name){
		return constraints(runtime(), null, false, greedy, table, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String table, String name){
		return constraints(greedy, new Table(table), name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, Table table){
		return constraints(greedy, table, null);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String table){
		return constraints(greedy, new Table(table), null);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String catalog, String schema, String table){
		return constraints(greedy, new Table(catalog, schema, table), null);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name){
		return constraints(false, table, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(String table, String name){
		return constraints(false, table, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table){
		return constraints(false, table);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(String table){
		return constraints(false, table);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(String catalog, String schema, String table){
		return constraints(false, catalog, schema, table);
	}


	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/

	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean recover, boolean greedy, Table table, List<Trigger.EVENT> events);
	default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events){
		return triggers(runtime(), null, false, greedy, table, events);
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/

	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String name);
	default <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String catalog, String schema, String name){
		return procedures(runtime(), null, false, greedy, catalog, schema, name);
	}


	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, boolean recover, boolean greedy, String catalog, String schema, String name);
	default <T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String catalog, String schema, String name){
		return functions(runtime(), null, false, greedy, catalog, schema, name);
	}


	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	boolean create(Table table) throws Exception;
	boolean alter(Table table) throws Exception;
	boolean drop(Table table) throws Exception;
	boolean rename(Table origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													view
	 ******************************************************************************************************************/
	boolean create(View view) throws Exception;
	boolean alter(View view) throws Exception;
	boolean drop(View view) throws Exception;
	boolean rename(View origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	boolean create(MasterTable table) throws Exception;
	boolean alter(MasterTable table) throws Exception;
	boolean drop(MasterTable table) throws Exception;
	boolean rename(MasterTable origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/
	boolean create(PartitionTable table) throws Exception;
	boolean alter(PartitionTable table) throws Exception;
	boolean drop(PartitionTable table) throws Exception;
	boolean rename(PartitionTable origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/
	boolean add(Column column) throws Exception;
	boolean alter(Table table, Column column) throws Exception;
	boolean alter(Column column) throws Exception;
	boolean drop(Column column) throws Exception;
	boolean rename(Column origin, String name) throws Exception;

	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	boolean add(Tag tag) throws Exception;
	boolean alter(Table table, Tag tag) throws Exception;
	boolean alter(Tag tag) throws Exception;
	boolean drop(Tag tag) throws Exception;
	boolean rename(Tag origin, String name) throws Exception;

	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/
	boolean add(PrimaryKey primary) throws Exception;
	boolean alter(PrimaryKey primary) throws Exception;
	boolean alter(Table table, PrimaryKey primary) throws Exception;
	boolean drop(PrimaryKey primary) throws Exception;
	boolean rename(PrimaryKey origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													foreign
	 ******************************************************************************************************************/
	boolean add(ForeignKey foreign) throws Exception;
	boolean alter(ForeignKey foreign) throws Exception;
	boolean alter(Table table, ForeignKey foreign) throws Exception;
	boolean drop(ForeignKey foreign) throws Exception;
	boolean rename(ForeignKey origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/
	boolean add(Index index) throws Exception;
	boolean alter(Index index) throws Exception;
	boolean alter(Table table, Index index) throws Exception;
	boolean drop(Index index) throws Exception;
	boolean rename(Index origin, String name) throws Exception;

	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/
	boolean add(Constraint constraint) throws Exception;
	boolean alter(Constraint constraint) throws Exception;
	boolean alter(Table table, Constraint constraint) throws Exception;
	boolean drop(Constraint constraint) throws Exception;
	boolean rename(Constraint origin, String name) throws Exception;


	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/
	/**
	 * 触发器
	 * @param trigger 触发器
	 * @return trigger
	 * @throws Exception 异常 Exception
	 */
	boolean add(Trigger trigger) throws Exception;
	boolean alter(Trigger trigger) throws Exception;
	boolean drop(Trigger trigger) throws Exception;
	boolean rename(Trigger origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/
	/**
	 * 触发器
	 * @param procedure 存储过程
	 * @return boolean
	 * @throws Exception 异常 Exception
	 */
	boolean create(Procedure procedure) throws Exception;
	boolean alter(Procedure procedure) throws Exception;
	boolean drop(Procedure procedure) throws Exception;
	boolean rename(Procedure origin, String name) throws Exception;
	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/
	/**
	 * 函数
	 * @param function 函数
	 * @return boolean
	 * @throws Exception 异常 Exception
	 */
	boolean create(Function function) throws Exception;
	boolean alter(Function function) throws Exception;
	boolean drop(Function function) throws Exception;
	boolean rename(Function origin, String name) throws Exception;
} 
