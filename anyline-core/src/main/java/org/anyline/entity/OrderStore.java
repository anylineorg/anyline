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

package org.anyline.entity;
 
import java.io.Serializable;
import java.util.List;
 
public interface OrderStore extends Cloneable, Serializable{
	List<Order> getOrders(); 
	void order(Order order, boolean override);
	void order(Order order);
	void order(String col, Order.TYPE type, boolean override);
	void order(String col, Order.TYPE type);
	void order(String col, String type, boolean override);
	void order(String col, String type);
	void order(String str, boolean override) ;
	void order(String str) ;
	public Order getOrder(String order); 
	public String getRunText(String delimiter);
	void clear();
	boolean isEmpty();
} 
