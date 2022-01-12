/*  
 * Copyright 2006-2022 www.anyline.org
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
 
package org.anyline.net; 
 
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom; 
 
import javax.crypto.Cipher; 
import javax.crypto.KeyGenerator; 
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.anyline.util.Base64Util;
import org.anyline.util.BasicUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
 
public class AESUtil { 
	private static Logger log = LoggerFactory.getLogger(AESUtil.class); 
	private static final String KEY = "AES";
	public static enum CIPHER{
		PKCS5			{public String getCode(){return "AES/ECB/PKCS5Padding";}},
		PKCS7			{public String getCode(){return "AES/CBC/PKCS7Padding";}};
		public abstract String getCode();
	};
	/** 
	 * AES 加密操作 
	 *
	 * @param cipher  cipher
	 * @param content  content 待加密内容
	 * @param password  password 加密密码
	 * @return 返回Base64转码后的加密数据 
	 */ 
	public static String encrypt(CIPHER cipher, String password, String content) {
		try {
			Cipher cipherInstance = Cipher.getInstance(cipher.getCode());// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipherInstance.init(Cipher.ENCRYPT_MODE, getSecretKey(password));// 初始化为加密模式的密码器
			byte[] result = cipherInstance.doFinal(byteContent);// 加密
			return Base64.encodeBase64String(result);// 通过Base64转码返回 
		} catch (Exception ex) { 
			log.error(ex.getMessage()); 
		} 
		return null; 
	}
	public static String encrypt(String password, String content) {
		return encrypt(CIPHER.PKCS5, password, content);
	}
 
	/** 
	 * AES 解密操作 
	 *
	 * @param vector  vector
	 * @param cipher  cipher
	 * @param content  content
	 * @param password  password
	 * @return return
	 */ 
	public static String decrypt(CIPHER cipher, String password, String vector, String content) {
		try { 
			// 实例化 
			Cipher cipherInstance = Cipher.getInstance(cipher.getCode());
			// 使用密钥初始化，设置为解密模式
			if(BasicUtil.isNotEmpty(vector)){
				AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(KEY);
				algorithmParameters.init(new IvParameterSpec(Base64Util.decode(vector)));
				Key key = new SecretKeySpec(Base64Util.decode(password), KEY);
				cipherInstance.init(Cipher.DECRYPT_MODE, key,algorithmParameters);
			}else {
				cipherInstance.init(Cipher.DECRYPT_MODE, getSecretKey(password));
			}
			// 执行操作 
			byte[] result = cipherInstance.doFinal(Base64Util.decode(content));
			return new String(result, "utf-8"); 
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage()); 
		} 
		return null; 
	}
	public static String decrypt(CIPHER cipher, String password, String content) {
		return decrypt(cipher, password, null, content);
	}
	public static String decrypt(String password, String vector, String content) {
		return decrypt(CIPHER.PKCS5, password, vector, content);
	}

	public static String decrypt(String password, String content) {
		return decrypt(CIPHER.PKCS5, password, null, content);
	}
	/** 
	 * 生成加密秘钥 
	 *  
	 * @return return
	 */ 
	private static SecretKeySpec getSecretKey(final String password) { 
		// 返回生成指定算法密钥生成器的 KeyGenerator 对象 
		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance(KEY);
			// AES 要求密钥长度为 128 
			kg.init(128, new SecureRandom(password.getBytes()));
			// 生成一个密钥 
			SecretKey secretKey = kg.generateKey(); 
			return new SecretKeySpec(secretKey.getEncoded(), KEY);// 转换为AES专用密钥
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage()); 
		} 
		return null; 
	} 
 
}
