

package org.anyline.config.db.impl;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.db.Group;
import org.anyline.config.db.GroupStore;
import org.anyline.util.BasicUtil;

public class GroupStoreImpl implements GroupStore{
	private List<Group> groups;

	public GroupStoreImpl() {
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
	 * 排序多列以,分隔
	 * order("CD","DESC");
	 * order("CD DESC");
	 * order("CD DESC,NM ASC");
	 * @param str
	 */
	public void group(String str) {
		if (BasicUtil.isEmpty(str)) {
			return;
		}
		if (str.toUpperCase().contains("GROUP BY")) {
			str = str.toUpperCase().replace("GROUP BY", "").trim();
		}
		String[] tmps = str.split(","); // 多列排序
		for (String tmp : tmps) {
			group(new GroupImpl(tmp));
		}
	}

	public Group getGroup(String group){
		if(null == group){
			return null;
		}
		if(null != groups){
			for(Group o:groups){
				if(group.equalsIgnoreCase(o.getColumn())){
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
	public String getRunText(String disKey) {
		StringBuilder builder = new StringBuilder();
		if(null != groups && groups.size() > 0){
			builder.append(" GROUP BY ");
			for(int i=0; i<groups.size(); i++){
				Group group = groups.get(i);
				builder.append(group.getColumn());
				if(i<groups.size()-1){
					builder.append(",");
				}
			}
		}
		return builder.toString();
	}
}
