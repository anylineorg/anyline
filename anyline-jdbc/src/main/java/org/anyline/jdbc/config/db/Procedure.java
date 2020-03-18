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


package org.anyline.jdbc.config.db; 
/** 
 * V3.0 
 */ 
import org.anyline.jdbc.config.db.impl.ProcedureParam;

import java.io.Serializable;
import java.util.List;
 
 
 
public interface Procedure extends Serializable{ 
	/** 
	 * 添加输入参数 
	 * @param value	值  value	值
	 * @param type	类型  type	类型
	 * @return return
	 */ 
	public Procedure addInput(Object value, Integer type);
	public Procedure addInput(String value);

	public List<ProcedureParam> getInputs();
	public List<ProcedureParam> getOutputs() ;

	/**
	 * 注册输出参数
	 * @param type	类型  type	类型
	 * @return return
	 */
	public Procedure regOutput(Integer type);
	public Procedure regOutput();

	/**
	 * 针对输入输出参数
	 * @param value 输入值
	 * @param type 输出类型
	 * @return Procedure
	 */
	public Procedure regOutput(Object value,Integer type);
	public Procedure regOutput(String value);
	public String getName() ; 
	public void setName(String name) ;
	public void setResult(List<Object> result);
	public List<Object> getResult();

} 
