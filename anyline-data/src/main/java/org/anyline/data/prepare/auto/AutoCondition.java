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

package org.anyline.data.prepare.auto;

import org.anyline.data.prepare.Condition;
import org.anyline.entity.Compare;

import java.util.List;
 
 
/** 
 * 自动生成的参数 
 * @author zh 
 * 
 */ 
public interface AutoCondition extends Condition{
	Object getValue();
	List<Object> getValues();
	String getId();
	String getColumn() ;
	void setColumn(String column) ;
	void setValues(Object values) ;
	Compare getCompare() ;
	AutoCondition setCompare(Compare compare) ;
	AutoCondition setOrCompare(Compare compare) ;
} 
