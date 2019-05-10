/* 
 * Copyright 2006-2015 www.anyline.org
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

import java.sql.Timestamp;
import java.util.Date;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class JSONDateFormatProcessor implements JsonValueProcessor {  
    private String format ="yyyy-MM-dd HH:mm:ss";  
      
    public JSONDateFormatProcessor() {  
        super();  
    }  
      
    public JSONDateFormatProcessor(String format) {  
        super();  
        this.format = format;  
    }  
  
    public Object processArrayValue(Object paramObject, JsonConfig paramJsonConfig) {  
        return process(paramObject);  
    }  
  
    public Object processObjectValue(String paramString, Object paramObject,  
            JsonConfig paramJsonConfig) {  
        return process(paramObject);  
    }  
      
      
    private Object process(Object value){ 
    	if(null == value){
    		return null;
    	}
        if(value instanceof Date){    
            String val = DateUtil.format((Date)value, format);
            return val;  
        }
        if(value instanceof Timestamp){
            String val = DateUtil.format((Timestamp)value, format);
            return val;  
        }
        return value == null ? "" : value.toString();    
    }  
  
}  