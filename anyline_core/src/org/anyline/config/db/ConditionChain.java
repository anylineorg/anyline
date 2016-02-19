

package org.anyline.config.db;

import java.util.List;

public interface ConditionChain extends Condition {
	/**
	 * 附加条件
	 * 
	 * @param condition
	 * @return
	 */
	public ConditionChain addCondition(Condition condition);

	/**
	 * 已拼接的条件数量
	 * 
	 * @return
	 */
	public int getJoinSize();

	public List<Condition> getConditions();
}
