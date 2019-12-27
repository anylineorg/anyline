package org.anyline.jdbc.config; 
 
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.Config;
import org.anyline.jdbc.config.ConfigChain;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DESKey;
import org.anyline.util.DESUtil;
import org.anyline.util.DateUtil;
import org.anyline.util.MD5Util;
import org.anyline.util.XssUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class ConfigParser { 
	static final Logger log = LoggerFactory.getLogger(ConfigParser.class); 
	public static final String PARAMS_PART_DECRYPT_MAP = "PARAMS_PART_DECRYPT_MAP"; // 参数值解密后MAP(逐个加密) 
	public static final String IS_PARAMS_DECRYPT = "IS_PARAMS_DECRYPT"; // 参数值是否已解密 
	private static Map<String, DESKey> deskeys = null; 
	private static DESKey defaultDesKey = null; 
	private static final int MAX_DES_VERSION_INDEX = 12; // 密文中插入版本号最大位置 
	private static final int DES_VERSION_LENGTH = 3; 
	private static final String ENCRYPT_TYPE_PARAM = "param"; 
	private static final String ENCRYPT_TYPE_KEY = "name"; 
	private static final String ENCRYPT_TYPE_VALUE = "value"; 
	static { 
		deskeys = new HashMap<String, DESKey>(); 
		try {
			String keyPath = ConfigTable.get("DES_KEY_FILE");
			if(BasicUtil.isNotEmpty(keyPath)){ 
				File keyFile = new File(ConfigTable.getWebRoot(), keyPath); 
				if (keyFile.exists()) { 
					SAXReader reader = new SAXReader(); 
					Document document = reader.read(keyFile); 
					Element root = document.getRootElement(); 
					for (Iterator<Element> itrKey = root.elementIterator(); itrKey.hasNext();) { 
						Element element = itrKey.next(); 
						DESKey key = new DESKey(); 
						String version = element.attributeValue("version"); 
						key.setVersion(version); 
						key.setKey(element.elementTextTrim("des-key")); 
						key.setKeyParam(element.elementTextTrim("des-key-param")); 
						key.setKeyParamName(element.elementTextTrim("des-key-param-name")); 
						key.setKeyParamValue(element.elementTextTrim("des-key-param-value")); 
						key.setPrefix(element.elementTextTrim("des-prefix")); 
						key.setPrefixParam(element.elementTextTrim("des-prefix-param")); 
						key.setPrefixParamName(element.elementTextTrim("des-prefix-param-name")); 
						key.setPrefixParamValue(element.elementTextTrim("des-prefix-param-value")); 
						if (null == defaultDesKey) { 
							defaultDesKey = key; 
						} else { 
							deskeys.put(version, key); 
						} 
					} 
				}
			} 
			if (null == defaultDesKey) { 
				defaultDesKey = new DESKey(); 
				String version = "vic"; 
				defaultDesKey.setVersion(version); 
				defaultDesKey.setKey("5*(YHU*6d9"); 
				defaultDesKey.setKeyParam("@#$%0(*7#"); 
				defaultDesKey.setKeyParamName("@#$%#"); 
				defaultDesKey.setKeyParamValue("@#23$%097#"); 
				defaultDesKey.setPrefix(""); 
				defaultDesKey.setPrefixParam("als7n6e9o1r5gv78ac1vice624c623f"); 
				defaultDesKey.setPrefixParamName("l80j0sa9n2y1l4i7n6e9o1r5gk"); 
				defaultDesKey.setPrefixParamValue("p298pn6e9o1r5gv"); 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 

	public static ParseResult parse(String config, boolean isKey) { 
		ParseResult result = parseInit(config); 
		result = parseCompare(result, isKey); 
		result = parseClassMethod(result); 
		result = parseOr(result); 
		result = parseDef(result); 
		result = parseEncrypt(result); 
		return result; 
	} 
	/** 
	 *  
	 * @param config +id.field:key | key 
	 * 	             +date.dateFr:dateFr | date 
	 * @return return
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
			if(key.contains("|")){
				String[] tmp = key.split("\\|"); 
				ParseResult or = new ParseResult(); 
				or.setKey(tmp[1]);
				or = parseEncrypt(or); 
				result.setOr(or); 
				key = config.substring(config.indexOf(":")+1,config.indexOf("|")); 
			}else{
				key = config.substring(config.indexOf(":")+1,config.length()); 
			}
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
	 * @param result  result
	 * @return return
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
	 * @param result  result
	 * @param isKey true:parseConfig参数 false:query参数 
	 * @return return
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
	 * 解析默认值 
	 * @param result  result
	 * @return return
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
	private static ParseResult parseOr(ParseResult result){ 
		String key = result.getKey(); 
		if(key.indexOf("|") != -1){ 
			String[] tmp = key.split("\\|"); 
			ParseResult or = new ParseResult(); 
			or.setKey(tmp[1]);
			or = parseEncrypt(or); 
			result.setOr(or); 
		}
		return result; 
	}
	/** 
	 * 解析加密方式 
	 * @param result  result
	 * @return return
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
	public static Object getValue(Map<String,Object> values, ParseResult parser){ 
		List<Object> list = getValues(values, parser); 
		if(null != list && list.size()>0){ 
			return list.get(0); 
		} 
		return null; 
	} 
	public static List<Object> getValues(Map<String,Object> values, ParseResult parser){ 
		List<Object> list = new ArrayList<Object>(); 
		if(null == parser){
			return list;
		}
		try{ 
			String key = parser.getKey(); 
			//String def = parser.getDef(); 
			String className = parser.getClazz(); 
			String methodName = parser.getMethod(); 
			//int fetchValueType = parser.getParamFetchType(); 
			int fetchValueType = Config.FETCH_REQUEST_VALUE_TYPE_MULIT; 
			boolean isKeyEncrypt = parser.isKeyEncrypt(); 
			boolean isValueEncrypt = parser.isValueEncrypt(); 
			 
			if(null != className && null != methodName){ 
				Class clazz = Class.forName(className); 
				Method method = clazz.getMethod(methodName, String.class); 
				if(Config.FETCH_REQUEST_VALUE_TYPE_SINGLE == fetchValueType){ 
					Object v = getRuntimeValue(values,key,isKeyEncrypt, isValueEncrypt); 
					v = method.invoke(clazz, v); 
					list.add(v); 
				}else{ 
					List<Object> vs = getRuntimeValues(values, key,isKeyEncrypt, isValueEncrypt); 
					for(Object v:vs){ 
						v = method.invoke(clazz, v); 
						list.add(v); 
					} 
				}		 
			}else{ 
				if(Config.FETCH_REQUEST_VALUE_TYPE_SINGLE == fetchValueType){ 
					Object v = getRuntimeValue(values,key,isKeyEncrypt, isValueEncrypt); 
					list.add(v); 
				}else{ 
					list = getRuntimeValues(values, key,isKeyEncrypt, isValueEncrypt); 
				} 
			} 
		}catch(Exception e){ 
			e.printStackTrace(); 
		} 
		if(BasicUtil.isEmpty(true, list)){ 
			list = getDefValues(values, parser); 
		} 
		return list; 
	} 
	/** 
	 * 默认值 
	 * @param values values
	 * @param parser  parser
	 * @return return
	 */ 
	private static List<Object> getDefValues(Map<String,Object> values, ParseResult parser){ 
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
						Object v = getRuntimeValue(values,key,def.isKeyEncrypt(), def.isValueEncrypt()); 
						if(BasicUtil.isNotEmpty(v)){ 
							result.add(v); 
						} 
					}else{//多值 
						result = getRuntimeValues(values, key,def.isKeyEncrypt(), def.isValueEncrypt()); 
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
	 * @param parser  parser
	 * @return return
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
	 * @param page  page
	 * @param order  order
	 * @param src  src
	 * @param store  store
	 * @param conditions  conditions
	 * @return return
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

	/** 
	 * 整体加密http 参数(cd=1&amp;nm=2) 
	 *  
	 * @param param  param
	 * @return return
	 */ 
	public static String encryptRequestParam(String param) { 
		if (null == param || "".equals(param.trim())) { 
			return ""; 
		} 
		return encryptByType(param, ENCRYPT_TYPE_PARAM); 
	} 
 
	/** 
	 * 整体解密http 参数(cd=1&amp;nm=2) 
	 *  
	 * @param param  param
	 * @return return
	 */ 
	public static String decryptRequestParam(String param) { 
		if (null == param) { 
			return null; 
		} 
		return decrypt(param, ENCRYPT_TYPE_PARAM); 
	} 
 
	/** 
	 * 加密http请求参数名 
	 *  
	 * @param key key
	 * @return return
	 */ 
	public static String encryptHttpRequestParamKey(String key) { 
		if (null == key || "".equals(key.trim())) { 
			return ""; 
		} 
		return encryptKey(key); 
	} 
 
	/** 
	 * 解密http请求参数名 
	 *  
	 * @param key key
	 * @return return
	 */ 
	public static String decryptParamKey(String key) { 
		if (null == key) { 
			return null; 
		} 
		return decrypt(key, ENCRYPT_TYPE_KEY); 
	} 
 
	/** 
	 * 加密http请求参数值 
	 * @param value value
	 * @return return
	 */ 
	public static String encryptHttpRequestParamValue(String value) { 
		if (null == value || "".equals(value.trim())) { 
			return ""; 
		} 
		return encryptValue(value); 
	} 
 
	/** 
	 * 解密http请求参数值 
	 * @param value value
	 * @return return
	 */ 
	public static String decryptParamValue(String value) { 
		if (null == value) { 
			return null; 
		} 
		return decrypt(value.trim(), ENCRYPT_TYPE_VALUE); 
	} 
	/** 
	 * 加密 
	 *  
	 * @param src  src
	 *            原文 
	 * @param type  type
	 *            原文类型 
	 * @return 加密>插入版本号>添加前缀 
	 */ 
	private static String encryptByType(String src, String type, boolean mix) { 
		String result = null; 
		if (null == src) { 
			return null; 
		}
		if(isEncrypt(src, type)){
			return src;
		}
		DESUtil des = DESUtil.getInstance(defaultDesKey.getKey(type)); 
		try { 
			result = des.encrypt(src); 
			result = insertDESVersion(result); 
			String pre = defaultDesKey.getPrefix(type); 
			if(mix && ENCRYPT_TYPE_VALUE.equals(type)){ 
				//随机URL 避免QQ等工具报警 每次生成不同URL 扰乱爬虫追溯 
				String rand = "v"+BasicUtil.getRandomNumberString(5)+"v"; 
				pre = rand+pre; 
			} 
			result = pre + result; 
		} catch (Exception e) { 
			result = null; 
		} 
		return result; 
	} 
	public static String encryptByType(String src, String type){ 
		return encryptByType(src, type, false); 
	} 
 
	public static String encryptKey(String src) { 
		if (null == src) { 
			return src; 
		} 
		return encryptByType(src, ENCRYPT_TYPE_KEY); 
	} 
 
	public static String encryptValue(String src, boolean mix) { 
		if (null == src) { 
			return src; 
		} 
		return encryptByType(src, ENCRYPT_TYPE_VALUE, mix); 
	} 
	public static String encryptValue(String src){ 
		return encryptValue(src, false); 
	} 

	/**
	 * 是否已加密 (应该根据规则判断,而不是解一次密)
	 * @param src src
	 * @param type type
	 * @return return
	 */
	public static boolean isEncrypt(String src, String type){
		if(null == src){
			return false;
		}
		try{
			String value = decrypt(src, type);
			if(null != value){
				return true;
			}
			return false;
		}catch(Exception e){
			return false;
		}
	} 
	/** 
	 * 解密 
	 *  
	 * @param src  src
	 *            密文 
	 * @param type  type
	 *            密文类型 
	 * @return 删除前缀 > 解析版本号 > 解密 
	 */ 
	private static String decrypt(String src, String type) { 
		if (null == src || null == type) { 
			return null; 
		} 
		String result = null; 
		result = decrypt(src, defaultDesKey, type); // 默认版本解密 
 
		if (null == result) { 
			// 没有对应版本号,逐个版本解密 
			for (Iterator<String> versions = deskeys.keySet().iterator(); versions 
					.hasNext();) { 
				DESKey key = deskeys.get(versions.next()); 
				result = decrypt(src, key, type); 
				if (null != result) { 
					break; 
				} 
			} 
		} 
		return result; 
	} 
	/** 
	 * 解密 
	 *  
	 * @param src  src
	 * @param key  key
	 * @param type  type
	 * @return return
	 */ 
	private static String decrypt(String src, DESKey key, String type) {
		if(ConfigTable.getBoolean("IS_DECRYPT_LOG")){
			log.warn("[decrypt][start][src:{}][type:{}]", src, type);
		} 
		String result = src; 
		if (null == src) { 
			return null; 
		} 
		//删除随机URL混淆码 
		if(ENCRYPT_TYPE_VALUE.equals(type)){ 
			if(RegularUtil.match(result,"v\\d{5}v", Regular.MATCH_MODE.PREFIX)){ 
				result = result.substring(7);
				if(ConfigTable.getBoolean("IS_DECRYPT_LOG")){
					log.warn("[decrypt][删除混淆码][result:{}]",result);
				} 
			} 
		} 
		// 删除前缀 
		try { 
			String prefix = key.getPrefix(type); 
			int sub = -1; 
			if (null != prefix) { 
				sub = prefix.length(); 
			} 
			if (sub > result.length() || sub == -1) { 
				return null; 
			} 
			result = result.substring(sub);
			if(ConfigTable.getBoolean("IS_DECRYPT_LOG")){
				log.warn("[decrypt][删除前缀][result:{}]",result);
			} 
			// 解析版本 
			String tmp[] = parseDESVersion(result); 
			if (null != tmp && tmp.length == 2) { 
				if (key.getVersion().equals(tmp[0])) { 
					// 有版本号并且版本号对应 
					result = tmp[1]; 
				} 
			} 
			DESUtil des = DESUtil.getInstance(key.getKey(type)); 
			// 根据对应版本解密 
			if (null != des) { 
				try { 
					result = des.decrypt(result); 
				} catch (Exception e) { 
					result = null; 
				} 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
			result = null; 
		}
		if(ConfigTable.getBoolean("IS_DECRYPT_LOG")){
			log.warn("[decrypt][end][result:{}]",result);
		} 
		return result; 
	} 
 
	/** 
	 * 加密url参数部分 
	 *  
	 * @param url  url
	 * @return return
	 */
	public static String encryptUrl(String url) {
		if (null == url || !url.contains("?")) {
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		try {
			param = encryptRequestParam(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		url = url.substring(0, url.indexOf("?") + 1) + param;
		return url;
	}
	public static String encryptUrl(String url, boolean union, boolean encryptKey, boolean encryptValue) {
		if (null == url || !url.contains("?")) {
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		try {
			if(union){
				param = encryptRequestParam(param);
			}else{
				String params[] = param.split("&");
				param = "";
				for(String p:params){
					String kv[] = p.split("=");
					if(kv.length == 2){
						String k = kv[0];
						if(encryptKey){
							k = encryptKey(k);
						}
						String v = kv[1];
						if(encryptValue){
							v = encryptValue(v);
						}
						if(!"".equals(params)){
							param += "&";
						}
						param += k + "=" + v;
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		url = url.substring(0, url.indexOf("?") + 1) + param;
		return url;
	} 
 
	/** 
	 * 加密htmla标签中的url 
	 *  
	 * @param tag  tag
	 * @return return
	 */ 
	public static String encryptHtmlTagA(String tag) { 
		try { 
			String url = RegularUtil.fetchUrl(tag); 
			tag = tag.replace(url, encryptUrl(url)); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
 
		return tag; 
	} 
 
	/** 
	 * 从解密后的参数MAP中取值 
	 *  
	 * @param values values
	 * @param key  key
	 *            //key 
	 * @param valueEncrypt  valueEncrypt
	 *            //value是否加密 
	 * @return return
	 */ 
	private static List<Object> getRuntimeValuesFormDecryptMap(Map<String,Object> values, String key, boolean valueEncrypt) { 
		List<Object> result = new ArrayList<Object>(); 
		if (values.get(IS_PARAMS_DECRYPT) == null) { 
			decryptParam(values); 
		} 
		Map<String, List<String>> partMap = (Map<String, List<String>>) values.get(PARAMS_PART_DECRYPT_MAP); 
		List<String> list = partMap.get(encryptHttpRequestParamKey(key)); 
		if (null != values) { 
			if (valueEncrypt) { 
				for (String value : list) { 
					value = decryptParamValue(value); 
					result.add(value); 
				} 
			} else { 
				result.addAll(list); 
			} 
		} 
		return result; 
	} 
 
	/** 
	 * 从解密后的参数MAP中取值 
	 *  
	 * @param values values
	 * @param param  param
	 * @return return
	 */ 
	private static String getRuntimeValueFormDecryptMap(Map<String,Object> values, String key, boolean valueEncrypt) { 
		String result = null; 
		List<Object> list = getRuntimeValuesFormDecryptMap(values, key,valueEncrypt); 
		if (null != list && list.size() > 0) { 
			Object tmp = list.get(0); 
			if (null != tmp) { 
				result = tmp.toString().trim(); 
			} 
		} 
		return result; 
	} 
 
	/** 
	 * 解密httprequet参数及参数值 
	 *  
	 * @param values values
	 */ 
	private static void decryptParam(Map<String,Object> values) { 
		Map<String, List<String>> fullMap = new HashMap<String, List<String>>(); 
		Map<String, List<String>> partMap = new HashMap<String, List<String>>(); 
		for (Map.Entry<String, Object> entry : values.entrySet()){
			String k = entry.getKey();
			Object obj = entry.getValue(); 
			List<String> list = partMap.get(k);
			if (null == list) { 
				list = new ArrayList<String>(); 
				partMap.put(k, list); 
			} 
			if (null != obj) {
				if(obj instanceof Collection){
					Collection cols = (Collection)obj;
					for(Object col:cols){
						list.add(filterIllegalChar(col.toString().trim()));
					}
				}else{
					list.add(filterIllegalChar(obj.toString().trim())); 
				}
			} 
			partMap.put(k, list); 
		}
		values.put(PARAMS_PART_DECRYPT_MAP, partMap); 
		values.put(IS_PARAMS_DECRYPT, true); 
	} 
 
	/** 
	 * http request参数 
	 *  
	 * @param values values
	 * @param key  key
	 * @param keyEncrypt  keyEncrypt
	 *            key是否加密 
	 * @param valueEncrypt  valueEncrypt
	 *            value是否加密 
	 * @return return
	 */ 
	public static List<Object> getRuntimeValues(Map<String,Object> values, String key, boolean keyEncrypt, boolean valueEncrypt) { 
		List<Object> result = new ArrayList<Object>(); 
		if (null == values || null == key) { 
			return null; 
		} 
		if(key.startsWith("{") && key.endsWith("}")){
			result.add(key.substring(1, key.length()-1));
		}else{
			if (keyEncrypt) { 
				// key已加密 
				result = getRuntimeValuesFormDecryptMap(values, key, valueEncrypt); 
			} else { 
				// key未加密 
				Object obj = values.get(key);
				if(null != obj){
					if (obj instanceof Collection) {
						Collection cols = (Collection)obj;
						for (Object value : cols) { 
							if (null == value) { 
								result.add(""); 
							}else{
								if (valueEncrypt) { 
									value = decryptParamValue(value.toString());
									value = filterIllegalChar(value.toString()); 
								} 
								if (null != value) { 
									value = value.toString().trim();
									value = filterIllegalChar(value.toString()); 
								} 
							}
							result.add(value); 
						} 
					}else{
						if (valueEncrypt) { 
							result.add(decryptParamValue(obj.toString()));
						}else{
							result.add(obj.toString());
						}
					}
				}else{
					result.add("");
				}
			} 
		}
		return result; 
	} 
 
	public static List<Object> getRuntimeValues(Map<String,Object> values, String param, boolean keyEncrypt) { 
		return getRuntimeValues(values, param, keyEncrypt, false); 
	} 
 
	public static List<Object> getRuntimeValues(Map<String,Object> values, String param) { 
		return getRuntimeValues(values, param, false, false); 
	} 
 
	/** 
	 * HTTP参数 
	 *  
	 * @param values values
	 * @param key  key
	 *            参数名 
	 * @param keyEncrypt  keyEncrypt
	 *            参数名是否加密过 
	 * @param valueEncrypt  valueEncrypt
	 *            参数值是否加密过,是则解密 
	 * @return return
	 */ 
	public static Object getRuntimeValue(Map<String,Object> values,String key, boolean keyEncrypt, boolean valueEncrypt) { 
		String result = ""; 
		 List<Object> list = getRuntimeValues(values, key, keyEncrypt, valueEncrypt); 
		 if(null != list && list.size()>0){ 
			 result = (String)list.get(0); 
		 } 
		return result; 
	} 
 
	public static Object getRuntimeValue(Map<String,Object> values, String param, boolean keyEncrypt) { 
		return getRuntimeValue(values, param, keyEncrypt, false); 
	} 
 
	public static Object getRuntimeValue(Map<String,Object> values, String param) { 
		return getRuntimeValue(values, param, false, false); 
	} 

	/** 
	 * 检查非法字符 
	 *  
	 * @param src  src
	 * @return return
	 */ 
	public static String filterIllegalChar(String src) { 
		if (null == src) { 
			return src; 
		} 
		src = XssUtil.strip(src);
		return src; 
	} 
	/** 
	 * 密文中插入版本号位置 
	 *  
	 * @param src  src
	 *            未插入版本号的密文 
	 * @return return
	 */ 
	private static int getDESVersionIndex(String src) { 
		int idx = -1; 
		if (null != src && src.length() > MAX_DES_VERSION_INDEX) { 
			String tmp = src.substring(MAX_DES_VERSION_INDEX); 
			int len = tmp.length(); 
			String chr = src.substring(len / 2, len / 2 + 1); 
			idx = (int) chr.toCharArray()[0]; 
			idx = Math.abs(idx % MAX_DES_VERSION_INDEX); 
		} 
		return idx; 
	} 
 
	/** 
	 * 密文中插入版本号 
	 *  
	 * @param src  src
	 *            未插入版本号的密文 
	 * @return return
	 */ 
	private static String insertDESVersion(String src, String version) { 
		int idx = getDESVersionIndex(src); 
		if (idx >= 0) { 
			src = BasicUtil.insert(src, idx, version); 
		} 
		return src; 
	} 
 
	private static String insertDESVersion(String src) { 
		return insertDESVersion(src, defaultDesKey.getVersion()); 
	} 
 
	/** 
	 * 解析加密版本号 
	 *  
	 * @param src  src
	 * @return return
	 */ 
	private static String[] parseDESVersion(String src) { 
		String result[] = null; 
		if (null != src && src.length() > DES_VERSION_LENGTH) { 
			try { 
				result = new String[2]; 
				String tmp = src.substring(DES_VERSION_LENGTH); 
				int idx = getDESVersionIndex(tmp); 
				if (idx >= 0) { 
					result[0] = src.substring(idx, idx + DES_VERSION_LENGTH); // 版本号 
					result[1] = src.substring(0, idx) 
							+ src.substring(idx + DES_VERSION_LENGTH); // 没有版本号的密文 
				} 
			} catch (Exception e) { 
				e.printStackTrace(); 
			} 
		} 
		return result; 
	} 
} 
