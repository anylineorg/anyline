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

package org.anyline.data.prepare;

import org.anyline.metadata.Column;
import org.anyline.util.SQLUtil;

import java.util.LinkedHashMap;
import java.util.List;
 
public interface ConditionChain extends Condition {
	/** 
	 * 附加条件 
	 *  
	 * @param condition  condition
	 * @return ConditionChain
	 */ 
	ConditionChain addCondition(Condition condition);
 
	/** 
	 * 已拼接的条件数量 
	 *  
	 * @return int
	 */ 
	int getJoinSize();
 
	List<Condition> getConditions();
	ConditionChain clone();

	/**
	 * 过滤不存在的列
	 * @param metadatas 可用范围
	 */
	default void filter(LinkedHashMap<String, Column> metadatas) {
		List<Condition> cons = getConditions();
		if(null != cons) {
			int size = cons.size();
			for(int i=size-1; i>=0; i--) {
				Condition con = cons.get(i);
				if(con instanceof ConditionChain) {
					((ConditionChain)con).filter(metadatas);
				}else {
					String id = con.getId();
					if (null != id && SQLUtil.isSingleColumn(id) && !metadatas.containsKey(id.toUpperCase())) {
						cons.remove(con);
					}
				}
			}
		}
	}
} 
