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

import org.anyline.data.param.DefaultPrepare;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.xml.init.DefaultXMLPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;
import org.anyline.metadata.Table;

import java.util.ArrayList;
import java.util.List;

public class DefaultTablePrepare extends DefaultAutoPrepare implements TablePrepare {

	public DefaultTablePrepare() {
		super();
		chain = new DefaultAutoConditionChain();
	}
	public DefaultTablePrepare(String table) {
		super();
		chain = new DefaultAutoConditionChain();
		setTable(table);
	}
	public DefaultTablePrepare(Table table) {
		super();
		chain = new DefaultAutoConditionChain();
		setTable(table);
	}

	@Override 
	public RunPrepare setTable(String table) {
		if(null != table) {
			this.table = new Table(table);
		}else{
			this.table = null;
		}
		parseTable(this.table);
		return this;
	}

	@Override
	public Run build(DataRuntime runtime) {
		TableRun run = new TableRun(runtime, this.getTable());
		run.setPrepare(this);
		run.setConfigStore(this.condition());
		List<RunPrepare> unions = getUnions();
		if(null != unions) {
			for(RunPrepare union:unions) {
				run.union(union.build(runtime));
			}
		}
		return run;
	}


	@Override
	public DataRow map(boolean empty, boolean join) {
		DataRow row = new OriginRow();
		row.put("table", table.getName());
		if(null != table.getAlias() || empty) {
			row.put("alias", table.getAlias());
		}
		if(join && null != this.join) {
			row.put("type", this.join.getType().name());
			row.put("conditions", this.join.getConditions().getConfigChain().map(empty));
		}
		return row;
	}

	public DefaultTablePrepare clone() {
		DefaultTablePrepare clone = null;
		try{
			clone = (DefaultTablePrepare)super.clone();
		}catch (Exception e) {
			clone = new DefaultTablePrepare();
		}
		return clone;
	}
}
