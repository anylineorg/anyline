/* 
 * Copyright 2006-2015 www.anyline.org
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


package org.anyline.config.db.impl;
import java.util.ArrayList;
import java.util.List;


/**
 * V3.0
 */
import org.anyline.config.db.Procedure;



public class ProcedureImpl  implements Procedure{
	private static final long serialVersionUID = -1421673036222025241L;
	private String name;
	private List<Integer> outputTypes;	//输出参数类型
	private List<String> inputValues;
	private List<Integer> inputTypes;
	private List<Object> result;	//执行结果|输入参数
	
	
	public ProcedureImpl(String name){
		this();
		this.name = name;
	}
	public ProcedureImpl(){
		inputValues = new ArrayList<String>();
		inputTypes = new ArrayList<Integer>();
		outputTypes = new ArrayList<Integer>();
	}
	/**
	 * 添加输入参数
	 * @param value	值
	 * @param type	类型
	 * @return
	 */
	public Procedure addInput(String value, Integer type){
		inputValues.add(value);
		inputTypes.add(type);
		return this;
	}
	public Procedure addInput(String value){
		return addInput(value, java.sql.Types.VARCHAR);
	}
	
	public List<String> getInputValues(){
		return inputValues;
	}
	public List<Integer> getInputTypes() {
		return inputTypes;
	}
	
	/**
	 * 注册输出参数
	 * @param type	类型
	 * @return
	 */
	public Procedure regOutput(Integer type){
		outputTypes.add(type);
		return this;
	}
	public Procedure regOutput(){
		return regOutput(java.sql.Types.VARCHAR);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Integer> getOutputTypes() {
		return outputTypes;
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

}
