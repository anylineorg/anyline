package org.anyline.util;

import java.util.Date;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class JSONDateFormatProcessor implements JsonValueProcessor {  
    private String format ="yyyy-MM-dd hh:mm:ss";  
      
    public JSONDateFormatProcessor() {  
        super();  
    }  
      
    public JSONDateFormatProcessor(String format) {  
        super();  
        this.format = format;  
    }  
  
    @Override  
    public Object processArrayValue(Object paramObject,  
            JsonConfig paramJsonConfig) {  
        return process(paramObject);  
    }  
  
    @Override  
    public Object processObjectValue(String paramString, Object paramObject,  
            JsonConfig paramJsonConfig) {  
        return process(paramObject);  
    }  
      
      
    private Object process(Object value){  
        if(value instanceof Date){    
            String val = DateUtil.format((Date)value, format);
            return val;  
        }    
        return value == null ? "" : value.toString();    
    }  
  
}  