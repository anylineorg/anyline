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

package org.anyline.metadata;

import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 
 
 
public class Procedure extends Metadata<Procedure> implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String keyword = "PROCEDURE"           ;
	private String returnType;
	private List<Parameter> parameters = new ArrayList<>();
	private List<Parameter> inputs = new ArrayList<>();
	private List<Parameter> outputs = new ArrayList<>();//输出参数, 输入输出参数
	private List<Object> result;	// 输出参数结果
	private List<DataSet> results = new ArrayList<>(); //返回多个结果集的存储过程
	private boolean hasReturn = false;
	private PageNavi navi;

	public Procedure(String name) {
		this();
		this.name = name;
	}
	public Procedure() {}

	public Procedure addInput(Parameter... params) {
		if(null != params) {
			for(Parameter parameter:params) {
				inputs.add(parameter);
			}
		}
		return this;
	}
	public Procedure addOutput(Parameter... params) {
		if(null != params) {
			for(Parameter parameter:params) {
				outputs.add(parameter);
			}
		}
		return this;
	}

	public void setInputs(List<Parameter> inputs) {
		this.inputs = inputs;
	}

	public void setOutputs(List<Parameter> outputs) {
		this.outputs = outputs;
	}

	/**
	 * 返回多个结果集的存储过程
	 * @return List
	 */
	public List<DataSet> getResults() {
		return results;
	}

	public void setResults(List<DataSet> results) {
		this.results = results;
	}

	/**
	 * 添加输入参数
	 * @param value	值 value	值
	 * @param type	类型 type	类型
	 * @return Procedure
	 */
	public Procedure addInput(Object value, Integer type) {
		Parameter param = new Parameter();
		param.setType(type);
		param.setValue(value);
		inputs.add(param);
		return this;
	}
	public Procedure addInput(String value) {
		return addInput(value, java.sql.Types.VARCHAR);
	}

	/**
	 * 注册输出参数(调用过程)
	 * @param type	类型 type	类型
	 * @return Procedure
	 */
	public Procedure regOutput(Integer type) {
		return regOutput(null, type);
	}

	/**
	 * 注册输出参数(调用过程)
	 * @return Procedure
	 */
	public Procedure regOutput() {
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
	public List<Object> getResult() {
		if(getmap && null != update) {
			return update.result;
		}
		return result;
	}
	public Procedure setResult(List<Object> result) {
		this.result = result;
		return this;
	}
	public List<Object> getOutput() {
		if(getmap && null != update) {
			return update.result;
		}
		return result;
	}

	public List<Parameter> getInputs() {
		if(getmap && null != update) {
			return update.inputs;
		}
		return inputs;
	}

	public List<Parameter> getOutputs() {
		if(getmap && null != update) {
			return update.outputs;
		}
		return outputs;
	}

	public Procedure regReturn() {
		if(getmap && null != update) {
			return update.regReturn();
		}
		hasReturn = true;
		return this;
	}
	public boolean hasReturn() {
		if(getmap && null != update) {
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
	public String getReturnType() {
		if(getmap && null != update) {
			return update.returnType;
		}
		return returnType;
	}

	public Procedure setReturnType(String returnType) {
		if(setmap && null != update) {
			update.returnType = returnType;
		}
		this.returnType = returnType;
		return this;
	}

	public Procedure clone() {
		Procedure copy = super.clone();

		List<Parameter> parameters = new ArrayList<>();
		for(Parameter parameter:this.parameters) {
			parameters.add(parameter.clone());
		}
		copy.parameters = parameters;

		List<Parameter> inputs = new ArrayList<>();
		for(Parameter parameter:this.inputs) {
			inputs.add(parameter.clone());
		}
		copy.inputs = inputs;

		List<Parameter> outputs = new ArrayList<>();
		for(Parameter parameter:this.outputs) {
			outputs.add(parameter.clone());
		}
		copy.outputs = outputs;

		return copy;
	}
	public String keyword() {
		return this.keyword;
	}

/* ********************************* field refer ********************************** */
    public static final String FIELD_KEYWORD                       = "KEYWORD";
    public static final String FIELD_RETURN_TYPE                   = "RETURN_TYPE";
    public static final String FIELD_PARAMETER                     = "PARAMETER";
    public static final String FIELD_INPUT                         = "INPUT";
    public static final String FIELD_OUTPUT                        = "OUTPUT";
    public static final String FIELD_RESULT                        = "RESULT";
    public static final String FIELD_HAS_RETURN                    = "HAS_RETURN";
    public static final String FIELD_HAS_RETURN_CHECK              = "HAS_RETURN_CHECK";
    public static final String FIELD_HAS_RETURN_CHECK_VALUE        = "HAS_RETURN_CHECK_VALUE";
    public static final String FIELD_NAVI                          = "NAVI";
}