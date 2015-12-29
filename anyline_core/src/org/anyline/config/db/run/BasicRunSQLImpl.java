
package org.anyline.config.db.run;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.PageNavi;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.SQLVariable;
import org.anyline.config.db.impl.GroupStoreImpl;
import org.anyline.config.db.impl.OrderStoreImpl;
import org.anyline.config.http.ConfigStore;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;


public abstract class BasicRunSQLImpl implements RunSQL {

	protected SQL sql;
	protected List<Object> values;
	protected PageNavi pageNavi;
	protected StringBuilder builder = new StringBuilder();
	protected ConditionChain conditionChain;			//查询条件
	protected ConfigStore configStore;
	protected OrderStore orderStore;
	protected GroupStore groupStore;
	protected List<SQLVariable> variables;
	
	
	protected SQLCreater creater;
	protected String disKeyFr;
	protected String disKeyTo;
	
	public void setCreater(SQLCreater creater){
		this.creater = creater;
	}
	public void init(){
		this.disKeyFr = creater.getDisKeyFr();
		this.disKeyTo = creater.getDisKeyTo();
		

		if(null != configStore){
			setPageNavi(configStore.getPageNavi());
			OrderStore orderStore = configStore.getOrders();
			List<Order> orders = null;
			if(null != orderStore){
				orders = orderStore.getOrders();
			}
			if(null != orders){
				for(Order order:orders){
					orderStore.order(order);
				}
			}
		}
		
	}
	public RunSQL group(String group){
		/*避免添加空条件*/
		if(BasicUtil.isEmpty(group)){
			return this;
		}
		
		if(null == groupStore){
			groupStore = new GroupStoreImpl();
		}

		group = group.trim().toUpperCase();

		
		/*添加新分组条件*/
		if(!groupStore.getGroups().contains(group)){
			groupStore.group(group);
		}
		
		return this;
	}
	public RunSQL order(String order){
		if(null == orderStore){
			orderStore = new OrderStoreImpl();
		}
		orderStore.order(order);
		return this;
	}
	public SQL getSql() {
		return sql;
	}
	public RunSQL setSql(SQL sql) {
		this.sql = sql;
		return this;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
	public void addValue(Object value){
		if(null == values){
			values = new ArrayList<Object>();
		}
		values.add(value);
	}
	public PageNavi getPageNavi() {
		return pageNavi;
	}
	public void setPageNavi(PageNavi pageNavi) {
		this.pageNavi = pageNavi;
	}
	public StringBuilder getBuilder() {
		return builder;
	}
	public void setBuilder(StringBuilder builder) {
		this.builder = builder;
	}
	public ConfigStore getConfigStore() {
		return configStore;
	}
	public void setConfigStore(ConfigStore configStore) {
		this.configStore = configStore;
	}
	
	public OrderStore getOrderStore() {
		return orderStore;
	}
	public void setOrderStore(OrderStore orderStore) {
		this.orderStore = orderStore;
	}
	public GroupStore getGroupStore() {
		return groupStore;
	}
	public void setGroupStore(GroupStore groupStore) {
		this.groupStore = groupStore;
	}
	public String getDisKeyFr() {
		return disKeyFr;
	}
	public void setDisKeyFr(String disKeyFr) {
		this.disKeyFr = disKeyFr;
	}
	public String getDisKeyTo() {
		return disKeyTo;
	}
	public void setDisKeyTo(String disKeyTo) {
		this.disKeyTo = disKeyTo;
	}
	public SQLCreater getCreater() {
		return creater;
	}

	@Override
	public RunSQL setConditionValue(String condition, String variable, Object value) {
		return this;
	}
	@Override
	public void setOrders(String[] orders) {
		if(null != orders){
			for(String order:orders){
				order(order);
			}
		}
	}
	@Override
	public String getFinalQueryTxt() {
		return creater.parseFinalQueryTxt(this);
	}
	@Override
	public String getTotalQueryTxt() {
		return creater.parseTotalQueryTxt(this);
	}
	@Override
	public String getBaseQueryTxt() {
		return builder.toString();
	}
	public RunSQL addOrders(OrderStore orderStore){
		if(null == orderStore){
			return this;
		}
		List<Order> orders = orderStore.getOrders();
		if(null == orders){
			return this;
		}
		for(Order order:orders){
			this.orderStore.order(order);
		}
		return this;
	}
	public RunSQL addOrder(Order order){
		this.orderStore.order(order);
		return this;
	}


	public RunSQL setConditionChain(ConditionChain chain){
		this.conditionChain = chain;
		return this;
	}
	public ConditionChain getConditionChain() {
		return this.conditionChain;
	}
	

	public RunSQL addCondition(Condition condition) {
		if(null != conditionChain){
			conditionChain.addCondition(condition);
		}
		return this;
	}


	public Condition getCondition(String name){
		for(Condition con:conditionChain.getConditions()){
			if(con.getId().equalsIgnoreCase(name)){
				return con;
			}
		}
		return null;
	}
	public RunSQL addConditions(String[] conditions) {
		/*添加查询条件*/
		if(null != conditions){
			for(String condition:conditions){
				if(condition.toUpperCase().contains("ORDER BY")){
					String orderStr = condition.toUpperCase().replace("ORDER BY", "");
					String orders[] = orderStr.split(",");
					for(String item:orders){
						order(item);
						if(null != configStore){
							configStore.order(item);
						}
						if(null != this.orderStore){
							this.orderStore.order(item);
						}
					}
					continue;
				}else if(condition.toUpperCase().contains("GROUP BY")){
					String groupStr = condition.toUpperCase().replace("GROUP BY", "");
					String groups[] = groupStr.split(",");
					for(String item:groups){
						if(null == groupStore){
							groupStore = new GroupStoreImpl();
						}
						groupStore.group(item);
					}
					continue;
				}
				this.addCondition(condition);
			}
		}
		return this;
	}
	public String getDeleteTxt(){
		return this.builder.toString();
	}
	public String getInsertTxt(){
		return this.builder.toString();
	}
	public String getUpdateTxt(){
		return this.builder.toString();
	}
	
	public RunSQL addVariable(SQLVariable var){
		if(null == variables){
			variables = new ArrayList<SQLVariable>();
		}
		variables.add(var);
		return this;
	}
	
	protected boolean hasWhere(String txt){
		boolean where = false;
		int fr = txt.lastIndexOf("WHERE");
		if(fr > 0){
			txt = txt.substring(fr);
			try{
				int bSize = 0;
				if(txt.contains(")")){
					bSize = RegularUtil.fetch(txt, ")").size();
				}
				int eSize = 0;
				if(txt.contains("(")){
					eSize = RegularUtil.fetch(txt, "(").size();
				}
				if(bSize == eSize){
					where = true;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return where;
	}
	@Override
	public String getExecuteTxt(){
		return builder.toString();
	}
}


