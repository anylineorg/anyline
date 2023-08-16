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
 *
 *          
 */


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


package org.anyline.data.prepare.auto.init;

import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.entity.Join;

import java.util.List;
 
public class DefaultTablePrepare extends DefaultAutoPrepare implements TablePrepare {

	public DefaultTablePrepare(){
		super();
		chain = new DefaultAutoConditionChain();
	}
	public DefaultTablePrepare(String table){
		super();
		chain = new DefaultAutoConditionChain();
		setTable(table);
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
		return super.getColumns();
	}



	public RunPrepare join(Join join){
		joins.add(join);
		return this;
	}
	public RunPrepare join(Join.TYPE type, String table, String condition){
		Join join = new Join();
		join.setName(table);
		join.setType(type);
		join.setCondition(condition);
		return join(join);
	}
	public RunPrepare inner(String table, String condition){
		return join(Join.TYPE.INNER, table, condition);
	}
	public RunPrepare left(String table, String condition){
		return join(Join.TYPE.LEFT, table, condition);
	}
	public RunPrepare right(String table, String condition){
		return join(Join.TYPE.RIGHT, table, condition);
	}
	public RunPrepare full(String table, String condition){
		return join(Join.TYPE.FULL, table, condition);
	}
} 
