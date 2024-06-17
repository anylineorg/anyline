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

import org.anyline.util.BasicUtil;
import org.anyline.util.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
 
public class MD5Util {
	private static final Logger log = LoggerFactory.getLogger(MD5Util.class);  

	public static String sign(String src) {
		return sign(src, "UTF-8");
	} 
	 
	public static String sign(String src, String encode) {
		if(null == src) return ""; 
		String result = null;
		 
        if (!src.isEmpty()) {
            try{

        		MessageDigest md = MessageDigest.getInstance("MD5");
        		if (BasicUtil.isEmpty(encode))
        			result = NumberUtil.byte2hex(md.digest(src.getBytes()));
        		else
        			result = NumberUtil.byte2hex(md.digest(src.getBytes(encode)));
        		 
            } catch(Exception ex) {
				log.error("md5 sign exception:", ex);
            }      
        }
        return result;      
	}

	/** 
	 * 字符MD5加密 
	 * @param src  src
	 * @return String
	 */ 
	public static String crypto(String src) {
		return sign(src, "UTF-8");
    }  
	public static String crypto(String src, String encode) {
		return sign(src, encode);
    }  
	public static String crypto2(String str) {
		return crypto2(str, "UTF-8");
	} 
	public static String crypto2(String str, String encode) {
		return crypto(crypto(str, encode), encode);
	} 

    /** 
    * 获取单个文件的MD5值！ 
    * @param file  文件
    * @return MD5
    */ 
    public static String crypto(File file) {
	    if (null == file || !file.isFile() || !file.exists()) {
	    	return null; 
	    } 
	    MessageDigest digest = null; 
	    FileInputStream in=null; 
	    byte buffer[] = new byte[1024]; 
	    int len; 
	    try {
		    digest = MessageDigest.getInstance("MD5"); 
		    in = new FileInputStream(file); 
		    while ((len = in.read(buffer, 0, 1024)) != -1) {
		    	digest.update(buffer, 0, len);
		    } 
		    in.close(); 
	    } catch (Exception e) {
			log.error("crypto file exception:", e);
	    	return null; 
	    } 
	    BigInteger bigInt = new BigInteger(1, digest.digest());
	    return bigInt.toString(16); 
    } 
 
    /** 
    * 获取文件夹中文件的MD5值 
    * @param file  file
    * @param recursion  true递归子目录中的文件
    * @return Map
    */ 
    public static Map<String, String> crypto(File file, boolean recursion) {
	    if(null == file || !file.isDirectory() || !file.exists()) {
	    	return null; 
	    } 
	    Map<String, String> map=new HashMap<>();
	    String md5; 
	    File files[]=file.listFiles(); 
	    for(int i=0;i<files.length;i++) {
	    	File f=files[i]; 
	    	if(f.isDirectory()&&recursion) {
	    		map.putAll(crypto(f, recursion));
	    	} else {
	    		md5=crypto(f);
	    		if(md5!=null) {
	    			map.put(f.getPath(), md5);
	    		} 
	    	} 
	    } 
	    return map; 
    }

} 
