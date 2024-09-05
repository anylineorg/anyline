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

package org.anyline.data.prepare.xml.init;

import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.auto.AutoCondition;
import org.anyline.data.prepare.init.AbstractConditionChain;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import java.util.List;
 
public class DefaultXMLConditionChain extends AbstractConditionChain implements ConditionChain{

	@Override
	public String getRunText(int lvl, String prefix, DataRuntime runtime, boolean placeholder, boolean unicode) {
		initRunValue(); 
		StringBuilder builder = new StringBuilder(); 
		if(null != conditions) {
			for(Condition condition: conditions) {
				if(null == condition) {
					continue;
				}
				String txt = ""; 
				if(condition.getVariableType() == VARIABLE_PLACEHOLDER_TYPE_NONE) {
					txt = condition.getRunText(prefix, runtime, placeholder, unicode);
				}else if(condition.isActive()) {
					txt = condition.getRunText(prefix, runtime, placeholder, unicode);
					List<RunValue> values = condition.getRunValues();
					if(BasicUtil.isEmpty(true, values)) {
						String reg = "=\\s*\\?";
						if(RegularUtil.match(txt, reg)) {
							txt = txt.replaceAll(reg, " IS NULL ");
						}
					}else{
						addRunValue(values);
					}
				}
				if(BasicUtil.isNotEmpty(txt) && condition instanceof AutoCondition) {
					txt = condition.getJoinText() + txt;
				}
				if(condition.isActive()) {
					builder.append("\n\t");
					builder.append(txt.trim());
				}
			} 
		} 
		return builder.toString(); 
	}

	@Override
	public Condition setRunText(String text) {
		this.text = text;
		return this;
	}

	public void setValue(String name, Object value) {
		if(null != conditions) {
			for(Condition con:conditions) {
				if(null == con) {
					continue;
				} 
				if(con.getId().equalsIgnoreCase(name)) {
					con.setValue(name, value); 
					break; 
				} 
			} 
		} 
	} 
	/** 
	 * 拼接查询条件 
	 * @param builder  builder
	 */ 
//	protected void appendCondition(StringBuilder builder) {
//		if(null == chain) {
//			return; 
//		} 
//		for(Condition condition: chain.getConditions()) {
//			if(condition.getVariableType() == 2) {
//				builder.append(condition.getRunText()); 
//			}else if(condition.isActive()) {
//				builder.append(BR_TAB); 
//				builder.append(condition.getRunText()); 
//				addRunValue(condition.getRunValues()); 
//			} 
//		} 
//	} 
 
} 
