/*  
 * Copyright 2006-2022 www.anyline.org
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

import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class MapUtil {

	private static final Logger log = LoggerFactory.getLogger(MapUtil.class);

	/**
	 * 提取集合中每个条目的多个key属性的值
	 * 如提取用户列表中的所有用户ID,CODE
	 * @param list  list
	 * @param keys  keys
	 * @return return
	 */
	public static List<Map<String,Object>> extracts(Collection<Map<String,Object>> list, String ... keys){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if(null != list){
			for(Map<String,Object> obj:list){
				Map<String,Object> map = new HashMap<String,Object>();
				if(null !=keys){
					for(String key:keys){
						map.put(key, obj.get(key));
					}
					result.add(map);
				}
			}
		}
		return result;
	}
	/**
	 * 去重
	 * @param list list
	 * @param keys 根据keys列或属性值比较
	 * @return   return
	 */
	public static  Collection<Map<String,Object>> distinct(Collection<Map<String,Object>> list, String ... keys){
		List<Map<String,Object>> result = new ArrayList<>();
		if(null != list){
			for(Map<String,Object> obj:list){
				if(null == keys || keys.length==0){
					if(!result.contains(obj)){
						result.add(extract(obj, keys));
					}
				}else{
					if(contain(result, obj, keys)){
						result.add(extract(obj, keys));
					}
				}
			}
		}
		return result;
	}
	public static Collection<Object> distinctValue(Collection<Map<String,Object>> list, String  key){
		List<Object> result = new ArrayList<>();
		if(null != list){
			for(Map<String,Object> obj:list){
				Object value = obj.get(key);
				if(!result.contains(value)){
					result.add(value);
				}
			}
		}
		return result;
	}
	public static  Collection<Map<String,Object>> distinct(Collection<Map<String,Object>> list, List<String> keys){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		if(null != list){
			for(Map<String,Object> obj:list){
				if(null == keys || keys.size()==0){
					if(!result.contains(obj)){
						result.add(extract(obj, keys));
					}
				}else{
					if(!contain(result, obj, keys)){
						result.add(extract(obj, keys));
					}
				}
			}
		}
		return result;
	}
	public static Map<String,Object> extract(Map<String,Object> src, List<String> keys){
		Map<String, Object> map = new HashMap<>();
		for(String key:keys){
			map.put(key, src.get(key));
		}
		return map;
	}
	public static Map<String,Object> extract(Map<String,Object> src, String ... keys){
		Map<String, Object> map = new HashMap<>();
		for(String key:keys){
			map.put(key, src.get(key));
		}
		return map;
	}
	public static  boolean contain(Collection<Map<String,Object>> list, Map<String,Object> obj, String ... keys){
		for(Map<String,Object> item:list){
			if(equals(item, obj)){
				return true;
			}
		}
		return false;
	}
	public static boolean contain(Collection<Map<String,Object>> list, Map<String,Object> obj, List<String> keys){
		for(Map<String,Object> item:list){
			if(equals(item, obj, keys)){
				return true;
			}
		}
		return false;
	}

	public static boolean equals(Map<String,Object> obj1, Map<String,Object> obj2, List<String> keys){

		for(String key:keys){
			Object v1 = obj1.get(key);
			Object v2 = obj2.get(key);
			if(null == v1 || !v1.equals(v2)){
				return false;
			}

		}
		return true;
	}

	public static boolean equals(Map<String,Object> obj1, Map<String,Object> obj2, String ... keys){
		return equals(obj1, obj2, Arrays.asList(keys));
	}


	public static String parseFinalValue(Map<String,Object> obj, String key){
		if(null == obj){
			return key;
		}
		String value = key;
		if(BasicUtil.isNotEmpty(key)){
			if(key.contains("{")){
				try{
					List<String> ks =RegularUtil.fetch(key, "\\{\\w+\\}",Regular.MATCH_MODE.CONTAIN,0);
					for(String k:ks){
						Object v =  obj.get(k.replace("{", "").replace("}", ""));
						if(null == v){
							v = "";
						}
						value = value.replace(k, v.toString());
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			} else {
				value = obj.get(key) + "";
			}
		}
		return value;
	}

	public static Map<String,Object> copy(Map<String,Object> src, Map<String,Object> copy, List<String> keys){
		if(null == copy ){
			return  src;
		}
		if(null != keys) {
			for (String key : keys) {
				String ks[] = BeanUtil.parseKeyValue(key);
				src.put(ks[0], copy.get(ks[1]));
			}
		}
		return src;
	}
	public static Map<String,Object> copy(Map<String,Object> src, Map<String,Object> copy, String ... keys){
		if(null == copy ){
			return  src;
		}
		if(null != keys) {
			for (String key : keys) {
				String ks[] = BeanUtil.parseKeyValue(key);
				src.put(ks[0], copy.get(ks[1]));
			}
		}
		return src;
	}
	public static Map<String,Object> copy(Map<String,Object> src, Map<String,Object> copy){
		return copy(src, copy, BeanUtil.getMapKeys(copy));
	}
	public static Map<String,Object> query(Collection<Map<String,Object>> datas, Map<String,Object> kvs){
		List<Map<String,Object>> list = querys(datas,0,1, kvs);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}

	public static List<Map<String,Object>> querys(Collection<Map<String,Object>> datas, int begin, String... params) {
		return querys(datas,begin, 0, params);
	}

	public static List<Map<String,Object>> querys(Collection<Map<String,Object>> datas, String... params) {
		return querys(datas,0, params);
	}

	public static List<Map<String,Object>> querys(Collection<Map<String,Object>> datas, int begin, int qty, String... params) {
		Map<String, Object> kvs = new HashMap<>();
		int len = params.length;
		int i = 0;
		String srcFlagTag = "srcFlag"; //参数含有{}的 在kvs中根据key值+tag 放入一个新的键值对
		while (i < len) {
			String p1 = params[i];
			if (BasicUtil.isEmpty(p1)) {
				i++;
				continue;
			} else if (p1.contains(":")) {
				String ks[] = BeanUtil.parseKeyValue(p1);
				kvs.put(ks[0], ks[1]);
				i++;
				continue;
			} else {
				if (i + 1 < len) {
					String p2 = params[i + 1];
					if (BasicUtil.isEmpty(p2) || !p2.contains(":")) {
						kvs.put(p1, p2);
						i += 2;
						continue;
					} else if (p2.startsWith("{") && p2.endsWith("}")) {
						p2 = p2.substring(1, p2.length() - 1);
						kvs.put(p1, p2);
						kvs.put(p1 + srcFlagTag, "true");
						i += 2;
						continue;
					} else {
						String ks[] = BeanUtil.parseKeyValue(p2);
						kvs.put(ks[0], ks[1]);
						i += 2;
						continue;
					}
				}

			}
			i++;
		}
		return querys(datas, begin, qty, kvs);
	}


	public static  List<Map<String,Object>> querys(Collection<Map<String,Object>> datas, int begin, int qty, Map<String, Object> kvs) {
		List<Map<String,Object>> set = new ArrayList<>();
		for (Map<String,Object> row:datas) {
			if(row.containsKey("_tmp_skip")){
				continue;
			}
			boolean chk = true;//对比结果
			for (String k : kvs.keySet()) {
				Object v = kvs.get(k);
				Object value = row.get(k);
				if (null == v) {
					if (null != value) {
						chk = false;
						break;
					}else{
						chk = true;
						break;
					}
				} else {
					if (null == value) {
						chk = false;
						break;
					}
					String str = value + "";
					str = str.toLowerCase();
					v = v.toString().toLowerCase();
					if (!v.equals(str)) {
						chk = false;
						break;
					}
				}
			}//end for kvs
			if (chk) {
				set.add(row);
				if (qty > 0 && set.size() >= qty) {
					break;
				}
			}
		}//end for rows
		return set;
	}
	private static String concatValue(Map<String,Object> row, String split){
		StringBuilder builder = new StringBuilder();
		List<String> keys = BeanUtil.getMapKeys(row);
		for(String key:keys){
			if(builder.length() > 0){
				builder.append(split);
			}
			builder.append(row.get(key));
		}
		return builder.toString();
	}
	/**
	 * 行转列
	 * 表结构(编号, 姓名, 年度, 科目, 分数, 等级)
	 * @param datas      数据集
	 * @param pks       唯一标识key(如编号,姓名)
	 * @param classKeys 分类key(如年度,科目)
	 * @param valueKeys 取值key(如分数,等级),如果不指定key则将整行作为value
	 * @return
	 * 如果指定key
	 * 返回结构 [
	 *      {编号:01,姓名:张三,2010-数学-分数:100},
	 *      {编号:01,姓名:张三,2010-数学-等级:A},
	 *      {编号:01,姓名:张三,2010-物理-分数:100}
	 *  ]
	 *  如果只有一个valueKey则返回[
	 *      {编号:01,姓名:张三,2010-数学:100},
	 *      {编号:01,姓名:张三,2010-物理:90}
	 *  ]
	 * 不指定valuekey则返回 [
	 *      {编号:01,姓名:张三,2010-数学:{分数:100,等级:A}},
	 *      {编号:01,姓名:张三,2010-物理:{分数:100,等级:A}}
	 *  ]
	 */
	public static  Collection<Map<String,Object>> pivot(Collection<Map<String,Object>> datas, List<String> pks, List<String> classKeys, List<String> valueKeys) {
		Collection<Map<String,Object>> result = distinct(datas,pks);
		Collection<Map<String,Object>> classValues =  distinct(datas,classKeys);  //[{年度:2010,科目:数学},{年度:2010,科目:物理},{年度:2011,科目:数学}]
		for (Map<String,Object> row : result) {
			for (Map<String,Object> classValue : classValues) {
				Map<String,Object> params = new HashMap<>();
				copy(params, row, pks);
				copy(params, classValue);
				Map<String,Object> valueRow = query(datas,params);
				if(null != valueRow){
					valueRow.put("_tmp_skip", "1");
				}
				String finalKey = concatValue(classValue,"-");//2010-数学
				if(null != valueKeys && valueKeys.size() > 0){
					if(valueKeys.size() == 1){
						if (null != valueRow) {
							row.put(finalKey,  valueRow.get(valueKeys.get(0)));
						} else {
							row.put(finalKey, null);
						}
					}else {
						for (String valueKey : valueKeys) {
							//{2010-数学-分数:100;2010-数学-等级:A}
							if (null != valueRow) {
								row.put(finalKey + "-" + valueKey, valueRow.get(valueKey));
							} else {
								row.put(finalKey + "-" + valueKey, null);
							}
						}
					}
				}else{
					if (null != valueRow){
						row.put(finalKey, valueRow);
					}else{
						row.put(finalKey, null);
					}
				}
			}
		}
		for(Map<String,Object> data:datas){
			data.remove("_tmp_skip");
		}
		return result;
	}

	public static Collection<Map<String,Object>> pivot(Collection<Map<String,Object>> datas, String[] pks, String[] classKeys, String[] valueKeys) {
		return pivot(datas, Arrays.asList(pks),Arrays.asList(classKeys),Arrays.asList(valueKeys));
	}
	/**
	 * 行转列
	 * @param datas    数据
	 * @param pk       唯一标识key(如姓名)多个key以,分隔如(编号,姓名)
	 * @param classKey 分类key(如科目)多个key以,分隔如(科目,年度)
	 * @param valueKey 取值key(如分数)多个key以,分隔如(分数,等级)
	 * @return
	 *  表结构(姓名,科目,分数)
	 *  返回结构 [{姓名:张三,数学:100,物理:90,英语:80},{姓名:李四,数学:100,物理:90,英语:80}]
	 */
	public static  Collection<Map<String,Object>> pivot(Collection<Map<String,Object>> datas, String pk, String classKey, String valueKey) {
		List<String> pks = new ArrayList<>(Arrays.asList(pk.trim().split(",")));
		List<String> classKeys = new ArrayList<>(Arrays.asList(classKey.trim().split(",")));
		List<String> valueKeys = new ArrayList<>(Arrays.asList(valueKey.trim().split(",")));
		return pivot(datas, pks, classKeys, valueKeys);
	}
	public static Collection<Map<String,Object>> pivot(Collection<Map<String,Object>> datas, String pk, String classKey) {
		List<String> pks = new ArrayList<>(Arrays.asList(pk.trim().split(",")));
		List<String> classKeys = new ArrayList<>(Arrays.asList(classKey.trim().split(",")));
		List<String> valueKeys = new ArrayList<>();
		return pivot(datas, pks, classKeys, valueKeys);
	}

	public static Collection<Map<String,Object>> pivot(Collection<Map<String,Object>> datas, List<String> pks, List<String> classKeys, String ... valueKeys) {
		List<String> list = new ArrayList<>();
		if(null != valueKeys){
			for(String item:valueKeys){
				list.add(item);
			}
		}
		return pivot(datas, pks, classKeys, valueKeys);
	}
} 
