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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;

@MappedSuperclass
public abstract class AnylineEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	@Transient
	protected static Logger log = Logger.getLogger(AnylineEntity.class);

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
	

	/**
	 * 实体类对应的列
	 * @param checkInsert
	 * 			是否检查可插入
	 * @param checkUpdate
	 * 			是否检查可更新
	 * @return
	 */
	public List<String> getColumns(boolean checkInsert, boolean checkUpdate){
		List<String> columns = new ArrayList<String>();
		/*读取类属性*/
		Class clazz = this.getClass();
		while(null != clazz){
			Field[] fields = clazz.getDeclaredFields();
			for(Field field:fields){
				String column = BeanUtil.getColumn(field, checkInsert, checkUpdate);
				if(null != column){
					columns.add(column);
				}	
			}
			clazz = clazz.getSuperclass();
		}
		return columns;
	}
	/**
	 * 根据属性读取对应的列名
	 * @param property
	 * @return
	 */
	public String getColumnByProperty(String property){
		String column = null;
		Field field = null;
		try{
			field = this.getClass().getDeclaredField(property);
		}catch(Exception ee){}
		if(null == field){
			try{
				field = this.getClass().getSuperclass().getDeclaredField(property);
			}catch(Exception ee){}
		}
		if(null != field){
			column = BeanUtil.getColumn(field, false, false);
		}
		return column;
	}
	/**
	 * 根据列名读取属性值
	 * @param column
	 * @return
	 */
	public Object getValueByColumn(String column){
		return BeanUtil.getValueByColumn(this, column);
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
		this.dataSource = dataSource;
		if(dataSource.contains(".") && !dataSource.contains(":")){
			schema = dataSource.substring(0,dataSource.indexOf("."));
			table = dataSource.substring(dataSource.indexOf(".") + 1);
		}
	}
}
