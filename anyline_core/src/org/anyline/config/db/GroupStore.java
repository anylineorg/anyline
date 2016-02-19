

package org.anyline.config.db;

import java.util.List;

public interface GroupStore{
	public List<Group> getGroups();
	public void group(Group group) ;
	/**
	 * 排序多列以,分隔
	 * order("CD","DESC");
	 * order("CD DESC");
	 * order("CD DESC,NM ASC");
	 * @param str
	 */
	public void group(String str) ;

	public Group getGroup(String group);
	public String getRunText(String disKey);
	public void clear();
}
