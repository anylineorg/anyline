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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.config.http.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.SQL;
import org.anyline.config.db.sql.auto.impl.AutoConditionImpl;
import org.anyline.config.http.Config;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
import org.apache.log4j.Logger;

public class ConfigImpl implements Config{
	protected static Logger LOG = Logger.getLogger(ConfigImpl.class);
	//从request 取值方式
	public static int FETCH_REQUEST_VALUE_TYPE_SINGLE = 1;	//单值
	public static int FETCH_REQUEST_VALUE_TYPE_MULIT  = 2;	//数组

	private String id;				//id
	private String variable;		//变量名(id.variable:request参数名)
	private String key;				//http参数key
	private List<Object> values;	//VALUE
	private int compare;			//比较方式
	private String join;			//拼接方式
	private String className;		//预处理类
	private String methodName;		//预处理方法
	private boolean empty;			//是否值为空
	private boolean require;		//是否必须参数
	
	private boolean isKeyEncrypt;	//是否HTTP参数名经过加密
	private boolean isValueEncrypt;	//是否HTTP参数值经过加密
	
	int fetchValueType = FETCH_REQUEST_VALUE_TYPE_SINGLE;	//从request取值方式 单个||数组
	
	@Override
	public Object clone(){
		ConfigImpl config = new ConfigImpl();
		config.id = this.id;
		config.variable = this.variable;
		config.key = this.key;
		config.compare = this.compare;
		config.join = this.join;
		config.className = this.className;
		config.methodName = this.methodName;
		config.empty = this.empty;
		config.require = this.require;
		config.isKeyEncrypt = this.isKeyEncrypt; 
		config.isValueEncrypt = this.isValueEncrypt;
		List<Object> values = new ArrayList<Object>();
		for(Object value:this.values){
			values.add(value);
		}
		config.values = values;
		return config;
	}
	public ConfigImpl(){}
	public String toString(){
		String str = "";
		str = "ID:"+ id +",KEY:"+key 
				+ ", COMPARE:"+compare
				+ ", REQUIRED:"+require
				+ ", EMPTY:"+empty
				+ ", VALUE:";
		if(null != values){
			for(Object value:values){
				str += " " + value;
			}
		}
		return str;
	}

	/**
	 * 解析配置
	 * 		[+]	SQL参数名	[.SQL变量名]	:	[>=]request参数名		:默认值
	 * 										[request参数名]
	 * 										%request参数名%
	 * 						
	 * @param config
	 * @return
	 */
	public ConfigImpl(String config){
		join = Condition.CONDITION_JOIN_TYPE_AND;
		/*确定id variable require*/
		id = config.substring(0,config.indexOf(":"));
		if(id.startsWith("+")){
			//必须参数
			require = true;
			id = id.substring(1,id.length());
		}
		if(id.contains(".")){
			//XML中自定义参数时,同时指定param.id及变量名
			variable = id.substring(id.indexOf(".")+1,id.length());
			id = id.substring(0,id.indexOf("."));
		}else{
			//默认变量名
			//variable = id;
		}
		
		/*取值配置*/
		String valueConfig = config.substring(config.indexOf(":")+1,config.length());
		valueConfig = parseCompareType(valueConfig);
		//解析预处理类.方法
		valueConfig = parseClassMethod(valueConfig);
		
		key = valueConfig;
		//加密配置
		key = parseEncrypt();
	}
	/**
	 * 参数加密配置 默认不加密
	 * @param config 参数名　参数名是否加密　参数值是否加密
	 * 只设置一项时　默认为设置参数名加密状态
	 * @return
	 */
	public String parseEncrypt(){
		if(null == key){
			return null;
		}
		if(key.endsWith("+") || key.endsWith("-")){
			String paramEncrypt = key.substring(key.length()-2,key.length()-1);
			String valueEncrypt = key.substring(key.length()-1);
			if("+".equals(paramEncrypt)){
				isKeyEncrypt = true;
			}
			if("+".equals(valueEncrypt)){
				isValueEncrypt = true;
			}
			key = key.replace("+", "").replace("-", "");
		}
		return key;
	}

	/**
	 * 赋值
	 * @param request
	 */
	public void setValue(HttpServletRequest request){
		if(null == values){
			values = new ArrayList<Object>();
		}
		try{
			String def = null;//默认值
			if(key.contains(":")){
				String keys[] = key.split(":");
				key = keys[0];
				def = keys[1];
			}
			if(null != className && null != methodName){
				Class clazz = Class.forName(className);
				Method method = clazz.getMethod(methodName, String.class);
				if(ConfigImpl.FETCH_REQUEST_VALUE_TYPE_SINGLE == fetchValueType){
					Object v = WebUtil.getHttpRequestParam(request,key,isKeyEncrypt, isValueEncrypt);
					v = method.invoke(clazz, v);
					values.add(v);
				}else{
					List<Object> vs = WebUtil.getHttpRequestParams(request, key,isKeyEncrypt, isValueEncrypt);
					for(Object v:vs){
						v = method.invoke(clazz, v);
						values.add(v);
					}
				}		
			}else{
				if(ConfigImpl.FETCH_REQUEST_VALUE_TYPE_SINGLE == fetchValueType){
					Object v = WebUtil.getHttpRequestParam(request,key,isKeyEncrypt, isValueEncrypt);
					values.add(v);
				}else{
					values = WebUtil.getHttpRequestParams(request, key,isKeyEncrypt, isValueEncrypt);
				}
			}
			if(null != def && BasicUtil.isEmpty(true,values)){
				values = new ArrayList<Object>();
				values.add(def);
			}
			empty = BasicUtil.isEmpty(true,values);
		}catch(Exception e){
			LOG.error(e);
		}
	}

	public List<Object> getValues() {
		return values;
	}
	public void addValue(Object value){
		if(null == values){
			values = new ArrayList<Object>();
		}
		if(null != value && value instanceof Collection){
			values.addAll((Collection)value);
		}else{
			values.add(value);
		}
	}
	/**
	 * 解析key 比较方式 及从request取值方式
	 */
	private String parseCompareType(String config){
		if(config.startsWith(">=")){
			compare = SQL.COMPARE_TYPE_GREAT_EQUAL;
			config = config.replace(">=", "");
		}else if(config.startsWith(">")){
			compare = SQL.COMPARE_TYPE_GREAT;
			config = config.replace(">", "");
		}else if(config.startsWith("<=")){
			compare = SQL.COMPARE_TYPE_LITTLE_EQUAL;
			config = config.replace("<=", "");
		}else if(config.startsWith("<>") || config.startsWith("!=")){
			compare = SQL.COMPARE_TYPE_NOT_EQUAL;
			config = config.replace("<>", "").replace("<>", "");
		}else if(config.startsWith("<")){
			compare = SQL.COMPARE_TYPE_LITTLE;
			config = config.replace("<", "");
		}else if(config.startsWith("[")){
			compare = SQL.COMPARE_TYPE_IN;
			config = config.replace("[", "");
			config = config.replace("]", "");
			fetchValueType = FETCH_REQUEST_VALUE_TYPE_MULIT;
		}else if(config.startsWith("%")){
			if(config.endsWith("%")){
				compare = SQL.COMPARE_TYPE_LIKE;
			}else{
				compare = SQL.COMPARE_TYPE_LIKE_SUBFIX;
			}
			config = config.replace("%", "");
		}else if(config.endsWith("%")){
			compare = SQL.COMPARE_TYPE_LIKE_PREFIX;
			config = config.replace("%", "");
		}else{
			compare = SQL.COMPARE_TYPE_EQUAL;
		}
		return config;
	}
	
	/**
	 * 解析预处理类.方法
	 * V2.0只支持一层处理方法
	 * @param config
	 */
	private String parseClassMethod(String config){
		if(config.contains("(")){
			//有预处理方法
			
			//解析class.method
			String classMethod = config.substring(0,config.indexOf("("));
			if(classMethod.contains(".")){
				//有特定类
				className = classMethod.substring(0,classMethod.lastIndexOf("."));
				methodName = classMethod.substring(classMethod.lastIndexOf(".")+1,classMethod.length());
			}else{
				//默认类
				methodName = classMethod;
			}
			config = config.substring(config.indexOf("(")+1,config.indexOf(")"));
		}
		return config;
	}

	/**
	 * 
	 * @param chain 容器
	 * @return
	 */
	public Condition createAutoCondition(ConditionChain chain){
		Condition condition = null;
		if(isRequire() || !isEmpty()){
			condition = new AutoConditionImpl(this).setJoin(join);
			condition.setContainer(chain);
		}
		return condition;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}



	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getCompare() {
		return compare;
	}

	public void setCompare(int compare) {
		this.compare = compare;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public boolean isRequire() {
		return require;
	}

	public void setRequire(boolean require) {
		this.require = require;
	}

	public String getJoin() {
		return join;
	}

	public void setJoin(String join) {
		this.join = join;
	}

	public boolean isKeyEncrypt() {
		return isKeyEncrypt;
	}

	public boolean isValueEncrypt() {
		return isValueEncrypt;
	}
}