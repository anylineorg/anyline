package org.anyline.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

public class SHA1Util {
	private static Logger log = Logger.getLogger(SHA1Util.class);
	private static MessageDigest digest = null;
	public static String sign(String src){
		if(ConfigTable.isDebug()){
			log.warn("[SHA1 SIGN][src:" + src+"]");
		}
		String result = "";
		try {
			if(null == digest){
				digest = MessageDigest.getInstance("SHA-1");
			}
			digest.update(src.getBytes());
			byte messageDigest[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			result = hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if(ConfigTable.isDebug()){
			log.warn("[SHA1 SIGN][sign:" + result+"]");
		}
		return result;
	}
}
