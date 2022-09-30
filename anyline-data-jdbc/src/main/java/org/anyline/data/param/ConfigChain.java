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


package org.anyline.data.param;

import org.anyline.entity.Compare;
import org.anyline.data.prepare.ConditionChain;

import java.util.List;
import java.util.Map;
 
public interface ConfigChain extends Config{
	public void addConfig(Config config);
	public Config getConfig(String prefix, String var);
	public Config getConfig(String prefix, String var, Compare type);
	
	public ConfigChain removeConfig(Config config);
	public ConfigChain removeConfig(String prefix, String  var);
	public ConfigChain removeConfig(String prefix, String var, Compare type);
	public void setValue(Map<String,Object> values); 
	public List<Config> getConfigs(); 
	public ConditionChain createAutoConditionChain(); 
}
