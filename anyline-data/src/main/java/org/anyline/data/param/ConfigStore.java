/*
 * Copyright 2006-2023 www.anyline.org
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


package org.anyline.data.param;

import org.anyline.data.handler.StreamHandler;
import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.entity.Compare;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.util.BeanUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/** 
 * 查询参数 
 * @author zh 
 * 
 */
public interface ConfigStore {
	ConfigStore stream(StreamHandler hanlder);
	StreamHandler stream();

	ConfigStore entityClass(Class clazz);
	Class entityClass();
	/**
	 * 解析查询配置参数 
	 * @param config "COMPANY_CD:company","NM:nmEn% | NM:nmCn%","STATUS_VALUE:[status]" 
	 * @return Config
	 */ 
	Config parseConfig(String config); 
	ConfigStore setPageNavi(PageNavi navi);
	ConfigStore copyPageNavi(PageNavi navi);

	/**
	 * 起止行 下标从0开始
	 * @param first 起
	 * @param last 止
	 * @return ConfigStore
	 */
	ConfigStore scope(long first, long last);
	/**
	 * 起止行 下标从0开始
	 * @param offset offset：指定第一个返回记录行的偏移量（即从哪一行开始返回） 初始行的偏移量为0
	 * @param rows 返回具体行数
	 * @return ConfigStore
	 */
	ConfigStore limit(long offset, int rows);

	/**
	 * 设置分页
	 * @param page 第page页 下标从1开始
	 * @param rows 每页rows行
	 * @return ConfigStore
	 */
	ConfigStore page(long page, int rows);

	/**
	 * 在配置了参数(调用and/or)之后，为参数赋值(值经常是来自WebUti.value(request))
	 * @param values 值
	 * @return ConfigStore
	 */
	ConfigStore setValue(Map<String,Object> values); 
	ConfigChain getConfigChain();
	Config getConfig(String key);
	ConfigStore removeConfig(String var);
	ConfigStore removeConfig(Config config);
	List<Object> getConfigValues(String var);
	Object getConfigValue(String var);
	Config getConfig(String key, Compare compare);
	ConfigStore removeConfig(String var, Compare compare);
	List<Object> getConfigValues(String var, Compare compare);
	Object getConfigValue(String var, Compare compare);

	/**
	 * 构造IN查询条件,如果只提供一个值与and一样
	 * @param swt 遇到空值处理方式
	 * @param compare 默认IN,可以换成FIND_IN_SET
	 * @param col 列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用 这与swt作用一样,不要与swt混用
	 * @param values 值 可以是集合
	 * @return ConfigStore
	 */

	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String col, Object ... values){
		return and(swt, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore ands(Compare compare, String col, Object ... values){
		return and(EMPTY_VALUE_SWITCH.NONE, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore ands(String var, Object ... values){
		return ands(EMPTY_VALUE_SWITCH.NONE, var, values);
	}
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String var, Object ... values){
		return and(swt, Compare.IN, var, BeanUtil.array2list(values));
	}

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合 如果是集合生成IN条件
	 * @return ConfigStore
	 */
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String var, Object value){
		return and(swt, var, value, false, false);
	}
	default ConfigStore and(String var, Object value){
		return and(EMPTY_VALUE_SWITCH.NONE, var, value);
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,如果不覆盖则与原来的值合成新的集合
	 * @return ConfigStore
	 */
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore and(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(EMPTY_VALUE_SWITCH.NONE, id, var, value, overCondition, overValue);
	}

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,如果不覆盖则与原来的值合成新的集合
	 * @return ConfigStore
	 */
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, (String)null, var, value, overCondition, overValue);
	}
	default ConfigStore and(String var, Object value, boolean overCondition, boolean overValue){
		return and(EMPTY_VALUE_SWITCH.NONE, var, value, overCondition, overValue);
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param text 可以是一条原生的SQL查询条件
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore and(String text){
		return and(EMPTY_VALUE_SWITCH.NONE, text);
	}

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @return ConfigStore
	 */
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value){
		return and(swt, compare, var, value, false, false);
	}
	default ConfigStore and(Compare compare, String var, Object value){
		return and(EMPTY_VALUE_SWITCH.NONE, compare, var, value);
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @return ConfigStore
	 */
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value){
		return and(swt, compare, id, var, value, false, false);
	}
	default ConfigStore and(Compare compare, String id, String var, Object value){
		return and(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value);
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore and(Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		return and(EMPTY_VALUE_SWITCH.NONE, compare, var, value, overCondition, overValue);
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue);
	default ConfigStore and(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value, overCondition, overValue);
	}

	/**
	 * 构造查询条件
	 * XML自定义SQL条件中指定变量赋值<br/>
	 * 这里不指定运算算，根据value情况生成IN或者=
	 * @param swt 遇到空值处理方式
	 * @param id condition.id或表名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @return ConfigStore
	 */
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String id, String var, Object value){
		return and(swt, id, var, value, false, false);
	}
	default ConfigStore and(String id, String var, Object value){
		return and(EMPTY_VALUE_SWITCH.NONE, id, var, value);
	}
	/**
	 * 构造查询条件
	 * @param config 查询条件
	 * @return ConfigStore
	 */
	ConfigStore and(Config config);
	ConfigStore and(ConfigStore config);

	default ConfigStore eq(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, id, var, value, overCondition, overValue);
	}
	default ConfigStore eq(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, var, value, overCondition, overValue);
	}
	default ConfigStore eq(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(id, var, value, overCondition, overValue);
	}
	default ConfigStore eq(String var, Object value, boolean overCondition, boolean overValue){
		return and(var, value, overCondition, overValue);
	}
	default ConfigStore eq(String var, Object value){
		return and(var, value);
	}


	default ConfigStore gt(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.GREAT, id, var, value, overCondition, overValue);
	}
	default ConfigStore gt(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.GREAT, var, value, overCondition, overValue);
	}
	default ConfigStore gt(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.GREAT, id, var, value, overCondition, overValue);
	}
	default ConfigStore gt(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.GREAT, var, value, overCondition, overValue);
	}
	default ConfigStore gt(String var, Object value){
		return and(Compare.GREAT, var, value);
	}

	default ConfigStore ge(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.GREAT_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore ge(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.GREAT_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore ge(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.GREAT_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore ge(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.GREAT_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore ge(String var, Object value){
		return and(Compare.GREAT_EQUAL, var, value);
	}
	default ConfigStore lt(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.LESS, id, var, value, overCondition, overValue);
	}
	default ConfigStore lt(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.LESS, var, value, overCondition, overValue);
	}
	default ConfigStore lt(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.LESS, id, var, value, overCondition, overValue);
	}
	default ConfigStore lt(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.LESS, var, value, overCondition, overValue);
	}
	default ConfigStore lt(String var, Object value){
		return and(Compare.LESS, var, value);
	}

	default ConfigStore le(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.LESS_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore le(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.LESS_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore le(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.LESS_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore le(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.LESS_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore le(String var, Object value){
		return and(Compare.LESS_EQUAL, var, value);
	}
	default ConfigStore in(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.IN, id, var, value, overCondition, overValue);
	}
	default ConfigStore in(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.IN, var, value, overCondition, overValue);
	}
	default ConfigStore in(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.IN, id, var, value, overCondition, overValue);
	}
	default ConfigStore in(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.IN, var, value, overCondition, overValue);
	}
	default ConfigStore in(String var, Object value){
		return and(Compare.IN, var, value);
	}


	default ConfigStore like(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.LIKE, id, var, value, overCondition, overValue);
	}
	default ConfigStore like(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.LIKE, var, value, overCondition, overValue);
	}
	default ConfigStore like(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.LIKE, id, var, value, overCondition, overValue);
	}
	default ConfigStore like(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.LIKE, var, value, overCondition, overValue);
	}
	default ConfigStore like(String var, Object value){
		return and(Compare.LIKE, var, value);
	}


	default ConfigStore likePrefix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.START_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.START_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefix(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.START_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefix(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.START_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefix(String var, Object value){
		return and(Compare.START_WITH, var, value);
	}

	default ConfigStore startWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.START_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore startWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.START_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore startWith(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.START_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore startWith(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.START_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore startWith(String var, Object value){
		return and(Compare.START_WITH, var, value);
	}

	default ConfigStore likeSuffix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.END_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.END_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffix(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.END_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffix(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.END_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffix(String var, Object value){
		return and(Compare.END_WITH, var, value);
	}

	default ConfigStore endWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.END_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore endWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.END_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore endWith(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.END_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore endWith(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.END_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore endWith(String var, Object value){
		return and(Compare.END_WITH, var, value);
	}

	default ConfigStore findInSet(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.FIND_IN_SET, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSet(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.FIND_IN_SET, var, value, overCondition, overValue);
	}
	default ConfigStore findInSet(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.FIND_IN_SET, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSet(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.FIND_IN_SET, var, value, overCondition, overValue);
	}
	default ConfigStore findInSet(String var, Object value){
		return and(Compare.FIND_IN_SET, var, value);
	}

	default ConfigStore findInSetOr(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.FIND_IN_SET_OR, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetOr(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.FIND_IN_SET_OR, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetOr(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.FIND_IN_SET_OR, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetOr(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.FIND_IN_SET_OR, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetOr(String var, Object value){
		return and(Compare.FIND_IN_SET_OR, var, value);
	}

	default ConfigStore findInSetAnd(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.FIND_IN_SET_AND, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetAnd(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.FIND_IN_SET_AND, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetAnd(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.FIND_IN_SET_AND, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetAnd(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.FIND_IN_SET_AND, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetAnd(String var, Object value){
		return and(Compare.FIND_IN_SET_AND, var, value);
	}


	default ConfigStore between(EMPTY_VALUE_SWITCH swt, String id, String var, Object min, Object max, boolean overCondition, boolean overValue){
		return and(swt, Compare.BETWEEN, id, var, Arrays.asList(min,max), overCondition, overValue);
	}
	default ConfigStore between(EMPTY_VALUE_SWITCH swt, String var, Object min, Object max, boolean overCondition, boolean overValue){
		return and(swt, Compare.BETWEEN, var, Arrays.asList(min,max), overCondition, overValue);
	}
	default ConfigStore between(String id, String var, Object min, Object max, boolean overCondition, boolean overValue){
		return and(Compare.BETWEEN, id, var, Arrays.asList(min,max), overCondition, overValue);
	}
	default ConfigStore between(String var, Object min, Object max, boolean overCondition, boolean overValue){
		return and(Compare.BETWEEN, var, Arrays.asList(min,max), overCondition, overValue);
	}
	default ConfigStore between(String var, Object min, Object max){
		return and(Compare.BETWEEN, var, Arrays.asList(min,max));
	}


	default ConfigStore ne(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore ne(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore ne(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore ne(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore ne(String var, Object value){
		return and(Compare.NOT_EQUAL, var, value);
	}


	default ConfigStore notIn(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_IN, id, var, value, overCondition, overValue);
	}
	default ConfigStore notIn(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_IN, var, value, overCondition, overValue);
	}
	default ConfigStore notIn(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_IN, id, var, value, overCondition, overValue);
	}
	default ConfigStore notIn(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_IN, var, value, overCondition, overValue);
	}
	default ConfigStore notIn(String var, Object value){
		return and(Compare.NOT_IN, var, value);
	}

	default ConfigStore notLike(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLike(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE, var, value, overCondition, overValue);
	}
	default ConfigStore notLike(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLike(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE, var, value, overCondition, overValue);
	}
	default ConfigStore notLike(String var, Object value){
		return and(Compare.NOT_LIKE, var, value);
	}

	default ConfigStore notLikePrefix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE_PREFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLikePrefix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE_PREFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notLikePrefix(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE_PREFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLikePrefix(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE_PREFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notLikePrefix(String var, Object value){
		return and(Compare.NOT_LIKE_PREFIX, var, value);
	}

	default ConfigStore notStartWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE_PREFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notStartWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE_PREFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notStartWith(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE_PREFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notStartWith(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE_PREFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notStartWith(String var, Object value){
		return and(Compare.NOT_LIKE_PREFIX, var, value);
	}


	default ConfigStore notLikeSuffix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE_SUFFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLikeSuffix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE_SUFFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notLikeSuffix(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE_SUFFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLikeSuffix(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE_SUFFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notLikeSuffix(String var, Object value){
		return and(Compare.NOT_LIKE_SUFFIX, var, value);
	}

	default ConfigStore notEndWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE_SUFFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notEndWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return and(swt, Compare.NOT_LIKE_SUFFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notEndWith(String id, String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE_SUFFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notEndWith(String var, Object value, boolean overCondition, boolean overValue){
		return and(Compare.NOT_LIKE_SUFFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notEndWith(String var, Object value){
		return and(Compare.NOT_LIKE_SUFFIX, var, value);
	}


	/**
	 * 用来给占位符或自定义SQL中的参数赋值
	 * @param swt 遇到空值处理方式
	 * @param id 自定义查询条件ID或表名表别名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @return ConfigStore
	 */
	default ConfigStore param(EMPTY_VALUE_SWITCH swt, String id, String var, Object value){
		return and(swt, Compare.NONE,  id, var, value);
	}
	default ConfigStore param(String id, String var, Object value){
		return and(EMPTY_VALUE_SWITCH.NONE, Compare.NONE,  id, var, value);
	}
	/**
	 * 用来给占位符或自定义SQL中的参数赋值
	 * Compare.NONE 只作为参数值为占位符赋值,不能独立生成新的查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @return ConfigStore
	 */
	default ConfigStore param(EMPTY_VALUE_SWITCH swt, String var, Object value){
		return and(swt, Compare.NONE, var, value);
	}
	default ConfigStore param(String var, Object value){
		return and(EMPTY_VALUE_SWITCH.NONE,  Compare.NONE, var, value);
	}
	/**
	 * 与ConfigStore中前一个条件合成or
	 * @param swt 遇到空值处理方式
	 * @param compare 匹配方式
	 * @param id 自定义查询条件ID或表名表别名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue);
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		return or(swt, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, String id, String var, Object value){
		return or(swt, id, var, value, false, false);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return or(swt, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore or(String id, String var, Object value){
		return or(EMPTY_VALUE_SWITCH.NONE, id, var, value);
	}
	default ConfigStore or(String id, String var, Object value, boolean overCondition, boolean overValue){
		return or(EMPTY_VALUE_SWITCH.NONE, id, var, value, overCondition, overValue);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value){
		return or(swt, compare, id, var, value, false, false);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value){
		return or(swt, compare, null, var, value);
	}
	default ConfigStore or(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue){
		return or(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value, overCondition, overValue);
	}

	default ConfigStore or(Compare compare, String id, String var, Object value){
		return or(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value);
	}
	default ConfigStore or(Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		return or(EMPTY_VALUE_SWITCH.NONE, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore or(Compare compare, String var, Object value){
		return or(EMPTY_VALUE_SWITCH.NONE, compare, var, value);
	}

	/**
	 * 与ConfigStore中前一个条件合成or <br/>
	 * 这里不指定运算算，根据value情况生成IN或者=
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return or(swt, compare(value), null, var, value, overCondition, overValue);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, String var, Object value){
		return or(swt, compare(value), var, value);
	}
	default ConfigStore or(String var, Object value, boolean overCondition, boolean overValue){
		return or(EMPTY_VALUE_SWITCH.NONE, var, value, overCondition, overValue);
	}
	default ConfigStore or(String var, Object value){
		return or(EMPTY_VALUE_SWITCH.NONE, var, value);
	}
	ConfigStore or(ConfigStore config);
	ConfigStore or(Config config);
	ConfigStore or(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore or(String text){
		return or(EMPTY_VALUE_SWITCH.NONE, text);
	}

	ConfigStore ors(ConfigStore config);
	ConfigStore ors(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore ors(String text){
		return ors(EMPTY_VALUE_SWITCH.NONE, text);
	}
	ConfigStore ors(EMPTY_VALUE_SWITCH swt, Config config);
	default ConfigStore ors(Config config){
		return ors(EMPTY_VALUE_SWITCH.NONE, config);
	}
	/**
	 * 与ConfigStore中当前所有的条件合成or
	 * @param swt 遇到空值处理方式
	 * @param compare 匹配方式
	 * @param id 自定义查询条件ID或表名表别名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue);
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return ors(swt, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, String id, String var, Object value){
		return ors(swt, compare(value), id, var, value, false, false);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		return ors(swt, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value){
		return ors(swt, compare, id, var, value, false, false);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value){
		return ors(swt, compare, null, var, value);
	}
	default ConfigStore ors(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue){
		return ors(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value, overCondition, overValue);
	}
	default ConfigStore ors(String id, String var, Object value, boolean overCondition, boolean overValue){
		return ors(EMPTY_VALUE_SWITCH.NONE, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore ors(String id, String var, Object value){
		return ors(EMPTY_VALUE_SWITCH.NONE, compare(value), id, var, value);
	}
	default ConfigStore ors(Compare compare, String id, String var, Object value){
		return ors(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value);
	}
	default ConfigStore ors(Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		return ors(EMPTY_VALUE_SWITCH.NONE, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore ors(Compare compare, String var, Object value){
		return ors(EMPTY_VALUE_SWITCH.NONE, compare, var, value);
	}
	/**
	 * 与ConfigStore中当前所有的条件合成or<br/>
	 * 	 * 这里不指定运算算，根据value情况生成IN或者=
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return ors(swt, compare(value), null, var, value, overCondition, overValue);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, String var, Object value){
		return ors(swt, compare(value), var, value);
	}
	default ConfigStore ors(String var, Object value, boolean overCondition, boolean overValue){
		return ors(EMPTY_VALUE_SWITCH.NONE, var, value, overCondition, overValue);
	}
	default ConfigStore ors(String var, Object value){
		return ors(EMPTY_VALUE_SWITCH.NONE, var, value);
	}


	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String var, Object value){
		if("or".equalsIgnoreCase(join)){
			return or(swt, var, value);
		}else{
			return and(swt, var, value);
		}
	}
	default ConfigStore condition(String join, String var, Object value){
		if("or".equalsIgnoreCase(join)){
			return or(var, value);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(var, value);
		}else{
			return and(var, value);
		}
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,如果不覆盖则与原来的值合成新的集合
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		if("or".equalsIgnoreCase(join)){
			return or(swt, id, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(swt, id, var, value, overCondition, overValue);
		}else{
			return and(swt, id, var, value, overCondition, overValue);
		}
	}
	default ConfigStore condition(String join, String id, String var, Object value, boolean overCondition, boolean overValue){
		if("or".equalsIgnoreCase(join)){
			return or(id, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(id, var, value, overCondition, overValue);
		}else{
			return and(id, var, value, overCondition, overValue);
		}
	}

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,如果不覆盖则与原来的值合成新的集合
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		if("or".equalsIgnoreCase(join)){
			return or(swt, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(swt, var, value, overCondition, overValue);
		}else{
			return and(swt, var, value, overCondition, overValue);
		}
	}
	default ConfigStore condition(String join, String var, Object value, boolean overCondition, boolean overValue){
		if("or".equalsIgnoreCase(join)){
			return or(var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(var, value, overCondition, overValue);
		}else{
			return and(var, value, overCondition, overValue);
		}
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param text 可以是一条原生的SQL查询条件
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String text){
		if("or".equalsIgnoreCase(join)){
			return or(swt, text);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(swt, text);
		}else{
			return and(swt, text);
		}
	}
	default ConfigStore condition(String join, String text){
		if("or".equalsIgnoreCase(join)){
			return or(text);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(text);
		}else{
			return and(text);
		}
	}

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value){
		if("or".equalsIgnoreCase(join)){
			return or(swt, compare, var, value);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(swt, compare, var, value);
		}else{
			return and(swt, compare, var, value);
		}
	}
	default ConfigStore condition(String join, Compare compare, String var, Object value){
		if("or".equalsIgnoreCase(join)){
			return or(compare, var, value);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(compare, var, value);
		}else{
			return and(compare, var, value);
		}
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value){
		if("or".equalsIgnoreCase(join)){
			return or(swt, compare, id, var, value);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(swt, compare, id, var, value);
		}else{
			return and(swt, compare, id, var, value);
		}
	}
	default ConfigStore condition(String join, Compare compare, String id, String var, Object value){
		if("or".equalsIgnoreCase(join)){
			return or(compare, id, var, value);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(compare, id, var, value);
		}else{
			return and(compare, id, var, value);
		}
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		if("or".equalsIgnoreCase(join)){
			return or(swt, compare, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(swt, compare, var, value, overCondition, overValue);
		}else{
			return and(swt, compare, var, value, overCondition, overValue);
		}
	}
	default ConfigStore condition(String join, Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		if("or".equalsIgnoreCase(join)){
			return or(compare, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(compare, var, value, overCondition, overValue);
		}else{
			return and(compare, var, value, overCondition, overValue);
		}
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue){
		if("or".equalsIgnoreCase(join)){
			return or(swt, compare, id, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(swt, compare, id, var, value, overCondition, overValue);
		}else{
			return and(swt, compare, id, var, value, overCondition, overValue);
		}
	}
	default ConfigStore condition(String join, Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue){
		if("or".equalsIgnoreCase(join)){
			return or(compare, id, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(compare, id, var, value, overCondition, overValue);
		}else{
			return and(compare, id, var, value, overCondition, overValue);
		}
	}

	/**
	 * 构造查询条件
	 * XML自定义SQL条件中指定变量赋值<br/>
	 * 这里不指定运算算，根据value情况生成IN或者=
	 * @param swt 遇到空值处理方式
	 * @param id condition.id或表名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String id, String var, Object value){
		if("or".equalsIgnoreCase(join)){
			return or(swt, id, var, value);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(swt, id, var, value);
		}else{
			return and(swt, id, var, value);
		}
	}
	default ConfigStore condition(String join, String id, String var, Object value){
		if("or".equalsIgnoreCase(join)){
			return or(id, var, value);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(id, var, value);
		}else{
			return and(id, var, value);
		}
	}
	/**
	 * 构造查询条件
	 * @param config 查询条件
	 * @return default ConfigStore
	 */
	default ConfigStore condition(String join, Config config){
		if("or".equalsIgnoreCase(join)){
			return or(config);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(config);
		}else{
			return and(config);
		}
	}
	default ConfigStore condition(String join, ConfigStore config){
		if("or".equalsIgnoreCase(join)){
			return or(config);
		}else if("ors".equalsIgnoreCase(join)){
			return ors(config);
		}else{
			return and(config);
		}
	}
	default Compare compare(Object value){
		Compare compare = Compare.EQUAL;
		if(null != value){
			if(value instanceof Collection){
				Collection col = (Collection) value;
				if(col.size()>1){
					compare = Compare.IN;
				}
			}else if(value instanceof Object[]){
				Object[] array = (Object[])value;
				if(array.length > 1){
					compare = Compare.IN;
				}
			}
		}
		return compare;
	}
	/** 
	 * 添加排序 
	 * @param order order
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return ConfigStore
	 */
	ConfigStore order(Order order, boolean override);
	ConfigStore order(Order order);


	/**
	 * 添加排序
	 * @param column 列名
	 * @param type ASC|DESC
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return ConfigStore
	 */
	ConfigStore order(String column, Order.TYPE type, boolean override);
	/**
	 * 添加排序
	 * @param column 列名
	 * @param type ASC|DESC
	 * @return ConfigStore
	 */
	ConfigStore order(String column, Order.TYPE type);
	/**
	 * 添加排序
	 * @param column 列名
	 * @param type ASC|DESC
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return ConfigStore
	 */
	ConfigStore order(String column, String type, boolean override);
	/**
	 * 添加排序
	 * @param column 列名
	 * @param type ASC|DESC
	 * @return ConfigStore
	 */
	ConfigStore order(String column, String type);
	/**
	 * 添加排序
	 * @param order 列名+排序方式
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return ConfigStore
	 */
	ConfigStore order(String order, boolean override);
	/**
	 * @param order 列名或原生的SQL 如 ID 或 ID ASC 或 ORDER BY CONVERT(id USING gbk) COLLATE gbk_chinese_ci DESC
	 * @return ConfigStore
	 */
	ConfigStore order(String order);
	OrderStore getOrders() ;
	ConfigStore setOrders(OrderStore orders) ; 
	/** 
	 * 添加分组 
	 * @param column 列名
	 * @return ConfigStore
	 */ 
	ConfigStore group(Group column);

	/**
	 * 添加排序
	 * @param column 列名
	 * @return ConfigStore
	 */
	ConfigStore group(String column);
	GroupStore getGroups() ; 
	ConfigStore setGroups(GroupStore groups) ; 
	PageNavi getPageNavi();
	/**
	 * 提取部分查询条件
	 * @param keys keys
	 * @return ConfigStore
	 */
	ConfigStore fetch(String ... keys);
	
	String toString();
	/**
	 * 开启记录总数懒加载 
	 * @param ms 缓存有效期(毫秒)
	 * @return ConfigStore
	 */
	ConfigStore setTotalLazy(long ms);

	boolean isValid();

	/**
	 * 设置城要查询的列
	 * @param columns 需要查询的列
	 * @return ConfigStore
	 */
	ConfigStore columns(String ... columns);
	List<String> columns();



	/**
	 * 设置不城要查询的列
	 * @param columns 需要查询的列
	 * @return ConfigStore
	 */
	ConfigStore excludes(String ... columns);
	List<String> excludes();
} 
 
 
