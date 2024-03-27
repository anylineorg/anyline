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


package org.anyline.data.jdbc.adapter;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.metadata.*;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.LinkedHashMap;

public interface JDBCAdapter extends DriverAdapter {

	<T extends BaseMetadata> void checkSchema(DataRuntime runtime, DataSource dataSource, T meta);
	<T extends BaseMetadata> void checkSchema(DataRuntime runtime, Connection con, T meta);
	/**
	 * insert[命令执行后]
	 * insert执行后 通过KeyHolder获取主键值赋值给data
	 * @param random log标记
	 * @param data data
	 * @param keyholder  keyholder
	 * @return boolean
	 */
	boolean identity(DataRuntime runtime, String random, Object data, ConfigStore configs, KeyHolder keyholder);

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
	Column column(DataRuntime runtime, Column column, ResultSetMetaData rsm, int index);


	/**
	 * column[结果集封装]<br/>(方法3)<br/>
	 * 有表名的情况下可用<br/>
	 * 根据jdbc.datasource.connection.DatabaseMetaData获取指定表的列数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param columns columns
	 * @param dbmd DatabaseMetaData
	 * @param table 表
	 * @param pattern 列名称通配符
	 * @return LinkedHashMap
	 * @param <T> Column
	 * @throws Exception 异常
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;


	/**
	 * column[结果集封装-子流程](方法3)<br/>
	 * 方法(3)内部遍历
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column column
	 * @param rs ResultSet
	 * @return Column
	 */
	Column column(DataRuntime runtime, Column column, ResultSet rs);


	/**
	 * column[结果集封装]<br/>(方法4)<br/>
	 * 解析查询结果metadata(0=1)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param columns columns
	 * @param table 表
	 * @param set SqlRowSet由spring封装过的结果集ResultSet
	 * @return LinkedHashMap
	 * @param <T> Column
	 * @throws Exception
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception;

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
	Column column(DataRuntime runtime, Column column, SqlRowSetMetaData rsm, int index);

	/**
	 * query[结果集封装-子流程]
	 * 封装查询结果行, 在外层遍历中修改rs下标
	 * @param system 系统表不检测列属性
	 * @param runtime  runtime
	 * @param metadatas metadatas
	 * @param rs jdbc返回结果
	 * @return DataRow
	 */
	DataRow row(boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas, ConfigStore configs, ResultSet rs);

}
