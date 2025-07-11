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

package org.anyline.data.prepare.text;
 
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.Variable;

/**
 * 通过XML定义的参数 
 * @author zh 
 * 
 */ 
public interface TextCondition extends Condition{
	 
	void init();
	/** 
	 * 赋值 
	 * @param variable  variable
	 * @param values  values
	 */ 
	void setValue(String variable, Object values);

	String getId() ;
 
	void setId(String id) ;
 
	String text() ;

	String getRunText() ;
	Variable getVariable(String key) ;
}
