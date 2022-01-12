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


package org.anyline.jdbc.config.db.impl; 
import java.util.ArrayList;
import java.util.List;


/** 
 * V3.0 
 */ 

import org.anyline.jdbc.config.db.Procedure;
 
 
 
public class ProcedureImpl  implements Procedure{ 
	private static final long serialVersionUID = -1421673036222025241L;
	private String name;
	private List<ProcedureParam> inputs = new ArrayList<ProcedureParam>();
	private List<ProcedureParam> outputs = new ArrayList<ProcedureParam>();//输出参数，输入输出参数
	private List<Object> result;	//输出参数结果
	private boolean hasReturn = false;
	
	
	public ProcedureImpl(String name){
		this();
		this.name = name;
	}
	public ProcedureImpl(){}
	/**
	 * 添加输入参数
	 * @param value	值 value	值
	 * @param type	类型 type	类型
	 * @return return
	 */
	public Procedure addInput(Object value, Integer type){
		ProcedureParam param = new ProcedureParam();
		param.setType(type);
		param.setValue(value);
		inputs.add(param);
		return this;
	}
	public Procedure addInput(String value){
		return addInput(value, java.sql.Types.VARCHAR);
	}

	/**
	 * 注册输出参数
	 * @param type	类型 type	类型
	 * @return return
	 */
	public Procedure regOutput(Integer type){
		return regOutput(null, type);
	}
	public Procedure regOutput(){
		return regOutput(java.sql.Types.VARCHAR);
	}

	@Override
	public Procedure regOutput(Object value, Integer type) {
		ProcedureParam param = new ProcedureParam();
		param.setValue(value);
		param.setType(type);
		outputs.add(param);
		return this;
	}

	@Override
	public Procedure regOutput(String value) {
		ProcedureParam param = new ProcedureParam();
		param.setValue(value);
		outputs.add(param);
		return this;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Object> getResult() {
		return result;
	}
	public void setResult(List<Object> result) {
		this.result = result;
	}
	public List<Object> getOutput(){
		return result;
	}

	@Override
	public List<ProcedureParam> getInputs() {
		return inputs;
	}

	@Override
	public List<ProcedureParam> getOutputs() {
		return outputs;
	}

	public void regReturn(){
		hasReturn = true;
	}
	public boolean hasReturn(){
		return hasReturn;
	}
}
