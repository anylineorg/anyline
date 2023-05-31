/* 
 * Copyright 2006-2023 www.anyline.org
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


package org.anyline.data.prepare.auto.init;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.init.DefaultConditionChain;
import org.anyline.data.run.RunValue;
import org.anyline.util.BasicUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DefaultAutoConditionChain extends DefaultConditionChain implements ConditionChain {
	public DefaultAutoConditionChain(){}
	public DefaultAutoConditionChain(ConfigChain chain){
		if(null == chain){
			return;
		}
		for(Config config:chain.getConfigs()){
			if(config instanceof ConfigChain){
				conditions.add(new DefaultAutoConditionChain((ConfigChain)config));
			}else{
				conditions.add(new DefaultAutoCondition(config));
			}
		}
	}
	@Override
	public String getRunText(String prefix, JDBCAdapter adapter){
		runValues = new ArrayList<>();
		int size = conditions.size(); 
		if(size == 0){ 
			return ""; 
		}
		StringBuilder subBuilder = new StringBuilder();
		String txt = "";
		for(int i=0; i<size; i++){
			Condition condition = conditions.get(i);
//			if(condition.isContainer()){
//				txt = ((ConditionChain) condition).getRunText(adapter);
//			}else{
//				txt = condition.getRunText(adapter);
//			}
			if(null == condition || condition.isVariableSlave()){
				continue;
			}
			txt = condition.getRunText(prefix, adapter);
			if(BasicUtil.isEmpty(txt)){
				continue;
			}
			List<RunValue> values = condition.getRunValues();
			if(condition.getVariableType() == Condition.VARIABLE_FLAG_TYPE_NONE 
					|| !BasicUtil.isEmpty(true, values) 
					|| condition.isActive()
					|| condition.isRequired()){
				// condition instanceof ConditionChain
				// if(i>0 /*&& !condition.isContainer()*/){
				if(joinSize>0){
					String chk = txt.toLowerCase().trim().replace("\n"," ").replace("\t", " ");
					if(!chk.startsWith("and ") && !chk.startsWith("or ") && !chk.startsWith("and(") && !chk.startsWith("or(")){
						subBuilder.append(condition.getJoin());
					}
				}
				if(subBuilder.length() > 0 && !txt.startsWith(" ") && !txt.startsWith("(")){
					subBuilder.append(" ");
				}

				subBuilder.append(txt);
				addRunValue(values);
				joinSize ++;
			}
		}
 
		if(joinSize > 0){
			StringBuilder builder = new StringBuilder();
			//没有上一级 或者上一级中的已经添加了其他条件
			if(!hasContainer() || getContainerJoinSize() > 0){
				builder.append("\nAND");
			}else{
				builder.append("\n\t");
			}
			String sub = subBuilder.toString().trim();
			String chk = sub.toUpperCase().replaceAll("\\s", " ");
			if(chk.startsWith("AND(") || chk.startsWith("AND ")){
				sub = sub.substring(3).trim();
			}
			if(chk.startsWith("OR(") || chk.startsWith("OR ")){
				sub = sub.substring(2).trim();
			}
			boolean pack = sub.startsWith("(") && sub.endsWith(")");
			if(!pack) {
				builder.append("(");
			}
			builder.append(sub);
			if(!pack) {
				builder.append(")");
			}
			builder.append("\n\t");
			return builder.toString(); 
		}else{
			return "";
		} 
	}

	@Override
	public Condition setRunText(String text) {
		this.text = text;
		return this;
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
