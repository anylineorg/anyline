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

package org.anyline.data.param;

import org.anyline.data.prepare.ConditionChain;
import org.anyline.entity.Compare;
import org.anyline.metadata.Column;
import org.anyline.util.SQLUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
public interface ConfigChain extends Config{
	void addConfig(Config config);
	Config getConfig(String prefix, String var);
	List<Config> getConfigs(String prefix, String var);
	Config getConfig(String prefix, String var, Compare type);
	List<Config> getConfigs(String prefix, String var, Compare type);

	ConfigChain removeConfig(Config config);
	ConfigChain removeConfig(List<Config> config);
	ConfigChain removeConfig(String prefix, String  var);
	ConfigChain removeConfig(String prefix, String var, Compare type);
	void setValue(Map<String,Object> values);
	List<Config> getConfigs();
	ConditionChain createAutoConditionChain();

	ConfigChain clone();
	/**
	 * 过滤不存在的列
	 * @param metadatas 可用范围
	 */
	default void filter(LinkedHashMap<String, Column> metadatas) {
		List<Config> configs = getConfigs();
		if(null != configs) {
			int size = configs.size();
			for(int i=size-1; i>=0; i--) {
				Config config = configs.get(i);
				if(config instanceof ConfigChain) {
					((ConfigChain)config).filter(metadatas);
				}else {
					String key = config.getVariable();
					if (null != key && SQLUtil.isSingleColumn(key) && !metadatas.containsKey(key.toUpperCase())) {
						configs.remove(key);
					}
				}
			}
		}
	}
}
