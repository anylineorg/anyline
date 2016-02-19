

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
