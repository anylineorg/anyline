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

package org.anyline.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;


public class BeanUtil {
	private static Logger log = Logger.getLogger(BeanUtil.class);
	public static boolean setFieldValue(Object obj, Field field, Object value){
		if(null == obj || null == field){
			return false;
		}
		try{
			if(field.isAccessible()){
				//可访问属性
				BeanUtils.setProperty(obj, field.getName(), value);
			}else{
				//不可访问属性
				field.setAccessible(true);
				BeanUtils.setProperty(obj, field.getName(), value);
				field.setAccessible(false);
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean setFieldValue(Object obj, String field, Object value){
		if(null == obj || null == field){
			return false;
		}
		if(obj instanceof Map){
			Map tmp = (Map)obj;
			tmp.put(field, value);
		}else{
			Field f = getField(obj.getClass(), field);
			setFieldValue(obj, f, value);
		}
		return true;
	}
	public static Field getField(Class<?> clazz, String name){
		Field field = null;
		try{
			field = clazz.getField(name);
		}catch(Exception e){}
		if(null == field){
			try{
				field = clazz.getDeclaredField(name);
			}catch(Exception e){
				
			}
		}
		return field;
	}
	public static Object getFieldValue(Object obj, Field field){
		Object value = null;
		if(null == obj || null == field){
			return null;
		}
		try{
			if(field.isAccessible()){
				//可访问属性
				value = field.get(obj);
			}else{
				//不可访问属性
				field.setAccessible(true);
				value = field.get(obj);
				field.setAccessible(false);
			}
		}catch(Exception e){
			return null;
		}
		if(null == value){
			try{
				value = BeanUtils.getProperty(obj, field.getName());
			}catch(Exception e){}
		}
		return value;
	}
	public static Object getFieldValue(Object obj, String field){
		if(null == obj){
			return null;
		}
		Object value = null;
		if(obj instanceof Map){
			Map map = (Map)obj;
			value = map.get(field);
		}else{
			Field f = getField(obj.getClass(), field);
			value = getFieldValue(obj, f);
		}
		return value;
		
	}
	
	public static List<String> getMapKeys(Map map){
		List<String> list = new ArrayList<String>();
		for(Object key:map.keySet()){
			list.add(key.toString());
		}
		return list;
	}
	/**
	 * 属性对应的列
	 * @param field
	 * @return
	 */
	public static String getColumn(Field field, boolean checkInsert, boolean checkUpdate){

		try{
			Annotation annotation = field.getAnnotation(Column.class);
			if(null == annotation){
				//没有Column注解
				return null;
			}
			String column = (String)getAnnotationValue(field, Column.class, "name");
			if(checkInsert){
				//检查是否可插入
				Object insertAble = getAnnotationValue(field, Column.class, "insertable");
				if(!BasicUtil.parseBoolean(insertAble, true)){
					return null;
				}
			}
			if(checkUpdate){
				//检查是否可更新
				Object updateAble = getAnnotationValue(field, Column.class, "updatable");
				if(!BasicUtil.parseBoolean(updateAble, true)){
					return null;
				}
			}
			return column;
		}catch(NoClassDefFoundError e){
			e.printStackTrace();
			return null;
		}
		catch(Exception e){
			return null;
		}
	}
	public static String getColumn(Class<?> clazz,String field, boolean checkInsert, boolean checkUpdate){
		try {
			Field _field = clazz.getDeclaredField(field);
			return getColumn(_field, checkInsert, checkUpdate);
		} catch (SecurityException e) {
			log.error(e);
		} catch (NoSuchFieldException e) {
			log.error(e);
		}
		return null;
	}
	/**
	 * 属性注解值
	 * @param field
	 * @param clazz
	 * @param property
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object getAnnotationValue(Field field, Class clazz, String property){
		try{
			Annotation annotation = field.getAnnotation(clazz);
			if(null == annotation){
				return null;
			}
			Method method = annotation.annotationType().getMethod(property);
			if(null == method){
				return null;
			}
			Object value = method.invoke(annotation);
			return value;
		}catch(Exception e){
			log.error(e);
			return null;
		}
	}
	/**
	 * 根据列名读取属性值
	 * @param column
	 * @return
	 */
	public static Object getValueByColumn(Object obj, String column){
		/*读取类属性*/
		List<Field> fields = getFields(obj.getClass());					
		for(Field field:fields){
			String col = getColumn(field, false, false);
			if(null != col && col.equals(column)){
				try{
					return getFieldValue(obj, field);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 提取类及父类的所有属性
	 * @param clazz
	 * @return
	 */
	public static List<Field> getFields(Class<?> clazz){
		List<Field> fields = new ArrayList<Field>();
		while(null != clazz){
			Field[] tmp = clazz.getDeclaredFields();
			for(Field field:tmp){
				fields.add(field);
			}
			clazz = clazz.getSuperclass();
		}
		return fields;
	}
	public static List<String> getFieldsName(Class<?> clazz){
		List<Field> fields = getFields(clazz);
		List<String> keys = new ArrayList<String>();
		for(Field field:fields){
			keys.add(field.getName());
		}
		return keys;
	}

	/**
	 * 实体bean对应的表
	 */
	public static String checkTable(Class<?> clazz){
		String result = null;
		try{
			Annotation annotation = clazz.getAnnotation(Table.class);					//提取Table注解
			Method method = annotation.annotationType().getMethod("name");				//引用name方法
			result = (String)method.invoke(annotation);									//执行name方法返回结果
		}catch(Exception e){
			log.error(e);
		}
		return result;
	}
	/**
	 * 查询指定类的有annotation注解的属性
	 * @param clazz
	 * @param annotation
	 * @return
	 */
	public static List<Field> searchFieldsByAnnotation(Class clazz, Class annotation){
		List<Field> list = new ArrayList<Field>();
		try{
			List<Field> fields = getFields(clazz);
			for(Field field:fields){
				Annotation at = field.getAnnotation(annotation);
				if(null != at){
					list.add(field);
				}
			}
		}catch(Exception e){
			log.error(e);
		}
		return list;
	}
	/**
	 * 主键列名
	 * @param clazz
	 * @return
	 */
	public static String getPrimaryKey(Class clazz){
		List<Field> fields = searchFieldsByAnnotation(clazz, Id.class);
		if(fields.size()>0){
			Field field = fields.get(0);
			return getColumn(field, false, false);
		}
		return null;
	}
	public static Object getPrimaryValue(Object obj){
		if(null == obj){
			return null;
		}
		String key = getPrimaryKey(obj.getClass());
		return getFieldValue(obj, key);
	}
	/**
	 * 对象转换成Map
	 * @param obj
	 * @return
	 */
	public static Map<String,Object> toMap(Object obj, String ... keys){
		if(null == obj){
			return null;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		if(null == keys || keys.length ==0){
			if(obj instanceof Map){
				// map to map
				Map<String,Object> tmp = (Map<String,Object>)obj;
				for(String key:tmp.keySet()){
					map.put(key, tmp.get(key));
				}
			}else{
				// object to map
				List<Field> fields = getFields(obj.getClass());
				for(Field field:fields){
					String key = field.getName();
					Object value = getFieldValue(obj, field);
					if(null == value){
						value = "";
					}
					map.put(key, value);
				}
			}
		}else{
			for(String key:keys){
				Object value = null;
				if(obj instanceof Map){
					value = ((Map<String,Object>)obj).get(key);
				}else{
					value = getFieldValue(obj, key);
					if(null == value){
						value = "";
					}
				}
				map.put(key, value);
			}
		}
		return map;
	}
	public static List<Map<String,Object>> toMaps(Collection<?> objs, String ... keys){
		if(null == objs){
			return null;
		}
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for(Object obj:objs){
			list.add(toMap(obj,keys));
		}
		return list;
	}
	/**
	 * 过虑指定属性
	 * @param objs
	 * @param keys
	 */
	public static void filter(Collection<Object> objs, String ... keys){
		if(null == keys || null == objs){
			return;
		}
		for(String key:keys){
			filter(objs, key);
		}
	}
	public static void filter(Object obj, String key){
		if(null == obj || null == key){
			return;
		}
		if(obj instanceof Map){
			((Map) obj).remove(key);
		}else{
			setFieldValue(obj, key, null);
		}
	}
	/**
	 * 提取指定属性值
	 * @param objs
	 * @param keys
	 */
	public static Collection<Object> fetch(Collection<Object> objs, String ... keys){
		if(null == objs){
			return null;
		}
		Collection<Object> list = new ArrayList<Object>();
		for(Object obj: objs){
			list.add(fetch(obj, keys));
		}
		return list;
	}
	public static Object fetch(Object obj, String ... keys){
		if(null == obj){
			return null;
		}
		Object result = null;
		try{
			result = obj.getClass().newInstance();
			if(null != keys){
				for(String key:keys){
					if(obj instanceof Map){
						Object value = ((Map)obj).get(key);
						((Map)obj).put(key, value);
					}else{
						Object value = BeanUtil.getFieldValue(obj, key);
						BeanUtil.setFieldValue(obj, key, value);
					}
				}
			}
		}catch(Exception e){
			
		}
		return result;
	}
	
}
