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


package org.anyline.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.anyline.entity.DataSet;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BeanUtil {
	private static final Logger log = LoggerFactory.getLogger(BeanUtil.class);
	public static boolean setFieldValue(Object obj, Field field, Object value){
		if(null == obj || null == field){
			return false;
		}
		if(Modifier.isStatic(field.getModifiers())){
			return false;
		}
		try{
			if(field.isAccessible()){
				//可访问属性
				field.set(obj, value);
				BeanUtils.setProperty(obj, field.getName(), value);
			}else{
				//不可访问属性
				field.setAccessible(true);
				field.set(obj, value);
				BeanUtils.setProperty(obj, field.getName(), value);
				field.setAccessible(false);
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean setFieldValue(Object obj, String field, Object value, boolean recursion){
		if(null == obj || null == field){
			return false;
		}
		if(obj instanceof Map){
			Map tmp = (Map)obj;
			tmp.put(field, value);
		}else{
			Field f = getField(obj.getClass(), field, recursion);
			setFieldValue(obj, f, value);
		}
		return true;
	}
	public static boolean setFieldValue(Object obj, String field, Object value){
		return setFieldValue(obj, field, value, true);
	}
	public static Method getMethod(Class<?> clazz, String name, boolean recursion, Class<?>... parameterTypes){
		Method method = null;
		try{
			method = clazz.getMethod(name, parameterTypes);
		}catch(Exception e){}
		if(null == method){
			try{
				method = clazz.getDeclaredMethod(name, parameterTypes);
			}catch(Exception e){
				
			}
		}
		//递归父类
		if(null == method && recursion){
			clazz = clazz.getSuperclass();
			if(null != clazz){
				method = getMethod(clazz, name, recursion, parameterTypes);
			}
		}
		return method;
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes){
		return getMethod(clazz, name, false, parameterTypes);
	}
	public static Field getField(Class<?> clazz, String name, boolean recursion){
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
		//递归父类
		if(null == field && recursion){
			clazz = clazz.getSuperclass();
			if(null != clazz){
				field = getField(clazz, name);
			}
		}
		return field;
	}

	public static Field getField(Class<?> clazz, String name){
		return getField(clazz, name, true);
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
	public static Object getFieldValue(Object obj, String field, boolean recursion){
		if(null == obj){
			return null;
		}
		Object value = null;
		if(obj instanceof Map){
			Map map = (Map)obj;
			value = map.get(field);
		}else{
			Field f = getField(obj.getClass(), field, recursion);
			value = getFieldValue(obj, f);
		}
		return value;
		
	}

	public static Object getFieldValue(Object obj, String field){
		return getFieldValue(obj, field, false);
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
				return field.getName();
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
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	public static String getColumn(Class<?> clazz,String field, boolean checkInsert, boolean checkUpdate){
		try {
			Field _field = clazz.getDeclaredField(field);
			return getColumn(_field, checkInsert, checkUpdate);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
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
			e.printStackTrace();
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
		if(null == obj || null == column){
			return null;
		}
		if(obj instanceof Map){
			return ((Map)obj).get(column);
		}else{
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 主键列名
	 * @param clazz
	 * @return
	 */
	public static String getPrimaryKey(Class<?> clazz){
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
	public static void removeProperty(Collection<Object> objs, String ... keys){
		if(null == keys || null == objs){
			return;
		}
		for(String key:keys){
			removeProperty(objs, key);
		}
	}
	public static void removeProperty(Object obj, String key){
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
	/**
	 * 参考 DataSet.getRows(String ... kvs);
	 * @param list
	 * @param params
	 * @return
	 */
	public static Collection<?> select(Collection<?> list, String ... params){
		if(null == list || null == params || params.length==0){
			return list;
		}
		if(list instanceof DataSet){
			return ((DataSet)list).getRows(params);
		}
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
		
		Object[] items = list.toArray();
		int size = list.size();
		for(i=size-1; i>=0; i--){
			Object obj = items[i];
			boolean chk = true;//对比结果
			for(String k : kvs.keySet()){
				String v = kvs.get(k);
				Object value = BeanUtil.getValueByColumn(obj, k);
				
				if(null == v){
					if(null != value){
						chk = false;
						break;
					}
				}else{
					if(!v.equals(value+"")){
						chk = false;
						break;
					}
				}
			}
			if(!chk){
				list.remove(obj);
			}
		}
		return list;
	}
	/**
	 * pack包下的所有类 不包括jar包中定义类
	 * @param pack
	 * @return
	 */
	
	public static List<Class> getClasses(String pack, Class ... bases){
		List<Class> list = new ArrayList<Class>();
		File dir = new File(BeanUtil.class.getResource("/").getFile(),pack.replace(".", File.separator));
		List<File> files = FileUtil.getAllChildrenFile(dir,".class");
		for(File file:files){
			try{
				String path = file.getAbsolutePath();
				if(ConfigTable.isDebug() && log.isWarnEnabled()){
					log.warn("[检索类][file:{}]",path);
				}
				if(path.contains(File.separator+"classes"+File.separator)){
					path = path.substring(path.indexOf(File.separator+"classes"+File.separator));
				}
				path = path.replace(File.separator, ".");
				path = path.replace(".classes.", "").replace(".class", "");
				if(ConfigTable.isDebug() && log.isWarnEnabled()){
					log.warn("[检索类][class:{}]",path);
				}
				Class clazz = Class.forName(path);
				if(clazz.getName().contains("$")){
					continue;
				}
				if(null != bases && bases.length>0){
					for(Class base:bases){
						if(clazz.equals(base)){
							continue;
						}
						if(base.isAssignableFrom(clazz)){
							list.add(clazz);
							continue;
						}
					}
				}else{
					list.add(clazz);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return list;
	}
	public static <T> T map2object(Map<String,?> map, Class<T> clazz, boolean recursion){
		T obj = null;
		try {
			obj = (T)clazz.newInstance();
			Set es = map.entrySet();
			Iterator it = es.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String k = (String) entry.getKey();
				Object v = entry.getValue();
				BeanUtil.setFieldValue(obj, k, v, recursion);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return obj;
	}
	public static <T> T map2object(Map<String,?> map, Class<T> clazz){
		return map2object(map, clazz, false);
	}
	public static <T> T json2oject(JSONObject json, Class<T> clazz){
		T obj = null;
		try{
			obj = (T)clazz.newInstance();
			Iterator it = json.keys();
			while (it.hasNext()) {
				String key = it.next().toString();
				Object v = json.get(key);
				BeanUtil.setFieldValue(obj, key, v);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return obj;
	}
	public static <T> T json2oject(String json, Class<T> clazz){
		return json2oject(JSONObject.fromObject(json), clazz);
	}
	public static String map2xml(Map<String,?> map, boolean border, boolean order){
		StringBuffer builder = new StringBuffer();
		if(border){
			builder.append("<xml>");
		}
		if(order){
			SortedMap<String, Object> sort = new TreeMap<String, Object>(map);
			Set es = sort.entrySet();
			Iterator it = es.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String k = (String) entry.getKey();
				String v = entry.getValue()+"";
				if("null".equals(v)){
					v = "";
				}
				builder.append("<" + k + ">" + v + "</" + k + ">");
			}
		}else{
			Set es = map.entrySet();
			Iterator it = es.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				String value = entry.getValue()+"";
				if("null".equals(value)){
					value = "";
				}
				builder.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
			}
		}
		if(border){
			builder.append("</xml>");
		}
		return builder.toString();
	}
	public static String map2xml(Map<String,?> map){
		return map2xml(map, true, false);
	}
	public static String map2json(Map<String,?> map){
		JsonConfig config = new JsonConfig();
		config.registerJsonValueProcessor(Date.class, new JSONDateFormatProcessor());  
		config.registerJsonValueProcessor(Timestamp.class, new JSONDateFormatProcessor());
		JSONObject json = JSONObject.fromObject(map, config);
		return json.toString();
	}
	public static Map<String,Object> xml2map(String xml){
		Map<String,Object> map = new HashMap<String,Object>();
		Document document;
		try {
			document =  DocumentHelper.parseText(xml); 
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator(); itrProperty.hasNext();){
				Element element = itrProperty.next();
				String key = element.getName();
				String value = element.getTextTrim();
				map.put(key, value);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return map;
	}
	public static <T> T xml2object(String xml, Class<T> clazz, boolean recursion){
		T obj = null;
		try {
			Map<String,?> map = xml2map(xml);
			obj = map2object(map, clazz, recursion);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	public static <T> T xml2object(String xml, Class<T> clazz){
		return xml2object(xml,  clazz, true);
	}
	public static String object2xml(Object obj){
		if(null == obj){
			return null;
		}
		StringBuffer builder = new StringBuffer();
		builder.append("<xml>");
		List<Field> fields = BeanUtil.getFields(obj.getClass());
		for(Field field:fields){
			Object value = BeanUtil.getFieldValue(obj, field);
			if(null == value){
				value = "";
			}
			builder.append("<").append(field.getName()).append(">")
			.append(value)
			.append("</").append(field.getName()).append(">");
		}
		builder.append("</xml>");
		return builder.toString();
	}
	public static Map<String,Object> object2map(Object obj){
		if(null == obj){
			return null;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		List<Field> fields = BeanUtil.getFields(obj.getClass());
		for(Field field:fields){
			Object value = BeanUtil.getFieldValue(obj, field);
			if(null == value){
				value = "";
			}
			map.put(field.getName(), value);
		}
		return map;
	}
	public static String object2json(Object obj){
		JsonConfig config = new JsonConfig();
		config.registerJsonValueProcessor(Date.class, new JSONDateFormatProcessor());  
		config.registerJsonValueProcessor(Timestamp.class, new JSONDateFormatProcessor());
		JSONObject json = JSONObject.fromObject(obj,config);
		return json.toString();
	}

	/**
	 * 提取集合中每个条目的key属性的值
	 * @param list
	 * @param key
	 * @return
	 */
	public static List<Object> extract(Collection<?> list, String key){
		List<Object> values = new ArrayList<Object>();
		if(null != list){
			for(Object obj:list){
				Object value = BeanUtil.getFieldValue(obj, key);
				values.add(value);
			}
		}
		return values;
	}
	public static List<Map<String,Object>> extracts(Collection<?> list, String ... keys){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if(null != list){
			for(Object obj:list){
				Map<String,Object> map = new HashMap<String,Object>();
				if(null !=keys){
					for(String key:keys){
						Object value = BeanUtil.getFieldValue(obj, key);
						map.put(key, value);
					}
					result.add(map);
				}
			}
		}
		return result;
	}
	/**
	 * 去重
	 * @param list
	 * @param keys 根据keys列或属性值比较
	 * @return
	 */
	public static <T> Collection<T> distinct(Collection<T> list, String ... keys){
		List<T> result = new ArrayList<T>();
		if(null != list){
			for(T obj:list){
				if(null == keys || keys.length==0){
					if(!result.contains(obj)){
						result.add(obj);
					}
				}else{
					if(contain(result, obj, keys)){
						result.add(obj);
					}
				}
			}
		}
		return result;
	}
	/**
	 * 是否包含
	 * @param list
	 * @param obj
	 * @param keys 只比较keys列,基础类型不需要指定列
	 * @return
	 */
	public static <T> boolean contain(Collection<T> list, T obj, String ... keys){
		for(T item:list){
			if(equals(item, obj)){
				return true;
			}
		}
		return false;
	}
	public static <T> boolean equals(T obj1, T obj2, String ... keys){
		if(null == keys || keys.length == 0){
			if(null == obj1){
				if(null == obj2){
					return true;
				}else{
					return false;
				}
			}else if(!BasicUtil.isWrapClass(obj1)){
				if(null == obj2){
					return false;
				}else{
					if(obj1.toString().equals(obj2.toString())){
						return true;
					}
				}
			}
			return false;
		}

		for(String key:keys){
			Object v1 = BeanUtil.getFieldValue(obj1, key);
			Object v2 = BeanUtil.getFieldValue(obj2, key);
			if(!equals(v1,v2)){
				return false;
			}
			
		}
		return true;
	}


	/**
	 * 数组拼接成字符串
	 * 
	 * @param list
	 *            数组
	 * @param split
	 *            分隔符
	 * @return
	 */

	public static String join(List<?> list, String key, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Object obj = list.get(i);
				Object val = getFieldValue(obj, key);
				builder.append(val);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static String join(List<?> list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				builder.append(list.get(i));
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static String join(List<?> list) {
		return join(list,",");
	}

	
	

	public static <T> String join(T[] list, String key, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.length;
			for (int i = 0; i < size; i++) {
				Object obj = list[i];
				Object val = getFieldValue(obj, key);
				builder.append(val);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static <T> String join(T[] list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.length;
			for (int i = 0; i < size; i++) {
				builder.append(list[i]);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static <T> String join(T[] list) {
		return join(list,",");
	}


	public static String join(int[] list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.length;
			for (int i = 0; i < size; i++) {
				builder.append(list[i]);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static String join(int[] list) {
		return join(list,",");
	}

	public static String join(long[] list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.length;
			for (int i = 0; i < size; i++) {
				builder.append(list[i]);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static String join(long[] list) {
		return join(list,",");
	}

	public static String join(double[] list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.length;
			for (int i = 0; i < size; i++) {
				builder.append(list[i]);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static String join(double[] list) {
		return join(list,",");
	}

	public static String join(float[] list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.length;
			for (int i = 0; i < size; i++) {
				builder.append(list[i]);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static String join(float[] list) {
		return join(list,",");
	}

	public static String join(short[] list, String split) {
		StringBuilder builder = new StringBuilder();
		if (null != list) {
			int size = list.length;
			for (int i = 0; i < size; i++) {
				builder.append(list[i]);
				if (i < size - 1) {
					builder.append(split);
				}
			}
		}
		return builder.toString();
	}
	public static String join(short[] list) {
		return join(list,",");
	}
	
	public static Object toUpperCaseKey(Object obj, String ... keys){
		if(null == obj){
			return null;
		}
		if (obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Date) {
			return obj;
		}
		if(obj instanceof Map){
			obj = toUpperCaseKey((Map<String,Object>)obj, keys);
		}else if(obj instanceof Collection){
			obj = toUpperCaseKey((Collection)obj, keys);
		}
		return obj;
	}
	public static Collection toUpperCaseKey(Collection con, String ... keys){
		if(null == con){
			return con;
		}
		for(Object obj :con){
			obj = toUpperCaseKey(obj, keys);
		}
		return con;
	}
	public static Map<String,Object> toUpperCaseKey(Map<String,Object> map, String ... keys){
		if(null == map){
			return map;
		}
		List<String> ks = getMapKeys(map);
		for(String k:ks){
			if(null == keys || keys.length == 0 || BasicUtil.containsString(keys, k)){
				Object v = map.get(k);
				String key = k.toUpperCase();
				map.remove(k);
				map.put(key, v);
			}
		}
		return map;
	}
	/**
	 * String 转map
	 * @param str name:zhang,age:20
	 * @return
	 */
	public static Map<String,String> string2map(String str){
		Map<String,String> map = new HashMap<String,String>();
		if(BasicUtil.isNotEmpty(str)){
			if(str.startsWith("{") && str.endsWith("}")){
				str = str.substring(1, str.length()-1);
			}
			String[] list = str.split(",");
			for(String item:list){
				String[] kv = item.split(":");
				if(kv.length ==2){
					map.put(kv[0], kv[1]);
				}
			}
		}
		return map;
	} 
	public static Map<String, String> createMap(String... params) {
		Map<String, String> result = new HashMap<String, String>();
		if (null != params) {
			int size = params.length;
			for (int i = 0; i < size - 1; i += 2) {
				String key = params[i];
				String value = params[i + 1];
				if (null == value) {
					value = "";
				}
				result.put(key.toString(), value);
			}
		}
		return result;
	}
	/**
	 * 删除空值 
	 * @param map
	 */
	public static void clearEmpty(Map<String, Object> map){
		if(null == map){
			return;
		}
		List<String> keys = BasicUtil.getMapKeys(map);
		for(String key:keys){
			Object value = map.get(key);
			if(BasicUtil.isEmpty(value)){
				map.remove(key);
			}
		}
	}
	/**
	 * 多个数组合并成一个数组(二维数组合成一维数组)
	 * @param <T>
	 * @param first
	 * @param rest
	 * @return
	 */
	public static <T> T[] union(T[] first, T[]... rest) {
		int len = first.length;
		for (T[] array : rest) {
			len += array.length;
		}
		T[] result = Arrays.copyOf(first, len);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}


	/**
	 * 差值最小的成员下标
	 * @param array
	 * @param value
	 * @return
	 */
	public static int closest(short[] array, short value){
		int index = 0;
		int dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			int abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Short[] array, short value){
		int index = 0;
		int dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			int abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(List<Short> array, short value){
		int index = 0;
		int dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++){
			if(array.get(i)==value){
				index = i;
				break;
			}
			int abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(int[] array, int value){
		int index = 0;
		int dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			int abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Integer[] array, int value){
		int index = 0;
		int dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			int abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(List<Integer> array, int value){
		int index = 0;
		int dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++){
			if(array.get(i)==value){
				index = i;
				break;
			}
			int abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(long[] array, long value){
		int index = 0;
		long dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			long abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Long[] array, long value){
		int index = 0;
		long dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			long abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(List<Long> array, long value){
		int index = 0;
		long dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++){
			if(array.get(i)==value){
				index = i;
				break;
			}
			long abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(float[] array, float value){
		int index = 0;
		float dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			float abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Float[] array, float value){
		int index = 0;
		float dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			float abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
			
		}
		return index;
	}
	public static int closest(List<Float> array, float value){
		int index = 0;
		float dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++){
			if(array.get(i)==value){
				index = i;
				break;
			}
			float abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(double[] array, double value){
		int index = 0;
		double dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			double abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(Double[] array, double value){
		int index = 0;
		double dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			if(array[i]==value){
				index = i;
				break;
			}
			double abs = Math.abs(array[i]-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
			
		}
		return index;
	}
	public static int closest(List<Double> array, double value){
		int index = 0;
		double dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++){
			if(array.get(i)==value){
				index = i;
				break;
			}
			double abs = Math.abs(array.get(i)-value);
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static int closest(BigDecimal[] array, BigDecimal value){
		int index = 0;
		double dif = -1;
		int len = array.length;
		for(int i=0; i<len; i++){
			double abs = Math.abs(array[i].subtract(value).doubleValue());
			if(abs==0){
				index = i;
				break;
			}
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}
	public static int closest(List<BigDecimal> array, BigDecimal value){
		int index = 0;
		double dif = -1;
		int len = array.size();
		for(int i=0; i<len; i++){
			double abs = Math.abs(array.get(i).subtract(value).doubleValue());
			if(abs==0){
				index = i;
				break;
			}
			if(dif == -1 || dif > abs){
				dif = abs;
				index = i;
			}
		}
		return index;
	}

	public static String parseFinalValue(Object obj, String key){
		if(null == obj){
			return key;
		}
		String value = key;
		if(BasicUtil.isNotEmpty(key)){
			if(key.contains("{")){
				try{
					List<String> ks =RegularUtil.fetch(key, "\\{\\w+\\}",Regular.MATCH_MODE.CONTAIN,0);
					for(String k:ks){
						Object v = BeanUtil.getFieldValue(obj,k.replace("{", "").replace("}", ""));
						if(null == v){
							v = "";
						}
						value = value.replace(k, v.toString());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			} else {
				value = BeanUtil.getFieldValue(obj, key) + "";
			}
		}
		return value;
	}
	/**
	 * 集合截取
	 * @param <T>
	 * @param list
	 * @param begin
	 * @param end
	 * @return
	 */
	public static <T> List<T> cuts(Collection<T> list, int begin, int end){
		List<T> result = new ArrayList<T>();
		if(null != list){
			if(begin <=0){
				begin = 0;
			}
			if(end < 0 || end >= list.size()){
				end = list.size()-1;
			}
		}
		int idx = 0;
		for(T obj:list){
			if(idx >= begin && idx <= end){
				result.add(obj);
			}
			idx ++;
		}
		return result;
	}

	
	/**
	 * 解析 key:vlue形式参数age:20
	 * 返回数组["age","20"]
	 * 如果值为空返回["age",""]
	 * 如果没有分隔符返回["age","age"]
	 * @param src
	 * @return
	 */
	public static String[] parseKeyValue(String src){
		if(BasicUtil.isEmpty(src)){
			return null;
		}
		int len = 2;
		String[] result = null;
		String key1 = src;
		String key2 = src;
		if(src.contains(":")){
			String tmp[] = src.split(":");
			len = NumberUtil.max(len, tmp.length);
			result = new String[len];
			key1 = tmp[0];
			if(tmp.length>1){
				key2 = tmp[1];
			}else{
				key2 = "";
			}
			for(int i=2; i<len; i++){
				result[i] = tmp[i];
			}
		}else{
			result = new String[2];
		}
		result[0] = key1;
		result[1] = key2;
		return result;
	}
}
