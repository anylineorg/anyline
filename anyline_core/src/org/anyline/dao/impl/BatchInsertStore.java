package org.anyline.dao.impl;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class BatchInsertStore {
	private ConcurrentHashMap<String,Vector<Object>> map = new ConcurrentHashMap<String,Vector<Object>>();
	
	public void addData(String table, String cols, Object data){
		String key = table + "_" + cols;
		Vector<Object> rows = map.get(key);
		if(null == rows){
			rows = new Vector<Object>();
			map.put(key, rows);
		}
		rows.add(data);
	}
}
