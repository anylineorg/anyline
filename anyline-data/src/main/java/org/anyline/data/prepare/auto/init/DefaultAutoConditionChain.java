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
 */

package org.anyline.data.prepare.auto.init;

import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.init.AbstractConditionChain;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.Compare;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;

import java.util.ArrayList;
import java.util.List;

public class DefaultAutoConditionChain extends AbstractConditionChain implements ConditionChain {
	public DefaultAutoConditionChain() {}
	public DefaultAutoConditionChain(ConfigChain chain) {
		if(null == chain) {
			return;
		}
		for(Config config:chain.getConfigs()) {
			if(config instanceof ConfigChain) {
				conditions.add(new DefaultAutoConditionChain((ConfigChain)config).setJoin(config.getJoin()));
			}else{
				conditions.add(new DefaultAutoCondition(config).setJoin(config.getJoin()));
			}
		}
		this.integrality(chain.integrality());
	}
	private static String TAB = "\t";
	private static String BR = "\n";
	private void tab(int lvl, boolean br, StringBuilder builder) {
		if(br) {
			builder.append(BR);
		}
		for(int i=0; i<lvl; i++) {
			builder.append(TAB);
		}
	}
	@Override
	public String getRunText(int lvl, String prefix, DataRuntime runtime, Boolean placeholder, Boolean unicode) {
		runValues = new ArrayList<>();
		int size = conditions.size();
		int active = 0; //有效条件
		if(size == 0) {
			return ""; 
		}
		StringBuilder subBuilder = new StringBuilder();
		String txt = "";
		for(int i=0; i<size; i++) {
			Condition condition = conditions.get(i);
			if(null == condition || condition.isVariableSlave()) {
				continue;
			}
			txt = condition.getRunText(lvl+1, prefix, runtime, placeholder, unicode);
			if(BasicUtil.isEmpty(txt)) {
				continue;
			}
			active ++;
			List<RunValue> values = condition.getRunValues();
			if(condition.getVariableType() == Condition.VARIABLE_PLACEHOLDER_TYPE_NONE 
					|| !BasicUtil.isEmpty(true, values)
					|| condition.isActive()
					|| condition.getSwt() == Compare.EMPTY_VALUE_SWITCH.NULL
					|| condition.getSwt() == Compare.EMPTY_VALUE_SWITCH.SRC
			) {

				txt = SQLUtil.trim(txt);
				if(joinSize>0) {
					if(txt.startsWith("(")) {
						tab(lvl+1, true, subBuilder);
					}
					subBuilder.append(condition.getJoinText());
				}
				if(subBuilder.length() > 0 && !txt.startsWith(" ") && !txt.startsWith("(")) {
					subBuilder.append(" ");
				}

				subBuilder.append(txt);
				addRunValue(values);
				joinSize ++;
			}
		}
 
		if(joinSize > 0) {
			StringBuilder builder = new StringBuilder();
			//没有上一级 或者上一级中的已经添加了其他条件
			if(!hasContainer() || getContainerJoinSize() > 0) {
				builder.append("\n").append(join).append(" ");
			}else{
				builder.append("\n\t");
			}
			/*String sub = subBuilder.toString().trim();
			String chk = sub.toUpperCase().replaceAll("\\s"," ");
			if(chk.startsWith("AND(") || chk.startsWith("AND ")) {
				sub = sub.substring(3).trim();
			}
			if(chk.startsWith("OR(") || chk.startsWith("OR ")) {
				sub = sub.substring(2).trim();
			}*/
			String sub = SQLUtil.trim(subBuilder.toString());

			//子串有没有()
			/*boolean pack = sub.startsWith("(") && sub.endsWith(")");
			if(!pack && integrality) {
				builder.append("(");
			}
			builder.append(sub);
			if(!pack && integrality) {
				builder.append(")");
			}*/

			if(integrality && active >1) {
				builder.append("(");
			}
			builder.append(sub);
			if(integrality && active >1) {
				builder.append(")");
			}

			builder.append("\n\t");
			return builder.toString().replaceAll("(\n\\s*\n+)", "\n");
		}else{
			return "";
		} 
	}

	@Override
	public Condition setRunText(String text) {
		this.text = text;
		return this;
	}

	private int getContainerJoinSize() {
		if(hasContainer()) {
			return getContainer().getJoinSize(); 
		}else{
			return 0; 
		} 
	}

	public String toString() {
		int size = conditions.size();
		String txt = "[";
		for(int i=0;i<size; i++) {
			if(i==0) {
				txt += conditions.get(i);
			}else{
				txt += ","+conditions.get(i);
			}
		}
		txt += "]";
		return txt;
	}
} 
