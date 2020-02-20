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


package org.anyline.jdbc.config.db.sql.xml.impl; 
 
import java.util.List;

import org.anyline.jdbc.config.db.Condition;
import org.anyline.jdbc.config.db.ConditionChain;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicConditionChain;
import org.anyline.jdbc.config.db.sql.auto.AutoCondition;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;
 
public class XMLConditionChainImpl extends BasicConditionChain implements ConditionChain{ 

	public String getRunText(SQLCreater creater){ 
		initRunValue(); 
		StringBuilder builder = new StringBuilder(); 
		if(null != conditions){ 
			for(Condition condition: conditions){
				if(null == condition){
					continue;
				}
				String txt = ""; 
				if(condition.getVariableType() == VARIABLE_FLAG_TYPE_NONE){ 
					builder.append("\n\t");
					txt = condition.getRunText(creater); 
				}else if(condition.isActive()){ 
					builder.append("\n\t");
					txt = condition.getRunText(creater); 
					List<Object> values = condition.getRunValues();
					if(BasicUtil.isEmpty(true, values)){
						String reg = "=\\s*\\?";
						if(RegularUtil.match(txt, reg)){
							txt = txt.replaceAll(reg, " IS NULL ");
						}
					}else{
						addRunValue(values);
					}
				}
				if(BasicUtil.isNotEmpty(txt) && condition instanceof AutoCondition){
					txt = condition.getJoin() + txt;
				} 
				if(condition.isActive()){
					builder.append(txt); 
				}
			} 
		} 
		return builder.toString(); 
	} 
	public void setValue(String name, Object value){ 
		if(null != conditions){ 
			for(Condition con:conditions){
				if(null == con){
					continue;
				} 
				if(con.getId().equalsIgnoreCase(name)){ 
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
//	protected void appendCondition(StringBuilder builder){ 
//		if(null == chain){ 
//			return; 
//		} 
//		for(Condition condition: chain.getConditions()){ 
//			if(condition.getVariableType() == 2){ 
//				builder.append(condition.getRunText()); 
//			}else if(condition.isActive()){ 
//				builder.append(BR_TAB); 
//				builder.append(condition.getRunText()); 
//				addRunValue(condition.getRunValues()); 
//			} 
//		} 
//	} 
 
} 
