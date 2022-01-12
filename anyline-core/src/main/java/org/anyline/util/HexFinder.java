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
 
 
public class HexFinder { 
	public static String format(byte[] bt) { 
		int line = 0; 
		StringBuilder buf = new StringBuilder(); 
		for (byte d : bt) { 
			if (line % 16 == 0) 
				buf.append(String.format("%05x: ", line)); 
			buf.append(String.format("%02x ", d)); 
			line++; 
			if (line % 16 == 0) 
				buf.append("\n"); 
		} 
		buf.append("\n"); 
		return buf.toString(); 
	} 
} 
