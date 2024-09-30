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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DefaultGroupStore implements GroupStore, Serializable {
	private static final long serialVersionUID = 1257282062069295247L;
	private LinkedHashMap<String, Group> groups;
 
	public DefaultGroupStore() {
		groups = new LinkedHashMap<String, Group>();
	} 
	public LinkedHashMap<String, Group> gets() {
		return groups; 
	} 
	public void add(Group group) {
		if(null == group) {
			return; 
		}
		groups.put(group.getColumn().toUpperCase(), group);
	} 
 
	/** 
	 * 排序多列以, 分隔
	 * group("CD","NM");
	 * group("CD, NM ");
	 * @param str  str
	 */ 
	public void add(String str) {
		if (BasicUtil.isEmpty(str)) {
			return; 
		} 
		if (str.toUpperCase().contains("GROUP BY")) {
			str = str.replaceAll("(?i)group\\s+by", "").trim();
		}
		if(str.contains(")") || str.contains("'")) {
			add(new AbstractGroup(str));
		}else{
			String[] tmps = str.split(","); // 多列排序
			for (String tmp : tmps) {
				add(new AbstractGroup(tmp));
			}
		}
	} 
 
	public Group get(String group) {
		if(null == group) {
			return null; 
		}
		return groups.get(group.toUpperCase());
	} 
 
	public void clear() {
		groups.clear(); 
	} 
	@Override 
	public String getRunText(String delimiter) {
		StringBuilder builder = new StringBuilder(); 
		if(null != groups && !groups.isEmpty()) {
			builder.append(" GROUP BY "); 
			for(int i=0; i<groups.size(); i++) {
				Group group = groups.get(i);
				if(null == group) {
					continue;
				}
				SQLUtil.delimiter(builder, group.getColumn(), delimiter);
				if(i<groups.size()-1) {
					builder.append(", ");
				} 
			} 
		} 
		return builder.toString(); 
	}
	public boolean isEmpty() {
		if(null != groups) {
			return groups.isEmpty();
		}
		return true;
	}
	public GroupStore clone() {
		DefaultGroupStore clone = null;
		try{
			clone = (DefaultGroupStore)super.clone();
		}catch (Exception e) {
			clone = new DefaultGroupStore();
		}
		if(null != groups) {
			for(Group group:groups.values()) {
				clone.add(group.clone());
			}
		}
		return clone;
	}
} 
