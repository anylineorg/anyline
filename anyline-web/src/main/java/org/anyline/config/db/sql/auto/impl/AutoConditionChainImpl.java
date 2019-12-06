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


package org.anyline.config.db.sql.auto.impl; 
 
import java.util.ArrayList;
import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.impl.BasicConditionChain;
import org.anyline.config.http.Config;
import org.anyline.config.http.ConfigChain;
import org.anyline.util.BasicUtil;
 
public class AutoConditionChainImpl extends BasicConditionChain implements ConditionChain{
	private static final long serialVersionUID = -3221296171103784900L;
	public AutoConditionChainImpl(){}
	public AutoConditionChainImpl(ConfigChain chain){
		if(null == chain){
			return;
		}
		for(Config config:chain.getConfigs()){
			conditions.add(new AutoConditionImpl(config));
		}
	} 
	public String getRunText(SQLCreater creater){ 
		runValues = new ArrayList<Object>(); 
		int size = conditions.size(); 
		if(size == 0){ 
			return ""; 
		}
		
		StringBuilder subBuilder = new StringBuilder();

		for(int i=0; i<size; i++){
			Condition condition = conditions.get(i);
			String txt = condition.getRunText(creater);
			if(BasicUtil.isEmpty(txt)){
				continue;
			}
			List<Object> values = condition.getRunValues();
			if(condition.getVariableType() == Condition.VARIABLE_FLAG_TYPE_NONE 
					|| !BasicUtil.isEmpty(true, values) 
					|| condition.isActive()
					|| condition.isRequired()){
				
				if(i>0 /*&& !condition.isContainer()*/){
					subBuilder.append(condition.getJoin());
				}
				subBuilder.append(txt);
				addRunValue(values);
				joinSize ++;
			}
		}
 
		if(joinSize > 0){
			StringBuilder builder = new StringBuilder();
			if(!hasContainer() || getContainerJoinSize() > 0){
				builder.append("\nAND");
			}else{
				builder.append("\n\t");
			}
			builder.append("(");
			builder.append(subBuilder.toString());
			builder.append(")\n\t");
			return builder.toString(); 
		}else{
			return "";
		} 
	} 
	private int getContainerJoinSize(){ 
		if(hasContainer()){ 
			return getContainer().getJoinSize(); 
		}else{ 
			return 0; 
		} 
	}
 
} 
