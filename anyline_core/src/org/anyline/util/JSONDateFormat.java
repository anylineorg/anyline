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
 */
package org.anyline.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class JSONDateFormat  implements JsonValueProcessor {   
	    
		  private String format ="yyyy-MM-dd";   
		     
		  public Object processArrayValue(Object value, JsonConfig config) {   
		    return process(value);   
		  }   
		  
		  public Object processObjectValue(String key, Object value, JsonConfig config) {   
		    return process(value);   
		  }   
		     
		  private Object process(Object value){   
		       
		    if(value instanceof Date){   
		      SimpleDateFormat sdf = new SimpleDateFormat(format,Locale.UK);   
		      return sdf.format(value);   
		    }   
		    return value == null ? "" : value.toString();   
		  }   
		}
