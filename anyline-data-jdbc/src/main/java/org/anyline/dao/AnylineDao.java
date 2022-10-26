/* 
 * Copyright 2006-2022 www.anyline.org
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

import org.anyline.data.entity.*;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Procedure;
import org.anyline.data.prepare.RunPrepare;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AnylineDao<E>{

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/
	public DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions);
	public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String ... conditions);
	public DataSet querys(RunPrepare prepare, String ... conditions);
	public DataSet selects(RunPrepare prepare, ConfigStore configs, String ... conditions);
	public DataSet selects(RunPrepare prepare, String ... conditions);


	public List<Map<String,Object>> maps(RunPrepare prepare, ConfigStore configs, String ... conditions);
	public List<Map<String,Object>> maps(RunPrepare prepare, String ... conditions);

	public int count(RunPrepare prepare, ConfigStore configs, String ... conditions);
	public int count(RunPrepare prepare, String ... conditions);
	
	public boolean exists(RunPrepare prepare, ConfigStore configs, String ... conditions);
	public boolean exists(RunPrepare prepare, String ... conditions);

	/**
	 * 更新记录
	 * @param data		需要更新的数据
	 * @param dest		需要更新的表，如果没有提供则根据data解析
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @return int 影响行数
	 */
	public int update(String dest, Object data, ConfigStore configs, List<String> columns);
	public int update(String dest, Object data, ConfigStore configs, String ... columns);
	public int update(Object data, ConfigStore configs, String ... columns);
	public int update(Object data, ConfigStore configs, List<String> columns);
	public int update(String dest, Object data, String ... columns);
	public int update(Object data, String ... columns);
	public int update(String dest, Object data, List<String> columns);
	public int update(Object data, List<String> columns);
	 
	/** 
	 * 添加 
	 * @param data 需要插入的数据 
	 * @param checkPrimary   是否需要检查重复主键,默认不检查 
	 * @param columns  需要插入的列 
	 * @param dest 表 
	 * @return int
	 */
	public int insert(String dest, Object data, boolean checkPrimary, String ... columns);
	public int insert(Object data, boolean checkPrimary, String ... columns);
	public int insert(String dest, Object data, String ... columns);
	public int insert(Object data, String ... columns);

	public int insert(String dest, Object data, boolean checkPrimary, List<String> columns);
	public int insert(Object data, boolean checkPrimary, List<String> columns);
	public int insert(String dest, Object data, List<String> columns);
	public int insert(Object data, List<String> columns);


	public int batchInsert(String dest, Object data, boolean checkPrimary, String ... columns);
	public int batchInsert(Object data, boolean checkPrimary, String ... columns);
	public int batchInsert(String dest, Object data, String ... columns);
	public int batchInsert(Object data, String ... columns);
	/** 
	 * 保存(insert|update) 
	 * @param dest  表
	 * @param data  data
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns  columns
	 * @return int
	 */ 
	public int save(String dest, Object data, boolean checkPrimary, String ... columns); 
	public int save(Object data, boolean checkPrimary, String ... columns); 
	public int save(String dest, Object data, String ... columns); 
	public int save(Object data, String ... columns); 
 

	public int execute(RunPrepare prepare, ConfigStore configs, String ... conditions);
	public int execute(RunPrepare prepare, String ... conditions);

 
	/** 
	 * 执行存储过程 
	 * @param procedure  procedure
	 * @return boolean
	 */ 
	public boolean execute(Procedure procedure);
	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @return DataSet
	 */
	// public DataSet querys(Procedure procedure);
	public DataSet querys(Procedure procedure, PageNavi navi);
	public int delete(String dest, Object obj, String ... columns);
	public int delete(String table, ConfigStore configs, String ... conditions);

	/**
	 * 删除多行
	 * @param table 表
	 * @param key 列
	 * @param values 值集合
	 * @return 影响行数
	 */
	public int deletes(String table, String key, Collection<Object> values);
	public int deletes(String table, String key, String ... values);


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
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													database
	 ******************************************************************************************************************/
	public LinkedHashMap<String, Database> databases();


	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	public LinkedHashMap<String, Table> tables(String catalog, String schema, String name, String types);
	public LinkedHashMap<String, Table> tables(String schema, String name, String types);
	public LinkedHashMap<String, Table> tables(String name, String types);
	public LinkedHashMap<String, Table> tables(String types);
	public LinkedHashMap<String, Table> tables();

	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	public LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types);
	public LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types);
	public LinkedHashMap<String, MasterTable> mtables(String name, String types);
	public LinkedHashMap<String, MasterTable> mtables(String types);
	public LinkedHashMap<String, MasterTable> mtables();

	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/
	public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String name, String types);
	public LinkedHashMap<String, PartitionTable> ptables(String schema, String name, String types);
	public LinkedHashMap<String, PartitionTable> ptables(String name, String types);
	public LinkedHashMap<String, PartitionTable> ptables(String types);
	public LinkedHashMap<String, PartitionTable> ptables();
	public LinkedHashMap<String, PartitionTable> ptables(MasterTable master);
	public LinkedHashMap<String, PartitionTable> ptables(MasterTable master, Map<String,Object> tags);

	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/
	public LinkedHashMap<String, Column> columns(Table table);
	public LinkedHashMap<String, Column> columns(String table);
	public LinkedHashMap<String, Column> columns(String catalog, String schema, String table);

	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	public LinkedHashMap<String, Tag> tags(Table table);
	public LinkedHashMap<String, Tag> tags(String table);
	public LinkedHashMap<String, Tag> tags(String catalog, String schema, String table);

	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/
	public LinkedHashMap<String, Index> indexs(Table table);
	public LinkedHashMap<String, Index> indexs(String table);
	public LinkedHashMap<String, Index> indexs(String catalog, String schema, String table);

	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/
	public LinkedHashMap<String, Constraint> constraints(Table table);
	public LinkedHashMap<String, Constraint> constraints(String table);
	public LinkedHashMap<String, Constraint> constraints(String catalog, String schema, String table);



	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/
	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	public boolean create(Table table) throws Exception;
	public boolean alter(Table table) throws Exception;
	public boolean drop(Table table) throws Exception;
	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	public boolean create(MasterTable table) throws Exception;
	public boolean alter(MasterTable table) throws Exception;
	public boolean drop(MasterTable table) throws Exception;
	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/
	public boolean create(PartitionTable table) throws Exception;
	public boolean alter(PartitionTable table) throws Exception;
	public boolean drop(PartitionTable table) throws Exception;

	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/
	public boolean add(Column column) throws Exception;
	public boolean alter(Table table, Column column) throws Exception;
	public boolean alter(Column column) throws Exception;
	public boolean drop(Column column) throws Exception;

	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	public boolean add(Tag tag) throws Exception;
	public boolean alter(Table table, Tag tag) throws Exception;
	public boolean alter(Tag tag) throws Exception;
	public boolean drop(Tag tag) throws Exception;

	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/
	public boolean add(Index index) throws Exception;
	public boolean alter(Index index) throws Exception;
	public boolean drop(Index index) throws Exception;

	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/
	public boolean add(Constraint constraint) throws Exception;
	public boolean alter(Constraint constraint) throws Exception;
	public boolean drop(Constraint constraint) throws Exception;


} 
