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
 
package org.anyline.util; 
 
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SQLUtil {
	static final Logger log = LoggerFactory.getLogger(SQLUtil.class);


	public static StringBuilder delimiter(StringBuilder builder, String src, String delimiter){
		if(!ConfigTable.IS_SQL_DELIMITER_OPEN){
			builder.append(src);
			return builder;
		}
		if(delimiter == null){
			builder.append(src);
			return builder;
		}
		String delimiterFr = "";
		String delimiterTo = "";
		delimiter = delimiter.replaceAll("\\s", "");
		if(delimiter.length() == 0){
			return builder;
		}else if(delimiter.length() ==1){
			delimiterFr = delimiter;
			delimiterTo = delimiter;
		}else{
			delimiterFr = delimiter.substring(0,1);
			delimiterTo = delimiter.substring(1,2);
		}
		return delimiter(builder, src, delimiterFr, delimiterTo);
	}
	public static StringBuilder delimiter(StringBuilder builder, String src, String delimiterFr, String delimiterTo){
		if(!ConfigTable.IS_SQL_DELIMITER_OPEN){
			builder.append(src);
			return builder;
		}
		src = src.trim();
		if(src.startsWith(delimiterFr) || src.endsWith(delimiterTo)){
			builder.append(src);
			return builder ;
		}
		String[] holder = placeholder();
		if(null != holder){
			if(src.startsWith(holder[0]) || src.endsWith(holder[1])){
				builder.append(src);
				return builder ;
			}
		}
		if(src.contains(".")){
			String[] cols = src.split("\\.");
			int size = cols.length;
			for(int i=0; i<size; i++){
				String col = cols[i];
				builder.append(delimiterFr).append(col).append(delimiterTo);
				if(i < size-1){
					builder.append(".");
				}
			}
		}else {
			builder.append(delimiterFr).append(src).append(delimiterTo);
		}

		return builder ;
	}
	public static String delimiter(String src, String delimiterFr, String delimiterTo){
		if(!ConfigTable.IS_SQL_DELIMITER_OPEN){
			return src;
		}
		if(src.startsWith(delimiterFr) || src.endsWith(delimiterTo)){
			return src ;
		}
		String result = SQLUtil.delimiter(new StringBuilder(), src, delimiterFr, delimiterTo).toString();
		return result;
	}

	public static String placeholder(String src, String delimiterFr, String delimiterTo){
		if(null == src){
			return src;
		}

		// 未开启占位符
		if(!ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			return src;
		}
		String[] holder = placeholder();
		if(null == holder){
			return src;
		}

		String holderFr = holder[0];
		String holderTo = holder[1];
		if(null == holderFr || null == holderTo || null == delimiterFr || null == delimiterTo){
			return src;
		}
		if(holderFr.equals(holderTo) && delimiterFr.equals(delimiterTo)) {
			src = src.replace(holderFr, delimiterFr);
		}else{
			try {
				String regxFr = holderFr.replace("(","\\(").replace("{", "、\\{").replace("[", "\\[");
				String regxTo = holderTo.replace(")","\\)").replace("}", "、\\}").replace("]", "\\]");
				List<List<String>> lists = RegularUtil.fetchs(src, "("+regxFr+")" + "(.+?)" + "("+regxTo+")");
				for(List<String> list: lists){
					String full = list.get(0);
					// String fr = list.get(1);
					String key = list.get(2).trim();
					// String to = list.get(3);
					String replace = delimiterFr + key + delimiterTo;
					src = src.replace(full, replace);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		return src;
	}

	private static String config_holder = null;
	private static String[] static_holder = null;
	public static String[] placeholder(){
		if (config_holder != null && config_holder.equals(ConfigTable.SQL_DELIMITER_PLACEHOLDER)) {
			if(null != static_holder){
				return static_holder;
			}
		}
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN){
			config_holder = ConfigTable.SQL_DELIMITER_PLACEHOLDER;
			if(null == config_holder){
				return null;
			}
			String holderFr = "";
			String holderTo = "";
			config_holder = config_holder.replaceAll("\\s", "");
			if(config_holder.length() == 0){
				return null;
			}else if(config_holder.length() ==1){
				holderFr = config_holder;
				holderTo = config_holder;
			}else{
				holderFr = config_holder.substring(0,1);
				holderTo = config_holder.substring(1,2);
			}
			static_holder = new String[]{holderFr, holderTo};
			return static_holder;
		}
		return null;
	}
} 
