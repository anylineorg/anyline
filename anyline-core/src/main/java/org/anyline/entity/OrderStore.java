/*
 * Copyright 2006-2025 www.anyline.org
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

import org.anyline.metadata.Column;
import org.anyline.util.BeanUtil;
import org.anyline.util.SQLUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
 
public interface OrderStore extends Cloneable, Serializable{
	LinkedHashMap<String, Order> gets();
	void add(Order order, boolean override);
	void add(Order order);
	void add(String col, Order.TYPE type, boolean override);
	void add(String col, Order.TYPE type);
	void add(String col, String type, boolean override);
	void add(String col, String type);
	void add(String str, boolean override) ;
	void add(String str) ;
	default void add(OrderStore orders){
		if(null != orders){
			for(Order order:orders.gets().values()){
				add(order);
			}
		}
	}
	Order get(String order);
	String getRunText(String delimiter);
	void clear();
	boolean isEmpty();
	void nullSet(String set);
	String nullSet();
	OrderStore clone();

	/**
	 * 过滤不存在的列
	 * @param metadatas 可用范围
	 */
	default void filter(LinkedHashMap<String, Column> metadatas) {
		LinkedHashMap<String, Order> orders = gets();
		if (null != orders) {
			List<String> keys = BeanUtil.getMapKeys(orders);
			for (String column:keys) {
				if (SQLUtil.isSingleColumn(column) && !metadatas.containsKey(column.toUpperCase())) {
					orders.remove(column);
				}
			}
		}
	}

} 
