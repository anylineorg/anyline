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

package org.anyline.data.param.init;

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ParseResult;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultAutoCondition;
import org.anyline.data.prepare.auto.init.DefaultAutoConditionChain;
import org.anyline.entity.Compare;
import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;
import org.anyline.util.BasicUtil;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.util.BeanUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.lang.reflect.Array;
import java.util.*;

public class DefaultConfig implements Config {
	protected static final Log log = LogProxy.get(DefaultConfig.class);
	protected String text					 ; // 静态条件(如原生SQL) 没有参数
	protected List<Object> values			 ; // VALUE
	protected List<Object> orValues			 ; // OR VALUE
	protected String datatype				 ; // 数据类型
	protected boolean empty					 ; // 是否值为空
	protected double index					 ; // 顺序
	protected RunPrepare prepare		     ; // 子查询 exists
	protected ParseResult parser			 ; //
	protected boolean overCondition  = false ; // 覆盖相同var的查询条件
	protected boolean overValue		 = true  ; // 相同查询条件第二次赋值是否覆盖上一次的值，如果不覆盖则追加到集合中
	//protected boolean apart          = false ; // 是否需要跟前面的条件 隔离，前面所有条件加到()中
	protected boolean integrality    = true	 ; // 是否作为一个整体，不可分割，与其他条件合并时以()包围
	public DefaultConfig() {
		this.parser = new ParseResult();
	}
	public DefaultConfig(ParseResult parser) {
		this.parser = parser;
	}
	public String toString() {
		Map<String, Object> map = new HashMap<>();
		Condition.JOIN join = getJoin();
		if(null != join) {
			map.put("join", join);
		}
		if(null != datatype) {
			map.put("datatype", datatype);
		}
		map.put("prefix", this.getPrefix());
		map.put("var", this.getVariable());
		map.put("key", this.getKey());
		map.put("compare", this.getCompare().getCode());
		map.put("values", values);
		return BeanUtil.map2json(map);
	}
	public DataRow map(boolean empty) {
		DataRow row = new OriginRow();
		Condition.JOIN join = getJoin();
		if(null != join) {
			row.put("join", join);
		}
		if(empty || BasicUtil.isNotEmpty(text)) {
			row.put("text", text);
		}
		String key = getKey();
		if(empty || BasicUtil.isNotEmpty(key)) {
			row.put("key", key);
		}
		String var = getVariable();
		if(empty || BasicUtil.isNotEmpty(var)) {
			row.put("var", var);
		}
		row.put("compare", getCompareCode());
		if(Condition.JOIN.OR == join) {
			if(empty || BasicUtil.isNotEmpty(orValues) || BasicUtil.isNotEmpty(values)) {
				if(BasicUtil.isEmpty(orValues)) {
					row.put("values", values);
				}else {
					row.put("values", orValues);
				}
			}
		}else{
			if(empty || BasicUtil.isNotEmpty(values)) {
				row.put("values", values);
			}
		}
		if(null != datatype || empty) {
			row.put("datatype", datatype);
		}
		row.put("over_condition", overCondition);
		row.put("over_value", overValue);
		row.put("parser", parser.map(empty));
		return row;
	}
	public String json() {
		return map().json();
	}
	public String cacheKey() {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("prefix", this.getPrefix());
		map.put("compare", this.getCompare().getCode());
		map.put("values", values);
		return BeanUtil.map2json(map);
	}

	@Override
	public String datatype() {
		return datatype;
	}

	@Override
	public void datatype(String datatype) {
		this.datatype = datatype;
	}

	@Override
	public double index() {
		return index;
	}

	@Override
	public void index(double index) {
		this.index = index;
	}

	public Config prepare(RunPrepare prepare) {
		this.prepare = prepare;
		return this;
	}
	public RunPrepare prepare() {
		return this.prepare;
	}
	/**
	 * 解析配置 
	 * 		[+]	SQL参数名	[.SQL变量名]	:	[&gt;=]request参数名		:默认值 
	 * 										[request参数名] 
	 * 										%request参数名% 
	 * 						 
	 * @param config  config
	 */ 
	public DefaultConfig(String config) {
		parser = ConfigParser.parse(config, true); 
	}
	public void setValue(Map<String,Object> values) {
		try{
			//解析动态column ${column}
			ConfigParser.parseVar(values, parser);
			this.values = ConfigParser.getValues(values, parser);
			empty = BasicUtil.isEmpty(true, this.values); 
			setOrValue(values);
		}catch(Exception e) {
			log.error("set file value exception:", e);
		} 
	} 
	public void setOrValue(Map<String,Object> values) {
		try{
			this.orValues = ConfigParser.getValues(values, parser.getOr());
		}catch(Exception e) {
			log.error("set or value exception:", e);
		} 
	}

	public List<Object> getValues() {
		return values; 
	} 
	public List<Object> getOrValues() {
		return orValues; 
	} 
	@SuppressWarnings({"rawtypes","unchecked" })
	public void addValue(Object value) {
		values = append(values, value);
	}
	public void setValue(Object value) {
		values = new ArrayList<Object>();
		addValue(value);
	} 
	public void setOrValue(Object value) {
		orValues = new ArrayList<Object>();
		addValue(value);
	}
	public void addOrValue(Object value) {
		orValues = append(orValues, value);
	}

	/**
	 * 往values中追加value，如果values为空则新创建
	 * @param values values
	 * @param value Object | Collection | null
	 * @return
	 */
	private List<Object> append(List<Object> values, Object value) {
		if(null == values) {
			values = new ArrayList<>();
		}
		if(null != value) {
			if(value instanceof Collection) {
				Collection list = (Collection) value;
				for(Object item:list) {
					addValue(item);
				}
			}else if(value.getClass().isArray()) {
				int len = Array.getLength(value);
				for(int i=0; i<len; i++) {
					addValue(Array.get(value, i));
				}
			}else{
				values.add(value);
			}
		}else{
			values.add(value);
		}
		return values;
	}

	/** 
	 *  createAutoCondition
	 * @param chain 容器 
	 * @return Condition
	 */ 
	public Condition createAutoCondition(ConditionChain chain) {
		Condition condition = null;
		EMPTY_VALUE_SWITCH swt = parser.getSwt();
		if(!isEmpty() || swt == EMPTY_VALUE_SWITCH.NULL || swt == EMPTY_VALUE_SWITCH.SRC) { //非空 或 IS NULL 或 = ''
			if(this instanceof ConfigChain) {
				condition = new DefaultAutoConditionChain((ConfigChain)this);
				condition.setJoin(this.getJoin());
				condition.datatype(datatype);
				condition.setContainer(chain);
			}else{
				if(null != text) {
					condition = new DefaultAutoCondition(this);
					condition.setRunText(text);
					condition.setContainer(chain);
					condition.setActive(true);
					condition.setVariableType(Condition.VARIABLE_PLACEHOLDER_TYPE_NONE);
				}else {
					condition = new DefaultAutoCondition(this).setOrCompare(getOrCompare()).setJoin(parser.getJoin());
					condition.setContainer(chain);
				}
			} 
		}
		if(null != condition) {
			condition.setSwt(getSwt());
			//condition.apart(apart);
			condition.integrality(integrality);
			if(BasicUtil.isNotEmpty(parser.datatype())) {
				//parse中有可能没有设置
				condition.datatype(parser.datatype());
			}
		}
		return condition;
	} 
	public String getPrefix() {
		return parser.getPrefix();
	} 

	public void setPrefix(String prefix) {
		parser.setPrefix(prefix);
	} 
 
	public String getVariable() {
		return parser.getVar();
	} 
 
	public void setVariable(String variable) {
		parser.setVar(variable);
	} 
 
 
 
	public String getKey() {
		return parser.getKey(); 
	} 
 
	public void setKey(String key) {
		parser.setKey(key); 
	}

	public Compare getCompare() {
		return parser.getCompare();
	}
	public int getCompareCode() {
		Compare compare = parser.getCompare();
		if(null != compare) {
			return compare.getCode();
		}
		return Compare.EQUAL.getCode();
	}

	public void setCompare(Compare compare) {
		parser.setCompare(compare); 
	} 
 
	public boolean isEmpty() {
		EMPTY_VALUE_SWITCH sw = null;
		int compare = 10;
		if(null != parser) {
			sw = parser.getSwt();
			compare = parser.getCompare().getCode();
		}
		if(compare == 92 || compare == 192){
			return null == prepare;
		}
		if(compare == 90 || compare == 91 || compare == 190 || compare == 191) {
			//IS NULL IS EMPTY
			if(BasicUtil.isNotEmpty(parser.getVar())) {
				return false;
			}
		}
		if(sw == EMPTY_VALUE_SWITCH.NULL || sw == EMPTY_VALUE_SWITCH.SRC) {
			return false;
		}
		return BasicUtil.isEmpty(true, this.values) && BasicUtil.isEmpty(text);
	} 
 
	public void setEmpty(boolean empty) {
		this.empty = empty; 
	} 

	public Condition.JOIN getJoin() {
		return parser.getJoin(); 
	}

	@Override
	public void setJoin(Condition.JOIN join) {
		parser.setJoin(join);
	}

	public boolean isKeyEncrypt() {
		return parser.isKeyEncrypt(); 
	} 
 
	public boolean isValueEncrypt() {
		return parser.isValueEncrypt(); 
	}
	@Override
	public Compare getOrCompare() {
		ParseResult or = parser.getOr();
		if(null != or) {
			return or.getCompare();
		}
		return parser.getCompare();
	}
	@Override
	public void setOrCompare(Compare compare) {
		ParseResult or = parser.getOr();
		if(null != or) {
			or.setCompare(compare);
		}
	}
	public void setTable(String table) {

	}
	public String getTable() {
		return parser.getPrefix();
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public boolean isOverCondition() {
		return overCondition;
	}

	@Override
	public void setOverCondition(boolean overCondition) {
		this.overCondition = overCondition;
	}

	@Override
	public boolean isOverValue() {
		return overValue;
	}

	@Override
	public void setOverValue(boolean overValue) {
		this.overValue = overValue;
	}

	@Override
	public EMPTY_VALUE_SWITCH getSwt() {
		return parser.getSwt();
	}

	@Override
	public void setSwt(EMPTY_VALUE_SWITCH swt) {
		this.parser.setSwt(swt);
	}

/*
	@Override
	public boolean apart() {
		return apart;
	}

	@Override
	public void apart(boolean apart) {
		this.apart = apart;
	}
*/

	@Override
	public boolean integrality() {
		return integrality;
	}

	@Override
	public void integrality(boolean integrality) {
		this.integrality = integrality;
	}
	@Override
	public Config clone() {
		DefaultConfig clone = null;
		try{
			clone = (DefaultConfig) super.clone();
		}catch (Exception ignored) {
			clone = new DefaultConfig();
		}
		clone.parser = this.parser;
		clone.empty = this.empty;
		List<Object> values = new ArrayList<>();
		if(null != this.values) {
			for (Object value : this.values) {
				values.add(value);
			}
		}
		clone.values = values;
		return clone;
	}
}
