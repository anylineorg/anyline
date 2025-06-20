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

import java.io.Serializable;
 
 
public interface Order extends Cloneable, Serializable{
/*	public static final String ORDER_TYPE_ASC = "ASC";
	public static final String ORDER_TYPE_DESC = "DESC";*/
	String getColumn() ;
	void setColumn(String column) ;
	TYPE getType() ;
	void setType(TYPE type) ;
	void setType(String type) ;
	Order clone();
	enum TYPE{
		ASC				{public String getCode() {return "ASC";} 	public String getName() {return "正序";}},
		DESC			{public String getCode() {return "DESC";} 	public String getName() {return "倒序";}};
		public abstract String getName();
		public abstract String getCode();
	}
} 
