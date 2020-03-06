/* 
 * Copyright 2006-2020 www.anyline.org
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
 
import java.util.Collection;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.ConfigStore;
 
public interface AnylineDao{ 
	public DataSet querys(SQL sql, ConfigStore configs, String ... conditions);
	public DataSet querys(SQL sql, String ... conditions);
	public DataSet selects(SQL sql, ConfigStore configs, String ... conditions);
	public DataSet selects(SQL sql, String ... conditions);

	public int count(SQL sql, ConfigStore configs, String ... conditions);
	public int count(SQL sql, String ... conditions);
	
	public boolean exists(SQL sql, ConfigStore configs, String ... conditions);
	public boolean exists(SQL sql, String ... conditions);
	/** 
	 * 更新 
	 * @param	columns  需要更新的列 
	 * @param	dst  表 
	 * @param	data data
	 * @return return
	 */ 
	public int update(String dst, Object data, String ... columns); 
	public int update(Object data, String ... columns); 
	 
	/** 
	 * 添加 
	 * @param data 需要插入的数据 
	 * @param checkParimary   是否需要检查重复主键,默认不检查 
	 * @param columns  需要插入的列 
	 * @param dst 表 
	 * @return return
	 */
	public int insert(String dst, Object data, boolean checkParimary, String ... columns);
	public int insert(Object data, boolean checkParimary, String ... columns); 
	public int insert(String dst, Object data, String ... columns); 
	public int insert(Object data, String ... columns); 
	

	public int batchInsert(String dst, Object data, boolean checkParimary, String ... columns);
	public int batchInsert(Object data, boolean checkParimary, String ... columns);
	public int batchInsert(String dst, Object data, String ... columns);
	public int batchInsert(Object data, String ... columns);
	/** 
	 * 保存(insert|update) 
	 * @param dst  dst
	 * @param data  data
	 * @param checkParimary 是否检查主键 
	 * @param columns  columns
	 * @return return
	 */ 
	public int save(String dst, Object data, boolean checkParimary, String ... columns); 
	public int save(Object data, boolean checkParimary, String ... columns); 
	public int save(String dst, Object data, String ... columns); 
	public int save(Object data, String ... columns); 
 

	public int execute(SQL sql, ConfigStore configs, String ... conditions);
	public int execute(SQL sql, String ... conditions);
	
	
 
 
	/** 
	 * 执行存储过程 
	 * @param procedure  procedure
	 * @return return
	 */ 
	public boolean executeProcedure(Procedure procedure); 
	/** 
	 * 根据存储过程查询 
	 * @param procedure  procedure
	 * @return return
	 */ 
	public DataSet queryProcedure(Procedure procedure);
	public int delete(String dest, DataSet set, String ... columns);
	public int delete(String dest, DataRow row, String ... columns);
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
} 
