package org.anyline.net; 
 
import java.io.BufferedReader; 
import java.io.ByteArrayOutputStream; 
import java.io.File; 
import java.io.FileInputStream; 
import java.io.InputStream; 
import java.io.InputStreamReader; 
import java.security.Key; 
import java.security.KeyFactory; 
import java.security.KeyPair; 
import java.security.KeyPairGenerator; 
import java.security.NoSuchAlgorithmException; 
import java.security.PrivateKey; 
import java.security.PublicKey; 
import java.security.Signature; 
import java.security.interfaces.RSAPrivateKey; 
import java.security.interfaces.RSAPublicKey; 
import java.security.spec.InvalidKeySpecException; 
import java.security.spec.PKCS8EncodedKeySpec; 
import java.security.spec.X509EncodedKeySpec; 
import java.util.HashMap; 
import java.util.Map; 
 
import javax.crypto.Cipher; 
 
import org.anyline.util.Base64Util; 
import org.apache.commons.codec.binary.Base64; 
import org.apache.commons.io.IOUtils; 
 
public class RSAUtil { 
	public static final String CHARSET = "UTF-8"; 
	public static final String RSA_ALGORITHM = "RSA"; 
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA"; 
	 
	/** 
	 *  
	 * @param keySize 密钥长度 
	 * @return return
	 */ 
	public static Map<String, String> createKeys(int keySize) { 
		// 为RSA算法创建一个KeyPairGenerator对象 
		KeyPairGenerator kpg = null; 
		try { 
			kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM); 
		} catch (NoSuchAlgorithmException e) { 
			e.printStackTrace(); 
		} 
 
		// 初始化KeyPairGenerator对象,密钥长度 
		kpg.initialize(keySize); 
		// 生成密匙对 
		KeyPair keyPair = kpg.generateKeyPair(); 
		// 得到公钥 
		Key publicKey = keyPair.getPublic(); 
		String publicKeyStr = Base64.encodeBase64URLSafeString(publicKey.getEncoded()); 
		// 得到私钥 
		Key privateKey = keyPair.getPrivate(); 
		String privateKeyStr = Base64.encodeBase64URLSafeString(privateKey.getEncoded()); 
		Map<String, String> keys = new HashMap<String, String>(); 
		keys.put("public", publicKeyStr); 
		keys.put("private", privateKeyStr); 
		return keys; 
	} 
 
	/** 
	 * 得到公钥 
	 *  
	 * @param publicKey   密钥字符串（经过base64编码） 
	 * @return return
	 * @throws NoSuchAlgorithmException NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException InvalidKeySpecException 
	 */ 
	public static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException { 
		// 通过X509编码的Key指令获得公钥对象 
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM); 
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey)); 
		RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec); 
		return key; 
	} 
 
	/** 
	 * 得到私钥 
	 * @param privateKey  密钥字符串（经过base64编码） 
	 * @return return
	 * @throws NoSuchAlgorithmException  NoSuchAlgorithmException
	 * @throws InvalidKeySpecException  InvalidKeySpecException
	 */ 
	public static RSAPrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException { 
		// 通过PKCS#8编码的Key指令获得私钥对象 
		KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM); 
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey)); 
		RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec); 
		return key; 
	} 
 
	/** 
	 * 公钥加密 
	 *  
	 * @param data  data
	 * @param publicKey  publicKey
	 * @return return
	 */ 
	public static String publicEncrypt(String data, RSAPublicKey publicKey) { 
		try { 
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM); 
			cipher.init(Cipher.ENCRYPT_MODE, publicKey); 
			return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), publicKey.getModulus().bitLength())); 
		} catch (Exception e) { 
			throw new RuntimeException("[公钥加密异常][加密数据:" + data + "]", e); 
		} 
	} 
 
	/** 
	 * 私钥解密 
	 *  
	 * @param data  data
	 * @param privateKey  privateKey
	 * @return return
	 */ 
 
	public static String privateDecrypt(String data, RSAPrivateKey privateKey) { 
		try { 
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM); 
			cipher.init(Cipher.DECRYPT_MODE, privateKey); 
			return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), privateKey.getModulus().bitLength()), CHARSET); 
		} catch (Exception e) { 
			throw new RuntimeException("[私钥解密异常][解密数据:" + data + "]", e); 
		} 
	} 
 
	/** 
	 * 私钥加密 
	 *  
	 * @param data  data
	 * @param privateKey  privateKey
	 * @return return
	 */ 
 
	public static String privateEncrypt(String data, RSAPrivateKey privateKey) { 
		try { 
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM); 
			cipher.init(Cipher.ENCRYPT_MODE, privateKey); 
			return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), privateKey.getModulus().bitLength())); 
		} catch (Exception e) { 
			throw new RuntimeException("[私钥加密异常][加密数据:" + data + "]", e); 
		} 
	} 
 
	/** 
	 * 公钥解密 
	 *  
	 * @param data  data
	 * @param publicKey  publicKey
	 * @return return
	 */ 
 
	public static String publicDecrypt(String data, RSAPublicKey publicKey) { 
		try { 
			Cipher cipher = Cipher.getInstance(RSA_ALGORITHM); 
			cipher.init(Cipher.DECRYPT_MODE, publicKey); 
			return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), publicKey.getModulus().bitLength()), CHARSET); 
		} catch (Exception e) { 
			throw new RuntimeException("[公钥解密异常][解密数据:" + data + "]", e); 
		} 
	} 
 
	private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) { 
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
			throw new RuntimeException("[加密异常][加解密阀值:" + maxBlock + "]", e); 
		} 
		byte[] resultDatas = out.toByteArray(); 
		IOUtils.closeQuietly(out); 
		return resultDatas; 
	} 
	 
 
    /** 
     * 用私钥对信息生成数字签名 
     * @param data 已加密数据 
     * @param privateKey 私钥(BASE64编码) 
     *  
     * @return return
     * @throws Exception Exception
     */ 
    public static String sign(byte[] data, String privateKey) throws Exception { 
        PrivateKey privateK = getPrivateKey(privateKey); 
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM); 
        signature.initSign(privateK); 
        signature.update(data); 
        return Base64Util.encode(signature.sign()); 
         
		 
    } 
    public static String sign(String data, String privateKey) throws Exception { 
       return sign(data.getBytes(), privateKey); 
    } 
 
    /** 
     * <p> 
     * 校验数字签名 
     * </p> 
     *  
     * @param data 已加密数据 
     * @param publicKey 公钥(BASE64编码) 
     * @param sign 数字签名 
     * @return return
     * @throws Exception  Exception
     *  
     */ 
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception { 
        PublicKey publicK = getPublicKey(publicKey); 
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM); 
        signature.initVerify(publicK); 
        signature.update(data); 
        return signature.verify(Base64Util.decode(sign)); 
    } 
    public static boolean verify(String data, String publicKey, String sign) throws Exception { 
    	return verify(data.getBytes(), publicKey, sign); 
    } 
     
    /** 
     * 从文件中提取私钥 
     * @param file  file
     * @param keyAlgorithm  keyAlgorithm
     * @return return
     */ 
	public static RSAPrivateKey getPrivateKey(File file, String keyAlgorithm) { 
		RSAPrivateKey privateKey = null; 
		InputStream inputStream = null; 
		try { 
			inputStream = new FileInputStream(file); 
			privateKey = getPrivateKey(inputStream, keyAlgorithm); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} finally { 
			if (inputStream != null) { 
				try { 
					inputStream.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
				} 
			} 
		} 
		return privateKey; 
	} 
 
	public static RSAPublicKey getPublicKey(File file) { 
		RSAPublicKey publicKey = null; 
		InputStream inputStream = null; 
		try { 
			inputStream = new FileInputStream(file); 
			publicKey = getPublicKey(inputStream); 
		} catch (Exception e) { 
			e.printStackTrace();// EAD PUBLIC KEY ERROR 
		} finally { 
			if (inputStream != null) { 
				try { 
					inputStream.close(); 
				} catch (Exception e) { 
					e.printStackTrace(); 
				} 
			} 
		} 
		return publicKey; 
	} 
 
	 
	public static RSAPublicKey getPublicKey(InputStream inputStream) throws Exception { 
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)); 
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
			return getPublicKey(sb.toString()); 
	} 
 
	public static RSAPrivateKey getPrivateKey(InputStream inputStream, String keyAlgorithm) throws Exception { 
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream)); 
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
			return getPrivateKey(sb.toString()); 
	} 
} 
