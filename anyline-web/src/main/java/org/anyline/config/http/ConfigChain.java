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


package org.anyline.config.http;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.SQL;

public interface ConfigChain extends Config{
	public void addConfig(Config config);
	public Config getConfig(String key);
	public Config getConfig(String key, SQL.COMPARE_TYPE type);
	
	public ConfigChain removeConfig(Config config);
	public ConfigChain removeConfig(String key);
	public ConfigChain removeConfig(String key, SQL.COMPARE_TYPE type);

	/**
	 * 赋值
	 * @param request
	 */
	public void setValue(HttpServletRequest request);
	public List<Config> getConfigs();
	public ConditionChain createAutoConditionChain();
}