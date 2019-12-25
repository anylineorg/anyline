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


package org.anyline.jdbc.config.db.sql.auto.impl; 
 
import java.util.ArrayList;
import java.util.List;

import org.anyline.jdbc.config.Config;
import org.anyline.jdbc.config.ConfigChain;
import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicConditionChain;
import org.anyline.util.BasicUtil;
 
public class AutoConditionChainImpl extends BasicConditionChain implements ConditionChain{
	public AutoConditionChainImpl(){}
	public AutoConditionChainImpl(ConfigChain chain){
		if(null == chain){
			return;
		}
		for(Config config:chain.getConfigs()){
			if(config instanceof ConfigChain){
				conditions.add(new AutoConditionChainImpl((ConfigChain)config));
			}else{
				conditions.add(new AutoConditionImpl(config));
			}
		}
	} 
	public String getRunText(SQLCreater creater){ 
		runValues = new ArrayList<Object>(); 
		int size = conditions.size(); 
		if(size == 0){ 
			return ""; 
		}
		StringBuilder subBuilder = new StringBuilder();
		String txt = "";
		for(int i=0; i<size; i++){
			Condition condition = conditions.get(i);
//			if(condition.isContainer()){
//				txt = ((ConditionChain) condition).getRunText(creater);
//			}else{
//				txt = condition.getRunText(creater);
//			}
			txt = condition.getRunText(creater);
			if(BasicUtil.isEmpty(txt)){
				continue;
			}
			List<Object> values = condition.getRunValues();
			if(condition.getVariableType() == Condition.VARIABLE_FLAG_TYPE_NONE 
					|| !BasicUtil.isEmpty(true, values) 
					|| condition.isActive()
					|| condition.isRequired()){
				//condition instanceof ConditionChain
				//if(i>0 /*&& !condition.isContainer()*/){
				if(i>0){
					String chk = txt.toLowerCase().trim();
					if(!chk.startsWith("and") && !chk.startsWith("or")){
						subBuilder.append(condition.getJoin());
					}
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

	public String toString(){
		int size = conditions.size();
		String txt = "[";
		for(int i=0;i<size; i++){
			if(i==0){
				txt += conditions.get(i).toString();
			}else{
				txt += ","+conditions.get(i).toString();
			}
		}
		txt += "]";
		return txt;
	}
} 
