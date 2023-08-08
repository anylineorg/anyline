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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AnylineDao<E>{
	void setRuntime(DataRuntime runtime);
	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/
	DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions);
	<T> EntitySet<T> selects(RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions);

	DataRow sequence(boolean next, String ... names);


	List<Map<String,Object>> maps(RunPrepare prepare, ConfigStore configs, String ... conditions);
	long count(RunPrepare prepare, ConfigStore configs, String ... conditions);
	long count(RunPrepare prepare, String ... conditions);
	
	boolean exists(RunPrepare prepare, ConfigStore configs, String ... conditions);
	boolean exists(RunPrepare prepare, String ... conditions);

	/**
	 * 更新记录
	 * @param data		需要更新的数据
	 * @param dest		需要更新的表,如果没有提供则根据data解析
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @return int 影响行数
	 */
	int update(String dest, Object data, ConfigStore configs, List<String> columns);
	int update(String dest, Object data, ConfigStore configs, String ... columns);
	int update(Object data, ConfigStore configs, String ... columns);
	int update(Object data, ConfigStore configs, List<String> columns);
	int update(String dest, Object data, String ... columns);
	int update(Object data, String ... columns);
	int update(String dest, Object data, List<String> columns);
	int update(Object data, List<String> columns);
	 
	/** 
	 * 添加 
	 * @param data 需要插入的数据 
	 * @param checkPrimary   是否需要检查重复主键,默认不检查 
	 * @param columns  需要插入的列 
	 * @param dest 表 
	 * @return int
	 */
	int insert(String dest, Object data, boolean checkPrimary, String ... columns);
	int insert(Object data, boolean checkPrimary, String ... columns);
	int insert(String dest, Object data, String ... columns);
	int insert(Object data, String ... columns);

	int insert(String dest, Object data, boolean checkPrimary, List<String> columns);
	int insert(Object data, boolean checkPrimary, List<String> columns);
	int insert(String dest, Object data, List<String> columns);
	int insert(Object data, List<String> columns);


	/** 
	 * 保存(insert|update) 
	 * @param dest  表
	 * @param data  data
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns  columns
	 * @return int
	 */ 
	int save(String dest, Object data, boolean checkPrimary, String ... columns); 
	int save(Object data, boolean checkPrimary, String ... columns); 
	int save(String dest, Object data, String ... columns); 
	int save(Object data, String ... columns); 
 

	int execute(RunPrepare prepare, ConfigStore configs, String ... conditions);
	int execute(RunPrepare prepare, String ... conditions);

 
	/** 
	 * 执行存储过程 
	 * @param procedure  procedure
	 * @return boolean
	 */ 
	boolean execute(Procedure procedure);
	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @return DataSet
	 */
	// DataSet querys(Procedure procedure);
	DataSet selects(Procedure procedure, PageNavi navi);
	int delete(String dest, Object obj, String ... columns);
	int delete(String table, ConfigStore configs, String ... conditions);

	/**
	 * 删除多行
	 * @param table 表
	 * @param key 列
	 * @param values 值集合
	 * @return 影响行数
	 */
	<T> int deletes(String table, String key, Collection<T> values);
	<T> int deletes(String table, String key, T ... values);
	int truncate(String table);

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
	LinkedHashMap<String, Database> databases();


	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	<T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String catalog, String schema, String name, String types);
	<T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String schema, String name, String types);
	<T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String name, String types);
	<T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String types);
	<T extends Table> LinkedHashMap<String, T> tables(boolean greedy);
	<T extends Table> LinkedHashMap<String, T> tables(String catalog, String schema, String name, String types);
	<T extends Table> LinkedHashMap<String, T> tables(String schema, String name, String types);
	<T extends Table> LinkedHashMap<String, T> tables(String name, String types);
	<T extends Table> LinkedHashMap<String, T> tables(String types);
	<T extends Table> LinkedHashMap<String, T> tables();

	/**
	 * 查询表的创建SQL
	 * @param table table
	 * @param init 是否还原初始状态(自增ID)
	 * @return list
	 */
	List<String> ddl(Table table, boolean init);


	/* *****************************************************************************************************************
	 * 													views
	 ******************************************************************************************************************/
	<T extends View> LinkedHashMap<String, T> views(boolean greedy, String catalog, String schema, String name, String types);
	<T extends View> LinkedHashMap<String, T> views(boolean greedy, String schema, String name, String types);
	<T extends View> LinkedHashMap<String, T> views(boolean greedy, String name, String types);
	<T extends View> LinkedHashMap<String, T> views(boolean greedy, String types);
	<T extends View> LinkedHashMap<String, T> views(boolean greedy);
	<T extends View> LinkedHashMap<String, T> views(String catalog, String schema, String name, String types);
	<T extends View> LinkedHashMap<String, T> views(String schema, String name, String types);
	<T extends View> LinkedHashMap<String, T> views(String name, String types);
	<T extends View> LinkedHashMap<String, T> views(String types);
	<T extends View> LinkedHashMap<String, T> views();
	List<String> ddl(View view);

	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String catalog, String schema, String name, String types);
	<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String schema, String name, String types);
	<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String name, String types);
	<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String types);
	<T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy);
	<T extends MasterTable> LinkedHashMap<String, T> mtables(String catalog, String schema, String name, String types);
	<T extends MasterTable> LinkedHashMap<String, T> mtables(String schema, String name, String types);
	<T extends MasterTable> LinkedHashMap<String, T> mtables(String name, String types);
	<T extends MasterTable> LinkedHashMap<String, T> mtables(String types);
	<T extends MasterTable> LinkedHashMap<String, T> mtables();
	List<String> ddl(MasterTable table);

	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String catalog, String schema, String master, String name);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String schema, String master, String name);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master, String name);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String,Object> tags, String name);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String,Object> tags);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(String catalog, String schema, String master, String name);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(String schema, String master, String name);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(String master, String name);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(String master);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String,Object> tags, String name);
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String,Object> tags);
	List<String> ddl(PartitionTable table);

	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/
	<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table);
	<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table);
	<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String catalog, String schema, String table);
	<T extends Column> LinkedHashMap<String, T> columns(Table table);
	<T extends Column> LinkedHashMap<String, T> columns(String table);
	<T extends Column> LinkedHashMap<String, T> columns(String catalog, String schema, String table);

	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	<T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table);
	<T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table);
	<T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String catalog, String schema, String table);
	<T extends Tag> LinkedHashMap<String, T> tags(Table table);
	<T extends Tag> LinkedHashMap<String, T> tags(String table);
	<T extends Tag> LinkedHashMap<String, T> tags(String catalog, String schema, String table);

	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/
	PrimaryKey primary(boolean greedy, Table table);
	PrimaryKey primary(boolean greedy, String table);
	PrimaryKey primary(boolean greedy, String catalog, String schema, String table);
	PrimaryKey primary(Table table);
	PrimaryKey primary(String table);
	PrimaryKey primary(String catalog, String schema, String table);


	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table);
	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/
	<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, Table table, String name);
	<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String table, String name);
	<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, Table table);
	<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String table);
	<T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String catalog, String schema, String table);
	<T extends Index> LinkedHashMap<String, T> indexs(Table table, String name);
	<T extends Index> LinkedHashMap<String, T> indexs(String table, String name);
	<T extends Index> LinkedHashMap<String, T> indexs(Table table);
	<T extends Index> LinkedHashMap<String, T> indexs(String table);
	<T extends Index> LinkedHashMap<String, T> indexs(String catalog, String schema, String table);

	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/
	<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, Table table, String name);
	<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String table, String name);
	<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, Table table);
	<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String table);
	<T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String catalog, String schema, String table);
	<T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name);
	<T extends Constraint> LinkedHashMap<String, T> constraints(String table, String name);
	<T extends Constraint> LinkedHashMap<String, T> constraints(Table table);
	<T extends Constraint> LinkedHashMap<String, T> constraints(String table);
	<T extends Constraint> LinkedHashMap<String, T> constraints(String catalog, String schema, String table);


	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/

	<T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events);


	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/

	<T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String catalog, String schema, String name);


	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	<T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String catalog, String schema, String name);


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
