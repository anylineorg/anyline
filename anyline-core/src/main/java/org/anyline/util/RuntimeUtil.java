package org.anyline.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class RuntimeUtil {
	private static Logger log = Logger.getLogger(RuntimeUtil.class);
	
	public static String run(String cmd, boolean wait){
		String result = "";
		Runtime runTime = Runtime.getRuntime();
        if (runTime == null) {
            log.warn("[runtime][获取运行时环境失败]");
            return result;
        }
        try {
            Process ps = Runtime.getRuntime().exec(cmd);  
            if(wait){
            	ps.waitFor();  
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));  
            StringBuffer sb = new StringBuffer();  
            String line;  
            while ((line = br.readLine()) != null) {  
                sb.append(line).append("\n");  
            }  
            result = sb.toString();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        log.warn("[runtime][cmd:"+cmd+"][result:"+result+"]");
		return result;
	}
}
