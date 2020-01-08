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


package org.anyline.entity; 
 
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.anyline.util.EscapeUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
 
public class DataSet implements Collection<DataRow>, Serializable {
	private static final long serialVersionUID = 6443551515441660101L;
	protected static final Logger log = LoggerFactory.getLogger(DataSet.class); 
	private boolean result 			= true		; // 执行结果 
	private Exception exception		= null		; // 异常 
	private String message			= null		; // 提示信息 
	private PageNavi navi			= null		; // 分页 
	private List<String> head		= null		; // 表头 
	private List<DataRow> rows		= null		; // 数据 
	private List<String> primaryKeys= null		; // 主键
	private String datalink			= null		; // 数据连接
	private String dataSource		= null		; // 数据源(表|视图|XML定义SQL) 
	private String schema			= null		; 
	private String table			= null		;
	private long createTime 		= 0			; //创建时间
	private long expires 			= -1		; //过期时间(毫秒) 从创建时刻计时expires毫秒后过期 
	private boolean isFromCache		= false		; //是否来自缓存
	private boolean isAsc			= false		;
	private boolean isDesc			= false		;
	private Map<String, Object> queryParams 	= new HashMap<String,Object>()	;//查询条件
	
	/**
	 * 创建索引
	 * @param key key
	 * @return return
	 * crateIndex("ID");
	 * crateIndex("ID:ASC");
	 */
	public DataSet creatIndex(String key){
		return this;
	}
	public DataSet() {
		rows = new ArrayList<DataRow>();
		createTime = System.currentTimeMillis();
	}
	public static DataSet parse(Collection<Object> list){
		DataSet set = new DataSet();
		if(null != list){
			for(Object obj:list){
				DataRow row = DataRow.parse(obj);
				set.add(row);
			}
		}
		return set;
	}
	public static DataSet parseJson(String json){
		if(null != json){
			try{
				return parseJson(BeanUtil.JSON_MAPPER.readTree(json));
			}catch(Exception e){
				
			}
		}
		return null;
	}
	public static DataSet parseJson(JsonNode json){
		DataSet set = new DataSet();
		if(null != json){
			if(json.isArray()){
				Iterator<JsonNode>  items = json.iterator();
				while(items.hasNext()){
					JsonNode item = items.next();
					set.add(DataRow.parseJson(item));
				}
			}
		}
		return set;
	}
	public DataSet setIsNew(boolean bol){
		for(DataRow row:rows){
			row.setIsNew(bol);
		}
		return this;
	}
	public DataSet remove(String ... keys){
		for(DataRow row:rows){
			for(String key:keys){
				row.remove(key);
			}
		}
		return this;
	}
	/** 
	 * 添加主键 
	 * @param applyItem 是否应用到集合中的DataRow 默认true 
	 * @param pks  pks
	 * @return return
	 */
	public DataSet addPrimaryKey(boolean applyItem, String ... pks){
		if(null != pks){
			List<String> list = new ArrayList<String>();
			for(String pk:pks){
				list.add(pk);
			}
			addPrimaryKey(applyItem, list);
		}
		return this;
	}
	public DataSet addPrimaryKey(String ... pks){
		return addPrimaryKey(true, pks);
	} 
	public DataSet addPrimaryKey(boolean applyItem, Collection<String> pks) { 
		if (null == primaryKeys) { 
			primaryKeys = new ArrayList<String>(); 
		} 
		if (null == pks) {
			return this; 
		} 
		for (String pk : pks) { 
			if (BasicUtil.isEmpty(pk)) { 
				continue; 
			} 
			pk = key(pk); 
			if (!primaryKeys.contains(pk)) { 
				primaryKeys.add(pk); 
			} 
		}
		if(applyItem){
			for(DataRow row : rows){
				row.setPrimaryKey(false, primaryKeys);
			}
		}
		return this; 
	}
	public DataSet addPrimaryKey(Collection<String> pks) {
		return addPrimaryKey(true, pks);
	} 
	/** 
	 * 设置主键 
	 *  
	 * @param applyItem  applyItem
	 * @param pks  pks
	 * @return return
	 */
	public DataSet setPrimaryKey(boolean applyItem, String ... pks){
		if(null != pks){
			List<String> list = new ArrayList<String>();
			for(String pk:pks){
				list.add(pk);
			}
			setPrimaryKey(applyItem, list);
		}
		return this;
	}
	public DataSet setPrimaryKey(String ... pks){
		return setPrimaryKey(true, pks);
	} 
	public DataSet setPrimaryKey(boolean applyItem, Collection<String> pks) { 
		if (null == pks) {
			return this; 
		} 
		this.primaryKeys = new ArrayList<String>(); 
		addPrimaryKey(applyItem, pks);
		return this; 
	} 
	public DataSet setPrimaryKey(Collection<String> pks) {
		return setPrimaryKey(true, pks);
	}

	public DataSet set(int index, DataRow item){
		rows.set(index, item);
		return this;
	} 
 
	/** 
	 * 是否有主键 
	 *  
	 * @return return
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
	 * @return return
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
	 * @param col  col
	 * @return return
	 */ 
	public DataSet addHead(String col) { 
		if (null == head) { 
			head = new ArrayList<String>(); 
		} 
		if ("ROW_NUMBER".equals(col)) {
			return this; 
		} 
		if (head.contains(col)) {
			return this; 
		} 
		head.add(col);
		return this; 
	} 
 
	/** 
	 * 表头 
	 *  
	 * @return return
	 */ 
	public List<String> getHead() { 
		return head; 
	} 
	public int indexOf(Object obj){
		return rows.indexOf(obj);
	}
	public DataSet cut(int begin){
		return cut(begin, rows.size()-1);
	} 
	public DataSet cut(int begin, int end){
		if(begin < 0){
			begin = 0;
		}
		if(end >= rows.size()){
			end = rows.size() - 1;
		}
		rows = rows.subList(begin, end);
		return this;
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
	 * @return return
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
	 * @return return
	 */ 
	public boolean isException() { 
		return null != exception; 
	}

	public boolean isFromCache(){
		return isFromCache;
	}
	public DataSet setIsFromCache(boolean bol){
		this.isFromCache = bol;
		return this;
	} 
 
	/** 
	 * 返回数据是否为空 
	 *  
	 * @return return
	 */ 
	public boolean isEmpty() { 
		boolean result = true; 
		if (null == rows) { 
			result = true; 
		} else if (rows instanceof Collection) { 
			result = ((Collection<?>) rows).isEmpty(); 
		} 
		return result; 
	} 
 
	 
	/** 
	 * 读取一行数据 
	 *  
	 * @param index  index
	 * @return return
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
 
	public DataRow getRow(String... params) {
		return getRow(0, params);
	}
	public DataRow getRow(int begin, String... params) {
		DataSet set = getRows(begin,1, params);
		if (set.size() > 0) {
			return set.getRow(0);
		}
		return null;
	} 
	/**
	 * distinct
	 * @param keys keys
	 * @return return
	 */ 
	public DataSet distinct(String... keys) { 
		DataSet result = new DataSet();
		if (null != rows) { 
			int size = rows.size(); 
			for (int i = 0; i < size; i++) { 
				DataRow row = rows.get(i);
				//查看result中是否已存在
				String[] params = packParam(row, keys);
				if(result.getRows(params).size() == 0){
					DataRow tmp = new DataRow();
					for(String key:keys){
						tmp.put(key, row.get(key));
					}
					result.addRow(tmp);
				} 
			} 
		}
		result.cloneProperty(this); 
		return result; 
	}
	public Object clone(){
		DataSet set = new DataSet();
		List<DataRow> rows = new ArrayList<DataRow>();
		for(DataRow row:this.rows){
			rows.add((DataRow)row.clone());
		}
		set.setRows(rows);
		set.cloneProperty(this);
		return set;
	}
	private DataSet cloneProperty(DataSet from){
		return cloneProperty(from, this);
	}
	public static DataSet cloneProperty(DataSet from, DataSet to){
		if(null != from && null != to){
			to.exception = from.exception;
			to.message = from.message;
			to.navi = from.navi;
			to.head = from.head;
			to.primaryKeys = from.primaryKeys;
			to.dataSource = from.dataSource;
			to.datalink = from.datalink;
			to.schema = from.schema;
			to.table = from.table;
		}
		return to;
	}
	/**
	 * 筛选符合条件的集合
	 * @param params key1,value1,key2:value2,key3,value3
	 * "NM:zh%","AGE:&gt;20","NM","%zh%"
	 * @param begin begin
	 * @param qty 最多筛选多少个 0表示不限制
	 * @return return
	 */
	public DataSet getRows(int begin, int qty, String... params) {
		DataSet set = new DataSet();
		Map<String,String> kvs = new HashMap<String,String>();
		int len = params.length;
		int i = 0;
		while(i<len){
			String p1 = params[i];
			if(BasicUtil.isEmpty(p1)){
				i++;
				continue;
			}else if(p1.contains(":")){
				String ks[] = BeanUtil.parseKeyValue(p1);
				kvs.put(ks[0], ks[1]);
				i++;
				continue;
			}else{
				if(i+1<len){
					String p2 = params[i+1];
					if(BasicUtil.isEmpty(p2) || !p2.contains(":")){
						kvs.put(p1, p2); 
						i+=2;
						continue;
					}else{
						String ks[] = BeanUtil.parseKeyValue(p2);
						kvs.put(ks[0], ks[1]);
						i+=2;
						continue;
					}
				}

			}
			i++;
		}
		int size = size();
		for(i=0; i<size; i++){
			DataRow row = getRow(i);
			boolean chk = true;//对比结果
			for(String k : kvs.keySet()){
				String v = kvs.get(k);
				Object value = row.get(k);// DataSet item value
//				if(null == v && null == value){
//					continue;
//				}
//				if(!v.equals(value)){
//					chk = false;
//					break;
//				}
				
				if(null == v){
					if(null != value){
						chk = false;
						break;
					}
				}else{
					//与SQL.COMPARE_TYPE保持一致
					int compare = 10;
					if(v.startsWith("=")){
						compare = 10;
						v = v.substring(1);
					}else if(v.startsWith(">")){
						compare = 20;
						v = v.substring(1);
					}else if(v.startsWith(">=")){
						compare = 21;
						v = v.substring(2);
					}else if(v.startsWith("<")){
						compare = 30;
						v = v.substring(1);
					}else if(v.startsWith("<=")){
						compare = 31;
						v = v.substring(2);
					}else if(v.startsWith("%") && v.endsWith("%")){
						compare = 50;
						v = v.substring(1,v.length()-1);
					}else if(v.endsWith("%")){
						compare = 51;
						v = v.substring(0,v.length()-1);
					}else if(v.startsWith("%")){
						compare = 52;
						v = v.substring(1);
					}
					
					if(BasicUtil.isNumber(value)){
						//数字类型
						if(BasicUtil.isNumber(v)){
							double d1 = BasicUtil.parseDouble(value, 0d); 
							double d2 =BasicUtil.parseDouble(v, 0d);
							if(compare == 10){
								if(d1 != d2){
									chk = false;
									break;
								}
							}else if(compare == 20){
								if(!(d1 > d2)){
									chk = false;
									break;
								}
							}else if(compare == 21){
								if(!(d1 >= d2)){
									chk = false;
									break;
								}
							}else if(compare == 30){
								if(!(d1 < d2)){
									chk = false;
									break;
								}
							}else if(compare == 31){
								if(!(d1 <= d2)){
									chk = false;
									break;
								}
							}
						}
					}
					String str = value + "";
					str = str.toLowerCase();
					v = v.toLowerCase();
					if(compare ==10){
						if(!v.equals(str)){
							chk = false;
							break;
						}
					}else if(compare == 50){
						if(!str.contains(v)){
							chk = false;
							break;
						}
					}else if(compare == 51){
						if(!str.startsWith(v)){
							chk = false;
							break;
						}
					}else if(compare == 52){
						if(!str.endsWith(v)){
							chk = false;
							break;
						}
					}
				}
			}//end for kvs
			if(chk){
				set.add(row);
				if(qty > 0 && set.size() >= qty){
					break;
				}
			}
		}//end for rows
		set.cloneProperty(this); 
		return set; 
	}

	public DataSet getRows(int begin, String... params) {
		return getRows(begin, 0, params);
	}
	public DataSet getRows(String... params) {
		return getRows(0, params);
	}
	/**
	 * 数字格式化
	 * @param format format
	 * @param cols cols
	 * @return return
	 */
	public DataSet formatNumber(String format, String ... cols){
		if(null == cols || BasicUtil.isEmpty(format)){
			return this;
		}
		int size = size();
		for(int i=0; i<size; i++){
			DataRow row = getRow(i);
			row.formatNumber(format, cols);
		}
		return this;
	}
	/**
	 * 日期格式化
	 * @param format format
	 * @param cols cols
	 * @return return
	 */
	public DataSet formatDate(String format, String ... cols){
		if(null == cols || BasicUtil.isEmpty(format)){
			return this;
		}
		int size = size();
		for(int i=0; i<size; i++){
			DataRow row = getRow(i);
			row.formatDate(format, cols);
		}
		return this;
	}
	/**
	 * 提取符合指定属性值的集合
	 * @param begin begin
	 * @param end end
	 * @param key key
	 * @param value value
	 * @return return
	 */
	public DataSet filter(int begin, int end, String key, String value) {
		DataSet set = new DataSet();
		String tmpValue;
		int size = size();
		if(begin < 0){
			begin = 0;
		}
		for (int i = begin; i < size && i<=end; i++) {
			tmpValue = getString(i, key);
			if ((null == value && null == tmpValue)
					|| (null != value && value.equals(tmpValue))) {
				set.add(getRow(i));
			}
		}
		set.cloneProperty(this);
		return set;
	}

	public DataSet getRows(int fr, int to) { 
		DataSet set = new DataSet();
		int size = this.size();
		if(fr < 0){
			fr = 0;
		} 
		for (int i = fr; i < size && i <= to; i++) {
			set.addRow(getRow(i)); 
		} 
		return set; 
	} 
	 
	public BigDecimal sum(int begin, int end, String key){ 
		BigDecimal result = BigDecimal.ZERO; 
		int size = rows.size();
		if(begin <=0){
			begin = 0;
		} 
		for (int i = begin; i < size && i <=end; i++) {
			BigDecimal tmp = getDecimal(i, key);
			if(null != tmp){
				result = result.add(getDecimal(i, key));
			} 
		} 
		return result; 
	} 
	public BigDecimal sum(String key) { 
		BigDecimal result = BigDecimal.ZERO; 
		result = sum(0,size()-1, key); 
		return result; 
	}
	/**
	 * sum
	 * @param keys keys
	 * @return return
	 */
	public DataRow sums(String ... keys){
		DataRow row = new DataRow();
		if(size()>0){
			if(null != keys){
				for(String key:keys){
					row.put(key, sum(key));
				}
			}else{
				List<String> numberKeys = getRow(0).numberKeys();
				for(String key:numberKeys){
					row.put(key, sum(key));
				}
			}
		}
		return row;
	}
	/**
	 * avg
	 * @param keys keys
	 * @return return
	 */
	public DataRow avgs(String ... keys){
		DataRow row = new DataRow();
		if(size()>0){
			if(null != keys){
				for(String key:keys){
					row.put(key, avg(key));
				}
			}else{
				List<String> numberKeys = getRow(0).numberKeys();
				for(String key:numberKeys){
					row.put(key, avg(key));
				}
			}
		}
		return row;
	}
	
	/** 
	 * 最大值 
	 * @param top 多少行 
	 * @param key  key
	 * @return return
	 */ 
	public BigDecimal maxDecimal(int top, String key){ 
		BigDecimal result = null; 
		int size = rows.size(); 
		if(size>top){ 
			size = top; 
		} 
		for (int i = 0; i < size; i++) {
			BigDecimal tmp = getDecimal(i, key);
			if(null != tmp && (null == result || tmp.compareTo(result) > 0)){
				result = tmp;
			} 
		} 
		return result; 
	} 
	public BigDecimal maxDecimal(String key){ 
		return maxDecimal(size(),key);
	}
	public int maxInt(int top, String key){
		BigDecimal result = maxDecimal(top, key);
		if(null == result){
			return 0;
		}
		return result.intValue();
	}
	public int maxInt(String key){
		return maxInt(size(), key);
	}

	public double maxDouble(int top, String key){
		BigDecimal result = maxDecimal(top, key);
		if(null == result){
			return 0;
		}
		return result.doubleValue();
	}
	public double maxDouble(String key){
		return maxDouble(size(), key);
	}

//	public BigDecimal max(int top, String key){
//		BigDecimal result = maxDecimal(top, key);
//		return result;
//	}
//	public BigDecimal max(String key){
//		return maxDecimal(size(), key);
//	}
	
	
	
	/** 
	 * 最小值 
	 * @param top 多少行 
	 * @param key  key
	 * @return return
	 */ 
	public BigDecimal minDecimal(int top, String key){ 
		BigDecimal result = null;
		int size = rows.size();
		if(size>top){
			size = top;
		}
		for (int i = 0; i < size; i++) {
			BigDecimal tmp = getDecimal(i, key);
			if(null != tmp && (null == result || tmp.compareTo(result) < 0)){
				result = tmp;
			}
		}
		return result; 
	} 
	public BigDecimal minDecimal(String key){ 
		return minDecimal(size(),key); 
	} 

	public int minInt(int top, String key){
		BigDecimal result = minDecimal(top, key);
		if(null == result){
			return 0;
		}
		return result.intValue();
	}
	public int minInt(String key){
		return minInt(size(), key);
	}

	public double minDouble(int top, String key){
		BigDecimal result = minDecimal(top, key);
		if(null == result){
			return 0;
		}
		return result.doubleValue();
	}
	public double minDouble(String key){
		return minDouble(size(), key);
	}

//	public BigDecimal min(int top, String key){
//		BigDecimal result = minDecimal(top, key);
//		return result;
//	}
//	public BigDecimal min(String key){
//		return minDecimal(size(), key);
//	}
	
	/**
	 * key对应的value最大的一行
	 * @param key key
	 * @return return
	 */
	public DataRow max(String key){
		int size = size();
		if(size ==0){
			return null;
		}
		DataRow row = null;
		if(isAsc){
			row = getRow(size-1);
		}else if(isDesc){
			row = getRow(0);
		}else{
			asc(key);
			row = getRow(size-1);
		}
		return row;
	}
	public DataRow min(String key){
		int size = size();
		if(size ==0){
			return null;
		}
		DataRow row = null;
		if(isAsc){
			row = getRow(0);
		}else if(isDesc){
			row = getRow(size-1);
		}else{
			asc(key);
			row = getRow(0);
		}
		return row;
	} 
	/** 
	 * 平均值 空数据不参与加法但参与除法 
	 * @param top 多少行 
	 * @param key key
	 * @return return
	 */ 
	public BigDecimal avg(int top, String key){ 
		BigDecimal result = BigDecimal.ZERO; 
		int size = rows.size(); 
		if(size>top){ 
			size = top; 
		} 
		int count = 0; 
		for (int i = 0; i < size; i++) {
			BigDecimal tmp = getDecimal(i, key);
			if(null != tmp){ 
				result = result.add(tmp);
			} 
			count ++; 
		} 
		if(count >0){ 
			result = result.divide(new BigDecimal(count)); 
		} 
		return result; 
	} 
	public BigDecimal avg(String key){ 
		BigDecimal result = avg(size(),key); 
		return result; 
	} 
	 
	public DataSet addRow(DataRow row) { 
		if (null != row) { 
			rows.add(row); 
		}
		return this; 
	} 
 
	public DataSet addRow(int idx, DataRow row) { 
		if (null != row) { 
			rows.add(idx, row); 
		}
		return this; 
	}
	
	/**
	 * 合并key值 以connector连接
	 * @param key key
	 * @param connector connector
	 * @return return
	 */
	public String concat(String key, String connector){
		return BasicUtil.concat(getStrings(key), connector);
	}
	public String concatNvl(String key, String connector){
		return BasicUtil.concat(getNvlStrings(key), connector);
	}
	public String concatWithoutNull(String key, String connector){
		return BasicUtil.concat(getStringsWithoutNull(key), connector);
	}
	public String concatWithoutEmpty(String key, String connector){
		return BasicUtil.concat(getStringsWithoutEmpty(key), connector);
	}
	public String concatNvl(String key){
		return BasicUtil.concat(getNvlStrings(key), ",");
	}
	public String concatWithoutNull(String key){
		return BasicUtil.concat(getStringsWithoutNull(key), ",");
	}
	public String concatWithoutEmpty(String key){
		return BasicUtil.concat(getStringsWithoutEmpty(key), ",");
	}
	public String concat(String key){
		return BasicUtil.concat(getStrings(key), ",");
	} 
	/** 
	 * 提取单列值 
	 *  
	 * @param key  key
	 * @return return
	 */ 
	public List<Object> fetchValues(String key) { 
		List<Object> result = new ArrayList<Object>(); 
		for (int i = 0; i < size(); i++) { 
			result.add(get(i, key)); 
		} 
		return result; 
	}
	 
	/**
	 * 取单列不重复的值
	 * @param key key
	 * @return return
	 */ 
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
	public List<String> fetchDistinctValues(String key) {
		return fetchDistinctValue(key);
	} 
 
	/** 
	 * 分页 
	 * @param link link
	 * @return return
	 */ 
	public String displayNavi(String link) { 
		String result = ""; 
		if (null != navi) { 
			result = navi.toString(); 
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
	 * @param index  index
	 * @param key  key
	 * @return return
	 */ 
	public String getString(int index, String key) { 
		String result = null; 
		DataRow row = getRow(index); 
		if (null != row) 
			result = row.getString(key); 
		return result; 
	}
	public Object get(int index, String key){
		DataRow row = getRow(index);
		if(null != row){
			return row.get(key);
		}
		return null;
	} 
 
	public String getString(String key) { 
		return getString(0, key); 
	}
	public List<String> getStrings(String key){
		List<String> result = new ArrayList<String>();
		for(DataRow row:rows){
			result.add(row.getString(key));
		}
		return result;
		
	}
	public List<Integer> getInts(String key){
		List<Integer> result = new ArrayList<Integer>();
		for(DataRow row:rows){
			result.add(row.getInt(key));
		}
		return result;
		
	}
	public List<Object> getObjects(String key){
		List<Object> result = new ArrayList<Object>();
		for(DataRow row:rows){
			result.add(row.get(key));
		}
		return result;
		
	}
	public List<String> getDistinctStrings(String key){
		return fetchDistinctValue(key);
	}
	public List<String> getNvlStrings(String key){
		List<String> result = new ArrayList<String>();
		List<Object> list = fetchValues(key);
		for(Object val:list){
			if(null != val){
				result.add(val.toString());
			}else{
				result.add("");
			}
		}
		return result;
	}
	public List<String> getStringsWithoutEmpty(String key){
		List<String> result = new ArrayList<String>();
		List<Object> list = fetchValues(key);
		for(Object val:list){
			if(BasicUtil.isNotEmpty(val)){
				result.add(val.toString());
			}
		}
		return result;
	}
	public List<String> getStringsWithoutNull(String key){
		List<String> result = new ArrayList<String>();
		List<Object> list = fetchValues(key);
		for(Object val:list){
			if(null != val){
				result.add(val.toString());
			}
		}
		return result;
	}
	public BigDecimal getDecimal(int idx, String key){
		BigDecimal result = null;
		DataRow row = getRow(idx);
		if (null != row)
			result = row.getDecimal(key);
		return result;
	}
	public BigDecimal getDecimal(int idx, String key, double def){
		return getDecimal(idx, key, new BigDecimal(def));
	}
	public BigDecimal getDecimal(int idx, String key, BigDecimal def){
		BigDecimal result =getDecimal(idx, key);
		if(null ==result){
			result = def;
		}
		return result;
	} 
 
	/** 
	 * html格式(未实现) 
	 *  
	 * @param index  index
	 * @param key  key
	 * @return return
	 */ 
	public String getHtmlString(int index, String key) { 
		String result = getString(index, key); 
		return result; 
	} 
 
	public String getHtmlString(String key) { 
		return getHtmlString(0, key); 
	}
 
 
	/** 
	 * escape String 
	 *  
	 * @param index  index
	 * @param key  key
	 * @return return
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
	 * @param index  index
	 * @param key  key
	 * @return return
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
	 * @param index  index
	 * @param key  key
	 * @return return
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
	/**
	 * rows 列表中的数据格式化成json格式   不同与toJSON
	 *  map.put("type", "list");
    	map.put("result", result);
    	map.put("message", message);
    	map.put("rows", rows);
    	map.put("success", result);
    	map.put("navi", navi);
	 */ 
	public String toString() { 
		Map<String,Object> map = new HashMap<String,Object>(); 
    	map.put("type", "list"); 
    	map.put("result", result); 
    	map.put("message", message); 
    	map.put("rows", rows); 
    	map.put("success", result); 
    	map.put("navi", navi); 
		return BeanUtil.map2json(map);
	}
	/**
	 * rows 列表中的数据格式化成json格式   不同与toString
	 * @return return
	 */
	public String toJson(){
		return BeanUtil.object2json(this);
	}
	public String getJson(){
		return toJSON();
	}

	public String toJSON(){
		return toJson();
	}
	/**
	 * 根据指定列生成map
	 * @param key  ID,{ID}_{NM}
	 * @return return
	 */
	public Map<String,DataRow> toMap(String key){
		Map<String,DataRow> maps = new HashMap<String,DataRow>();
		for(DataRow row:rows){
			maps.put(row.getString(key), row);
		}
		return maps;
	} 
	/** 
	 * 子类 
	 * @param idx idx
	 * @return return
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
 
	public DataSet setChildren(int idx, Object children) { 
		DataRow row = getRow(idx); 
		if (null != row) { 
			row.setChildren(children); 
		}
		return this; 
	} 
 
	public DataSet setChildren(Object children) { 
		setChildren(0, children);
		return this; 
	} 
 
	/** 
	 * 父类 
	 * @param idx idx
	 * @return return
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
 
	public DataSet setParent(int idx, Object parent) { 
		DataRow row = getRow(idx); 
		if (null != row) { 
			row.setParent(parent); 
		}
		return this; 
	} 
 
	public DataSet setParent(Object parent) { 
		setParent(0, parent);
		return this; 
	} 
 
	/** 
	 * 转换成对象 
	 * @param <T> T
	 * @param index index
	 * @param clazz  clazz
	 * @return return
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
	 * @param <T> T
	 * @param clazz  clazz
	 * @return return
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
 
	public DataSet setDataSource(String dataSource) { 
		if (null == dataSource) {
			return this; 
		}
		this.dataSource = dataSource; 
		if (dataSource.contains(".") && !dataSource.contains(":")) { 
			schema = dataSource.substring(0, dataSource.indexOf(".")); 
			table = dataSource.substring(dataSource.indexOf(".") + 1); 
		}
		for(DataRow row:rows){
			if(BasicUtil.isEmpty(row.getDataSource())){
				row.setDataSource(dataSource);
			}
		}
		return this; 
	} 
 
	public DataSet union(DataSet set, String ... keys) { 
		DataSet result = new DataSet(); 
		if (null != rows) { 
			int size = rows.size(); 
			for (int i = 0; i < size; i++) { 
				result.add(rows.get(i)); 
			} 
		} 
		if (null == keys || keys.length==0) {
			keys = new String[1]; 
			keys[0] = ConfigTable.getString("DEFAULT_PRIMARY_KEY"); 
		} 
		int size = set.size(); 
		for (int i = 0; i < size; i++) { 
			DataRow item = set.getRow(i); 
			if (!result.contains(item, keys)) { 
				result.add(item); 
			} 
		} 
		return result; 
	} 
	/**
	 * 合并
	 * @param set set
	 * @return return
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
	/**
	 * 是否包含这一行
	 * @param row row
	 * @param keys keys
	 * @return return
	 */ 
	public boolean contains(DataRow row, String ... keys) { 
		if (null == rows || rows.size() == 0 || null == row) { 
			return false; 
		}
		if (null == keys || keys.length==0) {
			keys = new String[1];
			keys[0] = ConfigTable.getString("DEFAULT_PRIMARY_KEY","ID"); 
		}
		String params[] = packParam(row, keys);
		return getRows(params).size() > 0;
	}

	public String[] packParam(DataRow row, String ... keys){
		if(null == keys || null == row){
			return null;
		}
		String params[] = new String[keys.length*2];
		int idx = 0;
		for(String key:keys){
			if(null == key){
				continue;
			}
			String ks[] = BeanUtil.parseKeyValue(key);
			params[idx++] = ks[0];
			params[idx++] = row.getString(ks[1]);
		}
		return params;
	}
	
	/**
	 * 从items中按相应的key提取数据 存入
	 * dispatchItem("children",items, "DEPAT_CD")
	 * dispatchItems("children",items, "CD:BASE_CD")
	 * @param field 默认"ITEMS" field:默认"ITEMS"
	 * @param recursion recursion
	 * @param items items
	 * @param keys keys
	 * @return return
	 */ 
	public DataSet dispatchItems(String field, boolean recursion, DataSet items, String ... keys){ 
		if(null == items || null == keys || keys.length == 0){ 
			return this; 
		}
		if(BasicUtil.isEmpty(field)){
			field = "ITEMS";
		} 
		for(DataRow row : rows){
			if(null == row.get(field)){ 
				String[] params = packParam(row, reverseKey(keys));
				DataSet set = items.getRows(params);
				if(recursion){
					set.dispatchItems(field, recursion, items, keys);
				} 
				row.put(field, set);
			} 
		} 
		return this; 
	}
	public DataSet dispatchItems(String field,DataSet items, String ... keys){
		return dispatchItems(field, false, items, keys);
	}
	public DataSet dispatchItems(DataSet items, String ... keys){
		return dispatchItems("ITEMS",items, keys);
	}
	public DataSet dispatchItems(boolean recursion, String ... keys){
		return dispatchItems("ITEMS", recursion, this, keys);
	}
	public DataSet dispatchItems(String field, boolean recursion, String ... keys){
		return dispatchItems(field, recursion, this, keys);
	}

	public DataSet dispatchItem(String field, boolean recursion, DataSet items, String ... keys){
		if(null == items || null == keys || keys.length == 0){
			return this;
		}
		if(BasicUtil.isEmpty(field)){
			field = "ITEM";
		}
		for(DataRow row : rows){
			if(null == row.get(field)){
				String[] params = packParam(row, reverseKey(keys));
				DataRow result = items.getRow(params);
				row.put(field, result);
			}
		}
		return this;
	}
	public DataSet dispatchItem(String field,DataSet items, String ... keys){
		return dispatchItem(field, false, items, keys);
	}
	public DataSet dispatchItem(DataSet items, String ... keys){
		return dispatchItem("ITEM",items, keys);
	}
	public DataSet dispatchItem(boolean recursion, String ... keys){
		return dispatchItem("ITEM", recursion, this, keys);
	}
	public DataSet dispatchItem(String field, boolean recursion, String ... keys){
		return dispatchItem(field, recursion, this, keys);
	}
	
	
	
	/**
	 * 根据keys列建立关联，并将关联出来的结果拼接到集合的条目上，如果有重复则覆盖条目
	 * @param items 被查询的集合
	 * @param keys 关联条件列
	 * @return return
	 */
	public DataSet join(DataSet items, String ... keys){
		if(null == items || null == keys || keys.length == 0){
			return this;
		}
		for(DataRow row : rows){
			String[] params = packParam(row, reverseKey(keys));
			DataRow result = items.getRow(params);
			if(null != result){
				row.copy(result, result.keys());
			}
		}
		return this;
	}


	public DataSet toLowerKey(){
		for(DataRow row:rows){
			row.toLowerKey();
		}
		return this;
	}
	public DataSet toUpperKey(){
		for(DataRow row:rows){
			row.toUpperKey();
		}
		return this;
	}
	/**
	 * 按keys分组
	 * @param keys keys
	 * @return return
	 */
	public DataSet group(String ... keys){
		DataSet result = distinct(keys);
		result.dispatchItems(this, keys);
		return result;
	}
	public DataSet or(DataSet set, String ... keys){
		return this.union(set, keys);
	}
	/**
	 * 交集
	 * @param set set
	 * @param keys keys
	 * @return return
	 */
	public DataSet intersection(DataSet set, String ... keys){
		DataSet result = new DataSet();
		if(null == set){
			return result;
		}
		for(DataRow row:rows){
			if(set.contains(row, reverseKey(keys))){
				result.add((DataRow)row.clone());
			}
		}
		return result;
	}
	public DataSet and(DataSet set, String ... keys){
		return intersection(set, keys);
	}
	/**
	 * 补集
	 * 在this中，但不在set中
	 * this作为超集 set作为子集
	 * @param set set
	 * @param keys keys
	 * @return return 
	 */
	public DataSet complement(DataSet set, String ... keys){
		DataSet result = new DataSet();
		for(DataRow row:rows){
			if(null == set || !set.contains(row, reverseKey(keys))){
				result.add((DataRow)row.clone());
			}
		}
		return result;
	}
	/**
	 * 差集
	 * 从当前集合中删除set中存在的row
	 * @param set set
	 * @param keys CD,"CD:WORK_CD"
	 * @return return
	 */
	public DataSet difference(DataSet set, String ... keys){
		DataSet result = new DataSet();
		for(DataRow row:rows){
			if(null == set || !set.contains(row, reverseKey(keys))){
				result.add((DataRow)row.clone());
			}
		}
		return result;
	}
	private String[] reverseKey(String[] keys){
		if(null == keys){
			return new String[0];
		}
		int size = keys.length;
		String result[] = new String[size];
		for(int i=0; i<size; i++){
			String key = keys[i];
			if(BasicUtil.isNotEmpty(key) && key.contains(":")){
				String ks[] = BeanUtil.parseKeyValue(key);
				key = ks[1] + ":" + ks[0];
			}
			result[i] = key;
		}
		return result;
	}
	/**
	 * 清除空列
	 * @return return
	 */
	public DataSet clearEmpty(){
		for(DataRow row:rows){
			row.clearEmpty();
		}
		return this;
	}
	/**
	 * 清除指定列全为空的行,如果不指定则清除所有列为空的行
	 * @param keys keys
	 * @return return
	 */
	public DataSet clearEmptyRow(String ... keys){
		int size = this.size();
		for(int i=size-1; i>=0; i--){
			DataRow row = getRow(i);
			if(null == keys || keys.length==0){
				if(row.isEmpty()){
					this.remove(row);
				}
			}else{
				boolean isEmpty = true;
				for(String key:keys){
					if(row.isNotEmpty(key)){
						isEmpty = false;
						break;
					}
				}
				if(isEmpty){
					this.remove(row);
				}
			}
		}
		return this;
	}
	/**
	 * NULL &gt; ""
	 * @return return
	 */
	public DataSet nvl(){
		for(DataRow row:rows){
			row.nvl();
		}
		return this;
	} 
	/* ********************************************** 实现接口 *********************************************************** */ 
	public boolean add(DataRow e) {
		return rows.add((DataRow) e); 
	} 
 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean addAll(Collection c) { 
		return rows.addAll(c); 
	} 
 
	public void clear() { 
		rows.clear(); 
	} 
 
	public boolean contains(Object o) { 
		return rows.contains(o); 
	} 
 
	public boolean containsAll(Collection<?> c) { 
		return rows.containsAll(c); 
	} 
 
	public Iterator<DataRow> iterator() { 
		return rows.iterator(); 
	} 
 
	public boolean remove(Object o) { 
		return rows.remove(o); 
	} 
 
	public boolean removeAll(Collection<?> c) { 
		return rows.removeAll(c); 
	} 
 
	public boolean retainAll(Collection<?> c) { 
		return rows.retainAll(c); 
	} 
 
	public Object[] toArray() { 
		return rows.toArray(); 
	} 
 
	@SuppressWarnings("unchecked")
	public Object[] toArray(Object[] a) { 
		return rows.toArray(a); 
	} 
 
	public String getSchema() { 
		return schema; 
	} 
 
	public DataSet setSchema(String schema) { 
		this.schema = schema;
		return this; 
	} 
 
	public String getTable() { 
		return table; 
	} 
 
	public DataSet setTable(String table) {
		if(null != table && table.contains(".")){
			String[] tbs = table.split("\\.");
			this.table = tbs[1];
			this.schema = tbs[0];
		}else{ 
			this.table = table;
		}
		return this; 
	}
	/**
	 * 验证是否过期
	 * 根据当前时间与创建时间对比
	 * 过期返回 true
	 * @param millisecond	过期时间(毫秒) millisecond	过期时间(毫秒)
	 * @return return
	 */
	public boolean isExpire(int millisecond){
		if(System.currentTimeMillis() - createTime > millisecond){
			return true;
		}
		return false;
	}
	public boolean isExpire(long millisecond){
		if(System.currentTimeMillis() - createTime > millisecond){
			return true;
		}
		return false;
	}

	public boolean isExpire(){
		if(getExpires() == -1){
			return false;
		}
		if(System.currentTimeMillis() - createTime > getExpires()){
			return true;
		}
		return false;
	} 
	public long getCreateTime() {
		return createTime;
	}
	public List<DataRow> getRows(){
		return rows;
	}

	/************************** getter setter ***************************************/

	/**
	 * 过期时间(毫秒)
	 * @return return
	 */
	public long getExpires() {
		return expires;
	}
	public DataSet setExpires(long millisecond) {
		this.expires = millisecond;
		return this;
	}
	public DataSet setExpires(int millisecond) {
		this.expires = millisecond;
		return this;
	}
	public boolean isResult() {
		return result;
	}

	public boolean isSuccess() {
		return result;
	}

	public DataSet setResult(boolean result) {
		this.result = result;
		return this;
	}

	public Exception getException() {
		return exception;
	}

	public DataSet setException(Exception exception) {
		this.exception = exception;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public DataSet setMessage(String message) {
		this.message = message;
		return this;
	}

	public PageNavi getNavi() {
		return navi;
	}

	public DataSet setNavi(PageNavi navi) {
		this.navi = navi;
		return this;
	}


	public DataSet setRows(List<DataRow> rows) {
		this.rows = rows;
		return this;
	}

	public String getDataSource() {
		String ds = table;
		if(BasicUtil.isNotEmpty(ds) && BasicUtil.isNotEmpty(schema)){
			ds = schema + "." + ds;
		}
		if(BasicUtil.isEmpty(ds)){
			ds = dataSource;
		}
		return ds;
	}
	public DataSet order(final String ... keys){
		return asc(keys);
	}
	public Object put(String key, Object value, boolean pk, boolean override){
		for(DataRow row:rows){
			row.put(key, value, pk, override);
		}
		return this;
	}
	public Object put(String key, Object value, boolean pk){
		for(DataRow row:rows){
			row.put(key, value, pk);
		}
		return this;
	}
	public Object put(String key, Object value){
		for(DataRow row:rows){
			row.put(key, value);
		}
		return this;
	}
	/**
	 * 行转列
	 * @param pk		唯一标识key(如姓名) pk		唯一标识key(如姓名)
	 * @param classKey  分类key(如科目)
	 * @param valueKey	取值key(如分数) valueKey	取值key(如分数)
	 * @return return
	 */
	public DataSet pivot(String pk, String classKey, String valueKey){
		DataSet result = distinct(pk);
		for(DataRow row:result){
			List<String> classValues = getDistinctStrings(classKey);
			for(String classValue:classValues){
				DataRow valueRow = getRow(pk, row.getString(pk), classKey, classValue);
				if(null != valueRow){
					row.put(classValue, valueRow.get(valueKey));
				}else{
					row.put(classValue, null);
				}
			}
		}
		return result;
	}
	/**
	 * 排序
	 * @param keys keys
	 * @return return
	 */
	public DataSet asc(final String ... keys){
		Collections.sort(rows, new Comparator<DataRow>() {  
            public int compare(DataRow r1, DataRow r2) {
            	int result = 0;
            	for(String key:keys){
            		Object v1 = r1.get(key);
            		Object v2 = r2.get(key);
            		if(null == v1){
            			if(null == v2){
            				continue;
            			}
            			return -1;
            		}else{
            			if(null == v2){
            				return 1;
            			}
            		}
            		if(BasicUtil.isNumber(v1) && BasicUtil.isNumber(v2)){
            			BigDecimal val1 = new BigDecimal(v1.toString());
            			BigDecimal val2 = new BigDecimal(v2.toString());
            			result = val1.compareTo(val2);
            		}else if((BasicUtil.isDate(v1) && BasicUtil.isDate(v2))
            				||(BasicUtil.isDateTime(v1) && BasicUtil.isDateTime(v2))
            				){
            			Date date1 = DateUtil.parse(v1.toString());
            			Date date2 = DateUtil.parse(v2.toString());
            			result = date1.compareTo(date2);
            		}else{
            			result = v1.toString().compareTo(v2.toString());
            		}
            		if(result != 0){
            			return result;
            		}
            	}
            	return 0;
            }  
        }); 
		isAsc = true;
		isDesc = false;
		return this;
	}
	public DataSet desc(final String ... keys){
		Collections.sort(rows, new Comparator<DataRow>() {  
			public int compare(DataRow r1, DataRow r2) {
            	int result = 0;
            	for(String key:keys){
            		Object v1 = r1.get(key);
            		Object v2 = r2.get(key);
            		if(null == v1){
            			if(null == v2){
            				continue;
            			}
            			return 1;
            		}else{
            			if(null == v2){
            				return -1;
            			}
            		}
            		if(BasicUtil.isNumber(v1) && BasicUtil.isNumber(v2)){
            			BigDecimal val1 = new BigDecimal(v1.toString());
            			BigDecimal val2 = new BigDecimal(v2.toString());
            			result = val2.compareTo(val1);
            		}else if((BasicUtil.isDate(v1) && BasicUtil.isDate(v2))
            				||(BasicUtil.isDateTime(v1) && BasicUtil.isDateTime(v2))
            				){
            			Date date1 = DateUtil.parse(v1.toString());
            			Date date2 = DateUtil.parse(v2.toString());
            			result = date2.compareTo(date1);
            		}else{
            			result = v2.toString().compareTo(v1.toString());
            		}
            		if(result != 0){
            			return result;
            		}
            	}
            	return 0;
            } 
        }); 
		isAsc = false;
		isDesc = true;
		return this;
	}
	public DataSet addAllUpdateColumns(){
		for(DataRow row:rows){
			row.addAllUpdateColumns();
		}
		return this;
	}
	public DataSet clearUpdateColumns(){
		for(DataRow row:rows){
			row.clearUpdateColumns();
		}
		return this;
	}
	public DataSet clearNull(){
		for(DataRow row:rows){
			row.clearNull();
		}
		return this;
	}
	private static String key(String key){
		if(null != key && ConfigTable.IS_UPPER_KEY){
			key = key.toUpperCase();
		}
		return key;
	}

	/**
	 * 替换所有NULL值
	 * @param value value
	 * @return return
	 */
	public DataSet replaceNull(String value){
		for(DataRow row:rows){
			row.replaceNull(value);
		}
		return this;
	}

	/**
	 * 替换所有空值
	 * @param value value
	 * @return return
	 */
	public DataSet replaceEmpty(String value){
		for(DataRow row:rows){
			row.replaceEmpty(value);
		}
		return this;
	}
	/* ************************* 类sql操作 ************************************** */
	public DataRow random(){
		DataRow row = null;
		int size = size();
		if(size > 0){
			row = getRow(BasicUtil.getRandomNumber(0, size-1));
		}
		return row;
	}
	public DataSet randoms(int qty){
		DataSet set = new DataSet();
		int size = size();
		if(qty <0){
			qty = 0;
		}
		if(qty > size){
			qty = size;
		}
		for(int i=0; i<qty; i++){
			while(true){
				int idx = BasicUtil.getRandomNumber(0, size-1);
				DataRow row = set.getRow(idx);
				if(!set.contains(row)){
					set.add(row);
					break;
				}
			}
		}
		set.cloneProperty(this);
		return set;
	}
	public DataSet randoms(int min, int max){
		int qty = BasicUtil.getRandomNumber(min, max);
		return randoms(qty);
	}
	public DataSet unique(String ... keys){
		return distinct(keys);
	}
	public DataSet regex(String key, String regex, Regular.MATCH_MODE mode){
		DataSet set = new DataSet();
		String tmpValue;
		for(DataRow row:this){
			tmpValue = row.getString(key);
			if(RegularUtil.match(tmpValue, regex, mode)){
				set.add(row);
			}
		}
		set.cloneProperty(this);
		return set;
	}
	public DataSet regex(String key, String regex){
		return regex(key, regex, Regular.MATCH_MODE.MATCH);
	}
	public boolean checkRequired(String ... keys){
		for(DataRow row:rows){
			if(!row.checkRequired(keys)){
				return false;
			}
		}
		return true;
	}
	public Map<String, Object> getQueryParams() {
		return queryParams;
	}
	public DataSet setQueryParams(Map<String, Object> params) {
		this.queryParams = params;
		return this;
	}
	public Object getQueryParam(String key){
		return queryParams.get(key);
	}

	public DataSet addQueryParam(String key, Object param) {
		queryParams.put(key,param);
		return this;
	}
	public String getDatalink() {
		return datalink;
	}
	public void setDatalink(String datalink) {
		this.datalink = datalink;
	}
	public class Select implements Serializable{
		private static final long serialVersionUID = 1L;
		public DataSet equals(String key, String value){
			return equals(DataSet.this, key, value);
		}
		private DataSet equals(DataSet src, String key, String value){
			DataSet set = new DataSet();
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null != tmpValue &&  tmpValue.equals(value)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet equalsIgnoreCase(String key, String value){
			return equalsIgnoreCase(DataSet.this, key, value);
		}
		private DataSet equalsIgnoreCase(DataSet src, String key, String value){
			DataSet set = new DataSet();
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null != tmpValue &&  tmpValue.equalsIgnoreCase(value)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}

		public DataSet notEquals(String key, String value){
			return notEquals(DataSet.this, key, value);
		}
		private DataSet notEquals(DataSet src, String key, String value){
			DataSet set = new DataSet();
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null != tmpValue &&  !tmpValue.equals(value)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet notEqualsIgnoreCase(String key, String value){
			return notEqualsIgnoreCase(DataSet.this, key, value);
		}
		private DataSet notEqualsIgnoreCase(DataSet src, String key, String value){
			DataSet set = new DataSet();
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null != tmpValue &&  !tmpValue.equalsIgnoreCase(value)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		/**
		 * key列的值是否包含value
		 * @param key key
		 * @param value value
		 * @return return
		 */
		public DataSet contains(String key, String value){
			return contains(DataSet.this, key, value);
		}
		private DataSet contains(DataSet src, String key, String value){
			DataSet set = new DataSet();
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null != tmpValue && tmpValue.contains(value)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}

		public DataSet like(String key, String pattern){
			return like(DataSet.this, key, pattern);
		}
		private DataSet like(DataSet src, String key, String pattern){
			DataSet set = new DataSet();
			if(null == pattern){
				return set;
			}
			pattern = pattern.replace("!", "^").replace("_", "\\s|\\S").replace("%", "(\\s|\\S)*");
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null != tmpValue && RegularUtil.match(tmpValue, pattern, Regular.MATCH_MODE.MATCH)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}

		public DataSet notLike(String key, String pattern){
			return notLike(DataSet.this, key, pattern);
		}
		private DataSet notLike(DataSet src, String key, String pattern){
			DataSet set = new DataSet();
			if(null == pattern){
				return set;
			}
			pattern = pattern.replace("!", "^").replace("_", "\\s|\\S").replace("%", "(\\s|\\S)*");
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null == tmpValue || !RegularUtil.match(tmpValue, pattern, Regular.MATCH_MODE.MATCH)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet startWith(String key, String prefix){
			return startWith(DataSet.this, key, prefix); 
		}
		private DataSet startWith(DataSet src, String key, String prefix){
			DataSet set = new DataSet();
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null != tmpValue && tmpValue.startsWith(prefix)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}

		public DataSet endWith(String key, String suffix){
			return endWith(DataSet.this, key, suffix);
		}
		private DataSet endWith(DataSet src, String key, String suffix){
			DataSet set = new DataSet();
			String tmpValue;
			for(DataRow row:src){
				tmpValue = row.getString(key);
				if (null != tmpValue && tmpValue.endsWith(suffix)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet in(String key, String ... values){
			return in(DataSet.this, key, values);
		}
		private DataSet in(DataSet src, String key, String ... values){
			DataSet set = new DataSet();
			Object tmpValue;
			for(DataRow row:src){
				tmpValue = row.get(key);
				if (null != tmpValue && BasicUtil.containsString(values, tmpValue)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}

		public DataSet in(String key, Collection<Object> values){
			return in(DataSet.this, key, values);
		}
		private DataSet in(DataSet src, String key, Collection<Object> values){
			DataSet set = new DataSet();
			Object tmpValue;
			for(DataRow row:src){
				tmpValue = row.get(key);
				if (null != tmpValue && BasicUtil.containsString(values, tmpValue)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		
		public DataSet inIgnoreCase(String key, String ... values){
			return inIgnoreCase(DataSet.this, key, values);
		}
		private DataSet inIgnoreCase(DataSet src, String key, String ... values){
			DataSet set = new DataSet();
			Object tmpValue;
			for(DataRow row:src){
				tmpValue = row.get(key);
				if (null != tmpValue && BasicUtil.containsIgnoreCase(values, tmpValue)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet inIgnoreCase(String key, Collection<Object> values){
			return inIgnoreCase(DataSet.this, key, values);
		}
		private DataSet inIgnoreCase(DataSet src, String key, Collection<Object> values){
			DataSet set = new DataSet();
			Object tmpValue;
			for(DataRow row:src){
				tmpValue = row.get(key);
				if (null != tmpValue && BasicUtil.containsIgnoreCase(values, tmpValue)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}

		public DataSet notIn(String key, String ... values){
			return notIn(DataSet.this, key, values);
		}
		public DataSet notIn(DataSet src, String key, String ... values){
			DataSet set = new DataSet();
			Object tmpValue;
			for(DataRow row:src){
				tmpValue = row.get(key);
				if (null != tmpValue && !BasicUtil.containsString(values, tmpValue)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet notIn(String key, Collection<Object> values){
			return notIn(DataSet.this, key, values);
		}
		private DataSet notIn(DataSet src, String key, Collection<Object> values){
			DataSet set = new DataSet();
			Object tmpValue;
			for(DataRow row:src){
				tmpValue = row.get(key);
				if (null != tmpValue && !BasicUtil.containsString(values, tmpValue)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet notInIgnoreCase(String key, String ... values){
			return notInIgnoreCase(DataSet.this, key, values);
		}
		private DataSet notInIgnoreCase(DataSet src, String key, String ... values){
			DataSet set = new DataSet();
			Object tmpValue;
			for(DataRow row:src){
				tmpValue = row.get(key);
				if (null != tmpValue && !BasicUtil.containsIgnoreCase(values, tmpValue)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet notInIgnoreCase(String key, Collection<Object> values){
			return notInIgnoreCase(DataSet.this, key, values);
		}
		public DataSet notInIgnoreCase(DataSet src, String key, Collection<Object> values){
			DataSet set = new DataSet();
			Object tmpValue;
			for(DataRow row:src){
				tmpValue = row.get(key);
				if (null != tmpValue && !BasicUtil.containsIgnoreCase(values, tmpValue)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet isNull(String key){
			return isNull(DataSet.this, key);
		}
		private DataSet isNull(DataSet src, String key){
			DataSet set = new DataSet();
			for(DataRow row:src){
				if (null == row.get(key)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet isNull(String ... keys){
			return isNull(DataSet.this, keys);
		}
		private DataSet isNull(DataSet src, String ... keys){
			DataSet set = src;
			if(null != keys){
				for(String key:keys){
					set = isNull(set,key);
				}
			}
			return set;
		}
		public DataSet isNotNull(String key){
			return isNotNull(DataSet.this, key);
		}
		private DataSet isNotNull(DataSet src, String key){
			DataSet set = new DataSet();
			for(DataRow row:src){
				if (null != row.get(key)) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		
		public DataSet notNull(String key){
			return isNotNull(key);
		}

		public DataSet isNotNull(String ... keys){
			return isNotNull(DataSet.this, keys);
		}
		private DataSet isNotNull(DataSet src, String ... keys){
			DataSet set = src;
			if(null != keys){
				for(String key:keys){
					set = isNotNull(set,key);
				}
			}
			return set;
		}
		public DataSet notNull(String ... keys){
			return isNotNull(keys);
		}
		public DataSet isEmpty(String key){
			return isEmpty(DataSet.this, key);
		}
		private DataSet isEmpty(DataSet src, String key){
			DataSet set = new DataSet();
			for(DataRow row:src){
				if (BasicUtil.isEmpty(row.get(key))) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet empty(String key){
			return isEmpty(key);
		}
		public DataSet isEmpty(String ... keys){
			return isEmpty(DataSet.this, keys);
		}
		private DataSet isEmpty(DataSet src, String ... keys){
			DataSet set = src;
			if(null != keys){
				for(String key:keys){
					set = isEmpty(set,key);
				}
			}
			return set;
		}
		
		public DataSet empty(String ... keys){
			return isEmpty(keys);
		}
		public DataSet isNotEmpty(String key){
			return isNotEmpty(DataSet.this, key);
		}
		private DataSet isNotEmpty(DataSet src, String key){
			DataSet set = new DataSet();
			for(DataRow row:src){
				if (BasicUtil.isNotEmpty(row.get(key))) {
					set.add(row);
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet notEmpty(String key){
			return isNotEmpty(key);
		}

		public DataSet isNotEmpty(String ... keys){
			return isNotEmpty(DataSet.this, keys);
		}
		private DataSet isNotEmpty(DataSet src, String ... keys){
			DataSet set = src;
			if(null != keys){
				for(String key:keys){
					set = isNotEmpty(set,key);
				}
			}
			return set;
		}
		public DataSet notEmpty(String ... keys){
			return isNotEmpty(keys);
		}
		public DataSet less(String key, Object value){
			return less(DataSet.this, key, value);
		}
		private DataSet less(DataSet src, String key, Object value){
			DataSet set = new DataSet();
			if(null == value){
				return set;
			}
			if(BasicUtil.isNumber(value)){
				BigDecimal number = new BigDecimal(value.toString());
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.getDecimal(key).compareTo(number) < 0){
						set.add(row);
					}
				}
			}else if(BasicUtil.isDate(value) || BasicUtil.isDateTime(value)){
				Date date = DateUtil.parse(value.toString());
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.isNotEmpty(key) && 
							DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date, row.getDate(key,new Date())) < 0){
						set.add(row);
					}
				}
			}else{
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.getString(key).compareTo(value.toString()) < 0){
						set.add(row);
					}
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet lessEqual(String key, Object value){
			return lessEqual(DataSet.this, key, value); 
		}
		private DataSet lessEqual(DataSet src, String key, Object value){
			DataSet set = new DataSet();
			if(null == value){
				return set;
			}
			if(BasicUtil.isNumber(value)){
				BigDecimal number = new BigDecimal(value.toString());
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.getDecimal(key).compareTo(number) <= 0){
						set.add(row);
					}
				}
			}else if(BasicUtil.isDate(value) || BasicUtil.isDateTime(value)){
				Date date = DateUtil.parse(value.toString());
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.isNotEmpty(key) && 
							DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date, row.getDate(key,new Date())) <= 0){
						set.add(row);
					}
				}
			}else{
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.getString(key).compareTo(value.toString()) >= 0){
						set.add(row);
					}
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet greater(String key, Object value){
			return greater(DataSet.this, key, value);
		}
		private DataSet greater(DataSet src, String key, Object value){
			DataSet set = new DataSet();
			if(null == value){
				return set;
			}
			if(BasicUtil.isNumber(value)){
				BigDecimal number = new BigDecimal(value.toString());
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.getDecimal(key).compareTo(number) > 0){
						set.add(row);
					}
				}
			}else if(BasicUtil.isDate(value) || BasicUtil.isDateTime(value)){
				Date date = DateUtil.parse(value.toString());
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.isNotEmpty(key) && 
							DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date, row.getDate(key,new Date())) > 0){
						set.add(row);
					}
				}
			}else{
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.getString(key).compareTo(value.toString()) > 0){
						set.add(row);
					}
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet greaterEqual(String key, Object value){
			return greaterEqual(DataSet.this, key, value);
		}
		private DataSet greaterEqual(DataSet src, String key, Object value){
			DataSet set = new DataSet();
			if(null == value){
				return set;
			}
			if(BasicUtil.isNumber(value)){
				BigDecimal number = new BigDecimal(value.toString());
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.getDecimal(key).compareTo(number) >= 0){
						set.add(row);
					}
				}
			}else if(BasicUtil.isDate(value) || BasicUtil.isDateTime(value)){
				Date date = DateUtil.parse(value.toString());
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.isNotEmpty(key) && 
							DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date, row.getDate(key,new Date())) >= 0){
						set.add(row);
					}
				}
			}else{
				for(DataRow row:src){
					if(null == row.get(key)){
						continue;
					}
					if(row.getString(key).compareTo(value.toString()) >= 0){
						set.add(row);
					}
				}
			}
			set.cloneProperty(src);
			return set;
		}
		public DataSet between(String key, Object min, Object max){
			return between(DataSet.this, key, min, max);
		}
		private DataSet between(DataSet src, String key, Object min, Object max){
			DataSet set = greaterEqual(src,key, min);
			set = lessEqual(set,key, max);
			return set;
		}
		
	};
	public Select select = new Select();
}
 
