package org.anyline.dao.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BatchInsertStore {
	private ConcurrentHashMap<String,ConcurrentLinkedDeque<String>> map = new ConcurrentHashMap<String,ConcurrentLinkedDeque<String>>();
	
	public void addData(String table, String cols, String data){
		String key = table + "(" + cols +")";
		ConcurrentLinkedDeque<String> rows = map.get(key);
		if(null == rows){
			rows = new ConcurrentLinkedDeque<String>();
			map.put(key, rows);
		}
		rows.add(data);
	}
}
