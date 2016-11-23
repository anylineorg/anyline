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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */
package org.anyline.entity;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class DataRow extends HashMap<String, Object> implements Serializable{
	private static final long serialVersionUID = -2098827041540802313L;

	private static Logger log = Logger.getLogger(DataRow.class);

	public static String PARENT 		= "PARENT";				//上级数据
	public static String ALL_PARENT 	= "ALL_PARENT";			//所有上级数据
	public static String CHILDREN 		= "CHILDREN";			//子数据
	public static String PRIMARY_KEY	= ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD");
	public static String ITEMS			= "ITEMS";
	private DataSet container;									//包含当前对象的容器

	private List<String> primaryKeys = new ArrayList<String>();	//主键
	private List<String> updateColumns = new ArrayList<String>();
	private String dataSource;									//数据源(表|视图|XML定义SQL)
	private String schema;
	private String table;
	private Object clientTrace;									//客户端数据
	private long createTime = 0;								//创建时间
	private long expires = -1;									//过期时间(毫秒) 从创建时刻计时expires毫秒后过期
	
	protected Boolean isNew = false;							//强制新建(适应hibernate主键策略)

	@Autowired
	protected transient AnylineService service;
	
	/**
	 * 
	 * @param obj
	 * @param keys 列名:obj属性名 "ID:memberId"
	 * @return
	 */
	public static DataRow parse(Object obj, String ... keys){
		Map<String,String> map = new HashMap<String,String>();
		if(null != keys){
			for(String key:keys){
				String tmp[] = key.split(":");
				if(null != tmp && tmp.length>1){
					map.put(tmp[1].trim().toUpperCase(), tmp[0].trim().toUpperCase());
				}
			}
		}
		DataRow row = new DataRow();
		if(null != obj){
			List<String> fields = BeanUtil.getFieldsName(obj.getClass());
			for(String field : fields){
				String col = map.get(field.toUpperCase());
				if(null == col){
					col = field;
				}
				row.put(col, BeanUtil.getFieldValue(obj, field));
			}
		}
		return row;
	}
	public DataRow(){
		String pk = PRIMARY_KEY;
		if(null != pk){
			primaryKeys.add(PRIMARY_KEY);
		}
		createTime = System.currentTimeMillis();
	}
	public DataRow(String table){
		this();
		this.setTable(table);
	}
	public DataRow(Map<String,Object> map){
		this();
		for(Iterator<String> itr=map.keySet().iterator(); itr.hasNext();){
			String key = itr.next();
			Object value = map.get(key);
			put(key.toUpperCase(), value);
		}
	}
	public long getCreateTime(){
		return createTime;
	}
	
	public long getExpires() {
		return expires;
	}
	public void setExpires(long expires) {
		this.expires = expires;
	}
	public void setExpires(int expires) {
		this.expires = expires;
	}
	public DataRow merge(DataRow row, boolean over){
		List<String> keys = row.keys();
		for(String key : keys){
			if(over || null != this.get(key)){
				this.put(key, row.get(key));
			}
		}
		return this;
	}
	public DataRow merge(DataRow row){
		return merge(row, false);
	}
	public Boolean isNew() {
		String pk = getPrimaryKey();
		String pv = getString(pk);
		return (null == pv ||(null == isNew)|| isNew || BasicUtil.isEmpty(pv));
	}
	public String getCd(){
		return getString("CD");
	}
	public String getId(){
		return getString("ID");
	}
	public String getCode(){
		return getString("CODE");
	}
	public String getNm(){
		return getString("NM");
	}
	public String getName(){
		return getString("NAME");
	}
	public String getTitle(){
		return getString("TITLE");
	}
	public DataSet getItems(){
		Object items = get(ITEMS);
		if(items instanceof DataSet){
			return (DataSet)items;
		}
		return null;
	}
	public void putItems(Object obj){
		put(ITEMS,obj);
	}
	/**
	 * 保存之前处理
	 * @return
	 */
	public boolean processBeforeSave(){
		return true;
	}
	/**
	 * 显示之前处理
	 * @return
	 */
	public boolean processBeforeDisplay(){
		return true;
	}
	/**
	 * 添加主键
	 * 当前对象处于容器中时,设置容器主键,否则设置自身主键
	 * @param primary
	 */
	public DataRow addPrimaryKey(String ... primaryKeys){
		if(null != primaryKeys){
			List<String> list = new ArrayList<String>();
			for(String pk:primaryKeys){
				list.add(pk);
			}
			return addPrimaryKey(list);
		}
		return this;
	}
	public DataRow addPrimaryKey(Collection<String> primaryKeys){
		if(BasicUtil.isEmpty(primaryKeys)){
			return this;
		}
		/*设置容器主键*/
		if(hasContainer()){
			getContainer().addPrimary(primaryKeys);
			return this;
		}
		
		/*没有处于容器中时,设置自身主键*/
		if(null == this.primaryKeys){
			this.primaryKeys = new ArrayList<String>();
		}
		for(String item:primaryKeys){
			if(BasicUtil.isEmpty(item)){
				continue;
			}
			item = item.toUpperCase();
			if(!this.primaryKeys.contains(item)){
				this.primaryKeys.add(item);
			}
		}
		return this;
	}
	/**
	 * 设置主键 先清空之前设置过和主键
	 * 当前对象处于容器中时,设置容器主键,否则设置自身主键
	 * @param primary
	 */
	public DataRow setPrimaryKey(String ... primaryKeys){
		if(null != primaryKeys){
			List<String> list = new ArrayList<String>();
			for(String pk:primaryKeys){
				list.add(pk);
			}
			return setPrimaryKey(list);
		}
		return this;
	}
	public DataRow setPrimaryKey(Collection<String> primaryKeys){
		if(BasicUtil.isEmpty(primaryKeys)){
			return this;
		}
		/*设置容器主键*/
		if(hasContainer()){
			getContainer().setPrimary(primaryKeys);
			return this;
		}
		
		/*没有处于容器中时,设置自身主键*/
		if(null == this.primaryKeys){
			this.primaryKeys = new ArrayList<String>();
		}else{
			this.primaryKeys.clear();
		}
		this.addPrimaryKey(primaryKeys);
		return this;
	}
	/**
	 * 读取主键
	 * 主键为空时且容器有主键时,读取容器主键,否则返回默认主键
	 * @return
	 */
	public List<String> getPrimaryKeys(){
		/*有主键直接返回*/
		if(hasSelfPrimaryKeys()){
			return primaryKeys;
		}
		
		/*处于容器中并且容器有主键,返回容器主键*/
		if(hasContainer() && getContainer().hasPrimaryKeys()){
			return getContainer().getPrimaryKeys();
		}
		
		/*本身与容器都没有主键 返回默认主键*/
		List<String> defaultPrimary = new ArrayList<String>();
		String configKey = ConfigTable.getString("DEFAULT_PRIMARY_KEY");
		if(null != configKey && !configKey.trim().equals("")){
			defaultPrimary.add(configKey);	
		}

		return defaultPrimary;
	}
	public String getPrimaryKey(){
		List<String> keys = getPrimaryKeys();
		if(null != keys && keys.size()>0){
			return keys.get(0); 
		}
		return null;
	}
	/**
	 * 主键值
	 * @return
	 */
	public List<Object> getPrimaryValues(){
		List<Object> values = new ArrayList<Object>();
		List<String> keys = getPrimaryKeys();
		if(null != keys){
			for(String key:keys){
				values.add(get(key));
			}
		}
		return values;
	}
	public Object getPrimaryValue(){
		String key = getPrimaryKey();
		if(null != key){
			return get(key);
		}
		return null;
	}
	/**
	 * 是否有主键
	 * @return
	 */
	public boolean hasPrimaryKeys(){
		if(hasSelfPrimaryKeys()){
			return true;
		}
		if(null != getContainer()){
			return getContainer().hasPrimaryKeys();
		}
		if(keys().contains(ConfigTable.getString("DEFAULT_PRIMARY_KEY"))){
			return true;
		}
		return false;
	}
	/**
	 * 自身是否有主键
	 * @return
	 */
	public boolean hasSelfPrimaryKeys(){
		if(null != primaryKeys && primaryKeys.size()>0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 读取数据源
	 * 数据源为空时,读取容器数据源
	 * @return
	 */
	public String getDataSource() {
		String ds = table;
		if(BasicUtil.isNotEmpty(ds) && BasicUtil.isNotEmpty(schema)){
			ds = schema + "." + ds;
		}
		if(BasicUtil.isEmpty(ds)){
			ds = dataSource;
		}
		if(null == ds && null != getContainer()){
			ds = getContainer().getDataSource();
		}
		
		return ds;
	}

	/**
	 * 设置数据源
	 * 当前对象处于容器中时,设置容器数据源
	 * @param dataSource
	 */
	public void setDataSource(String dataSource){
		if(null == dataSource){
			return;
		}
		if(null  != getContainer()){
			getContainer().setDataSource(dataSource);
		}else{
			this.dataSource = dataSource;
			if(dataSource.contains(".") && !dataSource.contains(":")){
				schema = dataSource.substring(0,dataSource.indexOf("."));
				table = dataSource.substring(dataSource.indexOf(".") + 1);
			}
		}
	}
	/**
	 * 子类
	 * @return
	 */
	public Object getChildren(){
		return get(CHILDREN);
	}
	public void setChildren(Object children){
		put(CHILDREN, children);
	}
	/**
	 * 父类
	 * @return
	 */
	public Object getParent(){
		return get(PARENT);
	}
	public void setParent(Object parent){
		put(PARENT,parent);
	}
	/**
	 * 所有上级数据(递归)
	 * @return
	 */
	public List<Object> getAllParent(){
		if(null != get(ALL_PARENT)){
			return (List<Object>)get(ALL_PARENT);
		}
		List<Object> parents = new ArrayList<Object>();
		Object parent = getParent();
		if(null != parent){
			parents.add(parent);
			if(parent instanceof DataRow){
				DataRow tmp = (DataRow)parent;
				parents.addAll(tmp.getAllParent());
			}
		}
		return parents;
	}
	/**
	 * 转换成对象
	 * @param clazz
	 * @return
	 */
	public <T> T entity(Class<T> clazz){
		T entity = null;
		if(null == clazz){
			return entity;
		}
		try {
			entity = (T)clazz.newInstance();
			/*读取类属性*/
			List<Field> fields = BeanUtil.getFields(clazz);		
			for(Field field:fields){
				/*取request参数值*/
				String column = BeanUtil.getColumn(field, false, false);
				Object value = get(column);
				/*属性赋值*/
				BeanUtil.setFieldValue(entity, field, value);
			}//end 自身属性
		} catch (InstantiationException e) {
			log.error(e);
		} catch (IllegalAccessException e) {
			log.error(e);
		} catch (SecurityException e) {
			log.error(e);
		} catch (Exception e) {
			log.error(e);
		}
		return entity;
	}
	
	public List<String> keys(){
		List<String> keys = new ArrayList<String>();
		for(Iterator<String> itr=this.keySet().iterator(); itr.hasNext();){
			keys.add(itr.next());
		}
		return keys;
	}
	@Override
	public Object put(String key, Object value){
		if(null != key){
			super.put(key.toUpperCase(), value);
		}
		if(!updateColumns.contains(key.toUpperCase())){
			updateColumns.add(key.toUpperCase());
		}
		return this;
	}
	/**
	 * 
	 * @param key
	 * @param value
	 * @param pk		是否是主键
	 * @param override	是否覆盖之前的主键(追加到primaryKeys) 默认覆盖(单一主键)
	 * @return
	 */
	public Object put(String key, Object value, boolean pk, boolean override){
		if(pk){
			if(override){
				primaryKeys.clear();
			}
			this.addPrimaryKey(key);
		}
		this.put(key, value);
		return this;
	}
	public Object put(String key, Object value, boolean pk){
		this.put(key, value, pk , true);
		return this;
	}
	
	public Object get(String key){
		Object result = null;
		if(null != key){
			result = super.get(key.toUpperCase());
		}
		return result;
	}
	public String getStringNvl(String key, String ... defs){
		String result = getString(key);
		if(BasicUtil.isEmpty(result)){
			if(null == defs || defs.length == 0){
				result = "";
			}else{
				result = BasicUtil.nvl(result,defs).toString();
			}
		}
		return result;
	}
	public String getString(String key){
		String result = null;
		Object value = get(key);
		if(null != value)
			result = value.toString();
		return result;
	}
	/**
	 * boolean类型true 解析成 1
	 * @param key
	 * @return
	 */
	public int getInt(String key){
		int result = 0;
		try{
			Object val = get(key);
			if(null != val){
				if(val instanceof Boolean && (Boolean)val){
					result = 1;
				}else{
					result = Integer.parseInt(val.toString());
				}
			}
		}catch(Exception e){
			result = 0;
		}
		return result;
	}
	public double getDouble(String key){
		double result = 0;
		Object value = get(key);
		try{
			result = Double.parseDouble(value.toString());
		}catch(Exception e){
			result = 0;
		}
		return result;
	}
	public long getLong(String key){
		long result = 0;
		try{
			Object value = get(key);
			result = Long.parseLong(value.toString());
		}catch(Exception e){
			result = 0;
		}
		return result;
	}
	public boolean getBoolean(String key, boolean def){
		return BasicUtil.parseBoolean(getString(key), def);
	}
	public BigDecimal getDecimal(String key){
		BigDecimal result = null;
		try{
			result = new BigDecimal(getString(key));
		}catch(Exception e){
			result = null;
		}
		return result;
	}
	public BigDecimal getDecimal(String key, double def){
		return getDecimal(key, new BigDecimal(def));
	}
	public BigDecimal getDecimal(String key, BigDecimal def){
		BigDecimal result = getDecimal(key);
		if(null == result){
			result = def;
		}
		return result;
	}
	
	/**
	 * 转换成json格式
	 * @return
	 */
	public String toJSON(){
		String result = "";
		JSONObject json = JSONObject.fromObject(this);
		result = json.toString();
		return result;
	}
	/**
	 * 轮换成xml格式s
	 * @return
	 */
	public String toXML(){
		StringBuilder builder = new StringBuilder();
		return builder.toString();
	}
	/**
	 * 是否处于容器内
	 * @return
	 */
	public boolean hasContainer(){
		if(null != getContainer()){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 包含当前对象的容器
	 * @return
	 */
	public DataSet getContainer() {
		return container;
	}
	public void setContainer(DataSet container) {
		this.container = container;
	}
	public Object getClientTrace() {
		return clientTrace;
	}
	public void setClientTrace(Object clientTrace) {
		this.clientTrace = clientTrace;
	}
	public String getSchema() {
		if(null != schema){
			return schema;
		}else{
			DataSet container = getContainer();
			if(null != container){
				return container.getSchema();
			}else{
				return null;
			}
		}
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getTable() {
		if(null != table){
			return table;
		}else{
			DataSet container = getContainer();
			if(null != container){
				return container.getTable();
			}else{
				return null;
			}
		}
	}
	public void setTable(String table) {
		if(null != table && table.contains(".")){
			String[] tbs = table.split("\\.");
			this.table = tbs[1];
			this.schema = tbs[0];
		}else{
			this.table = table;
		}
	}
	/**
	 * 验证是否过期
	 * 根据当前时间与创建时间对比
	 * 过期返回 true
	 * @param expire	过期时间(毫秒)
	 * @return
	 */
	public boolean isExpire(int ms){
		if(System.currentTimeMillis() - createTime > ms){
			return true;
		}
		return false;
	}
	public boolean isExpire(long expire){
		if(System.currentTimeMillis() - createTime > expire){
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
	public Object clone(){
		DataRow row = (DataRow)super.clone();
		row.container = this.container;
		row.primaryKeys = this.primaryKeys;
		row.dataSource = this.dataSource;
		row.schema = this.schema;
		row.table = this.table;
		row.clientTrace = this.clientTrace;
		row.createTime = this.createTime;
		row.isNew = this.isNew;
		row.service = this.service;
		return row;
	}
	public Boolean getIsNew() {
		return isNew;
	}
	public void setIsNew(Boolean isNew) {
		this.isNew = isNew;
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
	
	public List<String> getUpdateColumns() {
		return updateColumns;
	}
	public DataRow remove(String key){
		if(null != key){
			super.remove(key.toUpperCase());
		}
		return this;
	}
	/**
	 * 清空需要更新的列
	 * @return
	 */
	public DataRow clearUpdateColumns(){
		updateColumns.clear();
		return this;
	}
	public DataRow removeUpdateColumns(String ... cols){
		if(null != cols){
			for(String col:cols){
				updateColumns.remove(col.toUpperCase());
			}
		}
		return this;
	}
	public DataRow addUpdateColumns(String ... cols){
		if(null != cols){
			for(String col:cols){
				if(!updateColumns.contains(col.toUpperCase())){
					updateColumns.add(col.toUpperCase());
				}
			}
		}
		return this;
	}
	public DataRow addAllUpdateColumns(){
		updateColumns.clear();//清空后，sql会根据所有的列生成
		return this;
	}
	/**
	 * 将数据从data中复制到this
	 * @param data
	 * @param keys this与data中的key不同时 "this.key:data.key"(CD:ORDER_CD)
	 * @return
	 */
	public DataRow copy(DataRow data, String ... keys){
		if(null == data || null == keys){
			return this;
		}
		for(String key:keys){
			String key1 = key;
			String key2 = key;
			if(key.contains(":")){
				String tmp[] = key.split(":");
				key1 = tmp[0];
				key2 = tmp[1];
			}
			this.put(key1, data.get(key2));
		}
		return this;
	}
}
