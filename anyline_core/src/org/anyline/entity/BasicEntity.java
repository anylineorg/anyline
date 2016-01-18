/* 
 * Copyright 2006-2015 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

@MappedSuperclass
public abstract class BasicEntity {
	@Transient
	protected static Logger LOG = Logger.getLogger(BasicEntity.class);
	   
	@Id         
	@Column(name="CD",insertable=true,updatable=false)   
	protected String cd;										//主键
	@Column(name="DESCRIPTION")
	protected String description;								//描述
	@Column(name="REMARK")
	protected String remark;									//备注
	@Column(name="IDX")
	protected String idx;										//排序
	@Column(name="REG_CD")
	protected String regCd;										//注册管理员CD
	@Column(name="REG_TIME",insertable=false,updatable=false)
	protected String regTime;									//注册时间
	@Column(name="REG_IP")
	protected String regIp;										//注册客户端IP
	@Column(name="REG_CLIENT_CD")
	protected String regClientCd;								//注册客户端CLIENT
	@Column(name="UPT_CD")
	protected String uptCd;										//修改管理员CD
	@Column(name="UPT_TIME")
	protected String uptTime;									//修改时间
	@Column(name="UPT_IP")
	protected String uptIp;										//修改客户端IP
	@Column(name="UPT_CLIENT_CD")
	protected String uptClientCd;								//修改客户端CLIENT
	@Column(name="STATUS",insertable=false,updatable=true)
	protected String status = "1";								//活动状态
	
	@Transient
	protected Boolean isNew = false;							//强制新建(适应hibernate主键策略)

	@Transient
	protected Object clientTrace;								//客户端数据
	public Object getClientTrace() {
		return clientTrace;
	}
	public void setClientTrace(Object clientTrace) {
		this.clientTrace = clientTrace;
	}
	/**
	 * 实体bean对应的表
	 */
	public String getTable(){
		String result = null;
		try{
			Annotation annotation = this.getClass().getAnnotation(Table.class);			//提取Table注解
			Method method = annotation.annotationType().getMethod("name");				//引用name方法
			result = (String)method.invoke(annotation);									//执行name方法返回结果
			result = result.replace("[", "").replace("]","");
		}catch(NoClassDefFoundError e){
			LOG.error(e);
		}catch(Exception e){
			LOG.error(e);
		}
		return result;
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
	public String getDataSource(){
		String ds = getTable();
		if(null != ds){
			if(!ds.contains(".") && !ds.contains("[")){
				ds = "[" + ds + "]";
			}
		}
		return ds;
	}
	/**
	 * 生成主键
	 */
	public void createPrimary(){
		if(BasicUtil.isEmpty(cd)){			
			setCd(BasicUtil.getRandomUpperString(10));
		}
	}
	public void setPrimaryValue(String value){
		setCd(value);
	}
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
	public String toString(){
		return cd;
	}

	public String getCd() {
		return cd;
	}

	public void setCd(String cd) {
		this.cd = cd;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getIdx() {
		return idx;
	}

	public void setIdx(String idx) {
		this.idx = idx;
	}

	public String getRegCd() {
		return regCd;
	}

	public void setRegCd(String regCd) {
		this.regCd = regCd;
	}

	public String getRegTime() {
		return regTime;
	}

	public void setRegTime(String regTime) {
		this.regTime = regTime;
	}

	public String getUptCd() {
		return uptCd;
	}

	public void setUptCd(String uptCd) {
		this.uptCd = uptCd;
	}

	public String getUptTime() {
		return uptTime;
	}

	public void setUptTime(String uptTime) {
		this.uptTime = uptTime;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Boolean isNew() {
		return (null == cd) ||(null == isNew)|| isNew || BasicUtil.isEmpty(cd);
	}

	public void setNew(Boolean isNew) {
		this.isNew = isNew;
	}
	public String getRegIp() {
		return regIp;
	}
	public void setRegIp(String regIp) {
		this.regIp = regIp;
	}
	public String getUptIp() {
		return uptIp;
	}
	public void setUptIp(String uptIp) {
		this.uptIp = uptIp;
	}
	public String getRegClientCd() {
		return regClientCd;
	}
	public void setRegClientCd(String regClientCd) {
		this.regClientCd = regClientCd;
	}
	public String getUptClientCd() {
		return uptClientCd;
	}
	public void setUptClientCd(String uptClientCd) {
		this.uptClientCd = uptClientCd;
	}
	
}
