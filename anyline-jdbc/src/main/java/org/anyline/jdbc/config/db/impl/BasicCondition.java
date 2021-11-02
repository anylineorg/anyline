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


package org.anyline.jdbc.config.db.impl; 
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.SQLVariable;
import org.anyline.util.BasicUtil;
 
 
 
/** 
 * 自动生成的参数 
 * @author zh 
 * 
 */ 
public abstract class BasicCondition implements Condition{ 
	protected boolean required = false;									//是否必须
	protected boolean strictRequired = false;							//是否必须 
	protected boolean active = false;									//是否活动(是否拼接到SQL中) 
	protected int variableType = VARIABLE_FLAG_TYPE_NONE;				//变量标记方式 
	protected List<Object> runValues = new ArrayList<Object>();			//运行时参数
	protected List<Object> runOrValues = new ArrayList<Object>();		//运行时参数(or)
	protected Map<String,Object> runValuesMap = new HashMap<String,Object>();//运行时参数
	protected Map<String,Object> runOrValuesMap = new HashMap<String,Object>();//运行时参数(or) 
	protected String join = Condition.CONDITION_JOIN_TYPE_AND;			//连接方式 
	protected ConditionChain container;									//当前条件所处容器 
	protected String id; 
	protected String text;												//静态条件
	protected String test;												//判断条件 
	public Object clone() throws CloneNotSupportedException{ 
		BasicCondition clone = (BasicCondition)super.clone(); 
		if(null != runValues){ 
			List<Object> cRunValues = new ArrayList<Object>(); 
			for(Object obj:runValues){ 
				Object tmp = obj; 
				cRunValues.add(tmp); 
			} 
			clone.runValues = cRunValues; 
		} 
		if(null != container){ 
			clone.container = (ConditionChain)container.clone(); 
		} 
		return clone; 
	} 
	 
	public void init(){ 
	} 
	public void initRunValue(){ 
		if(null == runValues){ 
			runValues = new ArrayList<Object>(); 
		}else{ 
			runValues.clear(); 
		} 
 
	}
	@Override 
	public void setActive(boolean active){ 
		this.active = active; 
	} 
	public boolean isActive(){ 
		return active; 
	} 
	public List<Object> getRunValues(){ 
		return runValues; 
	} 
	 
	public Condition setJoin(String join){ 
		this.join = join; 
		return this; 
	} 
 
	public String getJoin(){ 
		return join; 
	} 
	public ConditionChain getContainer() { 
		return container; 
	} 
	public Condition setContainer(ConditionChain container) { 
		this.container = container; 
		return this; 
	} 
	public boolean hasContainer(){ 
		return (null != container); 
	} 
	public boolean isContainer(){ 
		return (this instanceof ConditionChain); 
	} 
	public String getId(){ 
		return id; 
	} 
	public int getVariableType() { 
		return variableType;
	} 
 
	public void setVariableType(int variableType) { 
		this.variableType = variableType; 
	} 

	public boolean isRequired() {
		return required;
	}

	public boolean isStrictRequired() {
		return strictRequired;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
	public void setStrictRequired(boolean strictRequired) {
		this.strictRequired = strictRequired;
	} 
	/** 
	 * 赋值 
	 * @param variable  variable
	 * @param values  values
	 */ 
	public void setValue(String variable, Object values){ 
	}

	@Override
	public void setTest(String test) {
		this.test = test;
	}
	@Override
	public String getTest() {
		return test;
	}
	@Override
	public Map<String,Object> getRunValuesMap(){
		return runValuesMap;
	}
	public boolean isValid(){
		if(strictRequired && BasicUtil.isEmpty(true, runValues)){
			return false;
		}
		return true;
	}
	public List<SQLVariable> getVariables(){
		return null;
	} 
} 
