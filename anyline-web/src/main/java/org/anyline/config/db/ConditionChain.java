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


package org.anyline.config.db; 
 
import java.util.List; 
 
public interface ConditionChain extends Condition { 
	/** 
	 * 附加条件 
	 *  
	 * @param condition  condition
	 * @return return
	 */ 
	public ConditionChain addCondition(Condition condition); 
 
	/** 
	 * 已拼接的条件数量 
	 *  
	 * @return return
	 */ 
	public int getJoinSize(); 
 
	public List<Condition> getConditions(); 
} 
