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


public interface Group extends Cloneable, Serializable{
	String getColumn() ;
	void setColumn(String column) ;
	DataRow map(boolean empty);
	default DataRow map() {
		return map(false);
	}
	default String json(boolean empty) {
		return map(empty).json();
	}
	default String json() {
		return json(false);
	}
	Group clone();
} 
