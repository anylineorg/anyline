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

import org.anyline.adapter.DataReader;
import org.anyline.adapter.DataWriter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.metadata.type.ColumnType;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BeanUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface DriverAdapter {

	// 内置VALUE
	 enum SQL_BUILD_IN_VALUE{
		CURRENT_TIME("CURRENT_TIME","当前时间");
		private final String code;
		private final String name;
		SQL_BUILD_IN_VALUE(String code, String name){
			this.code = code;
			this.name = name;
		}
		String getCode(){
			return code;
		}
		String getName(){
			return name;
		}
	}
	DatabaseType type();
	String version();


	String TAB 		= "\t"		;
	String BR 		= "\n"		;
	String BR_TAB 	= "\n\t"	;


	/**
	 * 界定符(分隔符)
	 * @return String
	 */
	String getDelimiterFr();
	String getDelimiterTo();

	/**
	 * 对应的兼容模式，有些数据库会兼容oracle或pg,需要分别提供两个JDBCAdapter或者直接依赖oracle/pg的adapter
	 * 参考SQLAdapterUtil定位adapter的方法
	 * @return DatabaseType
	 */
	DatabaseType compatible();

	/**
	 * 转换成相应数据库支持类型
	 * @param type type
	 * @return ColumnType
	 */
	ColumnType type(String type);

	/**
	 * 根据java数据类型定位数据库数据类型
	 * 在不开启自动检测数据类型时会调用
	 * @param support class或ColumnType
	 * @return DataWriter
	 */
	DataWriter writer(Object support);
	//根据Java类型
	DataReader reader(Class clazz);
	//根据数据库数据类型
	DataReader reader(ColumnType type);


	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 * =================================================================================================================
	 * INSERT			: 插入
	 * UPDATE			: 更新
	 * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
	 * EXISTS			: 是否存在
	 * COUNT			: 统计
	 * EXECUTE			: 执行(原生SQL及存储过程)
	 * DELETE			: 删除
	 * COMMON			：其他通用
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													INSERT
	 ******************************************************************************************************************/
	/**
	 * insert [入口]
	 * <br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 数据
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 列
	 * @return 影响行数
	 */
	long insert(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, List<String> columns);
	default long insert(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, String ... columns){
		return insert(runtime, random, dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, Object data, boolean checkPrimary, String ... columns){
		return insert(runtime, random, null, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, String dest, Object data, List<String> columns){
		return insert(runtime, random, dest, data, false, columns);
	}
	default long insert(DataRuntime runtime, String random, String dest, Object data, String ... columns){
		return insert(runtime, random, dest, data, false, BeanUtil.array2list(columns));
	}
	default long insert(DataRuntime runtime, String random, Object data, String ... columns){
		return insert(runtime, random, null, data, false, BeanUtil.array2list(columns));
	}
	default long insert(String dest, Object data, boolean checkPrimary, String ... columns){
		return insert(RuntimeHolder.getRuntime(), null, dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long insert(Object data, boolean checkPrimary, String ... columns){
		return insert(RuntimeHolder.getRuntime(), null,  null, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long insert(String dest, Object data, List<String> columns){
		return insert(RuntimeHolder.getRuntime(), null, dest, data, false, columns);
	}
	default long insert(String dest, Object data, String ... columns){
		return insert(RuntimeHolder.getRuntime(), null, dest, data, false, BeanUtil.array2list(columns));
	}
	default long insert(Object data, String ... columns){
		return insert(RuntimeHolder.getRuntime(), null, null, data, false, BeanUtil.array2list(columns));
	}
	/**
	 * insert [build]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表
	 * @param obj 实体
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildInsertRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, List<String> columns);
	default Run buildInsertRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, String ... columns){
		return buildInsertRun(runtime, dest, obj, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(DataRuntime runtime, Object obj, boolean checkPrimary, String ... columns){
		return buildInsertRun(runtime, null, obj, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(DataRuntime runtime, String dest, Object obj, List<String> columns){
		return buildInsertRun(runtime, dest, obj, false, columns);
	}
	default Run buildInsertRun(DataRuntime runtime, String dest, Object obj, String ... columns){
		return buildInsertRun(runtime, dest, obj, false, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(DataRuntime runtime, Object obj, String ... columns){
		return buildInsertRun(runtime, null, obj, false, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(String dest, Object obj, boolean checkPrimary, List<String> columns){
		return buildInsertRun(RuntimeHolder.getRuntime(), dest, obj, checkPrimary, columns);
	}
	default Run buildInsertRun(String dest, Object obj, boolean checkPrimary, String ... columns){
		return buildInsertRun(RuntimeHolder.getRuntime(), dest, obj, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(Object obj, boolean checkPrimary, String ... columns){
		return buildInsertRun(RuntimeHolder.getRuntime(), null, obj, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(String dest, Object obj, List<String> columns){
		return buildInsertRun(RuntimeHolder.getRuntime(), dest, obj, false, columns);
	}
	default Run buildInsertRun(String dest, Object obj, String ... columns){
		return buildInsertRun(RuntimeHolder.getRuntime(), dest, obj, false, BeanUtil.array2list(columns));
	}
	default Run buildInsertRun(Object obj, String ... columns){
		return buildInsertRun(RuntimeHolder.getRuntime(), null, obj, false, BeanUtil.array2list(columns));
	}
	/**
	 * 填充inset命令内容(根据集合类型)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不指定则根据DataSet解析
	 * @param list 数据集
	 * @param columns 插入的列
	 */
	void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, LinkedHashMap<String, Column> columns);

	/**
	 * 填充inset命令内容(根据集合类型)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest 表 如果不指定则根据DataSet解析
	 * @param set 数据集
	 * @param columns 需要插入的列
	 */
	void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, LinkedHashMap<String, Column> columns);

	/**
	 * 确认需要插入的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param data  Entity或DataRow
	 * @param batch  是否批量
	 * @param columns 提供额外的判断依据<br/>
	 *                列可以加前缀<br/>
	 *                +:表示必须插入<br/>
	 *                -:表示必须不插入<br/>
	 *                ?:根据是否有值<br/>
	 *        如果没有提供columns,长度为0也算没有提供<br/>
	 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
	 *        如果提供了columns则根据columns获取insert列<br/>
	 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
	 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
	 *        则把执行结果与表结构对比,删除表中没有的列
	 * @return List
	 */
	LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object data, List<String> columns, boolean batch);

	/**
	 * 批量插入数据时,多行数据之间分隔符
	 * @return String
	 */
	String batchInsertSeparator ();
	/**
	 * 插入数据时是否支持占位符
	 * @return boolean
	 */
	boolean supportInsertPlaceholder ();
	/**
	 * 自增主键返回标识
	 * @return String
	 */
	String generatedKey();

	/**
	 * insert [执行]
	 * <br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data data
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param pks 需要返回的主键
	 * @return 影响行数
	 */
	long insert(DataRuntime runtime, String random, Object data, Run run, String[] pks);
	/**
	 * insert [执行]
	 * <br/>
	 * 有些不支持返回自增的单独执行<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data data
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param pks pks
	 * @param simple 没有实际作用 用来标识有些不支持返回自增的单独执行
	 * @return 影响行数
	 */
	long insert(DataRuntime runtime, String random, Object data, Run run, String[] pks, boolean simple);

	/**
	 * save [入口]
	 * <br/>
	 * 根据是否有主键值确认insert | update<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 数据
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 列
	 * @return 影响行数
	 */
	long save(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, List<String> columns);
	default long save(DataRuntime runtime, String random, Object data, boolean checkPrimary, List<String> columns){
		return save(runtime, random, null, data, checkPrimary, columns);
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, List<String> columns){
		return save(runtime, random, dest, data, false, columns);
	}
	default long save(DataRuntime runtime, String random, Object data, List<String> columns){
		return save(runtime, random, null, data, false, columns);
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, String ... columns){
		return save(runtime, random, dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long save(DataRuntime runtime, String random, Object data, boolean checkPrimary, String ... columns){
		return save(runtime, random, null, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long save(DataRuntime runtime, String random, String dest, Object data, String ... columns){
		return save(runtime, random, dest, data, false, BeanUtil.array2list(columns));
	}
	default long save(DataRuntime runtime, String random, Object data, String ... columns){
		return save(runtime, random, null, data, false, BeanUtil.array2list(columns));
	}

	default long save(String dest, Object data, boolean checkPrimary, List<String> columns){
		return save(RuntimeHolder.getRuntime(), null, dest, data, checkPrimary, columns);
	}
	default long save(Object data, boolean checkPrimary, List<String> columns){
		return save(RuntimeHolder.getRuntime(), null, null, data, checkPrimary, columns);
	}
	default long save(String dest, Object data, List<String> columns){
		return save(RuntimeHolder.getRuntime(), null, dest, data, false, columns);
	}
	default long save(Object data, List<String> columns){
		return save(RuntimeHolder.getRuntime(), null, null, data, false, columns);
	}
	default long save(String dest, Object data, boolean checkPrimary, String ... columns){
		return save(RuntimeHolder.getRuntime(), null, dest, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long save(Object data, boolean checkPrimary, String ... columns){
		return save(RuntimeHolder.getRuntime(), null, null, data, checkPrimary, BeanUtil.array2list(columns));
	}
	default long save(String dest, Object data, String ... columns){
		return save(RuntimeHolder.getRuntime(), null, dest, data, false, BeanUtil.array2list(columns));
	}
	default long save(Object data, String ... columns){
		return save(RuntimeHolder.getRuntime(), null, null, data, false, BeanUtil.array2list(columns));
	}
	/* *****************************************************************************************************************
	 * 													UPDATE
	 ******************************************************************************************************************/
	/**
	 * UPDATE [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 数据
	 * @param configs 条件
	 * @param columns 列
	 * @return 影响行数
	 */
	long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns);
	default long update(DataRuntime runtime, String random, Object data, ConfigStore configs, List<String> columns){
		return update(runtime, random, null, data, configs, columns);
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, List<String> columns){
		return update(runtime, random, dest, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, Object data, List<String> columns){
		return update(runtime, random, null, data, null, columns);
	}
	default long update(DataRuntime runtime, String random, Object data, ConfigStore configs){
		return update(runtime, random, null, data, configs);
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, String ... columns){
		return update(runtime, random, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(DataRuntime runtime, String random, Object data, ConfigStore configs, String ... columns){
		return update(runtime, random, null, data, configs, BeanUtil.array2list(columns));
	}
	default long update(DataRuntime runtime, String random, String dest, Object data, String ... columns){
		return update(runtime, random, dest, data, BeanUtil.array2string(columns));
	}
	default long update(DataRuntime runtime, String random, Object data, String ... columns){
		return update(runtime, random, null, data, BeanUtil.array2string(columns));
	}
	default long update(String dest, Object data, ConfigStore configs, List<String> columns){
		return update(RuntimeHolder.getRuntime(), null, dest, data, configs, columns);
	}
	default long update(Object data, ConfigStore configs, List<String> columns){
		return update(RuntimeHolder.getRuntime(), null, null, data, configs, columns);
	}
	default long update(String dest, Object data, List<String> columns){
		return update(RuntimeHolder.getRuntime(), null, dest, data, null, columns);
	}
	default long update(Object data, List<String> columns){
		return update(RuntimeHolder.getRuntime(), null, null, data, null, columns);
	}
	default long update(Object data, ConfigStore configs){
		return update(RuntimeHolder.getRuntime(), null, null, data, configs);
	}
	default long update(String dest, Object data, ConfigStore configs, String ... columns){
		return update(RuntimeHolder.getRuntime(), null, dest, data, configs, BeanUtil.array2list(columns));
	}
	default long update(Object data, ConfigStore configs, String ... columns){
		return update(RuntimeHolder.getRuntime(), null, null, data, configs, BeanUtil.array2list(columns));
	}
	default long update(String dest, Object data, String ... columns){
		return update(RuntimeHolder.getRuntime(), null, dest, data, BeanUtil.array2string(columns));
	}
	default long update(Object data, String ... columns){
		return update(RuntimeHolder.getRuntime(), null, null, data, BeanUtil.array2string(columns));
	}
	/**
	 * update [build]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表
	 * @param obj Entity或DtaRow
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要更新的列
	 * @param configs 更新条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns);
	default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns){
		return buildUpdateRun(runtime, null, obj, configs, checkPrimary, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, List<String> columns){
		return buildUpdateRun(runtime, dest, obj, null, checkPrimary, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, boolean checkPrimary, List<String> columns){
		return buildUpdateRun(runtime, null, obj, null, checkPrimary, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns){
		return buildUpdateRun(runtime, dest, obj, configs, false, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, List<String> columns){
		return buildUpdateRun(runtime, null, obj, configs, false, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, List<String> columns){
		return buildUpdateRun(runtime, dest, obj, null, false, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, List<String> columns){
		return buildUpdateRun(runtime, null, obj, null, false, columns);
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, boolean checkPrimary, String ... columns){
		return buildUpdateRun(runtime, dest, obj, configs, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, boolean checkPrimary, String ... columns){
		return buildUpdateRun(runtime, null, obj, configs, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, String ... columns){
		return buildUpdateRun(runtime, dest, obj, null, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, boolean checkPrimary, String ... columns){
		return buildUpdateRun(runtime, null, obj, null, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, String ... columns){
		return buildUpdateRun(runtime, dest, obj, configs, false, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, String ... columns){
		return buildUpdateRun(runtime, null, obj, configs, false, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, String ... columns){
		return buildUpdateRun(runtime, dest, obj, null, false, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(DataRuntime runtime, Object obj, String ... columns){
		return buildUpdateRun(runtime, null, obj, null, false, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), dest, obj, configs, checkPrimary, columns);
	}
	default Run buildUpdateRun(Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), null, obj, configs, checkPrimary, columns);
	}
	default Run buildUpdateRun(String dest, Object obj, boolean checkPrimary, List<String> columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), dest, obj, null, checkPrimary, columns);
	}
	default Run buildUpdateRun(Object obj, boolean checkPrimary, List<String> columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), null, obj, null, checkPrimary, columns);
	}
	default Run buildUpdateRun(String dest, Object obj, ConfigStore configs, List<String> columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), dest, obj, configs, false, columns);
	}
	default Run buildUpdateRun(Object obj, ConfigStore configs, List<String> columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), null, obj, configs, false, columns);
	}
	default Run buildUpdateRun(String dest, Object obj, List<String> columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), dest, obj, null, false, columns);
	}
	default Run buildUpdateRun(Object obj, List<String> columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), null, obj, null, false, columns);
	}
	default Run buildUpdateRun(String dest, Object obj, ConfigStore configs, boolean checkPrimary, String ... columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), dest, obj, configs, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(Object obj, ConfigStore configs, boolean checkPrimary, String ... columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), null, obj, configs, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(String dest, Object obj, boolean checkPrimary, String ... columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), dest, obj, null, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(Object obj, boolean checkPrimary, String ... columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), null, obj, null, checkPrimary, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(String dest, Object obj, ConfigStore configs, String ... columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), dest, obj, configs, false, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(Object obj, ConfigStore configs, String ... columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), null, obj, configs, false, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(String dest, Object obj, String ... columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), dest, obj, null, false, BeanUtil.array2list(columns));
	}
	default Run buildUpdateRun(Object obj, String ... columns){
		return buildUpdateRun(RuntimeHolder.getRuntime(), null, obj, null, false, BeanUtil.array2list(columns));
	}
	/**
	 * 根据实体对象创建 update 最终可执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表
	 * @param obj Entity
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要更新的列
	 * @param configs 更新条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, boolean checkPrimary, LinkedHashMap<String, Column> columns);
	/**
	 * 根据DataRow创建 update 最终可执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表
	 * @param row DtaRow
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 需要更新的列
	 * @param configs 更新条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, boolean checkPrimary, LinkedHashMap<String,Column> columns);

	/**
	 * update [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 数据
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	long update(DataRuntime runtime, String random, String dest, Object data, Run run);
	/* *****************************************************************************************************************
	 * 													QUERY
	 ******************************************************************************************************************/

	/**
	 * select [入口]
	 * <br/>
	 * 返回DataSet中包含元数据信息，如果性能有要求换成maps
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	DataSet querys(DataRuntime runtime, String random,  RunPrepare prepare, ConfigStore configs, String ... conditions);
	/**
	 * select procedure [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure 存储过程
	 * @param navi 分页
	 * @return DataSet
	 */
	DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi);

	/**
	 * select [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param clazz 类
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return EntitySet
	 * @param <T> Entity
	 */
	<T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) ;
	/**
	 * select [入口]
	 * <br/>
	 * 对性能有要求的场景调用，返回java原生map集合,结果中不包含元数据信息
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return maps 返回map集合
	 */
	List<Map<String,Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	/**
	 * 创建 select 最终可执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions);
	/**
	 * 创建 select sequence 最终可执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 存储过程名称s
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names);
	/**
	 * 填充 select 命令内容
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	void fillQueryContent(DataRuntime runtime, Run run);

	/**
	 * 合成最终 select 命令 包含分页 排序
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	String mergeFinalQuery(DataRuntime runtime, Run run);

	/**
	 * 构造 LIKE 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value 有占位符时返回占位值，没有占位符返回null
	 */
	Object createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value);

	/**
	 * 构造 FIND_IN_SET 查询条件
	 * 如果不需要占位符 返回null  否则原样返回value
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return value
	 */
	Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value);
	/**
	 * 构造(NOT) IN 查询条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param value value
	 * @return builder
	 */
	StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value);

	/**
	 * select [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param system 系统表不检测列属性
	 * @param table 表
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return DataSet
	 */
	DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run);

	/**
	 * select [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return maps
	 */
	List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run);
	/**
	 * select [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return map
	 */
	Map<String,Object> map(DataRuntime runtime, String random, Run run);

	/**
	 * JDBC执行完成后的结果处理
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param list JDBC执行返回的结果集
	 * @return  maps
	 */
	List<Map<String,Object>> process(DataRuntime runtime, List<Map<String,Object>> list);
	/**
	 * select [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next 是否查下一个序列值
	 * @param names 存储过程名称s
	 * @return DataRow 保存序列查询结果 以存储过程name作为key
	 */
	DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names);
	/* *****************************************************************************************************************
	 * 													COUNT
	 ******************************************************************************************************************/

	/**
	 * count [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return long
	 */
	long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
	/**
	 * count [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return long
	 */
	long count(DataRuntime runtime, String random, Run run);

 
	/**
	 * 合成最终 select count 命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	String mergeFinalTotal(DataRuntime runtime, Run run);


	/* *****************************************************************************************************************
	 * 													EXISTS
	 ******************************************************************************************************************/

	/**
	 * exists [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return boolean
	 */
	boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);

	/**
	 * 合成最终 exists 命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return String
	 */
	String mergeFinalExists(DataRuntime runtime, Run run);

	/* *****************************************************************************************************************
	 * 													EXECUTE
	 ******************************************************************************************************************/

	/**
	 * execute [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);

	/**
	 * procedure [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @param random  random
	 * @return 影响行数
	 */
	boolean execute(DataRuntime runtime, String random, Procedure procedure);
	/**
	 * 创建执行SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions);

	/**
	 * 填充 execute 命令内容
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	void fillExecuteContent(DataRuntime runtime, Run run);

	/**
	 * execute [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	long execute(DataRuntime runtime, String random, Run run);
	/* *****************************************************************************************************************
	 * 													DELETE
	 ******************************************************************************************************************/

	/**
	 * delete [入口]
	 * <br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param column 列
	 * @param values 列对应的值
	 * @return 影响行数
	 * @param <T> T
	 */
	<T> long deletes(DataRuntime runtime, String random, String table, String column, Collection<T> values);

	/**
	 * delete [入口]
	 * <br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return 影响行数
	 */
	long delete(DataRuntime runtime, String random, String table, Object obj, String... columns);

	/**
	 * delete [入口]
	 * <br/>
	 * 根据configs和conditions过滤条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions);

	/**
	 * truncate [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @return 1表示成功执行
	 */
	int truncate(DataRuntime runtime, String random, String table);
	/**
	 * 构造 delete 命令<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果为空 可以根据obj解析
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildDeleteRun(DataRuntime runtime, String table, Object obj, String ... columns);

	/**
	 * 构造 delete 命令<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param column 列
	 * @param values values
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildDeleteRun(DataRuntime runtime, String table, String column, Object values);

	/**
	 * 构造 delete 命令<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param column 列
	 * @param values values
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildDeleteRunFromTable(DataRuntime runtime, String table, String column, Object values);
	/**
	 * 构造 delete 命令<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果为空 可以根据obj解析
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	Run buildDeleteRunFromEntity(DataRuntime runtime, String table, Object obj, String ... columns);

	/**
	 * 填充 delete 命令内容
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 */
	void fillDeleteRunContent(DataRuntime runtime, Run run);

	/**
	 * 构造 truncate 命令<
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
	 */
	List<Run> buildTruncateRun(DataRuntime runtime, String table);



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

	void checkSchema(DataRuntime runtime, DataSource dataSource, Table table);
	void checkSchema(DataRuntime runtime, Connection con, Table table);
	void checkSchema(DataRuntime runtime, Table table);
	/* *****************************************************************************************************************
	 * 													database
	 ******************************************************************************************************************/

	LinkedHashMap<String, Database> databases(DataRuntime runtime, String random);
	Database database(DataRuntime runtime, String random, String name);
	/**
	 * 查询所有数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return sqls
	 * @throws Exception 异常
	 */
	List<Run> buildQueryDatabaseRun(DataRuntime runtime) throws Exception;
	List<Run> buildQueryDatabaseRun(DataRuntime runtime, String name) throws Exception;
	default List<Run> buildQueryDatabaseRun() throws Exception{
		return buildQueryDatabaseRun(RuntimeHolder.getRuntime());
	}

	/**
	 *  根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param databases 上一步查询结果
	 * @param set set
	 * @return databases
	 * @throws Exception 异常
	 */
	LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set) throws Exception;
	Database database(DataRuntime runtime, int index, boolean create, DataSet set) throws Exception;

	/* *****************************************************************************************************************
	 * 													table
	 ******************************************************************************************************************/
	<T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types);
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, String catalog, String schema, String pattern, String types);

	/**
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	List<Run> buildQueryTableRun(DataRuntime runtime, boolean greedy, String catalog, String schema, String pattern, String types) throws Exception;
	default List<Run> buildQueryTableRun(boolean greedy, String catalog, String schema, String pattern, String types) throws Exception{
		return buildQueryTableRun(RuntimeHolder.getRuntime(), greedy, catalog, schema, pattern, types);
	}

	/**
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	List<Run> buildQueryTableCommentRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception;
	default List<Run> buildQueryTableCommentRun(String catalog, String schema, String pattern, String types) throws Exception{
		return buildQueryTableCommentRun(RuntimeHolder.getRuntime(), catalog, schema, pattern, types);
	}

	/**
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;
	<T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, List<T> tables, DataSet set) throws Exception;
	/**
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception;
	<T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, String catalog, String schema, String pattern, String ... types) throws Exception;


	/**
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;


	List<String> ddl(DataRuntime runtime, String random, Table table, boolean init);
	/**
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return List
	 */
	List<Run> buildQueryDDLRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildQueryDDLRun(Table table) throws Exception{
		return buildQueryDDLRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
	 * @param table 表
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													view
	 ******************************************************************************************************************/

	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types);
	/**
	 * 查询视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return String
	 */
	List<Run> buildQueryViewRun(DataRuntime runtime, boolean greedy, String catalog, String schema, String pattern, String types) throws Exception;
	default List<Run> buildQueryViewRun(boolean greedy, String catalog, String schema, String pattern, String types) throws Exception{
		return buildQueryViewRun(RuntimeHolder.getRuntime(), greedy, catalog, schema, pattern, types);
	}

	/**
	 *  根据查询结果集构造View
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryViewRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param views 上一步查询结果
	 * @param set set
	 * @return views
	 * @throws Exception 异常
	 */
	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> views, DataSet set) throws Exception;

	/**
	 * 根据JDBC补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param views 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return views
	 * @throws Exception 异常
	 */
	<T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, String catalog, String schema, String pattern, String ... types) throws Exception;

	List<String> ddl(DataRuntime runtime, String random, View view);
	/**
	 * 查询viewDDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view view
	 * @return List
	 */
	List<Run> buildQueryDDLRun(DataRuntime runtime, View view) throws Exception;
	default List<Run> buildQueryDDLRun(View view) throws Exception{
		return buildQueryDDLRun(RuntimeHolder.getRuntime(), view);
	}
	/**
	 * 查询 view DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
	 * @param view view
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/
	<T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types);
	/**
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	List<Run> buildQueryMasterTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception;

	default List<Run> buildQueryMasterTableRun(String catalog, String schema, String pattern, String types) throws Exception{
		return buildQueryMasterTableRun(RuntimeHolder.getRuntime(), catalog, schema, pattern, types);
	}

	/**
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryMasterTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;

	/**
	 * 根据JDBC
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception;


	/**
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table MasterTable
	 * @return List
	 */
	List<Run> buildQueryDDLRun(DataRuntime runtime, MasterTable table) throws Exception;
	default List<Run> buildQueryDDLRun(MasterTable table) throws Exception{
		return buildQueryDDLRun(RuntimeHolder.getRuntime(), table);
	}

	List<String> ddl(DataRuntime runtime, String random, MasterTable table);
	/**
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/

	<T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String name);

	List<String> ddl(DataRuntime runtime, String random, PartitionTable table);
	/**
	 * 查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	List<Run> buildQueryPartitionTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception;
	default List<Run> buildQueryPartitionTableRun(String catalog, String schema, String pattern, String types) throws Exception{
	return 	buildQueryPartitionTableRun(RuntimeHolder.getRuntime(), catalog, schema, pattern, types);
	}

	/**
	 * 根据主表查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 主表
	 * @param tags 标签名+标签值
	 * @param name 分区表名
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags, String name) throws Exception;
	default List<Run> buildQueryPartitionTableRun(MasterTable master, Map<String,Object> tags, String name) throws Exception{
		return buildQueryPartitionTableRun(RuntimeHolder.getRuntime(), master, tags, name);
	}
	List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags) throws Exception;
	default List<Run> buildQueryPartitionTableRun(MasterTable master, Map<String,Object> tags) throws Exception{
		return buildQueryPartitionTableRun(RuntimeHolder.getRuntime(), master, tags);
	}

	/**
	 *  根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTableRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;

	/**
	 * 根据JDBC
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @return tables
	 * @throws Exception 异常
	 */
	<T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, MasterTable master) throws Exception;



	/**
	 * 查询 PartitionTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table PartitionTable
	 * @return List
	 */
	List<Run> buildQueryDDLRun(DataRuntime runtime, PartitionTable table) throws Exception;
	default List<Run> buildQueryDDLRun(PartitionTable table) throws Exception{
		return buildQueryDDLRun(RuntimeHolder.getRuntime(), table);
	}
	/**
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
	 * @param table MasterTable
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/


	/**
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 查所有库
	 * @param table 表
	 * @param primary 是否检测主键
	 * @return Column
	 * @param <T>  Column
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary);

	/**
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return sqls
	 */
	List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;

	/**
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;

	/**
	 * 解析JDBC get columns结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @return columns 上一步查询结果
	 * @return pattern attern
	 * @throws Exception 异常
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception;


	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean create, Table table, LinkedHashMap<String, T> columns, List<Run> runs);

	Column column(DataRuntime runtime, Column column, ResultSetMetaData rsm, int index);
	Column column(DataRuntime runtime, Column column, ResultSet rs);


	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table);
	/**
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否需要根据metadata
	 * @return sqls
	 */
	List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;
	default List<Run> buildQueryTagRun(Table table, boolean metadata) throws Exception{
		return buildQueryTagRun(RuntimeHolder.getRuntime(), table, metadata);
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryTagRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags
	 * @throws Exception 异常
	 */
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception;


	/**
	 *
	 * 解析JDBC get columns结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param pattern pattern
	 * @return tags
	 * @throws Exception 异常
	 */
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception;


	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/
	PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table);

	/**
	 * 查询表上的主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sqls
	 */
	List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildQueryPrimaryRun(Table table) throws Exception{
		return buildQueryPrimaryRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set) throws Exception;



	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
	/**
	 * 查询表上的外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sqls
	 */
	List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildQueryForeignsRun(Table table) throws Exception{
		return buildQueryForeignsRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	<T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception;


	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/

	<T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String name);
	/**
	 * 查询表上的索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param name 名称
	 * @return sqls
	 */
	List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, String name);
	default List<Run> buildQueryIndexRun(Table table, String name){
		return buildQueryIndexRun(RuntimeHolder.getRuntime(), table, name);
	}

	/**
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	<T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception;


	/**
	 * 解析JDBC getIndex结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	<T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception;


	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/

	/**
	 * 查询表上的约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param metadata 是否需要根据metadata
	 * @return sqls
	 */
	List<Run> buildQueryConstraintRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;
	default List<Run> buildQueryConstraintRun(Table table, boolean metadata) throws Exception{
		return buildQueryConstraintRun(RuntimeHolder.getRuntime(), table, metadata);
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> constraints, DataSet set) throws Exception;
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception;




	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/

	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events);
	/**
	 * 查询表上的trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param events INSERT|UPDATE|DELETE
	 * @return sqls
	 */
	List<Run> buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events) ;
	default List<Run> buildQueryTriggerRun(Table table, List<Trigger.EVENT> events) {
		return buildQueryTriggerRun(RuntimeHolder.getRuntime(), table, events);
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception;



	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/
	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random,  boolean greedy, String catalog, String schema, String name);
	List<Run> buildQueryProcedureRun(DataRuntime runtime, String catalog, String schema, String name) ;
	default List<Run> buildQueryProcedureRun(String catalog, String schema, String name) {
		return buildQueryProcedureRun(RuntimeHolder.getRuntime(), catalog, schema, name);
	}

	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;


	List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	/**
	 * 查询存储DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return List
	 */
	List<Run> buildQueryDDLRun(DataRuntime runtime, Procedure procedure) throws Exception;
	default List<Run> buildQueryDDLRun(Procedure procedure) throws Exception{
		return buildQueryDDLRun(RuntimeHolder.getRuntime(), procedure);
	}
	/**
	 * 查询 Procedure DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
	 * @param procedure Procedure
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set);
	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, boolean recover, String catalog, String schema, String name);
	List<Run> buildQueryFunctionRun(DataRuntime runtime, String catalog, String schema, String name) ;
	default List<Run> buildQueryFunctionRun(String catalog, String schema, String name) {
		return buildQueryFunctionRun(RuntimeHolder.getRuntime(), catalog, schema, name);
	}

	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;

	List<String> ddl(DataRuntime runtime, String random, Function function);
	/**
	 * 查询函数DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param function 函数
	 * @return List
	 */
	List<Run> buildQueryDDLRun(DataRuntime runtime, Function function) throws Exception;
	default List<Run> buildQueryDDLRun(Function function) throws Exception{
		return buildQueryDDLRun(RuntimeHolder.getRuntime(), function);
	}
	/**
	 * 查询 Function DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDDLRun 返回顺序
	 * @param function Function
	 * @param ddls 上一步查询结果
	 * @param set sql执行的结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set);
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

	/**
	 * 创建表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildCreateRun(Table table) throws Exception{
		return buildCreateRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildAppendCommentRun(Table table) throws Exception{
		return buildAppendCommentRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 修改表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildAlterRun(Table table) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), table);
	}
	/**
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param columns 列
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns) throws Exception;
	default List<Run> buildAlterRun(Table table, Collection<Column> columns) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), table, columns);
	}

	/**
	 * 重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildRenameRun(Table table) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildChangeCommentRun(Table table) throws Exception{
		return buildChangeCommentRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 删除表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, Table table) throws Exception;
	default List<Run> buildDropRun(Table table) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists);


	/**
	 * 创建主键在创建表的DDL结尾部分
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table);

	/**
	 * 单独创建主键
	 * @return String
	 */
	//String primary(Table table);

	/**
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table);

	/**
	 * 构造表名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	StringBuilder name(DataRuntime runtime, StringBuilder builder, Table table);



	/* *****************************************************************************************************************
	 * 													view
	 ******************************************************************************************************************/

	/**
	 * 创建视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, View view) throws Exception;
	default List<Run> buildCreateRun(View view) throws Exception{
		return buildCreateRun(RuntimeHolder.getRuntime(), view);
	}

	/**
	 * 添加视图备注(视图创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, View view) throws Exception;
	default List<Run> buildAppendCommentRun(View view) throws Exception{
		return buildAppendCommentRun(RuntimeHolder.getRuntime(), view);
	}

	/**
	 * 修改视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, View view) throws Exception;
	default List<Run> buildAlterRun(View view) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), view);
	}

	/**
	 * 重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, View view) throws Exception;
	default List<Run> buildRenameRun(View view) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), view);
	}

	/**
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, View view) throws Exception;
	default List<Run> buildChangeCommentRun(View view) throws Exception{
		return buildChangeCommentRun(RuntimeHolder.getRuntime(), view);
	}

	/**
	 * 删除视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, View view) throws Exception;
	default List<Run> buildDropRun(View view) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), view);
	}

	/**
	 * 创建或删除视图之前  检测视图是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists);

	/**
	 * 视图备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param view 视图
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, View view);

	/* *****************************************************************************************************************
	 * 													master table
	 ******************************************************************************************************************/

	/**
	 * 创建主有
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, MasterTable table) throws Exception;
	default List<Run> buildCreateRun(MasterTable table) throws Exception{
		return buildCreateRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table) throws Exception;
	default List<Run> buildAppendCommentRun(MasterTable table) throws Exception{
		return buildAppendCommentRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 修改主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, MasterTable table) throws Exception;
	default List<Run> buildAlterRun(MasterTable table) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 主表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, MasterTable table) throws Exception;
	default List<Run> buildRenameRun(MasterTable table) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 修改主表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table) throws Exception;
	default List<Run> buildChangeCommentRun(MasterTable table) throws Exception{
		return buildChangeCommentRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 删除主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, MasterTable table) throws Exception;
	default List<Run> buildDropRun(MasterTable table) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), table);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 ******************************************************************************************************************/

	/**
	 * 创建分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table) throws Exception;
	default List<Run> buildCreateRun(PartitionTable table) throws Exception{
		return buildCreateRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;
	default List<Run> buildAppendCommentRun(PartitionTable table) throws Exception{
		return buildAppendCommentRun(RuntimeHolder.getRuntime(), table);
	}
	/**
	 * 修改分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception;
	default List<Run> buildAlterRun(PartitionTable table) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 分区表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table) throws Exception;

	default List<Run> buildRenameRun(PartitionTable table) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 修改分区表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;
	default List<Run> buildChangeCommentRun(PartitionTable table) throws Exception{
		return buildChangeCommentRun(RuntimeHolder.getRuntime(), table);
	}

	/**
	 * 删除分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception;
	default List<Run> buildDropRun(PartitionTable table) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), table);
	}


	/* *****************************************************************************************************************
	 * 													column
	 ******************************************************************************************************************/

	/**
	 * 修改表的关键字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return String
	 */
	String alterColumnKeyword(DataRuntime runtime);

	/**
	 * 添加列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice) throws Exception;
	default List<Run> buildAddRun(Column column, boolean slice) throws Exception{
		return buildAddRun(RuntimeHolder.getRuntime(), column, slice);
	}
	List<Run> buildAddRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildAddRun(Column column) throws Exception{
		return buildAddRun(RuntimeHolder.getRuntime(), column);
	}

	/**
	 * 添加列引导
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param column 列
	 * @return String
	 */
	StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 修改列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice) throws Exception;
	default List<Run> buildAlterRun(Column column, boolean slice) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), column, slice);
	}
	List<Run> buildAlterRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildAlterRun(Column column) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), column);
	}

	/**
	 * 删除列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice) throws Exception;
	default List<Run> buildDropRun(Column column, boolean slice) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), column, slice);
	}
	List<Run> buildDropRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildDropRun(Column column) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), column);
	}
	/**
	 * 删除列引导
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param column 列
	 * @return String
	 */
	StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildRenameRun(Column column) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), column);
	}

	/**
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeTypeRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildChangeTypeRun(Column column) throws Exception{
		return buildChangeTypeRun(RuntimeHolder.getRuntime(), column);
	}

	/**
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildChangeDefaultRun(Column column) throws Exception{
		return buildChangeDefaultRun(RuntimeHolder.getRuntime(), column);
	}

	/**
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeNullableRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildChangeNullableRun(Column column) throws Exception{
		return buildChangeNullableRun(RuntimeHolder.getRuntime(), column);
	}

	/**
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildChangeCommentRun(Column column) throws Exception{
		return buildChangeCommentRun(RuntimeHolder.getRuntime(), column);
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildAppendCommentRun(Column column) throws Exception{
		return buildAppendCommentRun(RuntimeHolder.getRuntime(), column);
	}


	/**
	 * 取消自增
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropAutoIncrement(DataRuntime runtime, Column column) throws Exception;
	default List<Run> buildDropAutoIncrement(Column column) throws Exception{
		return buildDropAutoIncrement(RuntimeHolder.getRuntime(), column);
	}

	/**
	 * 定义列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder define(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 数据类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column);
	/**
	 * 列数据类型定义
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @param type 数据类型(已经过转换)
	 * @param isIgnorePrecision 是否忽略长度
	 * @param isIgnoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column, String type, boolean isIgnorePrecision, boolean isIgnoreScale);



	boolean isIgnorePrecision(DataRuntime runtime, Column column);
	boolean isIgnoreScale(DataRuntime runtime, Column column);
	Boolean checkIgnorePrecision(DataRuntime runtime, String datatype);
	Boolean checkIgnoreScale(DataRuntime runtime, String datatype);
	/**
	 * 非空
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 默认值
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column);


	/**
	 * 主键(注意不要跟表定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column column);
	/**
	 * 递增列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 更新行事件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 位置
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * 创建或删除列之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists);


	/* *****************************************************************************************************************
	 * 													tag
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Tag tag) throws Exception;
	default List<Run> buildAddRun(Tag tag) throws Exception{
		return buildAddRun(RuntimeHolder.getRuntime(), tag);
	}

	/**
	 * 修改标签
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Tag tag) throws Exception;
	default List<Run> buildAlterRun(Tag tag) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), tag);
	}

	/**
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Tag tag) throws Exception;
	default List<Run> buildDropRun(Tag tag) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), tag);
	}

	/**
	 * 修改标签名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Tag tag) throws Exception;
	default List<Run> buildRenameRun(Tag tag) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), tag);
	}

	/**
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag) throws Exception;
	default List<Run> buildChangeDefaultRun(Tag tag) throws Exception{
		return buildChangeDefaultRun(RuntimeHolder.getRuntime(), tag);
	}

	/**
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag) throws Exception;
	default List<Run> buildChangeNullableRun(Tag tag) throws Exception{
		return buildChangeNullableRun(RuntimeHolder.getRuntime(), tag);
	}

	/**
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag) throws Exception;
	default List<Run> buildChangeCommentRun(Tag tag) throws Exception{
		return buildChangeCommentRun(RuntimeHolder.getRuntime(), tag);
	}

	/**
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag) throws Exception;
	default List<Run> buildChangeTypeRun(Tag tag) throws Exception{
		return buildChangeTypeRun(RuntimeHolder.getRuntime(), tag);
	}

	/**
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists);


	/* *****************************************************************************************************************
	 * 													primary
	 ******************************************************************************************************************/

	/**
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary) throws Exception;
	default List<Run> buildAddRun(PrimaryKey primary) throws Exception{
		return buildAddRun(RuntimeHolder.getRuntime(), primary);
	}

	/**
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey primary) throws Exception;
	default List<Run> buildAlterRun(PrimaryKey primary) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), primary);
	}

	/**
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary) throws Exception;
	default List<Run> buildDropRun(PrimaryKey primary) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), primary);
	}

	/**
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary) throws Exception;
	default List<Run> buildRenameRun(PrimaryKey primary) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), primary);
	}



	/* *****************************************************************************************************************
	 * 													foreign
	 ******************************************************************************************************************/

	/**
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param foreign 外键
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, ForeignKey foreign) throws Exception;
	default List<Run> buildAddRun(ForeignKey foreign) throws Exception{
		return buildAddRun(RuntimeHolder.getRuntime(), foreign);
	}

	/**
	 * 修改外键
	 * @param foreign 外键
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, ForeignKey foreign) throws Exception;
	default List<Run> buildAlterRun(ForeignKey foreign) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), foreign);
	}

	/**
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param foreign 外键
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, ForeignKey foreign) throws Exception;
	default List<Run> buildDropRun(ForeignKey foreign) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), foreign);
	}

	/**
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param foreign 外键
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, ForeignKey foreign) throws Exception;
	default List<Run> buildRenameRun(ForeignKey foreign) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), foreign);
	}
	/* *****************************************************************************************************************
	 * 													index
	 ******************************************************************************************************************/

	/**
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 索引
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Index index) throws Exception;
	default List<Run> buildAddRun(Index index) throws Exception{
		return buildAddRun(RuntimeHolder.getRuntime(), index);
	}

	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 索引
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Index index) throws Exception;
	default List<Run> buildAlterRun(Index index) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), index);
	}

	/**
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 索引
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Index index) throws Exception;
	default List<Run> buildDropRun(Index index) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), index);
	}

	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 索引
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Index index) throws Exception;
	default List<Run> buildRenameRun(Index index) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), index);
	}

	/**
	 * 索引备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder
	 * @param index
	 */
	void comment(DataRuntime runtime, StringBuilder builder, Index index);
	/* *****************************************************************************************************************
	 * 													constraint
	 ******************************************************************************************************************/

	/**
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param constraint 约束
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Constraint constraint) throws Exception;
	default List<Run> buildAddRun(Constraint constraint) throws Exception{
		return buildAddRun(RuntimeHolder.getRuntime(), constraint);
	}

	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param constraint 约束
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Constraint constraint) throws Exception;
	default List<Run> buildAlterRun(Constraint constraint) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), constraint);
	}

	/**
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param constraint 约束
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Constraint constraint) throws Exception;
	default List<Run> buildDropRun(Constraint constraint) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), constraint);
	}

	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param constraint 约束
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Constraint constraint) throws Exception;
	default List<Run> buildRenameRun(Constraint constraint) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), constraint);
	}


	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/

	/**
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param trigger 触发器
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Trigger trigger) throws Exception;
	default List<Run> buildCreateRun(Trigger trigger) throws Exception{
		return buildCreateRun(RuntimeHolder.getRuntime(), trigger);
	}
	void each(DataRuntime runtime, StringBuilder builder, Trigger trigger);

	/**
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param trigger 触发器
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Trigger trigger) throws Exception;
	default List<Run> buildAlterRun(Trigger trigger) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), trigger);
	}

	/**
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param trigger 触发器
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Trigger trigger) throws Exception;
	default List<Run> buildDropRun(Trigger trigger) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), trigger);
	}

	/**
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param trigger 触发器
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Trigger trigger) throws Exception;
	default List<Run> buildRenameRun(Trigger trigger) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), trigger);
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/
	/**
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Procedure procedure) throws Exception;
	default List<Run> buildCreateRun(Procedure procedure) throws Exception{
		return buildCreateRun(RuntimeHolder.getRuntime(), procedure);
	}

	/**
	 * 生在输入输出参数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param parameter parameter
	 */
	void parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter);
	/**
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Procedure procedure) throws Exception;
	default List<Run> buildAlterRun(Procedure procedure) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), procedure);
	}

	/**
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Procedure procedure) throws Exception;
	default List<Run> buildDropRun(Procedure procedure) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), procedure);
	}

	/**
	 * 修改存储过程名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Procedure procedure) throws Exception;
	default List<Run> buildRenameRun(Procedure procedure) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), procedure);
	}

	StringBuilder name(DataRuntime runtime, StringBuilder builder, Procedure procedure);
	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/

	/**
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param function 函数
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Function function) throws Exception;
	default List<Run> buildCreateRun(Function function) throws Exception{
		return buildCreateRun(RuntimeHolder.getRuntime(), function);
	}

	/**
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param function 函数
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Function function) throws Exception;
	default List<Run> buildAlterRun(Function function) throws Exception{
		return buildAlterRun(RuntimeHolder.getRuntime(), function);
	}

	/**
	 * 删除函数
	 * @param function 函数
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Function function) throws Exception;
	default List<Run> buildDropRun(Function function) throws Exception{
		return buildDropRun(RuntimeHolder.getRuntime(), function);
	}

	/**
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param function 函数
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Function function) throws Exception;
	default List<Run> buildRenameRun(Function function) throws Exception{
		return buildRenameRun(RuntimeHolder.getRuntime(), function);
	}
	StringBuilder name(DataRuntime runtime, StringBuilder builder, Function function);
	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 ******************************************************************************************************************/

	/**
	 * 获取单主键列名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param obj obj
	 * @return String
	 */
	String getPrimaryKey(DataRuntime runtime, Object obj);

	/**
	 * 获取单主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param obj obj
	 * @return Object
	 */
	Object getPrimaryValue(DataRuntime runtime, Object obj);
/*
	*//**
	 * 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog catalog
	 * @param schema schema
	 * @param table 表
	 * @param run  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, String catalog, String schema, String table, RunValue run);
	boolean convert(DataRuntime runtime, Table table, Run run);

	/**
	 * 数据类型转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param columns 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, Map<String, Column> columns, RunValue run);

	/**
	 * 数据类型转换,没有提供column的根据value类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, Column column, RunValue run);
	Object convert(DataRuntime runtime, Column column, Object value);
	Object convert(DataRuntime runtime, ColumnType columnType, Object value);
	/**
	 * 在不检测数据库结构时才生效,否则会被convert代替
	 * 生成value格式 主要确定是否需要单引号  或  类型转换
	 * 有些数据库不提供默认的 隐式转换 需要显示的把String转换成相应的数据类型
	 * 如 TO_DATE('')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param row DataRow 或 Entity
	 * @param key 列名
	 */
	void value(DataRuntime runtime, StringBuilder builder, Object row, String key);

	/**
	 * 根据数据类型生成SQL(如是否需要'',是否需要格式转换函数)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param value value
	 */
	//void format(StringBuilder builder, Object value);

	/**
	 * 从数据库中读取数据,常用的基本类型可以自动转换,不常用的如json/point/polygon/blob等转换成anyline对应的类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param metadata Column 用来定位数据类型
	 * @param value value
	 * @param clazz 目标数据类型(给entity赋值时可以根据class, DataRow赋值时可以指定class，否则按检测metadata类型转换 转换不不了的原样返回)
	 * @return Object
	 */
	Object read(DataRuntime runtime, Column metadata, Object value, Class clazz);

	/**
	 * 通过占位符写入数据库前转换成数据库可接受的Java数据类型<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param metadata Column 用来定位数据类型
	 * @param placeholder 是否占位符
	 * @param value value
	 * @return Object
	 */
	Object write(DataRuntime runtime, Column metadata, Object value, boolean placeholder);
 	/**
	 * 拼接字符串
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param args args
	 * @return String
	 */
	String concat(DataRuntime runtime, String ... args);

	/**
	 * 是否是数字列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isNumberColumn(DataRuntime runtime, Column column);

	/**
	 * 是否是boolean列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isBooleanColumn(DataRuntime runtime, Column column);

	/**
	 * 是否是字符类型
	 * 决定值是否需要加单引号
	 * number boolean 返回false
	 * 其他返回true
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isCharColumn(DataRuntime runtime, Column column);

	/**
	 * 内置函数
	 * 如果需要引号,方法应该一块返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列属性,不同的数据类型解析出来的值可能不一样
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value);
	void addRunValue(DataRuntime runtime, Run run, Compare compare, Column column, Object value);
	/**
	 * 转换成相应数据库的数据类型包含精度
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	//String type(Column column);

	/**
	 * 数据库类型转换成java类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type type
	 * @return String
	 */
	//String type2class(String type);

	/**
	 * 对象名称格式化(大小写转换)，在查询系统表时需要
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name name
	 * @return String
	 */
	String objectName(DataRuntime runtime, String name);
}
