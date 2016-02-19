

package org.anyline.config.db.sql.auto.impl;

import java.util.List;

import org.anyline.config.db.sql.auto.TableSQL;

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
