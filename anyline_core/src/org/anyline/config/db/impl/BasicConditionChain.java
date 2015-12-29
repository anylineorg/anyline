
package org.anyline.config.db.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;

/**
 * 自动生成的参数
 * @author Administrator
 *
 */
public abstract class BasicConditionChain extends BasicCondition implements ConditionChain{
	protected List<Condition> conditions = new ArrayList<Condition>();;
	protected int joinSize;
	
	public void init(){
		for(Condition condition:conditions){
			condition.init();
		}
	}
	/**
	 * 附加条件
	 * @param condition
	 * @return
	 */
	public ConditionChain addCondition(Condition condition){
		conditions.add(condition);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	protected void addRunValue(Object value){
		if(null == value){
			return;
		}
		if(value instanceof Collection){
			runValues.addAll((Collection<Object>)value);
		}else{
			runValues.add(value);
		}
	}
	public List<Object> getRunValues(){
		return runValues;
	}
	public String getJoin(){
		return Condition.CONDITION_JOIN_TYPE_AND;
	}
	public int getJoinSize(){
		return joinSize;
	}
	public List<Condition> getConditions() {
		return conditions;
	}
	
}
