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
 *          
 */


package org.anyline.util; 
 
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.crypto.provider.SunJCE;
 
public class DESUtil {
	private static final Logger log = LoggerFactory.getLogger(DESUtil.class); 
	public static final String DEFAULT_SECRET_KEY = "L~@L$^N*)E+";	//默认密钥 
	public static final String DEFAULT_SALT = "!@)A(#$N%^&Y*(";	//盐值 
	private Cipher encryptCipher = null;					//加密 
	private Cipher decryptCipher = null;					//解密
	private String salt = DEFAULT_SALT; 
	 
	private static Map<String,DESUtil> instances = new Hashtable<String,DESUtil>(); 
	/** 
	 * 频繁加密解密时,使用单例模式,减少new耗时 
	 * @return return
	 */ 
	public static DESUtil getInstance(){ 
		DESUtil instance = instances.get(DEFAULT_SECRET_KEY); 
		try{ 
			instance = new DESUtil();
			instances.put(DEFAULT_SECRET_KEY, instance); 
		}catch(Exception e){ 
			 log.warn("[des insance][result:fail][msg:{}]",e.getMessage());
		} 
		return instance; 
	} 
	public static DESUtil getInstance(String key){ 
		if(null == key || key.trim().equals("")){ 
			key = DEFAULT_SECRET_KEY; 
		} 
		DESUtil instance = instances.get(key); 
		if(null == instance){ 
			try{ 
				instance = new DESUtil(key);
				instances.put(key, instance); 
			}catch(NoSuchPaddingException e){ 
				e.printStackTrace(); 
			}catch(NoSuchAlgorithmException e){
				e.printStackTrace(); 
			}catch(InvalidKeyException e){
				e.printStackTrace(); 
			}catch(Exception e){
				e.printStackTrace(); 
			} 
		} 
		return instance; 
	} 
	protected DESUtil() throws NoSuchPaddingException,NoSuchAlgorithmException,InvalidKeyException{ 
		this(DEFAULT_SECRET_KEY); 
	} 
	protected DESUtil(String key) throws NoSuchPaddingException,NoSuchAlgorithmException,InvalidKeyException{ 
 
		Security.addProvider(new SunJCE()); 
		Key _key = getKey(key.getBytes()); 
		 
		encryptCipher = Cipher.getInstance("DES"); 
		encryptCipher.init(Cipher.ENCRYPT_MODE, _key);//加密 
		 
		decryptCipher = Cipher.getInstance("DES"); 
		decryptCipher.init(Cipher.DECRYPT_MODE, _key);//解密 
	} 
	private static String byteArr2HexStr(byte[] arrB){ 
		int iLen = arrB.length; 
		/* 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍 */ 
		StringBuffer sb = new StringBuffer(iLen * 2); 
		for (int i = 0; i < iLen; i++) { 
			int intTmp = arrB[i]; 
			/* 把负数转换为正数 */ 
			while (intTmp < 0) { 
				intTmp = intTmp + 256; 
			} 
			/* 小于0F的数需要在前面补0 */ 
			if (intTmp < 16) { 
				sb.append("0"); 
			} 
			sb.append(Integer.toString(intTmp, 16)); 
		} 
		return sb.toString(); 
	} 
	 
	 
	private static byte[] hexStr2ByteArr(String strIn){ 
		byte[] arrB = strIn.getBytes(); 
		int iLen = arrB.length; 
		 
		/* 两个字符表示一个字节，所以字节数组长度是字符串长度除以2 */ 
		byte[] arrOut = new byte[iLen / 2]; 
		for (int i = 0; i < iLen; i = i + 2) { 
			String strTmp = new String(arrB, i, 2); 
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16); 
		} 
		return arrOut; 
	} 
	 
	 
	 
	/** 
	 * 加密 
	 * @param arrB  arrB
	 * @return return
	 * @throws BadPaddingException  BadPaddingException
	 * @throws IllegalBlockSizeException  BadPaddingException
	 */ 
	private byte[] encrypt(byte[] arrB) throws BadPaddingException,IllegalBlockSizeException{ 
		return encryptCipher.doFinal(arrB); 
	} 
	public String encrypt(String str) throws BadPaddingException,IllegalBlockSizeException{ 
		str = salt + str; 
		return byteArr2HexStr(encrypt(str.getBytes())); 
	} 
	 
	/** 
	 * 解密 
	 * @param arrB  arrB
	 * @return return
	 * @throws BadPaddingException  BadPaddingException
	 * @throws IllegalBlockSizeException  IllegalBlockSizeException
	 */ 
	private byte[] decrypt(byte[] arrB) throws BadPaddingException, IllegalBlockSizeException{ 
		return decryptCipher.doFinal(arrB); 
	} 
	 
	 
	public String decrypt(String str)throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{ 
		String result = "";
			result = new String(decrypt(hexStr2ByteArr(str)),ConfigTable.getString("DES_ENCODE","UTF-8"));
			result = result.substring(salt.length());
		return result; 
	} 
	 
	 
	/**
	 * 
	 * @param arrBTmp arrBTmp
	 * @return return
	 */
	private Key getKey(byte[] arrBTmp) { 
		byte[] arrB = new byte[8];	// 创建一个空的8位字节数组（默认值为0） 
		/* 将原始字节数组转换为8位 */ 
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) { 
			arrB[i] = arrBTmp[i]; 
		} 
		/* 生成密钥 */ 
		Key key = new SecretKeySpec(arrB, "DES"); 
		 
		return key; 
	}  
	/** 
	 * 加密集合中的keys属性值 
	 * @param list  list
	 * @param keys  keys
	 * @throws BadPaddingException BadPaddingException
	 * @throws IllegalBlockSizeException  IllegalBlockSizeException
	 */ 
	public static void encrypt(Collection<?> list, String ... keys) throws BadPaddingException, IllegalBlockSizeException{ 
		if(null == keys || null == list){ 
			return; 
		} 
		for(Object obj:list){ 
			encrypt(obj, keys); 
		} 
	} 
	/** 
	 * 加密obj的keys属性值 
	 * @param obj  obj
	 * @param keys  keys
	 * @throws BadPaddingException  BadPaddingException
	 * @throws IllegalBlockSizeException  IllegalBlockSizeException
	 */ 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void encrypt(Object obj, String ... keys) throws BadPaddingException, IllegalBlockSizeException{ 
		if(null == keys || null == obj){ 
			return; 
		} 
		for(String key: keys){ 
			if(obj instanceof Map){ 
				Map map = (Map)obj; 
				Object value = map.get(key); 
				if(null != value){ 
					map.put(key, DESUtil.getInstance().encrypt(value.toString())); 
				} 
			}else{ 
				Object value = BeanUtil.getFieldValue(obj, key); 
				if(null != value){ 
					value = DESUtil.getInstance().encrypt(value.toString()); 
					BeanUtil.setFieldValue(obj, key, value); 
				} 
			} 
		} 
	} 
	
	/*
	 * public static final String HTTP_REQUEST_PARAM_KEY_PREFIX =
	 * "wwwanylineorgk"; //参数名加密前缀 public static final String
	 * HTTP_REQUEST_PARAM_VALUE_PREFIX = "wwwanylineorgv"; //参数值加密前缀 public
	 * static final String HPPT_REQUEST_PARAM_PREFIX = "wwwanylineorgf";
	 * //参数整体加密前缀 public static final String HTTP_REQUEST_PARAM_FULL_DES_KEY =
	 * "@#$%0(*7#"; //整体加密密钥 public static final String
	 * HTTP_REQUEST_PARAM_KEY_DES_KEY = "@#$%#"; //参数名加密密钥 public static final
	 * String HTTP_REQUEST_PARAM_VALUE_DES_KEY = "@#23$%097#"; //参数值加密密钥
	 */
	public static final String PARAMS_FULL_DECRYPT_MAP = "PARAMS_FULL_DECRYPT_MAP"; // request参数值解密后MAP(整体加密)
	public static final String PARAMS_PART_DECRYPT_MAP = "PARAMS_PART_DECRYPT_MAP"; // request参数值解密后MAP(逐个加密)
	public static final String IS_PARAMS_DECRYPT = "IS_PARAMS_DECRYPT"; // request参数值是否已解密
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
			if (BasicUtil.isNotEmpty(keyPath)) {
				File keyFile = new File(ConfigTable.getWebRoot(), keyPath);
				if (keyFile.exists()) {
					SAXReader reader = new SAXReader();
					Document document = reader.read(keyFile);
					Element root = document.getRootElement();
					for (Iterator<Element> itrKey = root.elementIterator(); itrKey
							.hasNext();) {
						Element element = itrKey.next();
						DESKey key = new DESKey();
						String version = element.attributeValue("version");
						key.setVersion(version);
						key.setKey(element.elementTextTrim("des-key"));
						key.setKeyParam(element
								.elementTextTrim("des-key-param"));
						key.setKeyParamName(element
								.elementTextTrim("des-key-param-name"));
						key.setKeyParamValue(element
								.elementTextTrim("des-key-param-value"));
						key.setPrefix(element.elementTextTrim("des-prefix"));
						key.setPrefixParam(element
								.elementTextTrim("des-prefix-param"));
						key.setPrefixParamName(element
								.elementTextTrim("des-prefix-param-name"));
						key.setPrefixParamValue(element
								.elementTextTrim("des-prefix-param-value"));
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

	/**
	 * 检查非法字符
	 * 
	 * @param src
	 *            src
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
	 * 整体加密http 参数(cd=1&amp;nm=2)
	 * 
	 * @param param
	 *            param
	 * @return return
	 */
	public static String encryptParam(String param) {
		if (null == param || "".equals(param.trim())) {
			return "";
		}
		return encryptByType(param, ENCRYPT_TYPE_PARAM);
	}

	/**
	 * 整体解密http 参数(cd=1&amp;nm=2)
	 * 
	 * @param param
	 *            param
	 * @return return
	 */
	public static String decryptParam(String param) {
		if (null == param) {
			return null;
		}
		return decrypt(param, ENCRYPT_TYPE_PARAM);
	}

	/**
	 * 加密http请求参数名
	 * 
	 * @param key
	 *            key
	 * @return return
	 */
	public static String encryptParamKey(String key) {
		if (null == key || "".equals(key.trim())) {
			return "";
		}
		return encryptKey(key);
	}

	/**
	 * 解密http请求参数名
	 * 
	 * @param key
	 *            key
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
	 * 
	 * @param value
	 *            value
	 * @return return
	 */
	public static String encryptParamValue(String value) {
		if (null == value || "".equals(value.trim())) {
			return "";
		}
		return encryptValue(value);
	}

	/**
	 * 解密http请求参数值
	 * 
	 * @param value
	 *            value
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
	 * @param src
	 *            src 原文
	 * @param type
	 *            type 原文类型
	 * @return 加密>插入版本号>添加前缀
	 */
	private static String encryptByType(String src, String type, boolean mix) {
		String result = null;
		if (null == src) {
			return null;
		}
		if (isEncrypt(src, type)) {
			return src;
		}
		DESUtil des = DESUtil.getInstance(defaultDesKey.getKey(type));
		try {
			result = des.encrypt(src);
			result = insertDESVersion(result);
			String pre = defaultDesKey.getPrefix(type);
			if (mix && ENCRYPT_TYPE_VALUE.equals(type)) {
				// 随机URL 避免QQ等工具报警 每次生成不同URL 扰乱爬虫追溯
				String rand = "v" + BasicUtil.getRandomNumberString(5) + "v";
				pre = rand + pre;
			}
			result = pre + result;
		} catch (Exception e) {
			result = null;
		}
		return result;
	}

	public static String encryptByType(String src, String type) {
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

	public static String encryptValue(String src) {
		return encryptValue(src, false);
	}

	/**
	 * 是否已加密 (应该根据规则判断,而不是解一次密)
	 * 
	 * @param src
	 *            src
	 * @param type
	 *            type
	 * @return return
	 */
	public static boolean isEncrypt(String src, String type) {
		if (null == src) {
			return false;
		}
		try {
			String value = decrypt(src, type);
			if (null != value) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 解密
	 * 
	 * @param src
	 *            src 密文
	 * @param type
	 *            type 密文类型
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
	 * @param src
	 *            src
	 * @param key
	 *            key
	 * @param type
	 *            type
	 * @return return
	 */
	private static String decrypt(String src, DESKey key, String type) {
		if (ConfigTable.getBoolean("IS_DECRYPT_LOG")) {
			log.warn("[decrypt][start][src:{}][type:{}]", src, type);
		}
		String result = src;
		if (null == src) {
			return null;
		}
		// 删除随机URL混淆码
		if (ENCRYPT_TYPE_VALUE.equals(type)) {
			if (RegularUtil
					.match(result, "v\\d{5}v", Regular.MATCH_MODE.PREFIX)) {
				result = result.substring(7);
				if (ConfigTable.getBoolean("IS_DECRYPT_LOG")) {
					log.warn("[decrypt][删除混淆码][result:{}]", result);
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
			if (ConfigTable.getBoolean("IS_DECRYPT_LOG")) {
				log.warn("[decrypt][删除前缀][result:{}]", result);
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
		if (ConfigTable.getBoolean("IS_DECRYPT_LOG")) {
			log.warn("[decrypt][end][result:{}]", result);
		}
		return result;
	}

	/**
	 * 加密url参数部分
	 * 
	 * @param url
	 *            url
	 * @return return
	 */
	public static String encryptUrl(String url) {
		if (null == url || !url.contains("?")) {
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		try {
			param = encryptParam(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		url = url.substring(0, url.indexOf("?") + 1) + param;
		return url;
	}

	public static String encryptUrl(String url, boolean union,
			boolean encryptKey, boolean encryptValue) {
		if (null == url || !url.contains("?")) {
			return url;
		}
		String param = url.substring(url.indexOf("?") + 1);
		try {
			if (union) {
				param = encryptParam(param);
			} else {
				String params[] = param.split("&");
				param = "";
				for (String p : params) {
					String kv[] = p.split("=");
					if (kv.length == 2) {
						String k = kv[0];
						if (encryptKey) {
							k = encryptKey(k);
						}
						String v = kv[1];
						if (encryptValue) {
							v = encryptValue(v);
						}
						if (!"".equals(params)) {
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
	 * @param tag
	 *            tag
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
	 * 解析加密版本号
	 * 
	 * @param src
	 *            src
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

	/**
	 * 密文中插入版本号位置
	 * 
	 * @param src
	 *            src 未插入版本号的密文
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
	 * @param src
	 *            src 未插入版本号的密文
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

	/*************************************加密key***********************************************/ 
	/** 
	 * 加密map 
	 * @param map  map
	 * @param mix  mix
	 * @param keys  keys
	 * @return return
	 */ 
	private static Map<String, Object> encryptKey(Map<String, Object> map, boolean mix, String... keys) { 
		if (null == map) { 
			return map; 
		} 
		List<String> ks = BeanUtil.getMapKeys(map); 
		for (String k : ks) { 
			Object v = map.get(k); 
			if (null == v || v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Date) { 
				if(null == keys || keys.length == 0 || BasicUtil.contains(keys, k)){ 
					String key = encryptByType(k, DESUtil.ENCRYPT_TYPE_KEY, mix); 
					map.remove(k); 
					map.put(key, v); 
				} 
			} else{ 
				v = encryptKey(v, mix, keys); 
			} 
			//map.put(k, v); 
		} 
		return map; 
	} 
 
	 
	/** 
	 * 加密集合 
	 * @param list  list
	 * @param mix  mix
	 * @param keys  keys
	 * @return return
	 */ 
	private static Collection<Object> encryptKey(Collection<Object> list, boolean mix, String... keys) { 
		if (null == list) { 
			return list; 
		} 
		for (Object obj : list) { 
			obj = encryptKey(obj, mix, keys); 
		} 
		return list; 
	} 
 
	public static Collection<Object> encryptKey(Collection<Object> list, String... keys) { 
		return encryptKey(list, false, keys); 
	} 
	/** 
	 * 加密obj的keys属性值(递归Collection, Map) 
	 * @param mix 是否混淆url 生成随机URL用来防止QQ等工具报警 
	 * @param obj  obj
	 * @param keys  keys
	 * @return return
	 */ 
	@SuppressWarnings("unchecked")
	public static Object encryptKey(Object obj, boolean mix, String... keys) { 
		if (null == obj) { 
			return obj; 
		} 
		if (obj instanceof Map) { 
			obj = encryptKey((Map<String, Object>) obj, mix, keys); 
		} else if (obj instanceof Collection) { 
			obj = encryptKey((Collection<Object>) obj, mix, keys); 
		} else { 
			//Object无法加密 
		} 
		return obj; 
	} 
	public static Object encryptKey(Object obj, String... keys) { 
		return encryptKey(obj, false, keys); 
	}
} 
 
