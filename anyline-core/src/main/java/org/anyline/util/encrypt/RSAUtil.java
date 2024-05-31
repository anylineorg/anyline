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



package org.anyline.util.encrypt;

import org.anyline.util.Base64Util;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
public class RSAUtil {
	public static final String CHARSET = "UTF-8"; 
	public static final String RSA_ALGORITHM = "RSA"; 
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

	/**
	 * 创建密钥对
	 * 返回密钥对后可以getPrivate()获取私钥或getPublic()获取公钥
	 * 获取公钥私钥后可以通过base64(key)生成String用来交换密钥
	 * @param size 位数
	 * @return KeyPair
	 */
	public static KeyPair create(int size) {
		// 为RSA算法创建一个KeyPairGenerator对象
		KeyPairGenerator keys = null;
		try {
			keys = KeyPairGenerator.getInstance(RSA_ALGORITHM);
		} catch (NoSuchAlgorithmException ignored) {
		}

		// 初始化KeyPairGenerator对象, 密钥长度
		keys.initialize(size);
		// 生成密匙对
		KeyPair pair = keys.generateKeyPair();
		return pair;
	}
	public static KeyPair create() {
		return create(1024);
	}

	/**
	 * 密钥生成base64字符
	 * @param key 密钥
	 * @return String
	 */
	public static String base64(Key key){
		String base64 = null;
		base64 = Base64.encodeBase64URLSafeString(key.getEncoded());
		return base64;
	}

	/** 
	 * 创建公钥
	 *  
	 * @param base64   密钥字符串（经过base64编码）
	 * @return RSAPublicKey
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException InvalidKeySpecException 
	 */ 
	public static RSAPublicKey createPublicKey(String base64) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 通过X509编码的Key指令获得公钥对象
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM); 
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(base64));
		RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec); 
		return key; 
	} 
 
	/** 
	 * 创建私钥
	 * @param base64  密钥字符串（经过base64编码）
	 * @return RSAPrivateKey
	 * @throws NoSuchAlgorithmException  NoSuchAlgorithmException
	 * @throws InvalidKeySpecException  InvalidKeySpecException
	 */ 
	public static RSAPrivateKey createPrivateKey(String base64) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 通过PKCS#8编码的Key指令获得私钥对象
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM); 
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(base64));
		RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec); 
		return key; 
	} 
 
	/** 
	 * 公钥加密 
	 *  
	 * @param data  data
	 * @param key  RSA 公钥
	 * @return String
	 * @throws Exception 异常 Exception
	 */ 
	public static String encrypt(String data, PublicKey key) throws Exception {
		try {
			RSAPublicKey publicKey = null;
			if(key instanceof RSAPublicKey){
				publicKey = (RSAPublicKey) key;
			}else{
				throw new Exception("请提供RRSAPublicKey");
			}
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM); 
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), publicKey.getModulus().bitLength()));
		} catch (Exception e) {
			throw new Exception("[公钥加密异常][加密数据:" + data + "]", e);
		} 
	}

	public static String encrypt(String data, String key) throws Exception {
		return encrypt(data, createPublicKey(key));
	}

	/** 
	 * 私钥解密 
	 *  
	 * @param data  data
	 * @param key  RSA 私钥
	 * @return String
	 * @throws Exception 异常 Exception
	 */
	public static String decrypt(String data, PrivateKey key) throws Exception {
		try {
			RSAPrivateKey privateKey = null;
			if(key instanceof RSAPrivateKey) {
				privateKey = (RSAPrivateKey) key;
			}else{
				throw new Exception("请提供RSAPrivateKey");
			}
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM); 
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), privateKey.getModulus().bitLength()), CHARSET);
		} catch (Exception e) {
			throw new Exception("[私钥解密异常][解密数据:" + data + "]", e);
		} 
	}
	public static String decrypt(String data, String key) throws Exception {
		return decrypt(data, createPrivateKey(key));
	}
 
	/** 
	 * 私钥加密(加密应该用公钥)
	 *  
	 * @param data  data
	 * @param key  RSA 私钥
	 * @return String
	 * @throws Exception 异常 Exception
	 */ 
 
	public static String encrypt(String data, RSAPrivateKey key) throws Exception {
		try {
			RSAPrivateKey privateKey = null;
			if(key instanceof RSAPrivateKey) {
				privateKey = (RSAPrivateKey) key;
			}else{
				throw new Exception("请提供RSAPrivateKey");
			}
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM); 
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), privateKey.getModulus().bitLength()));
		} catch (Exception e) {
			throw new Exception("[私钥加密异常][加密数据:" + data + "]", e);
		} 
	} 
 
	/** 
	 * 公钥解密 
	 *  
	 * @param data  data
	 * @param key  RSA 公钥
	 * @return String
	 * @throws Exception 异常 Exception
	 */ 
 
	public static String decrypt(String data, RSAPublicKey key) throws Exception {
		try {
			RSAPublicKey publicKey = null;
			if(key instanceof RSAPublicKey){
				publicKey = (RSAPublicKey) key;
			}else{
				throw new Exception("请提供RRSAPublicKey");
			}
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM); 
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), publicKey.getModulus().bitLength()), CHARSET);
		} catch (Exception e) {
			throw new Exception("[公钥解密异常][解密数据:" + data + "]", e);
		} 
	} 

 
    /** 
     * 用私钥对信息生成数字签名 
     * @param data 已加密数据 
     * @param key 私钥(BASE64编码)
     *  
     * @return String
     * @throws Exception 异常 Exception
     */ 
    public static String sign(byte[] data, PrivateKey key) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM); 
        signature.initSign(key);
        signature.update(data); 
        return Base64Util.encode(signature.sign()); 
         
		 
    }
	public static String sign(String data, String key) throws Exception {
		return sign(data.getBytes(), createPrivateKey(key));
	}
	public static String sign(String data, PrivateKey key) throws Exception {
		return sign(data.getBytes(), key);
	}

	/**
     * 校验数字签名
     *  
     * @param data 已加密数据 
     * @param key 公钥(BASE64编码)
     * @param sign 数字签名 
     * @return boolean
     * @throws Exception 异常  Exception
     */
	public static boolean verify(byte[] data, PublicKey key, String sign) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(key);
		signature.update(data);
		return signature.verify(Base64Util.decode(sign));
	}
	public static boolean verify(byte[] data, String key, String sign) throws Exception {
		return verify(data, createPublicKey(key), sign);
	}

	/**
	 * 校验数字签名
	 *
	 * @param data 已加密数据
	 * @param key 公钥(BASE64编码)
	 * @param sign 数字签名
	 * @return boolean
	 * @throws Exception 异常  Exception
	 */
	public static boolean verify(String data, String key, String sign) throws Exception {
		return verify(data.getBytes(), key, sign);
	}

	/**
	 * 校验数字签名
	 *
	 * @param data 已加密数据
	 * @param key 公钥
	 * @param sign 数字签名
	 * @return boolean
	 * @throws Exception 异常  Exception
	 */
	public static boolean verify(String data, PublicKey key, String sign) throws Exception {
		return verify(data.getBytes(), key, sign);
	}

	/**
     * 从文件中提取私钥 
     * @param file  密钥文件
     * @return RSAPrivateKey
     */ 
	public static RSAPrivateKey createPrivateKey(File file) throws Exception {
		RSAPrivateKey privateKey = null; 
		InputStream inputStream = null; 
		try {
			inputStream = new FileInputStream(file); 
			privateKey = createPrivateKey(inputStream);
		}finally {
			if (inputStream != null) {
				try {
					inputStream.close(); 
				} catch (Exception ignored) {

				} 
			} 
		} 
		return privateKey; 
	}

	/**
	 * 从文件中提取公钥
	 * @param file  file
	 * @return RSAPublicKey
	 */
	public static RSAPublicKey createPublicKey(File file) throws Exception {
		RSAPublicKey publicKey = null; 
		InputStream inputStream = null; 
		try {
			inputStream = new FileInputStream(file); 
			publicKey = createPublicKey(inputStream);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close(); 
				} catch (Exception ignored) {
				} 
			} 
		} 
		return publicKey; 
	}

	/**
	 * 从输入流中提取公钥
	 * @param is  输入流
	 * @return RSAPublicKey
	 */
	public static RSAPublicKey createPublicKey(InputStream is) throws Exception {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder(); 
			String readLine = null; 
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue; 
				} else {
					sb.append(readLine); 
					sb.append('\r'); 
				} 
			} 
			return createPublicKey(sb.toString());
	}

	/**
	 * 从输入流中提取私钥
	 * @param is  输入流
	 * @return RSAPublicKey
	 */
	public static RSAPrivateKey createPrivateKey(InputStream is) throws Exception {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder(); 
			String readLine = null; 
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue; 
				} else {
					sb.append(readLine); 
					sb.append('\r'); 
				} 
			} 
			return createPrivateKey(sb.toString());
	}

	@SuppressWarnings("deprecation")
	private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) throws Exception {
		int maxBlock = 0;
		if (opmode == Cipher.DECRYPT_MODE) {
			maxBlock = keySize / 8;
		} else {
			maxBlock = keySize / 8 - 11;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] buff;
		int i = 0;
		try {
			while (datas.length > offSet) {
				if (datas.length - offSet > maxBlock) {
					buff = cipher.doFinal(datas, offSet, maxBlock);
				} else {
					buff = cipher.doFinal(datas, offSet, datas.length - offSet);
				}
				out.write(buff, 0, buff.length);
				i++;
				offSet = i * maxBlock;
			}
		} catch (Exception e) {
			throw new Exception("[加密异常][加解密阀值:" + maxBlock + "]", e);
		}
		byte[] result = out.toByteArray();
		IOUtils.closeQuietly(out);
		return result;
	}

} 
