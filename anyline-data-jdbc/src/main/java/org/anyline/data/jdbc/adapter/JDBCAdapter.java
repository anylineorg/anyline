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


package org.anyline.data.jdbc.adapter;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.metadata.*;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.LinkedHashMap;

public interface JDBCAdapter extends DriverAdapter {
	/**
	 * insert执行后 通过KeyHolder获取主键值赋值给data
	 * @param random log标记
	 * @param data data
	 * @param keyholder  keyholder
	 * @return boolean
	 */
	boolean identity(DataRuntime runtime, String random, Object data, KeyHolder keyholder);
	Column column(DataRuntime runtime, Column column, SqlRowSetMetaData rsm, int index);

	/**
	 * 解析查询结果metadata(0=1)
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns columns
	 * @param set set
	 * @return columns columns
	 * @throws Exception 异常
	 */
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception;
	<T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;

	/**
	 * 解析查询结果metadata(0=1)
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set 查询结果
	 * @return tags
	 * @throws Exception 异常
	 */
	<T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception;
	/**
	 *
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	<T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception;
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception;

	/**
	 * 封装查询结果
	 * @param system 系统表不检测列属性
	 * @param runtime  runtime
	 * @param metadatas metadatas
	 * @param rs jdbc返回结果
	 * @return DataRow
	 */
	DataRow row(boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas, ResultSet rs);

}
