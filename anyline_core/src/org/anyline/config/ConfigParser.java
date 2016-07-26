package org.anyline.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.SQL;
import org.anyline.config.http.impl.ConfigImpl;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;

public class ConfigParser {
	public static ParseResult parse(String config) {
		ParseResult result = parseInit(config);
		result = parseCompare(result);
		result = parseClassMethod(result);
		result = parseDef(result);
		result = parseEncrypt(result);
		return result;
	}
	private static ParseResult parseInit(String config){
		ParseResult result = new ParseResult();
		boolean required = false;
		String field = null;
		String id = config.substring(0,config.indexOf(":"));
		if(id.startsWith("+")){
			//必须参数
			required = true;
			id = id.substring(1,id.length());
		}
		if(id.contains(".")){
			//XML中自定义参数时,同时指定param.id及变量名
			field = id.substring(id.indexOf(".")+1,id.length());
			id = id.substring(0,id.indexOf("."));
		}else{
			//默认变量名
			field = id;
		}
		String key = config.substring(config.indexOf(":")+1,config.length());
		result.setId(id);
		result.setRequired(required);
		result.setField(field);
		result.setKey(key);
		return result;
	}
	
	/**
	 * 解析处理方法
	 * @param result
	 * @return
	 */
	private static ParseResult parseClassMethod(ParseResult result){
		String config = result.getKey();
		String className = null;
		String methodName = null;
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
		result.setClazz(className);
		result.setMethod(methodName);
		result.setKey(config);
		return result;
	}
	/**
	 * 解析 比较方式
	 * @param result
	 * @return
	 */
	private static ParseResult parseCompare(ParseResult result){
		String config = result.getKey();
		if (config.startsWith(">=")) {
			result.setCompare(SQL.COMPARE_TYPE_GREAT_EQUAL);
			config = config.substring(2, config.length());
		} else if (config.startsWith(">")) {
			result.setCompare(SQL.COMPARE_TYPE_GREAT);
			config = config.substring(1, config.length());
		} else if (config.startsWith("<=")) {
			result.setCompare(SQL.COMPARE_TYPE_LITTLE_EQUAL);
			config = config.substring(2, config.length());
		} else if (config.startsWith("<>") || config.startsWith("!=")) {
			result.setCompare(SQL.COMPARE_TYPE_NOT_EQUAL);
			config = config.substring(2, config.length());
		} else if (config.startsWith("<")) {
			result.setCompare(SQL.COMPARE_TYPE_LITTLE);
			config = config.substring(1, config.length());
		} else if (config.startsWith("[") && config.endsWith("]")) {
			//[1,2,3]或[1,2,3:4,5,6]
			result.setCompare(SQL.COMPARE_TYPE_IN);
			result.setParamFetchType(ParseResult.FETCH_REQUEST_VALUE_TYPE_MULIT);
			config = config.substring(1,config.length()-1);
			
		} else if (config.startsWith("%")) {
			if (config.endsWith("%")) {
				result.setCompare(SQL.COMPARE_TYPE_LIKE);
				config = config.substring(1, config.length()-1);
			} else {
				result.setCompare(SQL.COMPARE_TYPE_LIKE_SUBFIX);
				config = config.substring(1, config.length());
			}
		} else if (config.endsWith("%")) {
			result.setCompare(SQL.COMPARE_TYPE_LIKE_PREFIX);
			config = config.substring(0, config.length()-1);
		} else {
			result.setCompare(SQL.COMPARE_TYPE_EQUAL);
		}
		result.setKey(config);
		return result;
	}/**
	 * 解析加密方式
	 * @param result
	 * @return
	 */
	private static ParseResult parseDef(ParseResult result){
		String key = result.getKey();
		if(key.contains(":")){
			String[] tmp = key.split(":");
			result.setKey(tmp[0]);
			int size = tmp.length;
			for(int i=1; i<size; i++){
				if(BasicUtil.isEmpty(tmp[i])){
					continue;
				}
				ParseResult def = new ParseResult();
				def.setKey(tmp[i]);
				def = parseEncrypt(def);
				result.addDef(def);
			}
		}
		return result;
	}
	/**
	 * 解析加密方式
	 * @param result
	 * @return
	 */
	private static ParseResult parseEncrypt(ParseResult result){
		Map<String,Object> map = parseEncrypt(result.getKey());
		result.setKey((String)map.get("SRC"));
		result.setKeyEncrypt((Boolean)map.get("KEY_ENCRYPT"));
		result.setValueEncrypt((Boolean)map.get("VALUE_ENCRYPT"));
		return result;
	}
	private static Map<String,Object> parseEncrypt(String key){
		Map<String,Object> result = new HashMap<String,Object>();
		boolean isKeyEncrypt = false;
		boolean isValueEncrypt = false;
		if(null != key){
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
		}
		result.put("SRC", key);
		result.put("KEY_ENCRYPT", isKeyEncrypt);
		result.put("VALUE_ENCRYPT", isValueEncrypt);
		return result;
	} 
	public static List<Object> getValues(HttpServletRequest request, ParseResult parser){
		List<Object> values = new ArrayList<Object>();
		try{
			String key = parser.getKey();
			//String def = parser.getDef();
			String className = parser.getClazz();
			String methodName = parser.getMethod();
			int fetchValueType = parser.getParamFetchType();
			boolean isKeyEncrypt = parser.isKeyEncrypt();
			boolean isValueEncrypt = parser.isValueEncrypt();
			
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
		}catch(Exception e){
			e.printStackTrace();
		}
		if(BasicUtil.isEmpty(true, values)){
			values = getDefValues(request, parser);
		}
		return values;
	}
	/**
	 * 默认值
	 * @param request
	 * @param parser
	 * @return
	 */
	private static List<Object> getDefValues(HttpServletRequest request, ParseResult parser){
		List<Object> result = new ArrayList<Object>();
		List<ParseResult> defs = parser.getDefs();
		if(null != defs){
			for(ParseResult def:defs){
				String key = def.getKey();
				if(key.startsWith("{") && key.endsWith("}")){
					// col:value
					key = key.substring(1, key.length()-1);
					if(key.startsWith("[") && key.endsWith("]")){
						key = key.substring(1, key.length()-1);
						String tmps[] = key.split(",");
						for(String tmp:tmps){
							if(BasicUtil.isNotEmpty(tmp)){
								result.add(tmp);
							}
						}
					}else{
						if(BasicUtil.isNotEmpty(key)){
							result.add(key);
						}
					}
				}else{
					// col:key
					if(ConfigImpl.FETCH_REQUEST_VALUE_TYPE_SINGLE == parser.getParamFetchType()){
						Object v = WebUtil.getHttpRequestParam(request,key,def.isKeyEncrypt(), def.isValueEncrypt());
						result.add(v);
					}else{
						result = WebUtil.getHttpRequestParams(request, key,def.isKeyEncrypt(), def.isValueEncrypt());
					}
				}
				if(!result.isEmpty()){
					break;
				}
			}
		}
		return result;
	}
	public static List<Object> getValues(ParseResult parser){
		List<Object> result = new ArrayList<Object>();
		String value = parser.getKey();
		if(BasicUtil.isNotEmpty(value)){
			if(ParseResult.FETCH_REQUEST_VALUE_TYPE_MULIT == parser.getParamFetchType()){
				String[] values = value.split(",");
				for(String tmp:values){
					if(BasicUtil.isNotEmpty(tmp)){
						result.add(tmp);
					}
				}
			}else{
				result.add(parser.getKey());
			}
		}
		if(BasicUtil.isEmpty(true, result)){
			result = getDefValues(parser);
		}
		return result;
	}
	private static List<Object> getDefValues(ParseResult parser){
		List<Object> result = new ArrayList<Object>();
		List<ParseResult> defs = parser.getDefs();
		if(null != defs){
			for(ParseResult def:defs){
				String key = def.getKey();
				if(key.startsWith("[") && key.endsWith("]")){
					key = key.substring(1, key.length()-1);
				}
				if(ParseResult.FETCH_REQUEST_VALUE_TYPE_MULIT == parser.getParamFetchType()){
					String tmps[] = key.split(",");
					for(String tmp:tmps){
						if(BasicUtil.isNotEmpty(tmp)){
							result.add(tmp);
						}
					}
				}else{
					if(BasicUtil.isNotEmpty(key)){
						result.add(key);
					}
				}
				if(!result.isEmpty()){
					break;
				}
			}
		}
		return result;
	}
}
