package org.anyline.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.OrderStore;
import org.anyline.config.db.SQL;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigChain;
import org.anyline.config.http.ConfigStore;
import org.anyline.config.http.impl.ConfigImpl;
import org.anyline.entity.PageNavi;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.anyline.util.MD5Util;
import org.anyline.util.WebUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

public class ConfigParser {
	public static ParseResult parse(String config, boolean isKey) {
		ParseResult result = parseInit(config);
		result = parseCompare(result, isKey);
		result = parseClassMethod(result);
		result = parseDef(result);
		result = parseEncrypt(result);
		return result;
	}
	/**
	 * 
	 * @param config +id.field:key | key
	 * 	             +date.dateFr:dateFr | date
	 * @return
	 */
	private static ParseResult parseInit(String config){
		ParseResult result = new ParseResult();
		boolean required = false;
		boolean strictRequired = false;
		String field = null;
		String id = config;
		String key = config;
		if(key.contains(":")){
			id = config.substring(0,config.indexOf(":"));
			key = config.substring(config.indexOf(":")+1,config.length());
		}
		if(id.startsWith("+")){
			//必须参数
			required = true;
			id = id.substring(1,id.length());
			if(ConfigTable.getBoolean("CONDITION_VALUE_STRICT")){
				strictRequired = true;
			}
		}
		if(id.startsWith("+")){
			//必须参数
			strictRequired = true;
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
		result.setId(id);
		result.setRequired(required);
		result.setStrictRequired(strictRequired);
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
		String regx = "^[a-z]+[0-9a-zA-Z_]*(\\.[a-z]+[0-9a-zA-Z_]*)*\\.[A-Z]+[0-9a-zA-Z_]*\\.[a-z]+\\S+\\(\\S+\\)$";
		if(RegularUtil.match(config, regx, Regular.MATCH_MODE.MATCH)){
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
	 * @param isKey true:parseConfig参数 false:query参数
	 * @return
	 */
	private static ParseResult parseCompare(ParseResult result, boolean isKey){
		String config = result.getKey();
		if (config.startsWith(">=")) {
			result.setCompare(SQL.COMPARE_TYPE.GREAT_EQUAL);
			config = config.substring(2, config.length());
		} else if (config.startsWith(">")) {
			result.setCompare(SQL.COMPARE_TYPE.GREAT);
			config = config.substring(1, config.length());
		} else if (config.startsWith("<=")) {
			result.setCompare(SQL.COMPARE_TYPE.LESS_EQUAL);
			config = config.substring(2, config.length());
		} else if (config.startsWith("<>") || config.startsWith("!=")) {
			result.setCompare(SQL.COMPARE_TYPE.NOT_EQUAL);
			config = config.substring(2, config.length());
		} else if (config.startsWith("<")) {
			result.setCompare(SQL.COMPARE_TYPE.LESS);
			config = config.substring(1, config.length());
		} else if (config.startsWith("[") && config.endsWith("]")) {
			//[1,2,3]或[1,2,3]:[1,2,3]
			//id:[id:cd:{[1,2,3]}]
			result.setCompare(SQL.COMPARE_TYPE.IN);
			result.setParamFetchType(ParseResult.FETCH_REQUEST_VALUE_TYPE_MULIT);
			if(isKey){
				config = config.substring(1,config.length()-1);
			}
			
		} else if (config.startsWith("%")) {
			if (config.endsWith("%")) {
				result.setCompare(SQL.COMPARE_TYPE.LIKE);
				config = config.substring(1, config.length()-1);
			} else {
				result.setCompare(SQL.COMPARE_TYPE.LIKE_SUBFIX);
				config = config.substring(1, config.length());
			}
		} else if (config.endsWith("%")) {
			result.setCompare(SQL.COMPARE_TYPE.LIKE_PREFIX);
			config = config.substring(0, config.length()-1);
		} else {
			result.setCompare(SQL.COMPARE_TYPE.EQUAL);
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
		if(key.contains(":") && !DateUtil.isDate(key)){
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
	public static Object getValue(HttpServletRequest request, ParseResult parser){
		List<Object> values = getValues(request, parser);
		if(null != values && values.size()>0){
			return values.get(0);
		}
		return null;
	}
	public static List<Object> getValues(HttpServletRequest request, ParseResult parser){
		List<Object> values = new ArrayList<Object>();
		try{
			String key = parser.getKey();
			//String def = parser.getDef();
			String className = parser.getClazz();
			String methodName = parser.getMethod();
			//int fetchValueType = parser.getParamFetchType();
			int fetchValueType = ConfigImpl.FETCH_REQUEST_VALUE_TYPE_MULIT;
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
					if(ParseResult.FETCH_REQUEST_VALUE_TYPE_MULIT == parser.getParamFetchType()){
						if(key.startsWith("[") && key.endsWith("]")){
							key = key.substring(1, key.length()-1);
						}
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
					if(Config.FETCH_REQUEST_VALUE_TYPE_SINGLE == parser.getParamFetchType()){//单值
						Object v = WebUtil.getHttpRequestParam(request,key,def.isKeyEncrypt(), def.isValueEncrypt());
						if(BasicUtil.isNotEmpty(v)){
							result.add(v);
						}
					}else{//多值
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
				if(value.startsWith("[") && value.endsWith("]")){
					value = value.substring(1, value.length()-1);
				}
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
	/**
	 * 默认值
	 * @param parser
	 * @return
	 */
	private static List<Object> getDefValues(ParseResult parser){
		List<Object> result = new ArrayList<Object>();
		List<ParseResult> defs = parser.getDefs();
		if(null != defs){
			for(ParseResult def:defs){
				String key = def.getKey();
				if(ParseResult.FETCH_REQUEST_VALUE_TYPE_MULIT == parser.getParamFetchType()){
					if(key.startsWith("[") && key.endsWith("]")){
						key = key.substring(1, key.length()-1);
					}
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
	/**
	 * 生成SQL签名，用来唯一标签一条SQL
	 * @param page
	 * @param order
	 * @param src
	 * @param store
	 * @param conditions
	 * @return
	 */
	public static String createSQLSign(boolean page, boolean order, String src, ConfigStore store, String ... conditions){
		conditions = BasicUtil.compressionSpace(conditions);
		String result = src+"|";
		if(null != store){
			ConfigChain chain = store.getConfigChain();
			if(null != chain){
				List<Config> configs = chain.getConfigs();
				if(null != configs){
					for(Config config:configs){
						List<Object> values = config.getValues();
						if(null != values){
							result += config.toString() + "|";
						}
					}	
				}
			}
			PageNavi navi = store.getPageNavi();
			if(page && null != navi){
				result += "page=" + navi.getCurPage()+"|first=" + navi.getFirstRow() + "|last="+navi.getLastRow()+"|";
			}
			if(order){
				OrderStore orders = store.getOrders();
				if(null != orders){
					result += orders.getRunText("").toUpperCase() +"|";
				}
			}
		}
		if(null != conditions){
			for(String condition:conditions){
				if(BasicUtil.isNotEmpty(condition)){
					if(condition.trim().toUpperCase().startsWith("ORDER")){
						if(order){
							result += condition.toUpperCase() + "|";
						}
					}else{
						result += condition+"|";
					}
				}
			}
		}
		return MD5Util.crypto(result);
	}
}
