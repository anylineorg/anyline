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

package org.anyline.dao;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.metadata.*;
import org.anyline.metadata.differ.MetadataDiffer;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface AnylineDao<E>{
	void setRuntime(DataRuntime runtime);
	DataRuntime runtime();
	DriverAdapter adapter();

	/**
	 * 根据差异生成SQL
	 * @param differ differ
	 * @return runs
	 */
	List<Run> ddl(DataRuntime runtime, MetadataDiffer differ);
	default List<Run> ddl(MetadataDiffer differ) {
		return ddl(runtime(), differ);
	}
	/**
	 * 根据差异生成SQL
	 * @param differs differs
	 * @return runs
	 */
	List<Run> ddl(DataRuntime runtime, List<MetadataDiffer> differs);
	default List<Run> ddl(List<MetadataDiffer> differs) {
		return ddl(runtime(), differs);
	}
	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/

	/**
	 * 查询row列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return mpas
	 */
	DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return querys(runtime(), null, prepare, configs, conditions);
	}

	/**
	 * 查询entity列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return mpas
	 */
	<T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions);
	default <T> EntitySet<T> selects(RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions) {
		return selects(runtime(), null, prepare, clazz, configs, conditions);
	}

	/**
	 * 查询map列表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return mpas
	 */
	List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default List<Map<String, Object>> maps(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return maps(runtime(), null, prepare, configs, conditions);
	}

	/**
	 * 合计总行数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return long
	 */
	long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default long count(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return count(runtime(), null, prepare, configs, conditions);
	}

	/**
	 * 创建查询序列SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 可以是多个序列
	 * @return DataRow
	 */
	DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names);
	default DataRow sequence(boolean next, String ... names) {
		return sequence(runtime(), null, next, names);
	}

	/**
	 * 是否存在
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return boolean
	 */
	boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default boolean exists(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return exists(runtime(), null, prepare, configs, conditions);
	}

	/**
	 * 更新记录
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data		需要更新的数据
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @return int 影响行数
	 */

	default long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}
	default long update(int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime(), null, batch, dest, data, configs, columns);
	}
	default long update(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
		return update(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(int batch, Object data, ConfigStore configs, String ... columns) {
		return update(batch, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long update(int batch, Object data, ConfigStore configs, List<String> columns) {
		return update(batch, (String)null, data, configs, columns);
	}
	default long update(int batch, String dest, Object data, String ... columns) {
		return update(batch, dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(int batch, String dest, Object data, List<String> columns) {
		return update(batch, dest, data, null, columns);
	}
	default long update(int batch, Object data, List<String> columns) {
		return update(batch, (String)null, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, 0, dest, data, configs, columns);
	}
	default long update(String dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime(), null, dest, data, configs, columns);
	}
	default long update(String dest, Object data, ConfigStore configs, String ... columns) {
		return update(dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(Object data, ConfigStore configs, String ... columns) {
		return update(null, data, configs, BeanUtil.array2list(columns));
	}
	default long update(Object data, ConfigStore configs, List<String> columns) {
		return update(null, data, configs, columns);
	}
	default long update(String dest, Object data, String ... columns) {
		return update(dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(String dest, Object data, List<String> columns) {
		return update(dest, data, null, columns);
	}
	default long update(Object data, List<String> columns) {
		return update(null, data, null, columns);
	}
	long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long update(int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime(), null, batch, dest, data, configs, columns);
	}
	default long update(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
		return update(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(int batch, Table dest, Object data, String ... columns) {
		return update(batch, dest, data, null, BeanUtil.array2list(columns));
	}
	default long update(int batch, Table dest, Object data, List<String> columns) {
		return update(batch, dest, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
		return update(runtime, random, 0, dest, data, configs, columns);
	}

	/** 
	 * 添加
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data 需要插入的数据
	 * @param columns  需要插入的列 
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return int
	 */
	default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
	}
	default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, List<String> columns) {
		return insert(runtime, random, batch, dest, data, null, columns);
	}
	default long insert(int batch, String dest, Object data, List<String> columns) {
		return insert(runtime(), null, batch, dest, data, columns);
	}
	default long insert(int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(runtime(), null, batch, dest, data, configs, columns);
	}
	default long insert(int batch, String dest, Object data, String ... columns) {
		return insert(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
		return insert(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Object data, String ... columns) {
		return insert(batch, (String)null, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Object data, ConfigStore configs, String ... columns) {
		return insert(batch, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Object data, List<String> columns) {
		return insert(batch, (String)null, data, columns);
	}
	default long insert(int batch, Object data, ConfigStore configs, List<String> columns) {
		return insert(batch, (String)null, data, configs, columns);
	}

	default long insert(DataRuntime runtime, String random, String dest, Object data, List<String> columns) {
		return insert(runtime, random, 0, dest, data, columns);
	}
	default long insert(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(runtime, random, 0, dest, data, configs, columns);
	}
	default long insert(String dest, Object data, List<String> columns) {
		return insert(runtime(), null, 0, dest, data, columns);
	}
	default long insert(String dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(runtime(), null, 0, dest, data, configs, columns);
	}
	default long insert(String dest, Object data, String ... columns) {
		return insert(0, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(String dest, Object data, ConfigStore configs, String ... columns) {
		return insert(0, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(Object data, String ... columns) {
		return insert(0, (String)null, data, BeanUtil.array2list(columns));
	}
	default long insert( Object data, ConfigStore configs, String ... columns) {
		return insert(0, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(Object data, List<String> columns) {
		return insert(0, (String)null, data, columns);
	}
	default long insert(Object data, ConfigStore configs, List<String> columns) {
		return insert(0, (String)null, data, configs, columns);
	}

	long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
	default long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String> columns) {
		return insert(runtime, random, batch, dest, data, null, columns);
	}
	default long insert(int batch, Table dest, Object data, List<String> columns) {
		return insert(runtime(), null, batch, dest, data, columns);
	}
	default long insert(int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(runtime(), null, batch, dest, data, configs, columns);
	}
	default long insert(int batch, Table dest, Object data, String ... columns) {
		return insert(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
		return insert(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, Table dest, Object data, List<String> columns) {
		return insert(runtime, random, 0, dest, data, columns);
	}
	default long insert(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(runtime, random, 0, dest, data, configs, columns);
	}
	default long insert(Table dest, Object data, List<String> columns) {
		return insert(runtime(), null, 0, dest, data, columns);
	}
	default long insert(Table dest, Object data, ConfigStore configs, List<String> columns) {
		return insert(runtime(), null, 0, dest, data, configs, columns);
	}
	default long insert(Table dest, Object data, String ... columns) {
		return insert(0, dest, data, BeanUtil.array2list(columns));
	}
	default long insert(Table dest, Object data, ConfigStore configs, String ... columns) {
		return insert(0, dest, data, configs, BeanUtil.array2list(columns));
	}

	/**
	 * insert into table select * from table
	 * 与query参数一致
	 * @param dest 插入表
	 * @param prepare 查询表
	 * @param configs 查询条件及相关配置
	 * @param obj 查询条件
	 * @param conditions 查询条件
	 * @return 影响行数
	 */
	long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);
	default long insert(Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
		return insert(runtime(), null, dest, prepare, configs, obj, conditions);
	}
	default long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare) {
		return insert(runtime, random, dest, prepare, null);
	}
	default long insert(DataRuntime runtime, String random, String dest, RunPrepare prepare, ConfigStore configs) {
		return insert(runtime, random, new Table(dest), prepare, configs, null, (String) null);
	}
	default long insert(DataRuntime runtime, String random, String dest, RunPrepare prepare) {
		return insert(runtime, random, new Table(dest), prepare);
	}
	default long insert(Table dest, RunPrepare prepare) {
		return insert(dest, prepare, (ConfigStore)null);
	}
	default long insert(String dest, RunPrepare prepare, ConfigStore configs) {
		return insert(new Table(dest), prepare, configs);
	}
	default long insert(String dest, RunPrepare prepare) {
		return insert(new Table(dest), prepare, new DefaultConfigStore());
	}
	/**
	 * 保存(insert|update)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param data  data
	 * @param columns  columns
	 * @return int
	 */
	long save(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String>  columns);
	default long save(DataRuntime runtime, String random, int batch, String dest, Object data, List<String>  columns) {
		return save(runtime, random, batch, dest, data, null, columns);
	}
	default long save(int batch, String dest, Object data, List<String>  columns) {
		return save(runtime(), null, batch, dest, data, columns);
	}
	default long save(int batch, String dest, Object data, ConfigStore configs, List<String>  columns) {
		return save(runtime(), null, batch, dest, data, configs, columns);
	}
	default long save(int batch, String dest, Object data, String ... columns) {
		return save(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long save(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
		return save(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(int batch, Object data, String ... columns) {
		return save(batch, (String)null, data, BeanUtil.array2list(columns));
	}
	default long save(int batch, Object data, ConfigStore configs, String ... columns) {
		return save(batch, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, List<String>  columns) {
		return save(runtime, random, 0, dest, data, columns);
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String>  columns) {
		return save(runtime, random, 0, dest, data, configs, columns);
	}
	default long save(String dest, Object data, List<String>  columns) {
		return save(runtime(), null, 0, dest, data, columns);
	}
	default long save(String dest, Object data, ConfigStore confnigs, List<String>  columns) {
		return save(runtime(), null, 0, dest, data, confnigs, columns);
	}
	default long save(String dest, Object data, String ... columns) {
		return save(0, dest, data, BeanUtil.array2list(columns));
	}
	default long save(String dest, Object data, ConfigStore configs, String ... columns) {
		return save(0, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(Object data, String ... columns) {
		return save(0, (String)null, data, BeanUtil.array2list(columns));
	}
	default long save(Object data, ConfigStore configs, String ... columns) {
		return save(0, (String)null, data, configs, BeanUtil.array2list(columns));
	}
	long save(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String>  columns);
	default long save(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String>  columns) {
		return save(runtime, random, batch, dest, data, null, columns);
	}
	default long save(int batch, Table dest, Object data, List<String>  columns) {
		return save(runtime(), null, batch, dest, data, columns);
	}
	default long save(int batch, Table dest, Object data, ConfigStore configs, List<String>  columns) {
		return save(runtime(), null, batch, dest, data, configs, columns);
	}
	default long save(int batch, Table dest, Object data, String ... columns) {
		return save(batch, dest, data, BeanUtil.array2list(columns));
	}
	default long save(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
		return save(batch, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long save(DataRuntime runtime, String random, Table dest, Object data, List<String>  columns) {
		return save(runtime, random, 0, dest, data, columns);
	}
	default long save(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String>  columns) {
		return save(runtime, random, 0, dest, data, configs, columns);
	}
	default long save(Table dest, Object data, List<String>  columns) {
		return save(runtime(), null, 0, dest, data, columns);
	}
	default long save(Table dest, Object data, ConfigStore confnigs, List<String>  columns) {
		return save(runtime(), null, 0, dest, data, confnigs, columns);
	}
	default long save(Table dest, Object data, String ... columns) {
		return save(0, dest, data, BeanUtil.array2list(columns));
	}
	default long save(Table dest, Object data, ConfigStore configs, String ... columns) {
		return save(0, dest, data, configs, BeanUtil.array2list(columns));
	}

	/**
	 * 执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions 简单过滤条件
	 * @return 影响行数
	 */
	long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	default long execute(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return execute(runtime(), null, prepare, configs, conditions);
	}
	default long execute(RunPrepare prepare, String ... conditions) {
		return execute(prepare, null, conditions);
	}

	long execute(DataRuntime runtime, String random, int batch, RunPrepare prepare, Collection<Object> values);

	long execute(DataRuntime runtime, String random, int batch, int vol, RunPrepare prepare, Collection<Object> values);
	default long execute(int batch, RunPrepare prepare, Collection<Object> values) {
		return execute(runtime(), null, batch, prepare, values);
	}
	default long execute(int batch, int vol, RunPrepare prepare, Collection<Object> values) {
		return execute(runtime(), null, batch, vol, prepare, values);
	}

	/** 
	 * 执行存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure  存储过程
	 * @return boolean
	 */
	boolean execute(DataRuntime runtime, String random, Procedure procedure);
	default boolean execute(Procedure procedure) {
		return execute(runtime(), null, procedure);
	}

	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @return DataSet
	 */
	DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi);
	default DataSet querys(Procedure procedure, PageNavi navi) {
		return querys(runtime(), null, procedure, navi);
	}
	default long delete(DataRuntime runtime, String random, String dest, ConfigStore configs, Object obj, String ... columns) {
		return delete(runtime, random, DataSourceUtil.parseDest(dest, null, configs), configs, obj, columns);
	}
	default long delete(DataRuntime runtime, String random, String dest, Object obj, String ... columns) {
		return delete(runtime, random, dest, null, obj, columns);
	}
	default long delete(DataRuntime runtime, String random, String dest, Collection values) {
		return delete(runtime, random, dest, null, values);
	}
	default long delete(String dest, Object obj, String ... columns) {
		return delete(runtime(), null, dest, obj, columns);
	}
	default long delete(String dest, Collection values){
		return delete(runtime(), null, dest, values);
	}
	default long delete(Table dest, Collection values){
		return delete(runtime(), null, dest, values);
	}
	default long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String ... conditions) {
		return delete(runtime, random, new Table(table), configs, conditions);
	}
	default long delete(String table, ConfigStore configs, String ... conditions) {
		return delete(runtime(), null, table, configs, conditions);
	}
	long delete(DataRuntime runtime, String random, Table dest, ConfigStore configs, Object obj, String ... columns);
	default long delete(DataRuntime runtime, String random, Table dest, Object obj, String ... columns) {
		return delete(runtime, random, dest, null, obj, columns);
	}
	default long delete(Table dest, Object obj, String ... columns) {
		return delete(runtime(), null, dest, obj, columns);
	}
	long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String ... conditions);
	default long delete(Table table, ConfigStore configs, String ... conditions) {
		return delete(runtime(), null, table, configs, conditions);
	}

	/**
	 * 删除多行
	 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 列
	 * @param values 值集合
	 * @return 影响行数
	 */
	default <T> long deletes(DataRuntime runtime, String random, int batch, String table, String key, Collection<T> values) {
		return deletes(runtime, random, batch, new Table(table), key, values);
	}
	default <T> long deletes(int batch, String table, String key, Collection<T> values) {
		return deletes(runtime(), null, batch, table, key, values);
	}
	default <T> long deletes(int batch, String table, String key, T ... values) {
		return deletes(batch, table, key, BeanUtil.array2list(values));
	}
	<T> long deletes(DataRuntime runtime, String random, int batch, Table table, String key, Collection<T> values);
	default <T> long deletes(int batch, Table table, String key, Collection<T> values) {
		return deletes(runtime(), null, batch, table, key, values);
	}
	default <T> long deletes(int batch, Table table, String key, T ... values) {
		return deletes(batch, table, key, BeanUtil.array2list(values));
	}
	long truncate(DataRuntime runtime, String random, Table table);
	default long truncate(DataRuntime runtime, String random, String table) {
		return truncate(runtime, random, new Table(table));
	}
	default long truncate(String table) {
		return truncate(runtime(), null, table);
	}
	default long truncate(Table table) {
		return truncate(runtime(), null, table);
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

	/**
	 * 根据结果集对象获取列结构, 如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
	 * @param prepare RunPrepare
	 * @param comment 是否需要注释
	 * @return LinkedHashMap
	 */
	LinkedHashMap<String, Column> metadata(RunPrepare prepare, boolean comment);
	/* *****************************************************************************************************************
	 * 													database
	 ******************************************************************************************************************/

	/**
	 * 当前数据源 数据库类型
	 * @return DatabaseType
	 */
	DatabaseType type();

	/**
	 * 当前数据源 数据库版本 版本号比较复杂 不是全数字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	String version(DataRuntime runtime, String random);
	default String version() {
		return version(runtime(), null);
	}

	/**
	 * 当前数据源 数据库描述(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	String product(DataRuntime runtime, String random);
	default String product() {
		return product(runtime(), null);
	}
	Database database(DataRuntime runtime, String random);
	default Database database() {
		return database(runtime(), null);
	}
	<T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, String random, String name);
	default <T extends Database> LinkedHashMap<String, Database> databases(String name) {
		return databases(runtime(), null, name);
	}

	<T extends Database> List<T> databases(DataRuntime runtime, String random, boolean greedy, String name);
	default <T extends Database> List<T> databases(boolean greedy, String name) {
		return databases(runtime(), null, greedy, name);
	}
	Database database(DataRuntime runtime, String random, String name);
	default Database database(String name) {
		return database(runtime(), null, name);
	}

	/* *****************************************************************************************************************
	 * 													catalog
	 ******************************************************************************************************************/
	Catalog catalog(DataRuntime runtime, String random);
	default Catalog catalog() {
		return catalog(runtime(), null);
	}
	<T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, String name);
	default <T extends Catalog> LinkedHashMap<String, T> catalogs(String name) {
		return catalogs(runtime(), null, name);
	}
	<T extends Catalog> List<T> catalogs(DataRuntime runtime, String random, boolean greedy, String name);
	default <T extends Catalog> List<T> catalogs(boolean greedy, String name) {
		return catalogs(runtime(), null, greedy, name);
	}

	/* *****************************************************************************************************************
	 * 													schema
	 ******************************************************************************************************************/
	Schema schema(DataRuntime runtime, String random);
	default Schema schema() {
		return schema(runtime(), null);
	}
	<T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, String random, Catalog catalog, String name);
	default <T extends Schema> LinkedHashMap<String, T> schemas(Catalog catalog, String name) {
		return schemas(runtime(), null, catalog, name);
	}

	<T extends Schema> List<T> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name);
	default <T extends Schema> List<T> schemas(boolean greedy, Catalog catalog, String name) {
		return schemas(runtime(), null, greedy, catalog, name);
	}

	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	<T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Table query, int types, int struct, ConfigStore configs);
	default <T extends Table> List<T> tables(boolean greedy, Table query, int types, int struct, ConfigStore configs) {
		return tables(runtime(), null, greedy, query, types, struct, configs);
	}
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		Table query = new Table();
		return tables(runtime, random, greedy, query, types, struct, configs);
	}
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
		return tables(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return tables(runtime, random, greedy, catalog, schema, name, types, structs, configs);
	}
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return tables(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return tables(runtime(), null, greedy, catalog, schema, name, types, struct, configs);
	}
	default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
		return tables(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return tables(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types, int struct) {
		return tables(greedy, null, schema, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types, boolean struct) {
		return tables(greedy, null, schema, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, String name, int types, int struct) {
		return tables(greedy, null, null, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, String name, int types, boolean struct) {
		return tables(greedy, null, null, name, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, int types, int struct) {
		return tables(greedy, null, null, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, int types, boolean struct) {
		return tables(greedy, null, null, types, struct);
	}
	default <T extends Table> List<T> tables(boolean greedy, boolean struct) {
		return tables(greedy, null, null, null, 1, struct);
	}
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Table query, int types, int struct, ConfigStore configs);
	default <T extends Table> LinkedHashMap<String, T> tables(Table query, int types, int struct, ConfigStore configs) {
		return tables(runtime(), null, query, types, struct, configs);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		Table query = new Table();
		query.setCatalog(catalog);
		query.setSchema(schema);
		query.setName(name);
		return tables(runtime, random, query, types, struct, configs);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct) {
		return tables(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return tables(runtime, random, catalog, schema, name, types, structs, configs);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return tables(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, int struct) {
		return tables(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return tables(runtime(), null, catalog, schema, name, types, struct, configs);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return tables(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		return tables(runtime(), null, catalog, schema, name, types, struct, configs);
	}

	default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types, int struct) {
		return tables(null, schema, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types, boolean struct) {
		return tables(null, schema, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String name, int types, int struct) {
		return tables(null, null, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String name, int types, boolean struct) {
		return tables(null, null, name, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(int types, int struct) {
		return tables(null, null, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(int types, boolean struct) {
		return tables(null, null, types, struct);
	}
	default <T extends Table> LinkedHashMap<String, T> tables() {
		return tables(null, null, null, 1, false);
	}

	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
		return tables(runtime, random, greedy, catalog, schema, name, types, false, configs);
	}
	default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return tables(runtime, random, greedy, catalog, schema, name, types, false);
	}
	default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return tables(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types) {
		return tables(greedy, null, schema, name, types);
	}
	default <T extends Table> List<T> tables(boolean greedy, String name, int types) {
		return tables(greedy, null, null, name, types);
	}
	default <T extends Table> List<T> tables(boolean greedy, int types) {
		return tables(greedy, null, null, types);
	}
	default <T extends Table> List<T> tables(boolean greedy) {
		return tables(greedy, null, null, null, 1);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types) {
		return tables(runtime, random, catalog, schema, name, types, false);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types) {
		return tables(runtime(), null, catalog, schema, name, types);
	}

	default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types) {
		return tables(null, schema, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(String name, int types) {
		return tables(null, null, name, types);
	}
	default <T extends Table> LinkedHashMap<String, T> tables(int types) {
		return tables(null, null, types);
	}

	/**
	 *
	 * 查询表的创建SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param init 是否还原初始状态(自增ID)
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, Table table, boolean init);
	default List<String> ddl(Table table, boolean init) {
		return ddl(runtime(), null, table, init);
	}

	/* *****************************************************************************************************************
	 * 													edge
	 ******************************************************************************************************************/
	<T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, EdgeTable query, int types, int struct, ConfigStore configs);
	default <T extends EdgeTable> List<T> edges(boolean greedy, EdgeTable query, int types, int struct, ConfigStore configs){
		return edges(runtime(), null, greedy, query, types, struct, configs);
	}
	default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		EdgeTable query = new EdgeTable(catalog, schema, name);
		return edges(runtime, random, greedy, query, types, struct, configs);
	}
	default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
		return edges(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return edges(runtime, random, greedy, catalog, schema, name, types, structs, configs);
	}
	default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return edges(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return edges(runtime(), null, greedy, catalog, schema, name, types, struct, configs);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
		return edges(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return edges(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, Schema schema, String name, int types, int struct) {
		return edges(greedy, null, schema, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, Schema schema, String name, int types, boolean struct) {
		return edges(greedy, null, schema, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, String name, int types, int struct) {
		return edges(greedy, null, null, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, String name, int types, boolean struct) {
		return edges(greedy, null, null, name, types, struct);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, int types, int struct) {
		return edges(greedy, null, null, types, struct);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, int types, boolean struct) {
		return edges(greedy, null, null, types, struct);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, boolean struct) {
		return edges(greedy, null, null, null, 1, struct);
	}
	<T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, EdgeTable query, int types, int struct, ConfigStore configs);
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(EdgeTable query, int types, int struct, ConfigStore configs) {
		return edges(runtime(), null, query, types, struct, configs);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		EdgeTable query = new EdgeTable(catalog, schema, name);
		return edges(runtime, random, query, types, struct, configs);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct) {
		return edges(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return edges(runtime, random, catalog, schema, name, types, structs, configs);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return edges(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types, int struct) {
		return edges(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return edges(runtime(), null, catalog, schema, name, types, struct, configs);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return edges(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		return edges(runtime(), null, catalog, schema, name, types, struct, configs);
	}

	default <T extends EdgeTable> LinkedHashMap<String, T> edges(Schema schema, String name, int types, int struct) {
		return edges(null, schema, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(Schema schema, String name, int types, boolean struct) {
		return edges(null, schema, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(String name, int types, int struct) {
		return edges(null, null, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(String name, int types, boolean struct) {
		return edges(null, null, name, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types, int struct) {
		return edges(null, null, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types, boolean struct) {
		return edges(null, null, types, struct);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges() {
		return edges(null, null, null, 1, false);
	}

	default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
		return edges(runtime, random, greedy, catalog, schema, name, types, false, configs);
	}
	default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return edges(runtime, random, greedy, catalog, schema, name, types, false);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return edges(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, Schema schema, String name, int types) {
		return edges(greedy, null, schema, name, types);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, String name, int types) {
		return edges(greedy, null, null, name, types);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy, int types) {
		return edges(greedy, null, null, types);
	}
	default <T extends EdgeTable> List<T> edges(boolean greedy) {
		return edges(greedy, null, null, null, 1);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types) {
		return edges(runtime, random, catalog, schema, name, types, false);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types) {
		return edges(runtime(), null, catalog, schema, name, types);
	}

	default <T extends EdgeTable> LinkedHashMap<String, T> edges(Schema schema, String name, int types) {
		return edges(null, schema, name, types);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(String name, int types) {
		return edges(null, null, name, types);
	}
	default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types) {
		return edges(null, null, types);
	}

	/**
	 *
	 * 查询表的创建SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param edge 表
	 * @param init 是否还原初始状态(自增ID)
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, EdgeTable edge, boolean init);
	default List<String> ddl(EdgeTable edge, boolean init) {
		return ddl(runtime(), null, edge, init);
	}

	/* *****************************************************************************************************************
	 * 													vertex
	 ******************************************************************************************************************/
	<T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, VertexTable query, int types, int struct, ConfigStore configs);
	default <T extends VertexTable> List<T> vertexs(boolean greedy, VertexTable query, int types, int struct, ConfigStore configs) {
		return vertexs(runtime(), null, greedy, query, types, struct, configs);
	}
	default<T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		VertexTable query = new VertexTable(catalog, schema, name);
		return vertexs(runtime, random, greedy, query, types, struct, configs);
	}
	default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
		return vertexs(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return vertexs(runtime, random, greedy, catalog, schema, name, types, structs, configs);
	}
	default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return vertexs(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return vertexs(runtime(), null, greedy, catalog, schema, name, types, struct, configs);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
		return vertexs(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return vertexs(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, Schema schema, String name, int types, int struct) {
		return vertexs(greedy, null, schema, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, Schema schema, String name, int types, boolean struct) {
		return vertexs(greedy, null, schema, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, String name, int types, int struct) {
		return vertexs(greedy, null, null, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, String name, int types, boolean struct) {
		return vertexs(greedy, null, null, name, types, struct);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, int types, int struct) {
		return vertexs(greedy, null, null, types, struct);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, int types, boolean struct) {
		return vertexs(greedy, null, null, types, struct);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, boolean struct) {
		return vertexs(greedy, null, null, null, 1, struct);
	}
	<T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, VertexTable query, int types, int struct, ConfigStore configs);
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(VertexTable query, int types, int struct, ConfigStore configs) {
		return vertexs(runtime(), null, query, types, struct, configs);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		VertexTable query = new VertexTable(catalog, schema, name);
		return vertexs(runtime, random, query, types, struct, configs);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct) {
		return vertexs(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return vertexs(runtime, random, catalog, schema, name, types, structs, configs);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return vertexs(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types, int struct) {
		return vertexs(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return vertexs(runtime(), null, catalog, schema, name, types, struct, configs);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return vertexs(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		return vertexs(runtime(), null, catalog, schema, name, types, struct, configs);
	}

	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Schema schema, String name, int types, int struct) {
		return vertexs(null, schema, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Schema schema, String name, int types, boolean struct) {
		return vertexs(null, schema, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(String name, int types, int struct) {
		return vertexs(null, null, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(String name, int types, boolean struct) {
		return vertexs(null, null, name, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types, int struct) {
		return vertexs(null, null, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types, boolean struct) {
		return vertexs(null, null, types, struct);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs() {
		return vertexs(null, null, null, 1, false);
	}

	default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
		return vertexs(runtime, random, greedy, catalog, schema, name, types, false, configs);
	}
	default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return vertexs(runtime, random, greedy, catalog, schema, name, types, false);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return vertexs(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, Schema schema, String name, int types) {
		return vertexs(greedy, null, schema, name, types);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, String name, int types) {
		return vertexs(greedy, null, null, name, types);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy, int types) {
		return vertexs(greedy, null, null, types);
	}
	default <T extends VertexTable> List<T> vertexs(boolean greedy) {
		return vertexs(greedy, null, null, null, 1);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types) {
		return vertexs(runtime, random, catalog, schema, name, types, false);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types) {
		return vertexs(runtime(), null, catalog, schema, name, types);
	}

	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Schema schema, String name, int types) {
		return vertexs(null, schema, name, types);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(String name, int types) {
		return vertexs(null, null, name, types);
	}
	default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types) {
		return vertexs(null, null, types);
	}

	/**
	 *
	 * 查询表的创建SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param vertex 表
	 * @param init 是否还原初始状态(自增ID)
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, VertexTable vertex, boolean init);
	default List<String> ddl(VertexTable vertex, boolean init) {
		return ddl(runtime(), null, vertex, init);
	}

	/* *****************************************************************************************************************
	 * 													master
	 ******************************************************************************************************************/
	<T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, MasterTable query, int types, int struct, ConfigStore configs);
	default <T extends MasterTable> List<T> masters(boolean greedy, MasterTable query, int types, int struct, ConfigStore configs) {
		return masters(runtime(), null, greedy, query, types, struct, configs);
	}
	default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		MasterTable query = new MasterTable();
		query.setCatalog(catalog);
		query.setSchema(schema);
		query.setName(name);
		return masters(runtime, random, greedy, query, types, struct, configs);
	}
	default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
		return masters(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return masters(runtime, random, greedy, catalog, schema, name, types, structs, configs);
	}
	default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return masters(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return masters(runtime(), null, greedy, catalog, schema, name, types, struct, configs);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
		return masters(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return masters(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, Schema schema, String name, int types, int struct) {
		return masters(greedy, null, schema, name, types, struct);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, Schema schema, String name, int types, boolean struct) {
		return masters(greedy, null, schema, name, types, struct);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, String name, int types, int struct) {
		return masters(greedy, null, null, name, types, struct);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, String name, int types, boolean struct) {
		return masters(greedy, null, null, name, types, struct);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, int types, int struct) {
		return masters(greedy, null, null, types, struct);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, int types, boolean struct) {
		return masters(greedy, null, null, types, struct);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, boolean struct) {
		return masters(greedy, null, null, null, 1, struct);
	}
	<T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, MasterTable query, int types, int struct, ConfigStore configs);
	default <T extends MasterTable> LinkedHashMap<String, T> masters(MasterTable query, int types, int struct, ConfigStore configs) {
		return masters(runtime(), null, query, types, struct, configs);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		MasterTable query = new MasterTable();
		query.setCatalog(catalog);
		query.setSchema(schema);
		query.setName(name);
		return masters(runtime, random, query, types, struct, configs);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct) {
		return masters(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return masters(runtime, random, catalog, schema, name, types, structs, configs);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return masters(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types, int struct) {
		return masters(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return masters(runtime(), null, catalog, schema, name, types, struct, configs);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return masters(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		return masters(runtime(), null, catalog, schema, name, types, struct, configs);
	}

	default <T extends MasterTable> LinkedHashMap<String, T> masters(Schema schema, String name, int types, int struct) {
		return masters(null, schema, name, types, struct);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(Schema schema, String name, int types, boolean struct) {
		return masters(null, schema, name, types, struct);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(String name, int types, int struct) {
		return masters(null, null, name, types, struct);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(String name, int types, boolean struct) {
		return masters(null, null, name, types, struct);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(int types, int struct) {
		return masters(null, null, types, struct);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(int types, boolean struct) {
		return masters(null, null, types, struct);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters() {
		return masters(null, null, null, 1, false);
	}

	default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
		return masters(runtime, random, greedy, catalog, schema, name, types, false, configs);
	}
	default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return masters(runtime, random, greedy, catalog, schema, name, types, false);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return masters(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, Schema schema, String name, int types) {
		return masters(greedy, null, schema, name, types);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, String name, int types) {
		return masters(greedy, null, null, name, types);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy, int types) {
		return masters(greedy, null, null, types);
	}
	default <T extends MasterTable> List<T> masters(boolean greedy) {
		return masters(greedy, null, null, null, 1);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types) {
		return masters(runtime, random, catalog, schema, name, types, false);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types) {
		return masters(runtime(), null, catalog, schema, name, types);
	}

	default <T extends MasterTable> LinkedHashMap<String, T> masters(Schema schema, String name, int types) {
		return masters(null, schema, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(String name, int types) {
		return masters(null, null, name, types);
	}
	default <T extends MasterTable> LinkedHashMap<String, T> masters(int types) {
		return masters(null, null, types);
	}

	/* *****************************************************************************************************************
	 * 													view
	 ******************************************************************************************************************/
	<T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, View query, int types, int struct, ConfigStore configs);
	default <T extends View> List<T> views(boolean greedy, View query, int types, int struct, ConfigStore configs) {
		return views(runtime(), null, greedy, query, types, struct, configs);
	}
	default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		View query = new View(catalog, schema, name);
		return views(runtime, random, greedy, query, types, struct, configs);
	}
	default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct){
		return views(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return views(runtime, random, greedy, catalog, schema, name, types, structs, configs);
	}
	default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return views(runtime, random, greedy, catalog, schema, name, types, struct, null);
	}
	default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return views(runtime(), null, greedy, catalog, schema, name, types, struct, configs);
	}
	default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
		return views(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return views(runtime(), null, greedy, catalog, schema, name, types, struct);
	}
	default <T extends View> List<T> views(boolean greedy, Schema schema, String name, int types, int struct) {
		return views(greedy, null, schema, name, types, struct);
	}
	default <T extends View> List<T> views(boolean greedy, Schema schema, String name, int types, boolean struct) {
		return views(greedy, null, schema, name, types, struct);
	}
	default <T extends View> List<T> views(boolean greedy, String name, int types, int struct) {
		return views(greedy, null, null, name, types, struct);
	}
	default <T extends View> List<T> views(boolean greedy, String name, int types, boolean struct) {
		return views(greedy, null, null, name, types, struct);
	}
	default <T extends View> List<T> views(boolean greedy, int types, int struct) {
		return views(greedy, null, null, types, struct);
	}
	default <T extends View> List<T> views(boolean greedy, int types, boolean struct) {
		return views(greedy, null, null, types, struct);
	}
	default <T extends View> List<T> views(boolean greedy, boolean struct) {
		return views(greedy, null, null, null, 1, struct);
	}
	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, View query, int types, int struct, ConfigStore configs);
	default <T extends View> LinkedHashMap<String, T> views(View query, int types, int struct, ConfigStore configs) {
		return views(runtime(), null, query, types, struct, configs);
	}
	default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		View query = new View(catalog, schema, name);
		return views(runtime, random, query, types, struct, configs);
	}
	default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, int struct) {
		return views(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		int structs = 0;
		if(struct) {
			structs = 32767;
		}
		return views(runtime, random, catalog, schema, name, types, structs, configs);
	}
	default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return views(runtime, random, catalog, schema, name, types, struct, null);
	}
	default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, int struct) {
		return views(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
		return views(runtime(), null, catalog, schema, name, types, struct, configs);
	}
	default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, boolean struct) {
		return views(runtime(), null, catalog, schema, name, types, struct);
	}
	default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
		return views(runtime(), null, catalog, schema, name, types, struct, configs);
	}

	default <T extends View> LinkedHashMap<String, T> views(Schema schema, String name, int types, int struct) {
		return views(null, schema, name, types, struct);
	}
	default <T extends View> LinkedHashMap<String, T> views(Schema schema, String name, int types, boolean struct) {
		return views(null, schema, name, types, struct);
	}
	default <T extends View> LinkedHashMap<String, T> views(String name, int types, int struct) {
		return views(null, null, name, types, struct);
	}
	default <T extends View> LinkedHashMap<String, T> views(String name, int types, boolean struct) {
		return views(null, null, name, types, struct);
	}
	default <T extends View> LinkedHashMap<String, T> views(int types, int struct) {
		return views(null, null, types, struct);
	}
	default <T extends View> LinkedHashMap<String, T> views(int types, boolean struct) {
		return views(null, null, types, struct);
	}
	default <T extends View> LinkedHashMap<String, T> views() {
		return views(null, null, null, 1, false);
	}

	default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
		return views(runtime, random, greedy, catalog, schema, name, types, false, configs);
	}
	default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return views(runtime, random, greedy, catalog, schema, name, types, false);
	}
	default <T extends View> List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
		return views(runtime(), null, greedy, catalog, schema, name, types);
	}
	default <T extends View> List<T> views(boolean greedy, Schema schema, String name, int types) {
		return views(greedy, null, schema, name, types);
	}
	default <T extends View> List<T> views(boolean greedy, String name, int types) {
		return views(greedy, null, null, name, types);
	}
	default <T extends View> List<T> views(boolean greedy, int types) {
		return views(greedy, null, null, types);
	}
	default <T extends View> List<T> views(boolean greedy) {
		return views(greedy, null, null, null, 1);
	}
	default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name, int types) {
		return views(runtime, random, catalog, schema, name, types, false);
	}
	default <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types) {
		return views(runtime(), null, catalog, schema, name, types);
	}

	default <T extends View> LinkedHashMap<String, T> views(Schema schema, String name, int types) {
		return views(null, schema, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(String name, int types) {
		return views(null, null, name, types);
	}
	default <T extends View> LinkedHashMap<String, T> views(int types) {
		return views(null, null, types);
	}

	/**
	 *
	 * 查询视图的创建SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param view 视图
	 * @param init 是否还原初始状态(自增ID)
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, String random, View view, boolean init);
	default List<String> ddl(View view, boolean init) {
		return ddl(runtime(), null, view, init);
	}
	default List<String> ddl(View view) {
		return ddl(runtime(), null, view, false);
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/
	<T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, String random, boolean greedy, PartitionTable query);
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, PartitionTable query) {
		return partitions(runtime(), null, greedy, query);
	}
	default<T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String name) {
		PartitionTable query = new PartitionTable();
		query.setMaster(master);
		if(null != tags){
			for(String key:tags.keySet()){
				Tag tag = null;
				Object value = tags.get(key);
				if(value instanceof Tag){
					tag = (Tag)value;
				}else{
					tag = new Tag(key, value);
				}
				query.addTag(tag);
			}
		}
		query.setName(name);
		return partitions(runtime, random, greedy, query);
	}

	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, Catalog catalog, Schema schema, String master, String name) {
		return partitions(greedy, new MasterTable(catalog, schema, master), null, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, Schema schema, String master, String name) {
		return partitions(greedy, null, schema, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, String master, String name) {
		return partitions(greedy, null, null, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, String master) {
		return partitions(greedy, null, null, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, MasterTable master) {
		return partitions(greedy, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, MasterTable master, Map<String, Object> tags, String name) {
		return partitions(runtime(), null, greedy, master, tags, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, MasterTable master, Map<String, Object> tags) {
		return partitions(runtime(), null, greedy, master, tags, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(Catalog catalog, Schema schema, String master, String name) {
		MasterTable mtable = new MasterTable(catalog, schema, master);
		return partitions(false, mtable, null, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(Schema schema, String master, String name) {
		return partitions(false, null, schema, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(String master, String name) {
		return partitions(false, null, null, master, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(String master) {
		return partitions(false, null, null, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(MasterTable master) {
		return partitions(false, master, null);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(MasterTable master, Map<String, Object> tags, String name) {
		return partitions(false, master, tags, name);
	}
	default <T extends PartitionTable> LinkedHashMap<String, T> partitions(MasterTable master, Map<String, Object> tags) {
		return partitions(false, master, tags, null);
	}
	List<String> ddl(DataRuntime runtime, String random, PartitionTable table);
	default List<String> ddl(PartitionTable table) {
		return ddl(runtime(), null, table);
	}

	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/

	/**
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param primary 是否检测列的主键标识
	 * @return LinkedHashMap
	 * @param <T> Column
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary, ConfigStore configs);
	default <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary){
		return columns(runtime, random, greedy, table, primary, null);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table, ConfigStore configs) {
		return columns(runtime(), null, greedy, table, ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY, configs);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table) {
		return columns(runtime(), null, greedy, table, ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY, new DefaultConfigStore());
	}
	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table, ConfigStore configs) {
		return columns(greedy, new Table(table), configs);
	}

	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table) {
		return columns(greedy, new Table(table), null);
	}

	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Catalog catalog, Schema schema, String table, ConfigStore configs) {
		return columns(greedy, new Table(catalog, schema, table), configs);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Catalog catalog, Schema schema, String table) {
		return columns(greedy, new Table(catalog, schema, table), new DefaultConfigStore());
	}
	default <T extends Column> LinkedHashMap<String, T> columns(Table table, ConfigStore configs) {
		return columns(false, table, configs);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(Table table) {
		return columns(false, table, new DefaultConfigStore());
	}
	default <T extends Column> LinkedHashMap<String, T> columns(String table, ConfigStore configs) {
		return columns(false, table, configs);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(String table) {
		return columns(false, table, new DefaultConfigStore());
	}
	default <T extends Column> LinkedHashMap<String, T> columns(Catalog catalog, Schema schema, String table, ConfigStore configs) {
		return columns(new Table(catalog, schema, table), configs);
	}
	default <T extends Column> LinkedHashMap<String, T> columns(Catalog catalog, Schema schema, String table) {
		return columns(new Table(catalog, schema, table), new DefaultConfigStore());
	}
	<T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Column query, ConfigStore configs);

	default <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
		Column query = new Column();
		query.setCatalog(catalog);
		query.setSchema(schema);
		return columns(runtime, random, greedy, query, configs);
	}

	default <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema){
		return columns(runtime, random, greedy,catalog, schema, null);
	}

	default <T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
		return columns(runtime(), null, greedy, catalog, schema, configs);
	}
	default <T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema) {
		return columns(runtime(), null, greedy, catalog, schema, null);
	}
	default <T extends Column> List<T> columns(Catalog catalog, Schema schema, ConfigStore configs) {
		return columns(false, catalog, schema, configs);
	}
	default <T extends Column> List<T> columns(Catalog catalog, Schema schema) {
		return columns(false, catalog, schema, new DefaultConfigStore());
	}
	default <T extends Column> List<T> columns(boolean greedy, ConfigStore configs) {
		return columns(greedy, (Catalog) null, (Schema) null, configs);
	}
	default <T extends Column> List<T> columns(boolean greedy) {
		return columns(greedy, (Catalog) null, (Schema) null);
	}
	default <T extends Column> List<T> columns(ConfigStore configs) {
		return columns(false, (Catalog) null, (Schema) null, configs);
	}

	default <T extends Column> List<T> columns() {
		return columns(false, (Catalog) null, (Schema) null);
	}

	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table, Tag query);
	default <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table) {
		Tag query = new Tag();
		query.setTable(table);
		return tags(runtime, random, greedy, table, query);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table) {
		return tags(runtime(), null, greedy, new Table(table));
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Catalog catalog, Schema schema, String table) {
		return tags(runtime(), null, greedy, new Table(catalog, schema, table));
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table) {
		return tags(runtime(), null, greedy, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(Table table) {
		return tags(runtime(), null, false, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(String table) {
		return tags(false, table);
	}
	default <T extends Tag> LinkedHashMap<String, T> tags(Catalog catalog, Schema schema, String table) {
		return tags(false, catalog, schema, table);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/
	PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table);
	default PrimaryKey primary(boolean greedy, Table table) {
		return primary(runtime(), null, greedy, table);
	}
	default PrimaryKey primary(boolean greedy, String table) {
		return primary(greedy, new Table(table));
	}
	default PrimaryKey primary(boolean greedy, Catalog catalog, Schema schema, String table) {
		return primary(greedy, new Table(catalog, schema, table));
	}
	default PrimaryKey primary(Table table) {
		return primary(false, table);
	}
	default PrimaryKey primary(String table) {
		return primary(false, table);
	}
	default PrimaryKey primary(Catalog catalog, Schema schema, String table) {
		return primary(false, catalog, schema, table);
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
	default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table) {
		return foreigns(runtime(), null, greedy, table);
	}
	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/
	<T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String name);
	default <T extends Index> List<T> indexes(boolean greedy, Table table, String name) {
		return indexes(runtime(), null, greedy, table, name);
	}
	default <T extends Index> List<T> indexes(boolean greedy, String table, String name) {
		return indexes(greedy, new Table(table), name);
	}
	default <T extends Index> List<T> indexes(boolean greedy, Table table) {
		return indexes(greedy, table, null);
	}
	default <T extends Index> List<T> indexes(boolean greedy, String table) {
		return indexes(greedy, new Table(table), null);
	}
	default <T extends Index> List<T> indexes(boolean greedy, Catalog catalog, Schema schema, String table) {
		return indexes(greedy, new Table(catalog, schema, table), null);
	}

	<T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String name);
	default <T extends Index> LinkedHashMap<String, T> indexes(Table table, String name) {
		return indexes(runtime(), null, table, name);
	}
	default <T extends Index> LinkedHashMap<String, T> indexes(String table, String name) {
		return indexes(new Table(table), name);
	}
	default <T extends Index> LinkedHashMap<String, T> indexes(Table table) {
		return indexes(table, null);
	}
	default <T extends Index> LinkedHashMap<String, T> indexes(String table) {
		return indexes(new Table(table), null);
	}
	default <T extends Index> LinkedHashMap<String, T> indexes(Catalog catalog, Schema schema, String table) {
		return indexes(new Table(catalog, schema, table), null);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/
	<T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String name);
	default <T extends Constraint> List<T> constraints(boolean greedy, Table table, String name) {
		return constraints(runtime(), null, greedy, table, name);
	}
	default <T extends Constraint> List<T> constraints(boolean greedy, String table, String name) {
		return constraints(greedy, new Table(table), name);
	}
	default <T extends Constraint> List<T> constraints(boolean greedy, Table table) {
		return constraints(greedy, table, null);
	}
	default <T extends Constraint> List<T> constraints(boolean greedy, String table) {
		return constraints(greedy, new Table(table), null);
	}
	default <T extends Constraint> List<T> constraints(boolean greedy, Catalog catalog, Schema schema, String table) {
		return constraints(greedy, new Table(catalog, schema, table), null);
	}
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String name);
	default <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, String name) {
		return constraints(runtime, random, table, null, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name) {
		return constraints(runtime(), null, table, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Column column, String name) {
		return constraints(runtime(), null, column.getTable(), column, name);
	}
	default <T extends Constraint> LinkedHashMap<String, T>  constraints(String table, String name) {
		return constraints(new Table(table), name);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table) {
		return constraints(table, null);
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(String table) {
		return constraints(new Table(table));
	}
	default <T extends Constraint> LinkedHashMap<String, T> constraints(Catalog catalog, Schema schema, String table) {
		return constraints(new Table(catalog, schema, table));
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/

	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events);
	default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events) {
		return triggers(runtime(), null, greedy, table, events);
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/

	<T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name);
	default <T extends Procedure> List<T> procedures(boolean greedy, Catalog catalog, Schema schema, String name) {
		return procedures(runtime(), null, greedy, catalog, schema, name);
	}

	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name);
	default <T extends Procedure> LinkedHashMap<String, T> procedures(Catalog catalog, Schema schema, String name) {
		return procedures(runtime(), null, catalog, schema, name);
	}
	List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	default List<String> ddl(Procedure procedure) {
		return ddl(runtime(), null, procedure);
	}

	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	<T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name);
	default <T extends Function> List<T> functions(boolean greedy, Catalog catalog, Schema schema, String name) {
		return functions(runtime(), null, greedy, catalog, schema, name);
	}

	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name);
	default <T extends Function> LinkedHashMap<String, T> functions(Catalog catalog, Schema schema, String name) {
		return functions(runtime(), null, catalog, schema, name);
	}

	List<String> ddl(DataRuntime runtime, String random, Function function);
	default List<String> ddl(Function function) {
		return ddl(runtime(), null, function);
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 ******************************************************************************************************************/

	<T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String name);
	default <T extends Sequence> List<T> sequences(boolean greedy, Catalog catalog, Schema schema, String name) {
		return sequences(runtime(), null, greedy, catalog, schema, name);
	}

	<T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String name);
	default <T extends Sequence> LinkedHashMap<String, T> sequences(Catalog catalog, Schema schema, String name) {
		return sequences(runtime(), null, catalog, schema, name);
	}

	List<String> ddl(DataRuntime runtime, String random, Sequence sequence);
	default List<String> ddl(Sequence sequence) {
		return ddl(runtime(), null, sequence);
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
	/* *****************************************************************************************************************
	 * 													sequence
	 ******************************************************************************************************************/
	/**
	 * 函数
	 * @param sequence 序列
	 * @return boolean
	 * @throws Exception 异常 Exception
	 */
	boolean create(Sequence sequence) throws Exception;
	boolean alter(Sequence sequence) throws Exception;
	boolean drop(Sequence sequence) throws Exception;
	boolean rename(Sequence origin, String name) throws Exception;

	/* *****************************************************************************************************************
	 *
	 * 													Authorize
	 *
	 * =================================================================================================================
	 * user			: 用户
	 * grant		: 授权
	 * privilege	: 权限
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													role
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Role role) throws Exception
	 * <T extends Role> List<T> roles(Role query) throws Exception
	 * boolean rename(Role origin, Role update) throws Exception
	 * boolean drop(Role role) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 创建角色
	 * @param role 角色
	 * @return boolean
	 */
	boolean create(Role role) throws Exception;

	/**
	 * 查询角色
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	<T extends Role> List<T> roles(DataRuntime runtime, String random, boolean greedy, Role query) throws Exception;
	/**
	 * 查询角色
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	default <T extends Role> List<T> roles(Role query) throws Exception {
		return roles(runtime(), null, false, query);
	}
	/**
	 * 查询角色
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 角色名
	 * @return List
	 */
	default <T extends Role> List<T> roles(Catalog catalog, Schema schema, String pattern) throws Exception{
		Role query = new Role();
		query.setCatalog(catalog);
		query.setSchema(schema);
		query.setName(pattern);
		return roles(query);
	}
	/**
	 * 查询角色
	 * @return List
	 */
	default <T extends Role> List<T> roles() throws Exception {
		return roles(new Role());
	}

	/**
	 * 角色重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	boolean rename(Role origin, Role update) throws Exception;

	/**
	 * 删除角色
	 * @param role 角色
	 * @return boolean
	 */
	boolean drop(Role role) throws Exception;

	/* *****************************************************************************************************************
	 * 													user
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(User user) throws Exception
	 * <T extends Role> List<T> roles(User query) throws Exception
	 * boolean rename(User origin, Role update) throws Exception
	 * boolean drop(User user) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 创建用户
	 * @param user 用户
	 * @return boolean
	 */
	boolean create(User user) throws Exception;

	/**
	 * 创建用户
	 * @param name 用户名
	 * @param password 密码
	 * @return boolean
	 */
	default boolean create(String name, String password) throws Exception {
		return create(new User(name, password));
	}

	/**
	 * 用户重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	boolean rename(User origin, User update) throws Exception;

	/**
	 * 用户重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	default boolean rename(String origin, String update) throws Exception {
		return rename(new User(origin), new User(update));
	}

	/**
	 * 查询用户
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	<T extends User> List<T> users(DataRuntime runtime, String random, boolean greedy, User query) throws Exception;
	/**
	 * 查询用户
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	default List<User> users(User query) throws Exception {
		return users(runtime(), null, false, query);
	}
	/**
	 * 查询用户
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 用户名
	 * @return List
	 */
	default List<User> users(Catalog catalog, Schema schema, String pattern) throws Exception {
		User query = new User();
		query.setCatalog(catalog);
		query.setSchema(schema);
		query.setName(pattern);
		return users(query);
	}
	/**
	 * 查询用户
	 * @return List
	 */
	default List<User> users() throws Exception {
		return users(null, null, null);
	}
	/**
	 * 查询用户
	 * @param pattern 用户名
	 * @return List
	 */
	default List<User> users(String pattern) throws Exception {
		return users(null, null, pattern);
	}
	/**
	 * 删除用户
	 * @param user 用户
	 * @return boolean
	 */
	boolean drop(User user) throws Exception;
	/**
	 * 删除用户
	 * @param user 用户名
	 * @return boolean
	 */
	default boolean drop(String user) throws Exception {
		return drop(new User(user));
	}

	/* *****************************************************************************************************************
	 * 													grant
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean grant(User user, Privilege... privileges) throws Exception
	 * boolean grant(String user, Privilege ... privileges) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 授权
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	boolean grant(User user, Privilege... privileges) throws Exception;
	/**
	 * 授权
	 * @param role 角色
	 * @param privileges 权限
	 * @return boolean
	 */
	boolean grant(Role role, Privilege... privileges) throws Exception;
	/**
	 * 授权
	 * @param user 用户
	 * @param roles 角色
	 * @return boolean
	 */
	boolean grant(User user, Role ... roles) throws Exception;
	/**
	 * 授权
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	default boolean grant(String user, Privilege ... privileges) throws Exception {
		return grant(new User(user), privileges);
	}

	/* *****************************************************************************************************************
	 * 													privilege
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Privilege> privileges(Privilege query) throws Exception;
	 * List<Privilege> privileges(User user) throws Exception
	 * List<Privilege> privileges(String user) throws Exception
	 * boolean revoke(User user, Privilege... privileges) throws Exception
	 * boolean revoke(String user, Privilege ... privileges) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询用户权限
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	<T extends Privilege> List<T> privileges(DataRuntime runtime, String random, boolean greedy, Privilege query) throws Exception;
	/**
	 * 查询用户权限
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	default List<Privilege> privileges(Privilege query) throws Exception {
		return privileges(runtime(), null, false, query);
	}
	/**
	 * 查询用户权限
	 * @param user 用户
	 * @return List
	 */
	default List<Privilege> privileges(User user) throws Exception {
		Privilege query = new Privilege();
		query.setUser(user);
		return privileges(query);
	}

	/**
	 * 查询用户权限
	 * @param user 用户
	 * @return List
	 */
	default List<Privilege> privileges(String user) throws Exception {
		return privileges(new User(user));
	}

	/**
	 * 撤销授权
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	boolean revoke(User user, Privilege ... privileges) throws Exception;


	/**
	 * 撤销授权
	 * @param role 角色
	 * @param privileges 权限
	 * @return boolean
	 */
	boolean revoke(Role role, Privilege ... privileges) throws Exception;


	/**
	 * 撤销授权
	 * @param user 用户
	 * @param roles 角色
	 * @return boolean
	 */
	boolean revoke(User user, Role ... roles) throws Exception;

	/**
	 * 撤销授权
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	default boolean revoke(String user, Privilege ... privileges) throws Exception {
		return revoke(new User(user), privileges);
	}
} 
