/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.prepare.auto;

import org.anyline.data.prepare.RunPrepare;
import org.anyline.entity.Compare;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.metadata.Catalog;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;

public interface AutoPrepare extends RunPrepare {
	RunPrepare init();

	/* ****************************************************************************************** 
	 *  
	 * 										添加条件 
	 *  
	 * *******************************************************************************************/ 
	/** 
	 * 添加查询条件 
	 * @param swt 空值处理方式
	 * @param column  列名 
	 * @param value  值 
	 * @param compare  比较方式 
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	RunPrepare addCondition(EMPTY_VALUE_SWITCH swt, Compare compare, String column, Object value);
 
	/** 
	 * 添加静态文本查询条件 
	 * @param condition codition
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */ 
	RunPrepare addCondition(String condition) ;
	 /* ****************************************************************************************** 
	 *  
	 * 										赋值 
	 *  
	 * *******************************************************************************************/ 
	 
	 
	/* ******************************************************************************************* 
	 *  
	 * 										生成 RunPrepare
	 *  
	 * *******************************************************************************************/ 
	 
	void createRunText(StringBuilder builder);
/* ******************************************* END RunPrepare *********************************************************** */
	String getDest();
	RunPrepare setCatalog(Catalog catalog) ;
	RunPrepare setCatalog(String catalog) ;
	RunPrepare setSchema(Schema schema) ;
	RunPrepare setSchema(String schema) ;
	RunPrepare setTable(String table) ;
	RunPrepare setTable(Table table) ;
} 
