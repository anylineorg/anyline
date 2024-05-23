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



package org.anyline.data.prepare.init;

import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 
public class DefaultGroupStore implements GroupStore, Serializable {
	private static final long serialVersionUID = 1257282062069295247L;
	private List<Group> groups;
 
	public DefaultGroupStore() {
		groups = new ArrayList<Group>(); 
	} 
	public List<Group> getGroups(){
		return groups; 
	} 
	public void group(Group group) {
		if(null == group){
			return; 
		} 
		Group tmp = getGroup(group.getColumn()); 
		if(null == tmp){
			groups.add(group); 
		} 
	} 
 
	/** 
	 * 排序多列以, 分隔
	 * group("CD","NM");
	 * group("CD, NM ");
	 * @param str  str
	 */ 
	public void group(String str) {
		if (BasicUtil.isEmpty(str)) {
			return; 
		} 
		if (str.toUpperCase().contains("GROUP BY")) {
			str = str.toUpperCase().replace("GROUP BY","").trim();
		} 
		String[] tmps = str.split(","); // 多列排序
		for (String tmp : tmps) {
			group(new AbstractGroup(tmp));
		} 
	} 
 
	public Group getGroup(String group){
		if(null == group){
			return null; 
		} 
		if(null != groups){
			for(Group o:groups){
				if(null != o && group.equalsIgnoreCase(o.getColumn())){
					return o; 
				} 
			} 
		} 
		return null; 
	} 
 
	public void clear(){
		groups.clear(); 
	} 
	@Override 
	public String getRunText(String delimiter) {
		StringBuilder builder = new StringBuilder(); 
		if(null != groups && !groups.isEmpty()){
			builder.append(" GROUP BY "); 
			for(int i=0; i<groups.size(); i++){
				Group group = groups.get(i);
				if(null == group){
					continue;
				}
				SQLUtil.delimiter(builder, group.getColumn(), delimiter);
				if(i<groups.size()-1){
					builder.append(",");
				} 
			} 
		} 
		return builder.toString(); 
	}
	public boolean isEmpty(){
		if(null != groups){
			return groups.isEmpty();
		}
		return true;
	}
	public GroupStore clone(){
		DefaultGroupStore store = new DefaultGroupStore();
		if(null != groups){
			for(Group group:groups){
				store.group(group.clone());
			}
		}
		return store;
	}
} 
