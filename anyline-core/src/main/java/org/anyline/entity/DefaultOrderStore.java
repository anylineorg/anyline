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

import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DefaultOrderStore implements OrderStore{
	private static final long serialVersionUID = -2129393152486629564L;
	private LinkedHashMap<String, Order> orders = new LinkedHashMap<>();
 
	public DefaultOrderStore() {
	}
	@Override
	public void add(Order order, boolean override) {
		if(null == order) {
			return; 
		}
		String column = order.getColumn();
		Order exists = get(column.toUpperCase());
		if(null != exists) {
			if(override) {
				exists.setType(order.getType());
			}
		}else{
			orders.put(column.toUpperCase(), order);
		} 
	}

	public void add(Order order) {
		add(order, true);
	}

	public void add(String col, Order.TYPE type, boolean override) {
		add(new DefaultOrder(col, type), override);
	}
	public void add(String col, Order.TYPE type) {
		add(col, type, true);
	}

	public void add(String col, String type, boolean override) {
		add(new DefaultOrder(col, type), override);
	}
	public void add(String col, String type) {
		add(col, type, true);
	}

	/** 
	 * 排序多列以, 分隔
	 * order("CD","DESC");
	 * order("CD DESC"); 
	 * order("CD DESC, NM ASC");
	 * @param str  str
	 * @param override 如果已存在相同的排序列 是否覆盖
	 */ 
	public void add(String str, boolean override) {
		if (BasicUtil.isEmpty(str)) {
			return; 
		}
		str = str.trim();
		String up = str.toUpperCase().replaceAll("\\s+"," ").trim();
		if (up.startsWith("ORDER BY")) {
			str = str.substring(up.indexOf("ORDER BY") + "ORDER BY".length()).trim(); 
		} 
		String[] tmps = str.split(","); // 多列排序
		for (String tmp : tmps) {
			add(new DefaultOrder(tmp), override);
		} 
	}

	public void add(String str) {
		add(str, true);
	}
	public Order get(String order) {
		if(null == order) {
			return null; 
		}
		return orders.get(order.toUpperCase());
	} 
	public String getRunText(String delimiter) {
		StringBuilder builder = new StringBuilder(); 
		if(null != orders && !orders.isEmpty()) {
			builder.append("\nORDER BY ");
			for(int i=0; i<orders.size(); i++) {
				Order order = orders.get(i);
				if(null == order) {
					continue;
				}
				SQLUtil.delimiter(builder, order.getColumn(), delimiter).append(" ").append(order.getType());
				if(i<orders.size()-1) {
					builder.append(", ");
				} 
			} 
		} 
		return builder.toString(); 
	} 
 
	public void clear() {
		orders.clear(); 
	}
	public LinkedHashMap<String, Order> gets() {
		return this.orders;
	}
	public boolean isEmpty() {
		if(null != orders) {
			return orders.isEmpty();
		}
		return true;
	}
	public DefaultOrderStore clone() {
		DefaultOrderStore clone = null;
		try{
			clone = (DefaultOrderStore)super.clone();
		}catch (Exception ignored) {
			clone = new DefaultOrderStore();;
		}
		if(null != this.orders) {
			for(Order order:orders.values()) {
				clone.add(order.clone());
			}
		}
		return clone;
	}
} 
