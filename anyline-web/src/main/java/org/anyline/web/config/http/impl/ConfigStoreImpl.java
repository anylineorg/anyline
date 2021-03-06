/* 
 * Copyright 2006-2020 www.anyline.org
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


package org.anyline.web.config.http.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.jdbc.config.Config;
import org.anyline.web.config.http.ConfigStore;


/**
 * 查询参数 
 * @author zh 
 * 
 */ 
public class ConfigStoreImpl extends org.anyline.jdbc.config.impl.ConfigStoreImpl implements ConfigStore{
	@Override 
	public ConfigStore setValue(HttpServletRequest request){
		if(null == chain){ 
			return this; 
		} 
		List<Config> configs = chain.getConfigs(); 
		for(Config config:configs){
			if(null == config){
				continue;
			} 
			config.setValue(request); 
		} 
		setNaviParam();
		return this; 
	}

} 
 
 
