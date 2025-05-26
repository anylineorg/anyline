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

package org.anyline.util;

import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.io.*;
 
public class ObjectUtil {
	private static Log log = LogProxy.get(ObjectUtil.class);
	public static byte[] serialize(Object obj) {
		ObjectOutputStream obi = null; 
		ByteArrayOutputStream bai = null; 
		try {
			bai = new ByteArrayOutputStream(); 
			obi = new ObjectOutputStream(bai); 
			obi.writeObject(obj); 
			byte[] byt = bai.toByteArray(); 
			return byt; 
		} catch (IOException e) {
			log.error("serialize 异常:", e);
		} 
		return null; 
	} 
 
	public static Object unserizlize(byte[] byt) {
		ObjectInputStream oii = null; 
		ByteArrayInputStream bis = null; 
		bis = new ByteArrayInputStream(byt); 
		try {
			oii = new ObjectInputStream(bis); 
			Object obj = oii.readObject(); 
			return obj; 
		} catch (Exception e) {
			log.error("unserizlize 异常:", e);
		} 
		return null; 
	} 
} 
