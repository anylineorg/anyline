
package org.anyline.config.db;

import java.util.List;

public interface OrderStore extends Cloneable{

	public List<Order> getOrders();
	public void order(Order order);
	public void order(String col, String type);
	public void order(String str) ;
	public Order getOrder(String order);
	public String getRunText(String disKey);
	public void clear();
}
