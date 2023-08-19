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


package org.anyline.metadata;

import org.anyline.entity.PageNavi;
import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 
 
 
public class Procedure  implements Serializable {
	private String catalog;
	private String schema;
	private String name;
	private String returnType;
	private List<Parameter> parameters = new ArrayList<Parameter>();
	private List<Parameter> inputs = new ArrayList<Parameter>();
	private List<Parameter> outputs = new ArrayList<Parameter>();//输出参数,输入输出参数
	private List<Object> result;	// 输出参数结果
	private boolean hasReturn = false;
	private PageNavi navi;
	private String definition;
	private List<String> ddls;


	protected Procedure update;
	protected boolean setmap = false              ;  //执行了upate()操作后set操作是否映射到update上(除了catalog, schema,name)
	protected boolean getmap = false              ;  //执行了upate()操作后get操作是否映射到update上(除了catalog, schema,name)


	public Procedure(String name){
		this();
		this.name = name;
	}
	public Procedure(){}


	public String getDefinition() {
		if(getmap && null != update){
			return update.definition;
		}
		return definition;
	}


	public Procedure setDefinition(String definition) {
		this.definition = definition;
		return this;
	}


	public Procedure addInput(Parameter... params) {
		return this;

	}


	public Procedure addOutput(Parameter... params) {
		return this;

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
	 * 注册输出参数(调用过程)
	 * @param type	类型 type	类型
	 * @return Procedure
	 */
	public Procedure regOutput(Integer type){
		return regOutput(null, type);
	}

	/**
	 * 注册输出参数(调用过程)
	 * @return Procedure
	 */
	public Procedure regOutput(){
		return regOutput(java.sql.Types.VARCHAR);
	}

	/**
	 * 注册输出参数(调用过程)
	 * @param value	值
	 * @param type	类型 type	类型
	 * @return Procedure
	 */

	public Procedure regOutput(Object value, Integer type) {
		Parameter param = new Parameter();
		param.setValue(value);
		param.setType(type);
		outputs.add(param);
		return this;
	}


	/**
	 * 注册输出参数(调用过程)
	 * @param value	值
	 * @return Procedure
	 */
	public Procedure regOutput(String value) {
		Parameter param = new Parameter();
		param.setValue(value);
		outputs.add(param);
		return this;
	}
	public String getName() {
		return name;
	}
	public Procedure setName(String name) {
		this.name = name;
		return this;
	}
	public List<Object> getResult() {
		if(getmap && null != update){
			return update.result;
		}
		return result;
	}
	public Procedure setResult(List<Object> result) {
		this.result = result;
		return this;
	}
	public List<Object> getOutput(){
		if(getmap && null != update){
			return update.result;
		}
		return result;
	}


	public List<Parameter> getInputs() {
		if(getmap && null != update){
			return update.inputs;
		}
		return inputs;
	}


	public List<Parameter> getOutputs() {
		if(getmap && null != update){
			return update.outputs;
		}
		return outputs;
	}

	public Procedure regReturn(){
		if(getmap && null != update){
			return update.regReturn();
		}
		hasReturn = true;
		return this;
	}
	public boolean hasReturn(){
		if(getmap && null != update){
			return update.hasReturn;
		}
		return hasReturn;
	}


	public PageNavi getNavi() {
		return navi;
	}


	public Procedure setNavi(PageNavi navi) {
		this.navi = navi;
		return this;
	}
	public String getCatalog() {
		return catalog;
	}


	public Procedure setCatalog(String catalog) {
		this.catalog = catalog;
		return this;
	}


	public String getSchema() {
		return schema;
	}

	public String getReturnType() {
		if(getmap && null != update){
			return update.returnType;
		}
		return returnType;
	}

	public Procedure setReturnType(String returnType) {
		if(setmap && null != update){
			update.returnType = returnType;
		}
		this.returnType = returnType;
		return this;
	}

	public void setDdls(List<String> ddl) {
		this.ddls = ddl;
	}
	public List<String> ddls() {
		return ddls;
	}
	public List<String> getDdls() {
		return ddls;
	}

	public String ddl() {
		if(null != ddls && ddls.size()>0){
			return ddls.get(0);
		}
		return null;
	}
	public String getDdl() {
		if(null != ddls && ddls.size()>0){
			return ddls.get(0);
		}
		return null;
	}
	public Procedure setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	public Procedure update(){
		return update(true, true);
	}
	public Procedure update(boolean setmap, boolean getmap){
		this.setmap = setmap;
		this.getmap = getmap;
		update = clone();
		update.update = null;
		return update;
	}


	public Procedure getUpdate() {
		return update;
	}

	public Procedure setUpdate(Procedure update, boolean setmap, boolean getmap) {
		this.update = update;
		this.setmap = setmap;
		this.getmap = getmap;
		if(null != update) {
			update.update = null;
		}
		return this;
	}

	public Procedure setNewName(String newName){
		return setNewName(newName, true, true);
	}

	public Procedure setNewName(String newName, boolean setmap, boolean getmap) {
		if(null == update){
			update(setmap, getmap);
		}
		update.setName(newName);
		return update;
	}

	public Procedure clone(){
		Procedure copy = new Procedure();
		BeanUtil.copyFieldValue(copy, this);

		List<Parameter> parameters = new ArrayList<>();
		for(Parameter parameter:this.parameters){
			parameters.add(parameter.clone());
		}
		copy.parameters = parameters;

		List<Parameter> inputs = new ArrayList<>();
		for(Parameter parameter:this.inputs){
			inputs.add(parameter.clone());
		}
		copy.inputs = inputs;

		List<Parameter> outputs = new ArrayList<>();
		for(Parameter parameter:this.outputs){
			outputs.add(parameter.clone());
		}
		copy.outputs = outputs;

		copy.update = null;
		copy.setmap = false;
		copy.getmap = false;
		return copy;
	}
}
