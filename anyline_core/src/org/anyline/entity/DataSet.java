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
 */


package org.anyline.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.anyline.config.db.PageNavi;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.EscapeUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class DataSet implements Collection<Object>, Serializable {
	protected static Logger log = Logger.getLogger(DataSet.class);
	private boolean result = true; 		// 执行结果
	private Exception exception; 			// 异常
	private String message; 				// 提示信息
	private PageNavi navi; 					// 分页
	private List<String> head; 				// 表头
	private List<DataRow> rows; 			// 数据
	private List<String> primaryKeys; 		// 主键
	private String dataSource; 				// 数据源(表|视图|XML定义SQL)
	private String author;
	private String table;


	@Autowired
	protected AnylineService service;
	/**
	 * 添加主键
	 * 
	 * @param parmary
	 */
	public void addPrimary(String... primaryKey) {
		if (null == this.primaryKeys) {
			this.primaryKeys = new ArrayList<String>();
		}
		if (null == primaryKey) {
			return;
		}
		for (String item : primaryKey) {
			if (null == item) {
				continue;
			}
			item = item.toUpperCase();
			if (!this.primaryKeys.contains(primaryKey)) {
				this.primaryKeys.add(item);
			}
		}
	}
	public void set(int index, DataRow item){
		rows.set(index, item);
	}
	/**
	 * 设置主键
	 * 
	 * @param primary
	 */
	public void setPrimary(String... primaryKey) {
		if (null == primaryKey) {
			return;
		}
		this.primaryKeys = new ArrayList<String>();
		for (String item : primaryKey) {
			if (null == item) {
				continue;
			}
			item = item.toUpperCase();
			if (!this.primaryKeys.contains(item)) {
				this.primaryKeys.add(item);
			}
		}
	}

//	public DataSet toLowerKey() {
//		for (DataRow row : rows) {
//			row.toLowerKey();
//		}
//		return this;
//	}
//
//	public DataSet toUpperKey() {
//		for (DataRow row : rows) {
//			row.toUpperKey();
//		}
//		return this;
//	}

	/**
	 * 是否有主键
	 * 
	 * @return
	 */
	public boolean hasPrimaryKeys() {
		if (null != primaryKeys && primaryKeys.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 提取主键
	 * 
	 * @return
	 */
	public List<String> getPrimaryKeys() {
		if (null == primaryKeys) {
			primaryKeys = new ArrayList<String>();
		}
		return primaryKeys;
	}

	/**
	 * 添加表头
	 * 
	 * @param col
	 */
	public void addHead(String col) {
		if (null == head) {
			head = new ArrayList<String>();
		}
		if ("ROW_NUMBER".equals(col)) {
			return;
		}
		if (head.contains(col)) {
			return;
		}
		head.add(col);
	}

	/**
	 * 表头
	 * 
	 * @return
	 */
	public List<String> getHead() {
		return head;
	}

	public DataSet() {
		rows = new ArrayList<DataRow>();
	}

	public DataSet(List<Map<String, Object>> list) {
		rows = new ArrayList<DataRow>();
		if (null == list)
			return;
		for (Map<String, Object> map : list) {
			DataRow row = new DataRow(map);
			rows.add(row);
		}
	}

	/**
	 * 记录数量
	 * 
	 * @return
	 */
	public int size() {
		int result = 0;
		if (null != rows)
			result = rows.size();
		return result;
	}

	public int getSize() {
		return size();
	}

	/**
	 * 是否出现异常
	 * 
	 * @return
	 */
	public boolean isException() {
		return null != exception;
	}

	/**
	 * 返回数据是否为空
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		boolean result = true;
		if (null == rows) {
			result = true;
		} else if (rows instanceof Collection) {
			result = ((Collection) rows).isEmpty();
		}
		return result;
	}

	/**
	 * 转换成对象
	 * 
	 * @param clazz
	 * @return
	 */
	// public Object entity(Class clazz){
	// Object entity = null;
	// DataRow row = getRow(0);
	// if(null != row){
	// entity = row.entity(clazz);
	// }
	// return entity;
	// }
	// public List entityList(Class clazz){
	// List<Object> list = new ArrayList<Object>();
	// if(null == rows) return list;
	// for(DataRow row: rows){
	// list.add(row.entity(clazz));
	// }
	// return list;
	// }
	/**
	 * 读取一行数据
	 * 
	 * @param index
	 * @return
	 */
	public DataRow getRow(int index) {
		DataRow row = null;
		if (null != rows && index < rows.size()) {
			row = rows.get(index);
		}
		if (null != row) {
			row.setContainer(this);
		}
		return row;
	}

	/**
	 * 根据单个属性值读取一行
	 * 
	 * @param key
	 *            属性
	 * @param value
	 *            值
	 * @return
	 */
	public DataRow getRow(String... params) {
		DataSet set = getRows(params);
		if (set.size() > 0) {
			return set.getRow(0);
		}
		return null;
	}
	/**
	 * distinct
	 * @param keys
	 * @return
	 */
	public DataSet distinct(String... keys) {
		DataSet result = new DataSet();
		if (null != rows) {
			int size = rows.size();
			for (int i = 0; i < size; i++) {
				DataRow row = rows.get(i);
				//查看result中是否已存在
				String[] params = new String[keys.length*2];
				int idx = 0;
				for(String key:keys){
					params[idx++] = key;
					params[idx++] = row.getString(key);
				}
				if(result.getRows(params).size() == 0){
					DataRow tmp = new DataRow();
					for(String key:keys){
						tmp.put(key, row.get(key));
					}
					result.addRow(tmp);
				}
			}
		}
		return result;
	}
	/**
	 * 提取符合指定属性值的集合
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private DataSet filter(DataSet src, String key, String value) {
		DataSet set = new DataSet();
		String tmpValue;
		for (int i = 0; i < src.size(); i++) {
			tmpValue = src.getString(i, key);
			if ((null == value && null == tmpValue)
					|| (null != value && value.equals(tmpValue))) {
				set.add(src.getRow(i));
			}
		}
		return set;
	}

	public DataSet getRows(String... params) {
		DataSet set = this;
		if (null == params) {
			return new DataSet();
		}
		for (int i = 0; i < params.length - 1; i += 2) {
			if (i + 1 < params.length) {
				String key = params[i];
				String value = params[i + 1];
				set = filter(set, key, value);
			}
		}
		return set;
	}

	public DataSet getRows(int fr, int to) {
		DataSet set = new DataSet();
		for (int i = fr; i < this.size() && i <= to; i++) {
			set.addRow(this.getRow(i));
		}
		return set;
	}
	
	/**
	 * 合计
	 * @param top 多少行
	 * @param key
	 * @return
	 */
	public double sum(int top, String key){
		BigDecimal re = new BigDecimal("0");
		int size = rows.size();
		if(size>top){
			size = top;
		}
		for (int i = 0; i < size; i++) {
			String tmp = getString(i, key);
			if(null == tmp){
				continue;
			}
			BigDecimal bd = null;
			try{
				bd = new BigDecimal(tmp);
			}catch(Exception e){
				log.error(e);
			}
			re = re.add(bd);
		}
		return re.doubleValue();
	}
	public double sum(String key) {
		double result = 0.0;
		 result = sum(size(), key);
		return result;
	}
	/**
	 * 最大值
	 * @param top 多少行
	 * @param key
	 * @return
	 */
	public double max(int top, String key){
		double result = 0.0;
		int size = rows.size();
		if(size>top){
			size = top;
		}
		for (int i = 0; i < size; i++) {
			double tmp = getDouble(i, key);
			if(tmp > result){
				result = tmp;
			}
		}
		return result;
	}
	public double max(String key){
		double result = 0.0;
		result = max(size(),key);
		return result;
	}
	/**
	 * 最小值
	 * @param top 多少行
	 * @param key
	 * @return
	 */
	public double min(int top, String key){
		double result = 0.0;
		int size = rows.size();
		if(size>top){
			size = top;
		}
		for (int i = 0; i < size; i++) {
			double tmp = getDouble(i, key);
			if(tmp < result){
				result = tmp;
			}
		}
		return result;
	}
	public double min(String key){
		double result = 0.0;
		result = min(size(),key);
		return result;
	}

	/**
	 * 平均
	 * @param top 多少行
	 * @param key
	 * @return
	 */
	public double avg(int top, String key){
		double result = 0.0;
		int size = rows.size();
		if(size>top){
			size = top;
		}
		int count = 0;
		for (int i = 0; i < size; i++) {
			double tmp = getDouble(i, key);
			result += tmp;
			count ++;
		}
		if(count >0){
			result = result/count;
		}else{
			result = 0;
		}
		return result;
	}
	public double avg(String key){
		double result = 0.0;
		result = avg(size(),key);
		return result;
	}
	
	public void addRow(DataRow row) {
		if (null != row) {
			rows.add(row);
		}
	}

	public void addRow(int idx, DataRow row) {
		if (null != row) {
			rows.add(idx, row);
		}
	}

	public void addRow(Object... params) {
		DataRow row = new DataRow(params);
		rows.add(row);
	}

	/**
	 * 提取单列值
	 * 
	 * @param key
	 * @return
	 */
	public List<String> fetchValues(String key) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < size(); i++) {
			result.add(getString(i, key));
		}
		return result;
	}

	public List<String> fetchDistinctValue(String key) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < size(); i++) {
			String value = getString(i, key);
			if (result.contains(value)) {
				continue;
			}
			result.add(value);
		}
		return result;
	}

	/**
	 * 分页
	 * 
	 * @return
	 */
	public String displayNavi(String link) {
		String result = "";
		if (null != navi) {
			//result = navi.html(link);
		}
		return result;
	}

	public String navi(String link) {
		return displayNavi(link);
	}

	public String displayNavi() {
		return displayNavi(null);
	}

	public String navi() {
		return displayNavi(null);
	}
	public DataSet put(int idx, String key, Object value){
		DataRow row = getRow(idx);
		if(null != row){
			row.put(key, value);
		}
		return this;
	}
	/**
	 * String
	 * 
	 * @param index
	 * @param key
	 * @return
	 */
	public String getString(int index, String key) {
		String result = null;
		DataRow row = getRow(index);
		if (null != row)
			result = row.getString(key);
		return result;
	}

	public String getString(String key) {
		return getString(0, key);
	}
	public List<String> getStrings(String key){
		List<String> strings = new ArrayList<String>();
		int size = rows.size();
		for(int i=0; i<size; i++){
			String value = getString(i,key);
			strings.add(value);
		}
		return strings;
	}

	public BigDecimal getDecimal(int idx, String key){
		BigDecimal result = null;
		DataRow row = getRow(idx);
		if (null != row)
			result = row.getDecimal(key);
		return result;
	}
	public List<String> getDistinctStrings(String key){
		List<String> strings = new ArrayList<String>();
		int size = rows.size();
		for(int i=0; i<size; i++){
			String value = getString(i,key);
			if(strings.contains(value)){
				continue;
			}
			strings.add(value);
		}
		return strings;
	}

	/**
	 * htmlml格式(未实现)
	 * 
	 * @param index
	 * @param key
	 * @return
	 */
	public String getHtmlString(int index, String key) {
		String result = getString(index, key);
		return result;
	}

	public String getHtmlString(String key) {
		return getHtmlString(0, key);
	}
	public void put(String key, Object value){
		for(DataRow row: rows){
			row.put(key, value);
		}
	}

	/**
	 * escape String
	 * 
	 * @param index
	 * @param key
	 * @return
	 */
	public String getEscapeString(int index, String key) {
		String result = getString(index, key);
		result = EscapeUtil.escape(result).toString();
		return result;
	}

	public String getDoubleEscapeString(int index, String key) {
		String result = getString(index, key);
		result = EscapeUtil.doubleEscape(result);
		return result;
	}

	public String getEscapeString(String key) {
		return getEscapeString(0, key);
	}

	public String getDoubleEscapeString(String key) {
		return getDoubleEscapeString(0, key);
	}

	/**
	 * int
	 * 
	 * @param index
	 * @param key
	 * @return
	 */
	public int getInt(int index, String key) {
		int result = 0;
		DataRow row = getRow(index);
		if (null != row)
			result = row.getInt(key);
		return result;
	}

	public int getInt(String key) {
		return getInt(0, key);
	}

	/**
	 * double
	 * 
	 * @param index
	 * @param key
	 * @return
	 */
	public double getDouble(int index, String key) {
		double result = 0;
		DataRow row = getRow(index);
		if (null != row)
			result = row.getDouble(key);
		return result;
	}

	public double getDouble(String key) {
		return getDouble(0, key);
	}

	public String toString() {
		Map<String,Object> map = new HashMap<String,Object>();
    	map.put("type", "list");
    	map.put("result", result);
    	map.put("message", message);
    	map.put("rows", rows);
    	map.put("success", result);
    	map.put("navi", navi);
    	JSON json = JSONObject.fromObject(map);
		return json.toString();
	}
	public String toJSON(){
		JSONArray json = JSONArray.fromObject(rows);
		return json.toString();
	}

	/**
	 * 子类
	 * 
	 * @return
	 */
	public Object getChildren(int idx) {
		DataRow row = getRow(idx);
		if (null != row) {
			return row.getChildren();
		}
		return null;
	}

	public Object getChildren() {
		return getChildren(0);
	}

	public void setChildren(int idx, Object children) {
		DataRow row = getRow(idx);
		if (null != row) {
			row.setChildren(children);
		}
	}

	public void setChildren(Object children) {
		setChildren(0, children);
	}

	/**
	 * 父类
	 * 
	 * @return
	 */
	public Object getParent(int idx) {
		DataRow row = getRow(idx);
		if (null != row) {
			return row.getParent();
		}
		return null;
	}

	public Object getParent() {
		return getParent(0);
	}

	public void setParent(int idx, Object parent) {
		DataRow row = getRow(idx);
		if (null != row) {
			row.setParent(parent);
		}
	}

	public void setParent(Object parent) {
		setParent(0, parent);
	}

	/**
	 * 转换成对象
	 * 
	 * @param clazz
	 * @return
	 */
	public <T> T entity(int index, Class<T> clazz) {
		DataRow row = getRow(index);
		if (null != row) {
			return row.entity(clazz);
		}
		return null;
	}

	/**
	 * 转换成对象集合
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public <T> List<T> entity(Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		if (null != rows) {
			for (DataRow row : rows) {
				list.add(row.entity(clazz));
			}
		}
		return list;
	}

	public <T> T entity(Class<T> clazz, int idx) {
		DataRow row = getRow(idx);
		if (null != row) {
			return row.entity(clazz);
		}
		return null;
	}
	/************************** getter setter ***************************************/

	public boolean isResult() {
		return result;
	}

	public boolean isSuccess() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public PageNavi getNavi() {
		return navi;
	}

	public void setNavi(PageNavi navi) {
		this.navi = navi;
	}

	public List<DataRow> getRows() {
		return rows;
	}

	public void setRows(List<DataRow> rows) {
		this.rows = rows;
	}

	public String getDataSource() {
		String ds = dataSource;
		if (BasicUtil.isNotEmpty(ds) && BasicUtil.isNotEmpty(author)) {
			ds = author + "." + ds;
		}
		return ds;
	}

	public void setDataSource(String dataSource) {
		if (null == dataSource) {
			return;
		}
		this.dataSource = dataSource;
		if (dataSource.contains(".") && !dataSource.contains(":")) {
			author = dataSource.substring(0, dataSource.indexOf("."));
			table = dataSource.substring(dataSource.indexOf(".") + 1);
		}
	}

	public DataSet union(DataSet set, String chkCol) {
		DataSet result = new DataSet();
		if (null != rows) {
			int size = rows.size();
			for (int i = 0; i < size; i++) {
				result.add(rows.get(i));
			}
		}
		if (null == chkCol) {
			chkCol = "CD";
		}
		int size = set.size();
		for (int i = 0; i < size; i++) {
			DataRow item = set.getRow(i);
			if (!result.contains(item, chkCol)) {
				result.add(item);
			}
		}
		return result;
	}

	public DataSet union(DataSet set) {
		return union(set, "CD");
	}
	/**
	 * 合并
	 * @param set
	 * @return
	 */
	public DataSet unionAll(DataSet set) {
		DataSet result = new DataSet();
		if (null != rows) {
			int size = rows.size();
			for (int i = 0; i < size; i++) {
				result.add(rows.get(i));
			}
		}
		int size = set.size();
		for (int i = 0; i < size; i++) {
			DataRow item = set.getRow(i);
			result.add(item);
		}
		return result;
	}

	public boolean contains(DataRow row, String chkCol) {
		if (null == rows || rows.size() == 0) {
			return false;
		}
		if (null == chkCol) {
			chkCol = "CD";
		}
		int size = rows.size();
		for (int i = 0; i < size; i++) {
			DataRow item = rows.get(i);
			if (item.getString(chkCol).equals(row.getString(chkCol))) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 从items中按相应的key提取数据 存入
	 * @param items
	 * @param keys
	 * @return
	 */
	public DataSet dispatchItems(DataSet items, String ... keys){
		if(null == items || null == keys || keys.length == 0){
			return this;
		}
		for(DataRow row : rows){
			String[] params = new String[keys.length*2];
			int idx = 0;
			for(String key:keys){
				params[idx++] = key;
				params[idx++] = row.getString(key);
			}
			row.putItems(items.getRows(params));
		}
		return this;
	}
	/**
	 * 按keys分组
	 * @param keys
	 * @return
	 */
	public DataSet group(String ... keys){
		DataSet result = distinct(keys);
		result.dispatchItems(this, keys);
		return result;
	}
	/*********************************************** 实现接口 ************************************************************/
	public boolean add(Object e) {
		return rows.add((DataRow) e);
	}

	public boolean addAll(Collection c) {
		return rows.addAll(c);
	}

	public void clear() {
		rows.clear();
	}

	public boolean contains(Object o) {
		return rows.contains(o);
	}

	public boolean containsAll(Collection c) {
		return rows.containsAll(c);
	}

	public Iterator iterator() {
		return rows.iterator();
	}

	public boolean remove(Object o) {
		return rows.remove(o);
	}

	public boolean removeAll(Collection c) {
		return rows.removeAll(c);
	}

	public boolean retainAll(Collection c) {
		return rows.retainAll(c);
	}

	public Object[] toArray() {
		return rows.toArray();
	}

	public Object[] toArray(Object[] a) {
		return rows.toArray(a);
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		if(null != table && table.contains(".")){
			String[] tbs = table.split("\\.");
			this.table = tbs[1];
			this.author = tbs[0];
		}else{
			this.table = table;
		}
	}
	public int delete(){
		return service.delete(this);
	}
	public int save(){
		return service.save(this);
	}
	public AnylineService getService() {
		return service;
	}
	public void setService(AnylineService service) {
		this.service = service;
	}
	
}
