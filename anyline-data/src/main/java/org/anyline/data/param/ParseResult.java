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

package org.anyline.data.param;

import org.anyline.data.prepare.Condition;
import org.anyline.entity.Compare;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;
import org.anyline.util.BasicUtil;
import org.omg.CORBA.BAD_CONTEXT;

import java.util.ArrayList;
import java.util.List;

/*
 * id.field:key
 * field:key
 */
public class ParseResult {
	private static final long serialVersionUID = 1L; 
	public static int FETCH_REQUEST_VALUE_TYPE_SINGLE = 1;	// 单值 
	public static int FETCH_REQUEST_VALUE_TYPE_MULTIPLE  = 2;	// 数组 

	private EMPTY_VALUE_SWITCH swt = EMPTY_VALUE_SWITCH.IGNORE			; // 遇到空值处理方式
	private String prefix					; // xml定义中的id 或auto sql的表别名
	private String var						; // 实体属性或表列名
	private String clazz					; // 取值后处理类
	private String method					; // 处理方法
	private List<String> args = new ArrayList<>(); // 参数
	 
	private String key						; // http key
	private boolean isKeyEncrypt			; // key是否加密
	private boolean isValueEncrypt			; // value是否加密
	 
	private boolean setEncrypt = false		; // 是否已指定加密方式
	private String datatype;
 
	private List<ParseResult> defs = new ArrayList<>();	// 默认值
	private ParseResult or = null;	// or 只有value或defs有值时 ors才生效
	private Compare compare = Compare.EQUAL			; // 比较方式
	private Condition.JOIN join = Condition.JOIN.AND			; // 连接方式
	private int paramFetchType = FETCH_REQUEST_VALUE_TYPE_SINGLE	; // request取值方式

	public DataRow map() {
		return map(false);
	}
	public DataRow map(boolean empty) {
		DataRow row = new OriginRow();
		if(empty || BasicUtil.isNotEmpty(prefix)) {
			row.put("prefix", prefix);
		}
		if(empty || BasicUtil.isNotEmpty(var)) {
			row.put("var", var);
		}
		if(empty || BasicUtil.isNotEmpty(clazz)) {
			row.put("class", clazz);
		}
		if(empty || BasicUtil.isNotEmpty(method)) {
			row.put("method", method);
		}
		if(empty || BasicUtil.isNotEmpty(key)) {
			row.put("key", key);
		}
		if(empty || BasicUtil.isNotEmpty(true, defs)) {
			row.put("default", defs);
		}
		if(empty || BasicUtil.isNotEmpty(datatype)) {
			row.put("datatype", datatype);
		}
		row.put("compare", compare.getCode());
		row.put("join", join.getCode());
		row.put("swt", swt);
		return row;
	}
	public static ParseResult build(DataRow row) {
		ParseResult parser = new ParseResult();
		parser.setVar(row.getString("var"));
		DataRow parse = row.getRow("parser");
		if(null != parse) {
			parser.setPrefix(parse.getString("prefix"));
			parser.setVar(parse.getString("var"));
			parser.setClazz(parse.getString("class"));
			parser.setMethod(parse.getString("method"));
			parser.setKey(parse.getString("key"));
			String join = parse.getString("join");
			if(null != join) {
				parser.setJoin(Condition.JOIN.valueOf(join.trim().toUpperCase()));
			}
			parser.setCompare(ConfigBuilder.compare(parse.getInt("compare", Compare.EQUAL.getCode())));
			String swt = parse.getString("swt");
			if(BasicUtil.isNotEmpty(swt)) {
				try {
					parser.setSwt(Compare.EMPTY_VALUE_SWITCH.valueOf(swt));
				}catch (Exception ignored) {}
			}
		}
		return parser;
	}
	public String json() {
		return json(false);
	}
	public String json(boolean empty) {
		return map(empty).json();
	}
	public List<ParseResult> getDefs() {
		return defs; 
	}
	public Object getDefaultValue() {
		for(ParseResult def:defs) {
			Object val = def.getDefaultValue();
			if(null != val){
				return val;
			}
			String key = def.getKey();
			if(BasicUtil.checkEl(key)){
				val = key.substring(2, key.length()-1);
			}
			if(BasicUtil.isNotEmpty(val)){
				return val;
			}
		}
		Object val = null;
		if(BasicUtil.checkEl(key)){
			val = key.substring(2, key.length()-1);
		}
		if(BasicUtil.isNotEmpty(val)){
			return val;
		}
		return null;
	}
	public void addDef(ParseResult def) {
		defs.add(def); 
	} 
	 
	public ParseResult getOr() {
		return or;
	}
	public void setOr(ParseResult or) {
		this.or = or;
	} 
	public Compare getCompare() {
		return compare; 
	} 
	public void setCompare(Compare compare) {
		this.compare = compare; 
	} 
	public String getKey() {
		return key; 
	} 
	public void setKey(String key) {
		if(null != key) {
			key = key.trim();
		}
		this.key = key; 
	} 
	public String getVar() {
		return var;
	} 
	public void setVar(String var) {
		if(null != var) {
			var = var.trim();
		}
		this.var = var;
	} 
	public String getClazz() {
		return clazz; 
	} 
	public void setClazz(String clazz) {
		this.clazz = clazz; 
	} 
	public String getMethod() {
		return method; 
	} 
	public void setMethod(String method) {
		this.method = method; 
	} 
	public int getParamFetchType() {
		return paramFetchType; 
	} 
	public void setParamFetchType(int paramFetchType) {
		this.paramFetchType = paramFetchType; 
	} 
	public boolean isKeyEncrypt() {
		return isKeyEncrypt; 
	} 
	public void setKeyEncrypt(boolean isKeyEncrypt) {
		this.isKeyEncrypt = isKeyEncrypt; 
	} 
	public boolean isValueEncrypt() {
		return isValueEncrypt; 
	} 
	public void setValueEncrypt(boolean isValueEncrypt) {
		this.isValueEncrypt = isValueEncrypt; 
	} 
	public boolean isSetEncrypt() {
		return setEncrypt; 
	} 
	public void setSetEncrypt(boolean setEncrypt) {
		this.setEncrypt = setEncrypt; 
	} 
	public String getPrefix() {
		return prefix;
	} 
	public void setPrefix(String prefix) {
		if(null != prefix) {
			if(prefix.startsWith("++")) {
				setSwt(EMPTY_VALUE_SWITCH.BREAK);
			}else if(prefix.startsWith("+")) {
				setSwt(EMPTY_VALUE_SWITCH.NULL);
			}
			prefix = prefix.replace("+","");
		}
		this.prefix = prefix;
	} 
	public Condition.JOIN getJoin() {
		return join; 
	}
	public void setJoin(Condition.JOIN join) {
		this.join = join;
	}

	public ParseResult addArg(String arg) {
		args.add(arg);
		return this;
	}
	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public EMPTY_VALUE_SWITCH getSwt() {
		return swt;
	}

	public void setSwt(EMPTY_VALUE_SWITCH swt) {
		this.swt = swt;
	}
	public boolean isRequired() {
		if(swt == EMPTY_VALUE_SWITCH.NULL || swt == EMPTY_VALUE_SWITCH.SRC) {
			return true;
		}
		return false;
	}

	public String datatype() {
		return datatype;
	}

	public void datatype(String datatype) {
		this.datatype = datatype;
	}
}
