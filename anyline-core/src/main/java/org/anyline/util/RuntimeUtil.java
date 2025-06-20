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

import java.io.BufferedReader;
import java.io.InputStreamReader;
 
public class RuntimeUtil {
	private static final Log log = LogProxy.get(RuntimeUtil.class); 
	 
	public static String run(String cmd, boolean wait) {
		String result = ""; 
		Runtime runTime = Runtime.getRuntime(); 
        if (runTime == null) {
            log.warn("[runtime][获取运行时环境失败]"); 
            return result; 
        } 
        try {
            Process ps = Runtime.getRuntime().exec(cmd);   
            if(wait) {
            	ps.waitFor();   
            } 
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));   
            StringBuilder builder = new StringBuilder();
            String line;   
            while ((line = br.readLine()) != null) {
                builder.append(line).append("\n");
            }   
            result = builder.toString();
        } catch (Exception e) {
            log.error("run exception:", e);
        } 
         
        log.info("[runtime][cmd:"+cmd+"][result:"+result+"]");
		return result; 
	} 
	public static String run(String cmd) {
		return run(cmd, true);
	} 
} 
