package org.anyline.dao.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;


public class BatchInsertStore {
	public boolean isRun = false;
	private ConcurrentHashMap<String,ConcurrentLinkedDeque<DataRow>> map = new ConcurrentHashMap<String,ConcurrentLinkedDeque<DataRow>>();
	
	public void addData(String table, String cols, DataRow data){
		String key = table + "(" + cols +")";
		ConcurrentLinkedDeque<DataRow> rows = map.get(key);
		if(null == rows){
			rows = new ConcurrentLinkedDeque<DataRow>();
			map.put(key, rows);
		}
		rows.add(data);
	}
	/**
	 * 需要保存的数据列表
	 * @return
	 */
	public DataSet getDatas(){
		int min = 100;
		DataSet list = new DataSet();
		//第一次循环查找数量>=100的数据
				for(ConcurrentLinkedDeque<DataRow> rows :map.values()){
					if(rows.size() >= min){
						for(int i=0; i<min; i++){
							DataRow row = rows.poll();
							if(null != row){
								list.add(row);
								rows.remove(row);
							}
						}
						return list;
					}
				}
			//第一次失败后 补充第二次循环查找数量>=1的数据
			for(ConcurrentLinkedDeque<DataRow> rows :map.values()){
				if(rows.size() >= 0){
					for(int i=0; i<min && i<rows.size(); i++){
						DataRow row = rows.poll();
						if(null != row){
							list.add(row);
							rows.remove(row);
						}
					}
					return list;
				}
			}
			
		return list;
	}
}
