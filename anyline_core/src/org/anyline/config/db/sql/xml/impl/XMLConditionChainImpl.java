

package org.anyline.config.db.sql.xml.impl;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.impl.BasicConditionChain;

public class XMLConditionChainImpl extends BasicConditionChain implements ConditionChain{

	public String getRunText(String disKey){
		initRunValue();
		StringBuilder builder = new StringBuilder();
		if(null != conditions){
			for(Condition condition: conditions){
				if(condition.getVariableType() == VARIABLE_FLAG_TYPE_NONE){
					builder.append("\n\t");
					builder.append(condition.getRunText(disKey));
				}else if(condition.isActive()){
					builder.append("\n\t");
					builder.append(condition.getRunText(disKey));
					addRunValue(condition.getRunValues());
				}
			}
		}
		return builder.toString();
	}
	public void setValue(String name, Object value){
		if(null != conditions){
			for(Condition con:conditions){
				if(con.getId().equalsIgnoreCase(name)){
					con.setValue(name, value);
					break;
				}
			}
		}
	}
	/**
	 * 拼接查询条件
	 * @param builder
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
