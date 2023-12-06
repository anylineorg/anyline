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

import org.anyline.adapter.KeyAdapter;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.data.run.Run;
import org.anyline.entity.Compare;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.Column;
import org.anyline.metadata.Constraint;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.*;


/** 
 * 查询参数 
 * @author zh 
 * 
 */
public interface ConfigStore {

	boolean execute();
	ConfigStore execute(boolean execute);
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
	 * 执行过的命令
	 * @return List
	 */
	List<Run> runs();
	ConfigStore runs(List<Run> runs);
	ConfigStore add(Run run);
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
	default ConfigStore limit(int rows){
		return limit(0, rows);
	}

	/**
	 * 设置分页
	 * @param page 第page页 下标从1开始
	 * @param rows 每页rows行
	 * @return ConfigStore
	 */
	ConfigStore page(long page, int rows);
	/**
	 * 是否作为一个整体，不可分割，与其他条件合并时以()包围
	 * @return boolean
	 */
	boolean integrality();
	ConfigStore integrality(boolean integrality);
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

	Boolean override();
	List<String> overrideByColumns();
	Constraint overrideByConstraint();

	/**
	 * 如果数据库中存在相同数据(根据columns,如果不指定则根据主键或唯一索引) 是否覆盖 true或false会检测数据库null不检测
	 * @param columns 根据 columns列判断重复
	 * @param override boolean
	 * @return ConfigStore
	 */
	ConfigStore override(Boolean override, String ... columns);
	ConfigStore override(Boolean override, Constraint constraint);
	/**
	 * 添加主键
	 * @param pks pks
	 * @return DataRow
	 */
	default ConfigStore addPrimaryKey(String... pks) {
		return addPrimaryKey(BeanUtil.array2list(pks));
	}

	ConfigStore addPrimaryKey(Collection<String> pks);

	default ConfigStore setPrimaryKey(String... pks) {
		return setPrimaryKey(BeanUtil.array2list(pks));
	}


	/**
	 * 设置主键
	 * @param pks keys
	 * @return DataRow
	 */
	ConfigStore setPrimaryKey(Collection<String> pks) ;


	LinkedHashMap<String, Column> getPrimaryColumns();
	/**
	 * 读取主键
	 * 主键为空时且容器有主键时,读取容器主键,否则返回默认主键
	 * @return List
	 */
	List<String> getPrimaryKeys();

	String getPrimaryKey();

	/**
	 * 自身是否有主键
	 * @return boolean
	 */
	boolean hasSelfPrimaryKeys();

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
	 * @param prefix 表别名或XML中查询条件的ID或表名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue);
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


	default ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String col, Object ... values){
		return and(swt, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore and(Compare compare, String col, Object ... values){
		return and(EMPTY_VALUE_SWITCH.NONE, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore and(String var, Object ... values){
		return and(EMPTY_VALUE_SWITCH.NONE, var, values);
	}
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String var, Object ... values){
		return and(swt, Compare.IN, var, BeanUtil.array2list(values));
	}
	/**
	 * 构造查询条件
	 * @param config 查询条件
	 * @return ConfigStore
	 */
	ConfigStore and(Config config);

	/**
	 * 构造查询条件
	 * @param config ConfigStore
	 * @param apart 是否需要跟前面的条件保持隔离 <br/>
	 *              true:隔离,前面所有条件加到()，与configs合成一个新的list<br/>
	 *              false:不隔离,configs合并成原来的list中
	 * @return ConfigStore
	 */
	ConfigStore and(ConfigStore config, boolean apart);
	default ConfigStore and(ConfigStore config){
		return and(config, false);
	}

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合 如果是集合生成IN条件
	 * @return ConfigStore
	 */
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String var, Object value){
		return ands(swt, var, value, false, false);
	}
	default ConfigStore ands(String var, Object value){
		return ands(EMPTY_VALUE_SWITCH.NONE, var, value);
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue){
		return ands(swt, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore ands(String id, String var, Object value, boolean overCondition, boolean overValue){
		return ands(EMPTY_VALUE_SWITCH.NONE, id, var, value, overCondition, overValue);
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue){
		return ands(swt, (String)null, var, value, overCondition, overValue);
	}
	default ConfigStore ands(String var, Object value, boolean overCondition, boolean overValue){
		return ands(EMPTY_VALUE_SWITCH.NONE, var, value, overCondition, overValue);
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param text 可以是一条原生的SQL查询条件
	 * @return ConfigStore
	 */
	ConfigStore ands(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore ands(String text){
		return ands(EMPTY_VALUE_SWITCH.NONE, text);
	}

	ConfigStore ands(Config config);
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @return ConfigStore
	 */
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value){
		return ands(swt, compare, var, value, false, false);
	}
	default ConfigStore ands(Compare compare, String var, Object value){
		return ands(EMPTY_VALUE_SWITCH.NONE, compare, var, value);
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value){
		return ands(swt, compare, id, var, value, false, false);
	}
	default ConfigStore ands(Compare compare, String id, String var, Object value){
		return ands(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value);
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		return ands(swt, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore ands(Compare compare, String var, Object value, boolean overCondition, boolean overValue){
		return ands(EMPTY_VALUE_SWITCH.NONE, compare, var, value, overCondition, overValue);
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
	ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue);
	default ConfigStore ands(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue){
		return ands(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value, overCondition, overValue);
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String id, String var, Object value){
		return ands(swt, id, var, value, false, false);
	}
	default ConfigStore ands(String id, String var, Object value){
		return ands(EMPTY_VALUE_SWITCH.NONE, id, var, value);
	}

	default ConfigStore ands(ConfigStore config){
		return and(config, true);
	}

	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String col, Object ... values){
		return ands(swt, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore ands(Compare compare, String col, Object ... values){
		return ands(EMPTY_VALUE_SWITCH.NONE, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore ands(String var, Object ... values){
		return ands(EMPTY_VALUE_SWITCH.NONE, var, values);
	}
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String var, Object ... values){
		return ands(swt, Compare.IN, var, BeanUtil.array2list(values));
	}


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


	/**
	 * 构造查询条件
	 * @param config ConfigStore
	 * @param apart 是否需要跟前面的条件保持隔离 <br/>
	 *              true:隔离,前面所有条件加到()，与configs合成一个新的list<br/>
	 *              false:不隔离,configs合并成原来的list中
	 * @return ConfigStore
	 */
	ConfigStore or(ConfigStore config, boolean apart);
	default ConfigStore or(ConfigStore config){
		return or(config, true);
	}
	ConfigStore or(Config config);
	ConfigStore or(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore or(String text){
		return or(EMPTY_VALUE_SWITCH.NONE, text);
	}

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
	/**
	 * 构造查询条件
	 * @param join and或者or
	 * @param config ConfigStore
	 * @param apart 是否需要跟前面的条件保持隔离 <br/>
	 *              true:隔离,前面所有条件加到()，与configs合成一个新的list<br/>
	 *              false:不隔离,configs合并成原来的list中
	 * @return ConfigStore
	 */
	default ConfigStore condition(String join, ConfigStore config, boolean apart){
		if("or".equalsIgnoreCase(join)){
			return or(config, apart);
		}else{
			return and(config, apart);
		}
	}
	default ConfigStore condition(String join, ConfigStore config){
		return condition(join, config, true);
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
	ConfigStore columns(List<String> columns);
	List<String> columns();



	/**
	 * 设置不城要查询的列
	 * @param columns 需要查询的列
	 * @return ConfigStore
	 */
	ConfigStore excludes(String ... columns);
	ConfigStore excludes(List<String> columns);
	List<String> excludes();

	ConfigStore addStaticValue(Object value);
	List<Object> getStaticValues();
	KeyAdapter.KEY_CASE keyCase();
	ConfigStore keyCase(KeyAdapter.KEY_CASE kc);

	/**
	 * 设置配置项，用来覆盖ConfigTable
	 * @param key key
	 * @param value value
	 * @return this
	 */
	ConfigStore config(String key, Object value);
	Object config(String key);
	default boolean getBoolean(String key, boolean def){
		Object value = config(key);
		if(null == value){
			return def;
		}
		return BasicUtil.parseBoolean(value, def);
	}
	default String getString(String key, String def){
		Object value = config(key);
		if(null != value){
			return value.toString();
		}
		return def;
	}
	default String getString(String key){
		return getString(key, null);
	}
	default int getInt(String key, int def){
		Object value = config(key);
		if(null == value){
			return def;
		}
		return BasicUtil.parseInt(value, def);
	}
	default long getLong(String key, long def){
		Object value = config(key);
		if(null == value){
			return def;
		}
		return BasicUtil.parseLong(value, def);
	}

	/**
	 * 是否显示SQL
	 * @return boolean
	 */
	default boolean IS_LOG_SQL(){
		return getBoolean("IS_LOG_SQL", ConfigTable.IS_LOG_SQL);
	}
	default ConfigStore IS_LOG_SQL(boolean value){
		return config("IS_LOG_SQL", value);
	}

	/**
	 * 是否显示慢SQL
	 * @return boolean
	 */
	default boolean IS_LOG_SLOW_SQL(){
		return getBoolean("IS_LOG_SLOW_SQL", ConfigTable.IS_LOG_SLOW_SQL);
	}
	default ConfigStore IS_LOG_SLOW_SQL(boolean value){
		return config("IS_LOG_SLOW_SQL", value);
	}


	/**
	 * 异常时是否显示SQL
	 * @return boolean
	 */
	default boolean IS_LOG_SQL_WHEN_ERROR(){
		return getBoolean("IS_LOG_SQL_WHEN_ERROR", ConfigTable.IS_LOG_SQL_WHEN_ERROR);
	}
	default ConfigStore IS_LOG_SQL_WHEN_ERROR(boolean value){
		return config("IS_LOG_SQL_WHEN_ERROR", value);
	}
	/**
	 * 是否输出异常堆栈日志
	 * @return boolean
	 */
	default boolean IS_PRINT_EXCEPTION_STACK_TRACE(){
		return getBoolean("IS_PRINT_EXCEPTION_STACK_TRACE", ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE);
	}
	default ConfigStore IS_PRINT_EXCEPTION_STACK_TRACE(boolean value){
		return config("IS_PRINT_EXCEPTION_STACK_TRACE", value);
	}
	/**
	 * 是否显示SQL参数(占位符模式下有效)
	 * @return boolean
	 */
	default boolean IS_LOG_SQL_PARAM(){
		return getBoolean("IS_LOG_SQL_PARAM", ConfigTable.IS_LOG_SQL_PARAM);
	}
	default boolean IS_LOG_BATCH_SQL_PARAM(){
		return getBoolean("IS_LOG_BATCH_SQL_PARAM", ConfigTable.IS_LOG_BATCH_SQL_PARAM);
	}
	default ConfigStore IS_LOG_SQL_PARAM(boolean value){
		return config("IS_LOG_SQL_PARAM", value);
	}

	/**
	 * 异常时是否显示SQL参数(占位符模式下有效)
	 * @return boolean
	 */
	default boolean IS_LOG_SQL_PARAM_WHEN_ERROR(){
		return getBoolean("IS_LOG_SQL_PARAM_WHEN_ERROR", ConfigTable.IS_LOG_SQL_PARAM_WHEN_ERROR);
	}
	default ConfigStore IS_LOG_SQL_PARAM_WHEN_ERROR(boolean value){
		return config("IS_LOG_SQL_PARAM_WHEN_ERROR", value);
	}
	/**
	 * 是否显示SQL执行时间
	 * @return boolean
	 */
	default boolean IS_LOG_SQL_TIME(){
		return getBoolean("IS_LOG_SQL_TIME", ConfigTable.IS_LOG_SQL_TIME);
	}
	default ConfigStore IS_LOG_SQL_TIME(boolean value){
		return config("IS_LOG_SQL_TIME", value);
	}
	/**
	 * 是否抛出查询异常
	 * @return boolean
	 */
	default boolean IS_THROW_SQL_QUERY_EXCEPTION(){
		return getBoolean("IS_THROW_SQL_QUERY_EXCEPTION", ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION);
	}
	default ConfigStore IS_THROW_SQL_QUERY_EXCEPTION(boolean value){
		return config("IS_THROW_SQL_QUERY_EXCEPTION", value);
	}
	/**
	 * 是否抛出更新异常
	 * @return boolean
	 */
	default boolean IS_THROW_SQL_UPDATE_EXCEPTION(){
		return getBoolean("IS_THROW_SQL_UPDATE_EXCEPTION", ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION);
	}
	default ConfigStore IS_THROW_SQL_UPDATE_EXCEPTION(boolean value){
		return config("IS_THROW_SQL_UPDATE_EXCEPTION", value);
	}
	/**
	 * SQL日志是否启用占位符
	 * @return boolean
	 */
	default boolean IS_SQL_LOG_PLACEHOLDER(){
		return getBoolean("IS_SQL_LOG_PLACEHOLDER", ConfigTable.IS_SQL_LOG_PLACEHOLDER);
	}
	default ConfigStore IS_SQL_LOG_PLACEHOLDER(boolean value){
		return config("IS_SQL_LOG_PLACEHOLDER", value);
	}
	/**
	 * insert update 时是否自动检测表结构(删除表中不存在的属性)
	 * @return boolean
	 */
	default boolean IS_AUTO_CHECK_METADATA(){
		return getBoolean("IS_AUTO_CHECK_METADATA", ConfigTable.IS_AUTO_CHECK_METADATA);
	}
	default ConfigStore IS_AUTO_CHECK_METADATA(boolean value){
		return config("IS_AUTO_CHECK_METADATA", value);
	}
	/**
	 * 查询返回空DataSet时，是否检测元数据信息
	 * @return boolean
	 */
	default boolean IS_CHECK_EMPTY_SET_METADATA(){
		return getBoolean("IS_CHECK_EMPTY_SET_METADATA", ConfigTable.IS_CHECK_EMPTY_SET_METADATA);
	}
	default ConfigStore IS_CHECK_EMPTY_SET_METADATA(boolean value){
		return config("IS_CHECK_EMPTY_SET_METADATA", value);
	}


	/**
	 * 慢SQL判断标准(ms)
	 * @return long
	 */
	default long SLOW_SQL_MILLIS(){
		return getLong("SLOW_SQL_MILLIS", ConfigTable.SLOW_SQL_MILLIS);
	}
	default ConfigStore SLOW_SQL_MILLIS(long value){
		return config("SLOW_SQL_MILLIS", value);
	}


	/**
	 * DataRow是否更新nul值的列(针对DataRow)
	 * @return boolean
	 */
	default boolean IS_UPDATE_NULL_COLUMN(){
		return getBoolean("IS_UPDATE_NULL_COLUMN", ConfigTable.IS_UPDATE_NULL_COLUMN);
	}
	default ConfigStore IS_UPDATE_NULL_COLUMN(boolean value){
		return config("IS_UPDATE_NULL_COLUMN", value);
	}

	/**
	 * DataRow是否更新空值的列
	 * @return boolean
	 */
	default boolean IS_UPDATE_EMPTY_COLUMN(){
		return getBoolean("IS_UPDATE_EMPTY_COLUMN", ConfigTable.IS_UPDATE_EMPTY_COLUMN);
	}
	default ConfigStore IS_UPDATE_EMPTY_COLUMN(boolean value){
		return config("IS_UPDATE_EMPTY_COLUMN", value);
	}
	/**
	 * DataRow是否插入nul值的列
	 * @return boolean
	 */
	default boolean IS_INSERT_NULL_COLUMN(){
		return getBoolean("IS_INSERT_NULL_COLUMN", ConfigTable.IS_INSERT_NULL_COLUMN);
	}
	default ConfigStore IS_INSERT_NULL_COLUMN(boolean value){
		return config("IS_INSERT_NULL_COLUMN", value);
	}
	/**
	 * DataRow是否插入空值的列
	 * @return boolean
	 */
	default boolean IS_INSERT_EMPTY_COLUMN(){
		return getBoolean("IS_INSERT_EMPTY_COLUMN", ConfigTable.IS_INSERT_EMPTY_COLUMN);
	}
	default ConfigStore IS_INSERT_EMPTY_COLUMN(boolean value){
		return config("IS_INSERT_EMPTY_COLUMN", value);
	}
	/**
	 * Entity是否更新nul值的属性(针对Entity)
	 * @return boolean
	 */
	default boolean IS_UPDATE_NULL_FIELD(){
		return getBoolean("IS_UPDATE_NULL_FIELD", ConfigTable.IS_UPDATE_NULL_FIELD);
	}
	default ConfigStore IS_UPDATE_NULL_FIELD(boolean value){
		return config("IS_UPDATE_NULL_FIELD", value);
	}
	/**
	 * Entity是否更新空值的属性
	 * @return boolean
	 */
	default boolean IS_UPDATE_EMPTY_FIELD(){
		return getBoolean("IS_UPDATE_EMPTY_FIELD", ConfigTable.IS_UPDATE_EMPTY_FIELD);
	}
	default ConfigStore IS_UPDATE_EMPTY_FIELD(boolean value){
		return config("IS_UPDATE_EMPTY_FIELD", value);
	}
	/**
	 * Entity是否更新nul值的属性
	 * @return boolean
	 */
	default boolean IS_INSERT_NULL_FIELD(){
		return getBoolean("IS_INSERT_NULL_FIELD", ConfigTable.IS_INSERT_NULL_FIELD);
	}
	default ConfigStore IS_INSERT_NULL_FIELD(boolean value){
		return config("IS_INSERT_NULL_FIELD", value);
	}
	/**
	 * Entity是否更新空值的属性
	 * @return boolean
	 */
	default boolean IS_INSERT_EMPTY_FIELD(){
		return getBoolean("IS_INSERT_EMPTY_FIELD", ConfigTable.IS_INSERT_EMPTY_FIELD);
	}
	default ConfigStore IS_INSERT_EMPTY_FIELD(boolean value){
		return config("IS_INSERT_EMPTY_FIELD", value);
	}
	default boolean IS_LOG_SQL_WARN(){
		return getBoolean("IS_LOG_SQL_WARN", ConfigTable.IS_LOG_SQL_WARN);
	}
	default ConfigStore IS_LOG_SQL_WARN(boolean value){
		return config("IS_LOG_SQL_WARN", value);
	}
	default boolean IS_REPLACE_EMPTY_NULL(){
		return getBoolean("IS_REPLACE_EMPTY_NULL", ConfigTable.IS_REPLACE_EMPTY_NULL);
	}
	default ConfigStore IS_REPLACE_EMPTY_NULL(boolean value){
		return config("IS_REPLACE_EMPTY_NULL", value);
	}
	default ConfigStore IS_KEYHOLDER_IDENTITY(boolean value){
		return config("IS_KEYHOLDER_IDENTITY", value);
	}
	default boolean IS_KEYHOLDER_IDENTITY(){
		return getBoolean("IS_KEYHOLDER_IDENTITY", ConfigTable.IS_KEYHOLDER_IDENTITY);
	}

	/**
	 * 关闭所有SQL日志
	 * @return ConfigStore
	 */
	default ConfigStore closeAllSqlLog(){
		config("IS_LOG_SQL", false);
		config("IS_LOG_SQL_PARAM", false);
		config("IS_LOG_SQL_WHEN_ERROR", false);
		config("IS_LOG_SQL_PARAM_WHEN_ERROR", false);
		config("IS_LOG_SQL_TIME", false);
		config("IS_LOG_SLOW_SQL", false);
		return this;
	}
	/**
	 * 开启所有SQL日志
	 * @return ConfigStore
	 */
	default ConfigStore openAllSqlLog(){
		config("IS_LOG_SQL", true);
		config("IS_LOG_SQL_PARAM", true);
		config("IS_LOG_SQL_WHEN_ERROR", true);
		config("IS_LOG_SQL_PARAM_WHEN_ERROR", true);
		config("IS_LOG_SQL_TIME", true);
		config("IS_LOG_SLOW_SQL", true);
		return this;
	}
} 
 
 
