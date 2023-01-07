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


package org.anyline.data.prepare.auto;


import org.anyline.entity.Compare;
import org.anyline.data.prepare.RunPrepare;

public interface AutoPrepare extends RunPrepare {
	public RunPrepare init();
 
	/** 
	 * 设置数据源 
	 * @param table 表
	 * @return RunPrepare
	 */ 
	public RunPrepare setDataSource(String table);
	 
	/* ****************************************************************************************** 
	 *  
	 * 										添加条件 
	 *  
	 * *******************************************************************************************/ 
	/** 
	 * 添加查询条件 
	 * @param required  是否必须 
	 * @param strictRequired 是否严格验证 如果缺少严格验证的条件 整个SQL不执行
	 * @param column  列名 
	 * @param value  值 
	 * @param compare  比较方式 
	 * @return RunPrepare
	 */
	public RunPrepare addCondition(boolean required, boolean strictRequired, String column, Object value, Compare compare);
	public RunPrepare addCondition(boolean required, String column, Object value, Compare compare);
 
	/** 
	 * 添加静态文本查询条件 
	 * @param condition codition
	 * @return RunPrepare
	 */ 
	public RunPrepare addCondition(String condition) ;
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
	 
	public void createRunText(StringBuilder builder); 
/* ******************************************* END RunPrepare *********************************************************** */
	/** 
	 * 添加列  
	 * CD 
	 * CD,NM 
	 * @param columns  columns
	 */ 
	public void addColumn(String columns); 
	public String getDataSource(); 
	public void setSchema(String schema) ; 
	public void setTable(String table) ; 
	public String getDistinct();
} 
