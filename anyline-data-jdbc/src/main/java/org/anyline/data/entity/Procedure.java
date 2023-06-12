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


package org.anyline.data.entity;

import org.anyline.entity.PageNavi;
import org.anyline.entity.data.Parameter;

import java.util.ArrayList;
import java.util.List;
 
 
 
public class Procedure implements org.anyline.entity.data.Procedure {
	private static final long serialVersionUID = -1421673036222025241L;
	private String name;
	private List<Parameter> inputs = new ArrayList<Parameter>();
	private List<Parameter> outputs = new ArrayList<Parameter>();//输出参数,输入输出参数
	private List<Object> result;	// 输出参数结果
	private boolean hasReturn = false;
	private PageNavi navi;
	
	
	public Procedure(String name){
		this();
		this.name = name;
	}
	public Procedure(){}

	@Override
	public String getDefinition() {
		return null;
	}

	@Override
	public void setDefinition(String definition) {

	}

	@Override
	public void addInput(Parameter... params) {

	}

	@Override
	public void addOutput(Parameter... params) {

	}

	/**
	 * 添加输入参数
	 * @param value	值 value	值
	 * @param type	类型 type	类型
	 * @return Procedure
	 */
	public Procedure addInput(Object value, Integer type){
		Parameter param = new Parameter();
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
	 * @return Procedure
	 */
	public Procedure regOutput(Integer type){
		return regOutput(null, type);
	}
	public Procedure regOutput(){
		return regOutput(java.sql.Types.VARCHAR);
	}

	@Override
	public Procedure regOutput(Object value, Integer type) {
		Parameter param = new Parameter();
		param.setValue(value);
		param.setType(type);
		outputs.add(param);
		return this;
	}

	@Override
	public Procedure regOutput(String value) {
		Parameter param = new Parameter();
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
	public List<Parameter> getInputs() {
		return inputs;
	}

	@Override
	public List<Parameter> getOutputs() {
		return outputs;
	}

	public void regReturn(){
		hasReturn = true;
	}
	public boolean hasReturn(){
		return hasReturn;
	}

	@Override
	public PageNavi getNavi() {
		return navi;
	}

	@Override
	public Procedure setNavi(PageNavi navi) {
		this.navi = navi;
		return this;
	}
}
