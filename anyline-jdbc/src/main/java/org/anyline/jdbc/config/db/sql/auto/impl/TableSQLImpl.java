/* 
 * Copyright 2006-2020 www.anyline.org
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

import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.sql.auto.TableSQL;
 
public class TableSQLImpl extends AutoSQLImpl implements TableSQL{ 

	public TableSQLImpl(){ 
		super(); 
		chain = new AutoConditionChainImpl(); 
	} 
 
 
	@Override 
	public void setTable(String table) {
		this.table = table;
		parseTable();
	} 
 
	@Override 
	public String getDistinct() { 
		return this.distinct; 
	} 
 
	@Override 
	public List<String> getColumns() { 
		return this.columns; 
	}



	public SQL join(Join join){
		joins.add(join);
		return this;
	}
	public SQL join(Join.TYPE type, String table, String condition){
		Join join = new Join();
		join.setName(table);
		join.setType(type);
		join.setCondition(condition);
		return join(join);
	}
	public SQL inner(String table, String condition){
		return join(Join.TYPE.INNER, table, condition);
	}
	public SQL left(String table, String condition){
		return join(Join.TYPE.LEFT, table, condition);
	}
	public SQL right(String table, String condition){
		return join(Join.TYPE.RIGHT, table, condition);
	}
	public SQL full(String table, String condition){
		return join(Join.TYPE.FULL, table, condition);
	}
} 
