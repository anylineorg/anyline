
package org.anyline.config.db.impl;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.util.BasicUtil;

public class OrderStoreImpl implements OrderStore{
	private List<Order> orders = new ArrayList<Order>();;

	public OrderStoreImpl() {
	}
	public void order(Order order) {
		if(null == order){
			return;
		}
		Order tmp = getOrder(order.getColumn());
		if(null != tmp){
			tmp.setType(order.getType());
		}else{
			orders.add(order);
		}
	}

	public void order(String col, String type) {
		order(new OrderImpl(col, type));
	}
	/**
	 * 排序多列以,分隔
	 * order("CD","DESC");
	 * order("CD DESC");
	 * order("CD DESC,NM ASC");
	 * @param str
	 */
	public void order(String str) {
		if (BasicUtil.isEmpty(str)) {
			return;
		}
		if (str.toUpperCase().contains("ORDER BY")) {
			str = str.toUpperCase().replace("ORDER BY", "").trim();
		}
		String[] tmps = str.split(","); // 多列排序
		for (String tmp : tmps) {
			order(new OrderImpl(tmp));
		}
	}

	public Order getOrder(String order){
		if(null == order){
			return null;
		}
		if(null != orders){
			for(Order o:orders){
				if(order.equalsIgnoreCase(o.getColumn())){
					return o;
				}
			}
		}
		return null;
	}
	public String getRunText(String disKey){
		StringBuilder builder = new StringBuilder();
		if(null != orders && orders.size() > 0){
			builder.append(" ORDER BY ");
			for(int i=0; i<orders.size(); i++){
				Order order = orders.get(i);
				builder.append(order.getColumn()).append(" ").append(order.getType());
				if(i<orders.size()-1){
					builder.append(",");
				}
			}
		}
		return builder.toString();
	}

	public void clear(){
		orders.clear();
	}
	public List<Order> getOrders(){
		return this.orders;
	}
}
