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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import com.sun.crypto.provider.SunJCE;

public class DESUtil {
	static final Logger log = Logger.getLogger(DESUtil.class);
	public static final String DEFAULT_SECRET_KEY = "L~@L$^N*)E+";	//默认密钥
	public static final String DEFAULT_SALT = "!@)A(#$N%^&Y*(";	//盐值
	private Cipher encryptCipher = null;					//加密
	private Cipher decryptCipher = null;					//解密
	private String salt = DEFAULT_SALT;
	
	private static Map<String,DESUtil> instances = new Hashtable<String,DESUtil>();
	/**
	 * 频繁加密解密时,使用单例模式,减少new耗时
	 * @return
	 */
	public static DESUtil getInstance(){
		DESUtil instance = instances.get(DEFAULT_SECRET_KEY);
		if(null == instance){
			try{
				instance = new DESUtil();
				instances.put(DEFAULT_SECRET_KEY, instance);
			}catch(NoSuchPaddingException e){
				
			}catch(NoSuchAlgorithmException e){
				
			}catch(InvalidKeyException e){
				
			}catch(Exception e){
				
			}
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
	private DESUtil() throws NoSuchPaddingException,NoSuchAlgorithmException,InvalidKeyException{
		this(DEFAULT_SECRET_KEY);
	}
	private DESUtil(String key) throws NoSuchPaddingException,NoSuchAlgorithmException,InvalidKeyException{

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
	 * @param arrB
	 * @return
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
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
	 * @param arrB
	 * @return
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
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
	 * @param list
	 * @param keys
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
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
	 * @param obj
	 * @param keys
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
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

}

