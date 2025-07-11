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

package org.anyline.util.encrypt;

import org.anyline.util.*;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.*;
 
public class DESUtil {
	private static final Log log = LogProxy.get(DESUtil.class); 
	public static final String DEFAULT_SECRET_KEY = "L~@L$^N*)E+";	// 默认密钥
	public static final String DEFAULT_SALT = "!@)A(#$N%^&Y*(";		// 盐值
	private Cipher encryptCipher = null;							// 加密
	private Cipher decryptCipher = null;							// 解密
	private String salt = DEFAULT_SALT;
	 
	private static Map<String, DESUtil> instances = new Hashtable<String, DESUtil>(); 
	/** 
	 * 频繁加密解密时, 使用单例模式, 减少new耗时 
	 * @return DESUtil
	 */ 
	public static DESUtil getInstance() {
		DESUtil instance = instances.get(DEFAULT_SECRET_KEY); 
		try{
			instance = new DESUtil();
			instances.put(DEFAULT_SECRET_KEY, instance); 
		}catch(Exception e) {
			 log.warn("[des instance][result:fail][msg:{}]", e.toString());
		} 
		return instance; 
	} 
	public static DESUtil getInstance(String key) {
		if(null == key || key.trim().equals("")) {
			key = DEFAULT_SECRET_KEY; 
		} 
		DESUtil instance = instances.get(key); 
		if(null == instance) {
			try{
				instance = new DESUtil(key);
				instances.put(key, instance); 
			}catch(Exception e) {
				log.error("create instance exception:", e);
			} 
		} 
		return instance; 
	} 
	protected DESUtil() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		this(DEFAULT_SECRET_KEY); 
	} 
	protected DESUtil(String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

		//Security.addProvider(new SunJCE());
		java.security.Security.getProvider("SunJCE");
		Key _key = getKey(key.getBytes());

		encryptCipher = Cipher.getInstance("DES");
		encryptCipher.init(Cipher.ENCRYPT_MODE, _key);//加密 

		decryptCipher = Cipher.getInstance("DES");
		decryptCipher.init(Cipher.DECRYPT_MODE, _key);//解密 
	}
	 
	 
	 
	/** 
	 * 加密 
	 * @param bytes  bytes
	 * @return bytes
	 * @throws BadPaddingException  BadPaddingException
	 * @throws IllegalBlockSizeException  BadPaddingException
	 */ 
	private byte[] encrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
		return encryptCipher.doFinal(bytes);
	} 
	public String encrypt(String str) throws BadPaddingException, IllegalBlockSizeException {
		if(null == str || ignores.contains(str)) {
			return str;
		}
		str = salt + str; 
		return NumberUtil.byte2hex(encrypt(str.getBytes()));
	} 
	 
	/** 
	 * 解密 
	 * @param bytes  bytes
	 * @return bytes
	 * @throws BadPaddingException  BadPaddingException
	 * @throws IllegalBlockSizeException  IllegalBlockSizeException
	 */ 
	private byte[] decrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
		return decryptCipher.doFinal(bytes);
	}

	 
	/**
	 * 
	 * @param bytes bytes
	 * @return Key
	 */
	private Key getKey(byte[] bytes) {
		byte[] arrB = new byte[8];	// 创建一个空的8位字节数组（默认值为0）
		/* 将原始字节数组转换为8位 */ 
		for (int i = 0; i < bytes.length && i < arrB.length; i++) {
			arrB[i] = bytes[i];
		} 
		/* 生成密钥 */ 
		Key key = new SecretKeySpec(arrB, "DES"); 
		 
		return key; 
	}

	/**
	 * 加密String
	 * @param str 明文
	 * @return  密文
	 * @throws IllegalBlockSizeException IllegalBlockSizeException
	 * @throws BadPaddingException BadPaddingException
	 * @throws UnsupportedEncodingException UnsupportedEncodingException
	 */
	public String decrypt(String str)throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		if(null == str || ignores.contains(str)) {
			return str;
		}
		String result = "";
		result = new String(decrypt(NumberUtil.hex2bytes(str)), ConfigTable.getString("DES_ENCODE","UTF-8"));
		result = result.substring(salt.length());
		return result;
	}

	/** 
	 * 加密集合中的keys属性值 
	 * @param list  list
	 * @param keys  keys
	 * @throws BadPaddingException BadPaddingException
	 * @throws IllegalBlockSizeException  IllegalBlockSizeException
	 */ 
	public static void encrypt(Collection<?> list, String ... keys) throws BadPaddingException, IllegalBlockSizeException {
		if(null == keys || null == list) {
			return; 
		} 
		for(Object obj:list) {
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
	@SuppressWarnings({"rawtypes","unchecked" })
	public static void encrypt(Object obj, String ... keys) throws BadPaddingException, IllegalBlockSizeException {
		if(null == keys || null == obj) {
			return; 
		} 
		for(String key: keys) {
			if(obj instanceof Map) {
				Map map = (Map)obj; 
				Object value = map.get(key); 
				if(null != value) {
					map.put(key, DESUtil.getInstance().encrypt(value.toString())); 
				} 
			}else{
				Object value = BeanUtil.getFieldValue(obj, key, true);
				if(null != value) {
					value = DESUtil.getInstance().encrypt(value.toString()); 
					BeanUtil.setFieldValue(obj, key, value); 
				} 
			} 
		} 
	} 
	
	/*
	 * static final String HTTP_REQUEST_PARAM_KEY_PREFIX =
	 * "wwwanylineorgk"; // 参数名加密前缀 public static final String
	 * HTTP_REQUEST_PARAM_VALUE_PREFIX = "wwwanylineorgv"; // 参数值加密前缀 public
	 * static final String HPPT_REQUEST_PARAM_PREFIX = "wwwanylineorgf";
	 * // 参数整体加密前缀 public static final String HTTP_REQUEST_PARAM_FULL_DES_KEY =
	 * "@#$%0(*7#"; // 整体加密密钥 public static final String
	 * HTTP_REQUEST_PARAM_KEY_DES_KEY = "@#$%#"; // 参数名加密密钥 public static final
	 * String HTTP_REQUEST_PARAM_VALUE_DES_KEY = "@#23$%097#"; // 参数值加密密钥
	 */
	private static Map<String, DESKey> deskeys = null;
	private static DESKey defaultDesKey = null;
	private static final int MAX_DES_VERSION_INDEX = 12; // 密文中插入版本号最大位置
	private static final int DES_VERSION_LENGTH = 3;
	private static final String ENCRYPT_TYPE_PARAM = "param";
	private static final String ENCRYPT_TYPE_KEY = "name";
	private static final String ENCRYPT_TYPE_VALUE = "value";
	public static List<String> ignores = new ArrayList<>();
	static {
		deskeys = new HashMap<String, DESKey>();
		try {
			String ignoreList = ConfigTable.getString("DES_IGNORE");
			if(null != ignoreList) {
				String[] tmps = ignoreList.split(",");
				for(String tmp:tmps) {
					ignores.add(tmp);
				}
			}
			String keyPath = ConfigTable.getString("DES_KEY_FILE");
			if (BasicUtil.isNotEmpty(keyPath)) {
				if (keyPath.contains("${classpath}")) {
					keyPath = keyPath.replace("${classpath}", ConfigTable.getClassPath());
				} else if (keyPath.startsWith("/")) {
					keyPath = ConfigTable.getWebRoot() + keyPath;
				} else {

				}
				File keyFile = new File(keyPath);
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
			log.error("create des exception:", e);
		}
	}

	/**
	 * 检查非法字符
	 * 
	 * @param src src
	 * @return String
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
	 * @param param param
	 * @return String
	 */
	public static String encryptParam(String param) {
		if (null == param || param.trim().isEmpty()) {
			return "";
		}
		return encryptByType(param, ENCRYPT_TYPE_PARAM);
	}

	/**
	 * 整体解密http 参数(cd=1&amp;nm=2)
	 * 
	 * @param param  param
	 * @return String
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
	 * @param key key
	 * @return String
	 */
	public static String encryptParamKey(String key) {
		if (null == key || key.trim().isEmpty()) {
			return "";
		}
		return encryptKey(key);
	}

	/**
	 * 解密http请求参数名
	 * 
	 * @param key  key
	 * @return String
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
	 * @param value value
	 * @return String
	 */
	public static String encryptParamValue(String value) {
		if (null == value || value.trim().isEmpty()) {
			return "";
		}
		return encryptValue(value);
	}

	/**
	 * 解密http请求参数值
	 * 
	 * @param value  value
	 * @return String
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
	 * @param src src 原文
	 * @param type type 原文类型
	 * @return 加密>插入版本号>添加前缀
	 */
	private static String encryptByType(String src, String type, boolean mix) {
		String result = null;
		if(null == src || ignores.contains(src)) {
			return src;
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
	 * 是否已加密 (应该根据规则判断, 而不是解一次密)
	 * 
	 * @param src src
	 * @param type type
	 * @return boolean
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
	 * @param src src 密文
	 * @param type type 密文类型
	 * @return 删除前缀 &gt; 解析版本号 &gt; 解密
	 */
	private static String decrypt(String src, String type) {
		if (null == src || null == type || ignores.contains(src)) {
			return null;
		}
		String result = null;
		result = decrypt(src, defaultDesKey, type); // 默认版本解密

		if (null == result) {
			// 没有对应版本号, 逐个版本解密
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
	 * @param key key
	 * @param type type
	 * @return String
	 */
	private static String decrypt(String src, DESKey key, String type) {
		if (ConfigTable.getBoolean("IS_DECRYPT_LOG")) {
			log.debug("[decrypt][start][src:{}][type:{}]", src, type);
		}
		String result = src;
		if (null == src || ignores.contains(src)) {
			return null;
		}
		// 删除随机URL混淆码
		if (ENCRYPT_TYPE_VALUE.equals(type)) {
			if (RegularUtil
					.match(result, "v\\d{5}v", Regular.MATCH_MODE.PREFIX)) {
				result = result.substring(7);
				if (ConfigTable.getBoolean("IS_DECRYPT_LOG")) {
					log.debug("[decrypt][删除混淆码][result:{}]", result);
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
			if(result.startsWith(prefix)) {
				result = result.substring(sub);
				if (ConfigTable.getBoolean("IS_DECRYPT_LOG")) {
					log.debug("[decrypt][删除前缀][result:{}]", result);
				}
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
			log.debug("[decrypt][end][result:{}]", result);
		}
		return result;
	}

	/**
	 * 加密url参数部分
	 * 
	 * @param url url
	 * @return String
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
	 * @param tag 标签
	 * @return String
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
	 * @param src src
	 * @return String
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
	 * @param src 未插入版本号的密文
	 * @return int
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
	 * @param src 未插入版本号的密文
	 * @return String
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
	 * @return Map
	 */ 
	private static Map<String, Object> encryptKey(Map<String, Object> map, boolean mix, String... keys) {
		if (null == map) {
			return map; 
		} 
		List<String> ks = BeanUtil.getMapKeys(map); 
		for (String k : ks) {
			Object v = map.get(k); 
			if (null == v || v instanceof String || v instanceof Number || v instanceof Boolean || v instanceof Date) {
				if(null == keys || keys.length == 0 || BasicUtil.contains(keys, k)) {
					String key = encryptByType(k, DESUtil.ENCRYPT_TYPE_KEY, mix); 
					map.remove(k); 
					map.put(key, v); 
				} 
			} else{
				v = encryptKey(v, mix, keys); 
			} 
			// map.put(k, v);
		} 
		return map; 
	} 
 
	 
	/** 
	 * 加密集合 
	 * @param list  list
	 * @param mix  mix
	 * @param keys  keys
	 * @return Collection
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
	 * @return Object
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
			// Object无法加密
		} 
		return obj; 
	} 
	public static Object encryptKey(Object obj, String... keys) {
		return encryptKey(obj, false, keys); 
	}
} 
 
