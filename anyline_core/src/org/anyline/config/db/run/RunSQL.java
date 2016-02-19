

package org.anyline.config.db.run;

import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.PageNavi;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.http.ConfigStore;

public interface RunSQL {
	public void setCreater(SQLCreater creater);
	public void init();
	
	/**
	 * 添加查询条件
	 * @param	condition
	 * 			列名|查询条件ID
	 * @param	variable
	 * 			变量key
	 * @param	value
	 * 			值
	 */
	public RunSQL setConditionValue(String condition, String variable, Object value);
	public void setGroupStore(GroupStore groups) ;
	public GroupStore getGroupStore() ;
	public RunSQL group(String group);

	public void setOrderStore(OrderStore orders) ;
	public void setOrders(String[] orders);
	public OrderStore getOrderStore() ;
	public RunSQL order(String order);
	
	public void setConfigStore(ConfigStore configs);
	public ConfigStore getConfigStore() ;
	

	/**
	 * 添加查询条件
	 * @param	required 是否必须
	 * @param	column 列名
	 * @param	value 值
	 * @param	compare 比较方式
	 */
	public RunSQL addCondition(boolean requried, String column, Object value, int compare);
	public RunSQL setConditionChain(ConditionChain chain);
	public RunSQL addConditions(String[] conditions) ;
	public RunSQL addCondition(String condition);
	public RunSQL addCondition(Condition condition) ;
	public Condition getCondition(String name);
	public ConditionChain getConditionChain() ;
	
	/**
	 * 添加静态文本查询条件
	 */
	/**
	 * 添加参数值
	 * @param obj
	 * @return
	 */
	public RunSQL addValues(Object obj);
	public RunSQL addOrders(OrderStore orderStore);
	public RunSQL addOrder(Order order);
	public SQL getSql() ;
	public RunSQL setSql(SQL sql) ;
	public String getBaseQueryTxt() ;
	public String getFinalQueryTxt() ;
	public String getTotalQueryTxt() ;
	public String getInsertTxt();
	public String getDeleteTxt();
	public String getUpdateTxt();
	public String getExecuteTxt();
	public List<Object> getValues() ;
	public PageNavi getPageNavi() ;
	public void setPageNavi(PageNavi pageNavi) ;
	public StringBuilder getBuilder() ;
	public void setBuilder(StringBuilder builder) ;
	
}
