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


package org.anyline.config.db.sql.auto.impl;

import java.util.ArrayList;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.impl.BasicConditionChain;

public class AutoConditionChainImpl extends BasicConditionChain implements ConditionChain{
	public String getRunText(String disKey){
		runValues = new ArrayList<Object>();
		int size = conditions.size();
		if(size == 0){
			return "";
		}
		StringBuilder builder = new StringBuilder();
		if(!hasContainer() || getContainerJoinSize() > 0){
			builder.append(" AND");
		}else{
			builder.append("\n\t");
		}
		builder.append("(");
		for(int i=0; i<size; i++){
			Condition condition = conditions.get(i);
			if(i>0 && !condition.isContainer()){
				builder.append(condition.getJoin());
			}
			builder.append(condition.getRunText(disKey));
			addRunValue(condition.getRunValues());
			joinSize ++;
		}


		builder.append(")\n\t");
		
		return builder.toString();
	}
	private int getContainerJoinSize(){
		if(hasContainer()){
			return getContainer().getJoinSize();
		}else{
			return 0;
		}
	}


}
