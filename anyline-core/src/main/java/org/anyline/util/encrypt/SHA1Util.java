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
 
import org.anyline.util.ConfigTable;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
public class SHA1Util {
	private static final Log log = LogProxy.get(SHA1Util.class); 
	private static MessageDigest digest = null; 
	public static String sign(String src) {
		String result = ""; 
		try {
			if(null == digest) {
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
			log.error("sign exception:", e);
		} 
		if(ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
			log.debug("[SHA1 SIGN][src:{}][sign:{}]", src, result);
		} 
		return result; 
	} 
} 
