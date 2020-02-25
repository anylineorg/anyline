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


package org.anyline.jdbc.config; 
 
import java.util.List;
import java.util.Map;

import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.SQL;
 
public interface ConfigChain extends Config{
	public void addConfig(Config config);
	public Config getConfig(String key);
	public Config getConfig(String key, SQL.COMPARE_TYPE type);
	
	public ConfigChain removeConfig(Config config);
	public ConfigChain removeConfig(String key);
	public ConfigChain removeConfig(String key, SQL.COMPARE_TYPE type); 
	public void setValue(Map<String,Object> values); 
	public List<Config> getConfigs(); 
	public ConditionChain createAutoConditionChain(); 
}
