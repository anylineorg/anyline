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



package org.anyline.data.prepare;
 
import java.util.List;
 
public interface GroupStore extends Cloneable{
	List<Group> getGroups();
	void group(Group group) ;
	/** 
	 * 排序多列以, 分隔
	 * gropu("CD"); 
	 * group("CD, NM");
	 * @param str str
	 */ 
	void group(String str) ;
 
	Group getGroup(String group);
	String getRunText(String delimiter);
	void clear();
	boolean isEmpty();
	GroupStore clone();


} 
