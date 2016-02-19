

package org.anyline.config.db.sql.auto.impl;

import org.anyline.config.db.sql.auto.TextSQL;

public class TextSQLImpl extends AutoSQLImpl implements TextSQL{
	private String text;
	public TextSQLImpl(String text){
		super();
		this.text = text;
		chain = new AutoConditionChainImpl();
	}
	public String getText(){
		return this.text;
	}
	
}
