/* 
 * Copyright 2006-2015 www.anyline.org
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
 *
 *          
 */


package org.anyline.jdbc.config.db.sql.auto.impl; 
 
import java.util.List; 

import org.anyline.jdbc.config.db.sql.auto.TableSQL;
 
public class TableSQLImpl extends AutoSQLImpl implements TableSQL{ 

	public TableSQLImpl(){ 
		super(); 
		chain = new AutoConditionChainImpl(); 
	} 
 
 
	@Override 
	public void setTable(String table) { 
		this.table = table; 
	} 
 
	@Override 
	public String getDistinct() { 
		return this.distinct; 
	} 
 
	@Override 
	public List<String> getColumns() { 
		return this.columns; 
	} 
} 
