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
import org.anyline.data.handler.DataHandler;
import org.anyline.data.param.init.DefaultConfig;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 查询参数 
 * @author zh 
 * 
 */
public interface ConfigStore extends Cloneable{

	/**
	 * 属性转map
	 * @param empty 是否保留空值属性
	 * @return DataRow
	 */
	DataRow map(boolean empty);
	default DataRow map() {
		return map(false);
	}
	default String json(boolean empty) {
		return map(empty).json();
	}
	default String json() {
		return json(false);
	}
	String getRunText(DataRuntime runtime, Boolean placeholder, Boolean unicode);
	default String getRunText(DataRuntime runtime, Boolean placeholder) {
		return getRunText(runtime, placeholder, false);
	}
	/**
	 * 设置查询或操作的数据源
	 * @param datasource 查询或操作的数据源
	 * @return ConfigStore
	 */
	ConfigStore datasource(String datasource);

	/**
	 * 查询或操作的数据源
	 * @return String
	 */
	String datasource();
	/**
	 * 设置查询或操作的目标(表,存储过程,sql等)
	 * @param dest 查询或操作的目标
	 * @return ConfigStore
	 */
	ConfigStore dest(String dest);
	ConfigStore table(Table table);
	ConfigStore schema(Schema schema);
	ConfigStore catalog(Catalog catalog);

	ConfigStore table(String table);
	ConfigStore schema(String schema);
	ConfigStore catalog(String catalog);

	Table table();
	Schema schema();
	Catalog catalog();
	String tableName();
	String schemaName();
	String catalogName();

	Highlight getHighlight();

	Highlight getHighlight(String field);

	ConfigStore setHighlight(Highlight highlight);

	ConfigStore addHighlight(String field, Highlight highlight);
	ConfigStore addHighlight(String ... fields);

	/**
	 * 复制配置属性(不含查询条件)
	 * @param configs  ConfigStore
	 * @return ConfigStore
	 */
	ConfigStore copyProperty(ConfigStore configs);

	/**
	 * 自动识别and|or 如果遇到ES比较复杂的条件可以主动设置
	 * @param type and(must) or(should)  must_not  filter
	 * @return this
	 */

	ConfigStore setJoin(Condition.JOIN type);

	Condition.JOIN getJoin();

	/**
	 * 查询或操作的目标(表,存储过程,sql等)
	 * @return String
	 */
	String dest();
	/**
	 * 设置虚拟主键，主要是用作为更新条件
	 * @param keys keys
	 * @return this
	 */
	ConfigStore keys(String ... keys);
	/**
	 * 虚拟主键，主要是用作为更新条件
	 * @return List
	 */
	List<String> keys();

	boolean execute();
	ConfigStore execute(boolean execute);

	/**
	 * 设置流式(StreamHandlder)或异步(FinishHandler)的回调
	 * @param handler StreamHandler
	 * @return ConfigStore
	 */
	ConfigStore handler(DataHandler handler);
	DataHandler handler();
	ConfigStore aggregations(List<AggregationConfig> aggregations);
	ConfigStore aggregation(AggregationConfig ... aggregations);
	List<AggregationConfig> aggregations();
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
	default ConfigStore limit(int rows) {
		return limit(0, rows);
	}
	/**
	 * 设置分页
	 * @param page 第page页 下标从1开始
	 * @param rows 每页rows行
	 * @return ConfigStore
	 */
	ConfigStore page(long page, int rows);

	ConfigStore autoCount(boolean auto);

	Boolean autoCount();

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

	/**
	 * 查询条件是否为空
	 * @return boolean
	 */
	boolean isEmptyCondition();
	/**
	 * 查询条件及配置项等所有内容是否为空
	 * @return boolean
	 */
	boolean isEmpty();

	Config getConfig(String key);
	Hashtable<String, Object> getConfigs();
	ConfigStore removeConfig(String var);
	ConfigStore removeConfig(Config config);
	List<Object> getConfigValues(String var);
	Object getConfigValue(String var);
	Config getConfig(String key, Compare compare);
	ConfigStore removeConfig(String var, Compare compare);
	List<Object> getConfigValues(String var, Compare compare);
	Object getConfigValue(String var, Compare compare);

	/**
	 * 如果数据库中存在相同数据(根据overrideBy)是否覆盖 true或false会检测数据库 null不检测
	 * @return Boolean
	 */
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

	Boolean getPlaceholder();

	void setPlaceholder(Boolean placeholder);

	Boolean getUnicode();

	void setUnicode(Boolean unicode);
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
	default ConfigStore exists(EMPTY_VALUE_SWITCH swt, RunPrepare prepare) {
		Config conf = new DefaultConfig();
		conf.prepare(prepare);
		conf.setCompare(Compare.EXISTS);
		return and(conf);
	}
	default ConfigStore exists(RunPrepare prepare) {
		return exists(EMPTY_VALUE_SWITCH.IGNORE, prepare);
	}
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合 如果是集合生成IN条件
	 * @return ConfigStore
	 */
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String var, Object value) {
		return and(swt, var, value, false, false);
	}
	default ConfigStore and(String var, Object value) {
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
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore and(String id, String var, Object value, boolean overCondition, boolean overValue) {
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
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, (String)null, var, value, overCondition, overValue);
	}
	default ConfigStore and(String var, Object value, boolean overCondition, boolean overValue) {
		return and(EMPTY_VALUE_SWITCH.NONE, var, value, overCondition, overValue);
	}

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param text 可以是一条原生的SQL查询条件
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore and(String text) {
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
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value) {
		return and(swt, compare, var, value, false, false);
	}
	default ConfigStore and(Compare compare, String var, Object value) {
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
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value) {
		return and(swt, compare, id, var, value, false, false);
	}
	default ConfigStore and(Compare compare, String id, String var, Object value) {
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
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore and(Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
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
	default ConfigStore and(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue) {
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
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String id, String var, Object value) {
		return and(swt, id, var, value, false, false);
	}
	default ConfigStore and(String id, String var, Object value) {
		return and(EMPTY_VALUE_SWITCH.NONE, id, var, value);
	}

	default ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String col, Object ... values) {
		return and(swt, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore and(Compare compare, String col, Object ... values) {
		return and(EMPTY_VALUE_SWITCH.NONE, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore and(String var, Object ... values) {
		return and(EMPTY_VALUE_SWITCH.NONE, var, values);
	}
	default ConfigStore and(EMPTY_VALUE_SWITCH swt, String var, Object ... values) {
		return and(swt, Compare.AUTO, var, BeanUtil.array2list(values));
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
	default ConfigStore and(ConfigStore config) {
		return and(config, false);
	}

	/**
	 * 构造查询条件<br/>
	 * 最初ands是为了生成in条件，但and已经可以识别集合条件自动生成IN，<br/>
	 * 2023-10-21后<br/>
	 * ands改成了与ors类似的效果，即把之前所有的条件放在一个()内,然后and this<br/>
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合 如果是集合生成IN条件
	 * @return ConfigStore
	 */
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String var, Object value) {
		return ands(swt, var, value, false, false);
	}
	default ConfigStore ands(String var, Object value) {
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return ands(swt, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore ands(String id, String var, Object value, boolean overCondition, boolean overValue) {
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return ands(swt, (String)null, var, value, overCondition, overValue);
	}
	default ConfigStore ands(String var, Object value, boolean overCondition, boolean overValue) {
		return ands(EMPTY_VALUE_SWITCH.NONE, var, value, overCondition, overValue);
	}

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param text 可以是一条原生的SQL查询条件
	 * @return ConfigStore
	 */
	ConfigStore ands(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore ands(String text) {
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value) {
		return ands(swt, compare, var, value, false, false);
	}
	default ConfigStore ands(Compare compare, String var, Object value) {
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value) {
		return ands(swt, compare, id, var, value, false, false);
	}
	default ConfigStore ands(Compare compare, String id, String var, Object value) {
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		return ands(swt, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore ands(Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
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
	default ConfigStore ands(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue) {
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
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String id, String var, Object value) {
		return ands(swt, id, var, value, false, false);
	}
	default ConfigStore ands(String id, String var, Object value) {
		return ands(EMPTY_VALUE_SWITCH.NONE, id, var, value);
	}

	default ConfigStore ands(ConfigStore config) {
		return and(config, true);
	}

	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String col, Object ... values) {
		return ands(swt, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore ands(Compare compare, String col, Object ... values) {
		return ands(EMPTY_VALUE_SWITCH.NONE, compare, col, BeanUtil.array2list(values));
	}
	default ConfigStore ands(String var, Object ... values) {
		return ands(EMPTY_VALUE_SWITCH.NONE, var, values);
	}
	default ConfigStore ands(EMPTY_VALUE_SWITCH swt, String var, Object ... values) {
		return ands(swt, Compare.AUTO, var, BeanUtil.array2list(values));
	}

	default ConfigStore eq(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, id, var, value, overCondition, overValue);
	}
	default ConfigStore eq(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, var, value, overCondition, overValue);
	}
	default ConfigStore eq(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(id, var, value, overCondition, overValue);
	}
	default ConfigStore eq(String var, Object value, boolean overCondition, boolean overValue) {
		return and(var, value, overCondition, overValue);
	}
	default ConfigStore eq(String var, Object value) {
		return and(var, value);
	}

	default ConfigStore gt(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.GREAT, id, var, value, overCondition, overValue);
	}
	default ConfigStore gt(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.GREAT, var, value, overCondition, overValue);
	}
	default ConfigStore gt(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.GREAT, id, var, value, overCondition, overValue);
	}
	default ConfigStore gt(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.GREAT, var, value, overCondition, overValue);
	}
	default ConfigStore gt(String var, Object value) {
		return and(Compare.GREAT, var, value);
	}

	default ConfigStore ge(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.GREAT_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore ge(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.GREAT_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore ge(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.GREAT_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore ge(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.GREAT_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore ge(String var, Object value) {
		return and(Compare.GREAT_EQUAL, var, value);
	}
	default ConfigStore lt(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.LESS, id, var, value, overCondition, overValue);
	}
	default ConfigStore lt(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.LESS, var, value, overCondition, overValue);
	}
	default ConfigStore lt(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.LESS, id, var, value, overCondition, overValue);
	}
	default ConfigStore lt(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.LESS, var, value, overCondition, overValue);
	}
	default ConfigStore lt(String var, Object value) {
		return and(Compare.LESS, var, value);
	}

	default ConfigStore le(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.LESS_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore le(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.LESS_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore le(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.LESS_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore le(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.LESS_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore le(String var, Object value) {
		return and(Compare.LESS_EQUAL, var, value);
	}
	default ConfigStore in(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.IN, id, var, value, overCondition, overValue);
	}
	default ConfigStore in(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.IN, var, value, overCondition, overValue);
	}
	default ConfigStore in(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.IN, id, var, value, overCondition, overValue);
	}

	default ConfigStore in(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.IN, var, value, overCondition, overValue);
	}

	/**
	 * in
	 * @param var 列
	 * @param values 可以是集合 或对象 最终会组合到一维数组中
	 * @return ConfigStore
	 */
	default ConfigStore in(String var, Object ... values) {
		List<Object> list = BeanUtil.array2list(values);
		return and(Compare.IN, var, list);
	}

	default ConfigStore in(String var, Object values) {
		//这个方法不要删除 否则会NoSuchMethodError: org.anyline.data.param.ConfigStore.in(java/lang/string;java/lang/Object);
		return and(Compare.IN, var, values);
	}

	default ConfigStore likes(String value) {
		return likes(EMPTY_VALUE_SWITCH.IGNORE, value);
	}
	default ConfigStore likes(EMPTY_VALUE_SWITCH swt, String value) {
		return and(swt, Compare.LIKES, null, value);
	}
	default ConfigStore like(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.LIKE, id, var, value, overCondition, overValue);
	}
	default ConfigStore like(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.LIKE, var, value, overCondition, overValue);
	}
	default ConfigStore like(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.LIKE, id, var, value, overCondition, overValue);
	}
	default ConfigStore like(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.LIKE, var, value, overCondition, overValue);
	}
	default ConfigStore like(String var, Object value) {
		return and(Compare.LIKE, var, value);
	}


	default ConfigStore likesIgnoreCase(String value) {
		return likesIgnoreCase(EMPTY_VALUE_SWITCH.IGNORE, value);
	}
	default ConfigStore likesIgnoreCase(EMPTY_VALUE_SWITCH swt, String value) {
		return and(swt, Compare.LIKES_IGNORE_CASE, null, value);
	}
	default ConfigStore likeIgnoreCase(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.LIKE_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore likeIgnoreCase(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.LIKE_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore likeIgnoreCase(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.LIKE_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore likeIgnoreCase(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.LIKE_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore likeIgnoreCase(String var, Object value) {
		return and(Compare.LIKE_IGNORE_CASE, var, value);
	}


	default ConfigStore match(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.MATCH, id, var, value, overCondition, overValue);
	}
	default ConfigStore match(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.MATCH, var, value, overCondition, overValue);
	}
	default ConfigStore match(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.MATCH, id, var, value, overCondition, overValue);
	}
	default ConfigStore match(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.MATCH, var, value, overCondition, overValue);
	}
	default ConfigStore match(String var, Object value) {
		return and(Compare.MATCH, var, value);
	}

	default ConfigStore likePrefix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.START_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.START_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefix(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.START_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefix(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.START_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefix(String var, Object value) {
		return and(Compare.START_WITH, var, value);
	}


	default ConfigStore likePrefixIgnoreCase(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.START_WITH_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefixIgnoreCase(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.START_WITH_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefixIgnoreCase(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.START_WITH_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefixIgnoreCase(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.START_WITH_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore likePrefixIgnoreCase(String var, Object value) {
		return and(Compare.START_WITH_IGNORE_CASE, var, value);
	}

	default ConfigStore startWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.START_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore startWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.START_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore startWith(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.START_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore startWith(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.START_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore startWith(String var, Object value) {
		return and(Compare.START_WITH, var, value);
	}

	default ConfigStore startWithIgnoreCase(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.START_WITH_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore startWithIgnoreCase(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.START_WITH_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore startWithIgnoreCase(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.START_WITH_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore startWithIgnoreCase(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.START_WITH_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore startWithIgnoreCase(String var, Object value) {
		return and(Compare.START_WITH_IGNORE_CASE, var, value);
	}


	default ConfigStore likeSuffix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.END_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.END_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffix(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.END_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffix(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.END_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffix(String var, Object value) {
		return and(Compare.END_WITH, var, value);
	}


	default ConfigStore likeSuffixIgnoreCase(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.END_WITH_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffixIgnoreCase(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.END_WITH_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffixIgnoreCase(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.END_WITH_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffixIgnoreCase(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.END_WITH_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore likeSuffixIgnoreCase(String var, Object value) {
		return and(Compare.END_WITH_IGNORE_CASE, var, value);
	}

	default ConfigStore endWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.END_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore endWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.END_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore endWith(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.END_WITH, id, var, value, overCondition, overValue);
	}
	default ConfigStore endWith(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.END_WITH, var, value, overCondition, overValue);
	}
	default ConfigStore endWith(String var, Object value) {
		return and(Compare.END_WITH, var, value);
	}

	default ConfigStore endWithIgnoreCase(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.END_WITH_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore endWithIgnoreCase(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.END_WITH_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore endWithIgnoreCase(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.END_WITH_IGNORE_CASE, id, var, value, overCondition, overValue);
	}
	default ConfigStore endWithIgnoreCase(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.END_WITH_IGNORE_CASE, var, value, overCondition, overValue);
	}
	default ConfigStore endWithIgnoreCase(String var, Object value) {
		return and(Compare.END_WITH_IGNORE_CASE, var, value);
	}
	default ConfigStore regex(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.REGEX, id, var, value, overCondition, overValue);
	}
	default ConfigStore regex(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.REGEX, var, value, overCondition, overValue);
	}
	default ConfigStore regex(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.REGEX, id, var, value, overCondition, overValue);
	}
	default ConfigStore regex(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.REGEX, var, value, overCondition, overValue);
	}
	default ConfigStore regex(String var, Object value) {
		return and(Compare.REGEX, var, value);
	}

	default ConfigStore findInSet(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.FIND_IN_SET, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSet(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.FIND_IN_SET, var, value, overCondition, overValue);
	}
	default ConfigStore findInSet(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.FIND_IN_SET, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSet(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.FIND_IN_SET, var, value, overCondition, overValue);
	}
	default ConfigStore findInSet(String var, Object value) {
		return and(Compare.FIND_IN_SET, var, value);
	}

	default ConfigStore findInSetOr(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return or(swt, Compare.FIND_IN_SET_OR, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetOr(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return or(swt, Compare.FIND_IN_SET_OR, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetOr(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return or(Compare.FIND_IN_SET_OR, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetOr(String var, Object value, boolean overCondition, boolean overValue) {
		return or(Compare.FIND_IN_SET_OR, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetOr(String var, Object value) {
		return or(Compare.FIND_IN_SET_OR, var, value);
	}

	default ConfigStore findInSetAnd(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.FIND_IN_SET_AND, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetAnd(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.FIND_IN_SET_AND, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetAnd(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.FIND_IN_SET_AND, id, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetAnd(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.FIND_IN_SET_AND, var, value, overCondition, overValue);
	}
	default ConfigStore findInSetAnd(String var, Object value) {
		return and(Compare.FIND_IN_SET_AND, var, value);
	}

	default ConfigStore jsonContains(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContains(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContains(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContains(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContains(String var, Object value) {
		return and(Compare.JSON_CONTAINS, var, value);
	}


	default ConfigStore jsonContainsOr(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS_OR, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsOr(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS_OR, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsOr(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS_OR, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsOr(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS_OR, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsOr(String var, Object value) {
		return and(Compare.JSON_CONTAINS_OR, var, value);
	}


	default ConfigStore jsonContainsAnd(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS_AND, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsAnd(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS_AND, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsAnd(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS_AND, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsAnd(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS_AND, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsAnd(String var, Object value) {
		return and(Compare.JSON_CONTAINS_AND, var, value);
	}

	default ConfigStore jsonContainsPathOr(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS_PATH_OR, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsPathOr(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS_PATH_OR, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsPathOr(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS_PATH_OR, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsPathOr(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS_PATH_OR, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsPathOr(String var, Object value) {
		return and(Compare.JSON_CONTAINS_PATH_OR, var, value);
	}

	default ConfigStore jsonContainsPathAnd(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS_PATH_AND, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsPathAnd(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_CONTAINS_PATH_AND, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsPathAnd(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS_PATH_AND, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsPathAnd(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_CONTAINS_PATH_AND, var, value, overCondition, overValue);
	}
	default ConfigStore jsonContainsPathAnd(String var, Object value) {
		return and(Compare.JSON_CONTAINS_PATH_AND, var, value);
	}

	default ConfigStore jsonSearch(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_SEARCH, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonSearch(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.JSON_SEARCH, var, value, overCondition, overValue);
	}
	default ConfigStore jsonSearch(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_SEARCH, id, var, value, overCondition, overValue);
	}
	default ConfigStore jsonSearch(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.JSON_SEARCH, var, value, overCondition, overValue);
	}
	default ConfigStore jsonSearch(String var, Object value) {
		return and(Compare.JSON_SEARCH, var, value);
	}

	default ConfigStore between(EMPTY_VALUE_SWITCH swt, String id, String var, Object min, Object max, boolean overCondition, boolean overValue) {
		return and(swt, Compare.BETWEEN, id, var, Arrays.asList(min,max), overCondition, overValue);
	}
	default ConfigStore between(EMPTY_VALUE_SWITCH swt, String var, Object min, Object max, boolean overCondition, boolean overValue) {
		return and(swt, Compare.BETWEEN, var, Arrays.asList(min,max), overCondition, overValue);
	}
	default ConfigStore between(String id, String var, Object min, Object max, boolean overCondition, boolean overValue) {
		return and(Compare.BETWEEN, id, var, Arrays.asList(min,max), overCondition, overValue);
	}
	default ConfigStore between(String var, Object min, Object max, boolean overCondition, boolean overValue) {
		return and(Compare.BETWEEN, var, Arrays.asList(min,max), overCondition, overValue);
	}
	default ConfigStore between(String var, Object min, Object max) {
		return and(Compare.BETWEEN, var, Arrays.asList(min,max));
	}

	default ConfigStore ne(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore ne(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore ne(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_EQUAL, id, var, value, overCondition, overValue);
	}
	default ConfigStore ne(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_EQUAL, var, value, overCondition, overValue);
	}
	default ConfigStore ne(String var, Object value) {
		return and(Compare.NOT_EQUAL, var, value);
	}

	default ConfigStore isNull(String id, String var, boolean overCondition) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NULL, id, var, null, overCondition, false);
	}
	default ConfigStore isNull(String var, boolean overCondition) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NULL, var, null, overCondition, false);
	}
	default ConfigStore isNull(String var, boolean overCondition, boolean overValue) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NULL, var, null, overCondition, overValue);
	}
	default ConfigStore isNull(String var) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NULL, var);
	}

	default ConfigStore notNull(String id, String var, boolean overCondition) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NOT_NULL, id, var, null, overCondition, false);
	}
	default ConfigStore notNull(String var, boolean overCondition) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NOT_NULL, var, null, overCondition, false);
	}
	default ConfigStore notNull(String var, boolean overCondition, boolean overValue) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NOT_NULL, var, null, overCondition, overValue);
	}
	default ConfigStore notNull(String var) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NOT_NULL, var);
	}

	default ConfigStore isNotNull(String id, String var, boolean overCondition) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NOT_NULL, id, var, null, overCondition, false);
	}
	default ConfigStore isNotNull(String var, boolean overCondition) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NOT_NULL, var, null, overCondition, false);
	}
	default ConfigStore isNotNull(String var, boolean overCondition, boolean overValue) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NOT_NULL, var, null, overCondition, overValue);
	}
	default ConfigStore isNotNull(String var) {
		return and(EMPTY_VALUE_SWITCH.SRC, Compare.NOT_NULL, var);
	}

	default ConfigStore notIn(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_IN, id, var, value, overCondition, overValue);
	}
	default ConfigStore notIn(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_IN, var, value, overCondition, overValue);
	}
	default ConfigStore notIn(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_IN, id, var, value, overCondition, overValue);
	}
	default ConfigStore notIn(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_IN, var, value, overCondition, overValue);
	}
	default ConfigStore notIn(String var, Object ... values) {
		List<Object> list = BeanUtil.array2list(values);
		return and(Compare.NOT_IN, var, list);
	}

	default ConfigStore notLike(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLike(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE, var, value, overCondition, overValue);
	}
	default ConfigStore notLike(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLike(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE, var, value, overCondition, overValue);
	}
	default ConfigStore notLike(String var, Object value) {
		return and(Compare.NOT_LIKE, var, value);
	}

	default ConfigStore notLikePrefix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE_PREFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLikePrefix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE_PREFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notLikePrefix(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE_PREFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLikePrefix(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE_PREFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notLikePrefix(String var, Object value) {
		return and(Compare.NOT_LIKE_PREFIX, var, value);
	}

	default ConfigStore notStartWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE_PREFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notStartWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE_PREFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notStartWith(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE_PREFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notStartWith(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE_PREFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notStartWith(String var, Object value) {
		return and(Compare.NOT_LIKE_PREFIX, var, value);
	}

	default ConfigStore notLikeSuffix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE_SUFFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLikeSuffix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE_SUFFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notLikeSuffix(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE_SUFFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notLikeSuffix(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE_SUFFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notLikeSuffix(String var, Object value) {
		return and(Compare.NOT_LIKE_SUFFIX, var, value);
	}

	default ConfigStore notEndWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE_SUFFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notEndWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return and(swt, Compare.NOT_LIKE_SUFFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notEndWith(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE_SUFFIX, id, var, value, overCondition, overValue);
	}
	default ConfigStore notEndWith(String var, Object value, boolean overCondition, boolean overValue) {
		return and(Compare.NOT_LIKE_SUFFIX, var, value, overCondition, overValue);
	}
	default ConfigStore notEndWith(String var, Object value) {
		return and(Compare.NOT_LIKE_SUFFIX, var, value);
	}

	LinkedHashMap<String, Config> params();
	/**
	 * 占位符赋值
	 * @param swt 遇到空值处理方式
	 * @param prefix 表别名或XML中查询条件的ID或表名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	ConfigStore param(EMPTY_VALUE_SWITCH swt, String prefix, String var, Object value, boolean overCondition, boolean overValue);
	/**
	 * 用来给占位符或自定义SQL中的参数赋值
	 * @param swt 遇到空值处理方式
	 * @param id 自定义查询条件ID或表名表别名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @return ConfigStore
	 */
	default ConfigStore param(EMPTY_VALUE_SWITCH swt, String id, String var, Object value) {
		return param(swt, id, var, value, false, false);
	}
	default ConfigStore param(String id, String var, Object value) {
		return param(EMPTY_VALUE_SWITCH.NONE, id, var, value, false, false);
	}

	/**
	 * 用来给占位符或自定义SQL中的参数赋值
	 * Compare.NONE 只作为参数值为占位符赋值,不能独立生成新的查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @return ConfigStore
	 */
	default ConfigStore param(EMPTY_VALUE_SWITCH swt, String var, Object value) {
		return param(swt, null, var, value, false, false);
	}
	default ConfigStore param(String var, Object value) {
		return param(EMPTY_VALUE_SWITCH.NONE, var, value);
	}

	default ConfigStore param(Map<String, Object> params) {
		if(null != params) {
			for(String key:params.keySet()) {
				param(key, params.get(key));
			}
		}
		return this;
	}

	/**
	 * 根据占位符下标赋值,注意不需要提供下标,按顺序提供值即可
	 * @param values values
	 * @return this
	 */
	ConfigStore params(Object ... values);
	ConfigStore params(Collection<?> values);
	/**
	 * 根据占位符下标赋值
	 * @return list
	 */
	List<Object> values();
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
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		return or(swt, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, String id, String var, Object value) {
		return or(swt, id, var, value, false, false);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return or(swt, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore or(String id, String var, Object value) {
		return or(EMPTY_VALUE_SWITCH.NONE, id, var, value);
	}
	default ConfigStore or(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return or(EMPTY_VALUE_SWITCH.NONE, id, var, value, overCondition, overValue);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value) {
		return or(swt, compare, id, var, value, false, false);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value) {
		return or(swt, compare, null, var, value);
	}
	default ConfigStore or(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return or(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value, overCondition, overValue);
	}

	default ConfigStore or(Compare compare, String id, String var, Object value) {
		return or(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value);
	}
	default ConfigStore or(Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		return or(EMPTY_VALUE_SWITCH.NONE, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore or(Compare compare, String var, Object value) {
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
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return or(swt, compare(value), null, var, value, overCondition, overValue);
	}
	default ConfigStore or(EMPTY_VALUE_SWITCH swt, String var, Object value) {
		return or(swt, compare(value), var, value);
	}
	default ConfigStore or(String var, Object value, boolean overCondition, boolean overValue) {
		return or(EMPTY_VALUE_SWITCH.NONE, var, value, overCondition, overValue);
	}
	default ConfigStore or(String var, Object value) {
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
	default ConfigStore or(ConfigStore config) {
		return or(config, true);
	}
	ConfigStore or(Config config);
	ConfigStore or(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore or(String text) {
		return or(EMPTY_VALUE_SWITCH.NONE, text);
	}

	ConfigStore ors(EMPTY_VALUE_SWITCH swt, String text);
	default ConfigStore ors(String text) {
		return ors(EMPTY_VALUE_SWITCH.NONE, text);
	}
	ConfigStore ors(EMPTY_VALUE_SWITCH swt, Config config);
	default ConfigStore ors(Config config) {
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
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return ors(swt, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, String id, String var, Object value) {
		return ors(swt, compare(value), id, var, value, false, false);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		return ors(swt, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value) {
		return ors(swt, compare, id, var, value, false, false);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value) {
		return ors(swt, compare, null, var, value);
	}
	default ConfigStore ors(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue) {
		return ors(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value, overCondition, overValue);
	}
	default ConfigStore ors(String id, String var, Object value, boolean overCondition, boolean overValue) {
		return ors(EMPTY_VALUE_SWITCH.NONE, compare(value), id, var, value, overCondition, overValue);
	}
	default ConfigStore ors(String id, String var, Object value) {
		return ors(EMPTY_VALUE_SWITCH.NONE, compare(value), id, var, value);
	}
	default ConfigStore ors(Compare compare, String id, String var, Object value) {
		return ors(EMPTY_VALUE_SWITCH.NONE, compare, id, var, value);
	}
	default ConfigStore ors(Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		return ors(EMPTY_VALUE_SWITCH.NONE, compare, null, var, value, overCondition, overValue);
	}
	default ConfigStore ors(Compare compare, String var, Object value) {
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
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		return ors(swt, compare(value), null, var, value, overCondition, overValue);
	}
	default ConfigStore ors(EMPTY_VALUE_SWITCH swt, String var, Object value) {
		return ors(swt, compare(value), var, value);
	}
	default ConfigStore ors(String var, Object value, boolean overCondition, boolean overValue) {
		return ors(EMPTY_VALUE_SWITCH.NONE, var, value, overCondition, overValue);
	}
	default ConfigStore ors(String var, Object value) {
		return ors(EMPTY_VALUE_SWITCH.NONE, var, value);
	}

	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String var, Object value) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, var, value);
		}else{
			return and(swt, var, value);
		}
	}
	default ConfigStore condition(String join, String var, Object value) {
		if("or".equalsIgnoreCase(join)) {
			return or(var, value);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, id, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)) {
			return ors(swt, id, var, value, overCondition, overValue);
		}else{
			return and(swt, id, var, value, overCondition, overValue);
		}
	}
	default ConfigStore condition(String join, String id, String var, Object value, boolean overCondition, boolean overValue) {
		if("or".equalsIgnoreCase(join)) {
			return or(id, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)) {
			return ors(swt, var, value, overCondition, overValue);
		}else{
			return and(swt, var, value, overCondition, overValue);
		}
	}
	default ConfigStore condition(String join, String var, Object value, boolean overCondition, boolean overValue) {
		if("or".equalsIgnoreCase(join)) {
			return or(var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String text) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, text);
		}else if("ors".equalsIgnoreCase(join)) {
			return ors(swt, text);
		}else{
			return and(swt, text);
		}
	}
	default ConfigStore condition(String join, String text) {
		if("or".equalsIgnoreCase(join)) {
			return or(text);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, compare, var, value);
		}else if("ors".equalsIgnoreCase(join)) {
			return ors(swt, compare, var, value);
		}else{
			return and(swt, compare, var, value);
		}
	}
	default ConfigStore condition(String join, Compare compare, String var, Object value) {
		if("or".equalsIgnoreCase(join)) {
			return or(compare, var, value);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, compare, id, var, value);
		}else if("ors".equalsIgnoreCase(join)) {
			return ors(swt, compare, id, var, value);
		}else{
			return and(swt, compare, id, var, value);
		}
	}
	default ConfigStore condition(String join, Compare compare, String id, String var, Object value) {
		if("or".equalsIgnoreCase(join)) {
			return or(compare, id, var, value);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, compare, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)) {
			return ors(swt, compare, var, value, overCondition, overValue);
		}else{
			return and(swt, compare, var, value, overCondition, overValue);
		}
	}
	default ConfigStore condition(String join, Compare compare, String var, Object value, boolean overCondition, boolean overValue) {
		if("or".equalsIgnoreCase(join)) {
			return or(compare, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, compare, id, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)) {
			return ors(swt, compare, id, var, value, overCondition, overValue);
		}else{
			return and(swt, compare, id, var, value, overCondition, overValue);
		}
	}
	default ConfigStore condition(String join, Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue) {
		if("or".equalsIgnoreCase(join)) {
			return or(compare, id, var, value, overCondition, overValue);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, EMPTY_VALUE_SWITCH swt, String id, String var, Object value) {
		if("or".equalsIgnoreCase(join)) {
			return or(swt, id, var, value);
		}else if("ors".equalsIgnoreCase(join)) {
			return ors(swt, id, var, value);
		}else{
			return and(swt, id, var, value);
		}
	}
	default ConfigStore condition(String join, String id, String var, Object value) {
		if("or".equalsIgnoreCase(join)) {
			return or(id, var, value);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, Config config) {
		if("or".equalsIgnoreCase(join)) {
			return or(config);
		}else if("ors".equalsIgnoreCase(join)) {
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
	default ConfigStore condition(String join, ConfigStore config, boolean apart) {
		if("or".equalsIgnoreCase(join)) {
			return or(config, apart);
		}else{
			return and(config, apart);
		}
	}
	default ConfigStore condition(String join, ConfigStore config) {
		return condition(join, config, true);
	}
	default Compare compare(Object value) {
		Compare compare = Compare.EQUAL;
		if(null != value) {
			if(value instanceof Collection) {
				Collection col = (Collection) value;
				if(col.size()>1) {
					compare = Compare.IN;
				}
			}else if(value.getClass().isArray()) {
				int len = Array.getLength(value);
				if(len > 1) {
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
	default GroupStore groups(){
		return getGroups();
	}
	ConfigStore setGroups(GroupStore groups) ;

	default ConfigStore groups(GroupStore groups){
		return setGroups(groups);
	}

	ConfigStore having(String text);
	ConfigStore having(Compare compare, String key, Object value);
	default ConfigStore having(String key, Object value) {
		return having(Compare.EQUAL, key, value);
	}
	ConfigStore having(ConfigStore configs);

	ConfigStore having() ;
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
	LinkedHashMap<String, Column> getColumns();

	/**
	 * 级联(如删除点相关的边)
	 * @param cascade  是否开启
	 * @return ConfigStore
	 */
	ConfigStore cascade(boolean cascade);

	/**
	 * 是否启用级联(如删除点相关的边)
	 * @return boolean
	 */
	boolean cascade();

	/**
	 * 是否支持返回自增主键值
	 * @return boolean
	 */
	boolean supportKeyHolder();
	ConfigStore supportKeyHolder(boolean support);

	/**
	 * 自增主键值 keys
	 * @return keys
	 */
	List<String> keyHolders();
	ConfigStore keyHolders(String ... keys);
	/**
	 * 设置不城要查询的列
	 * @param columns 需要查询的列
	 * @return ConfigStore
	 */
	ConfigStore excludes(String ... columns);
	ConfigStore excludes(List<String> columns);
	List<String> excludes();
	Map<String, Column> getExcludes();

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

	void setExecuteTime(long time);
	long getExecuteTime();
	void setLastExecuteTime(long time);
	long getLastExecuteTime();

	void setPackageTime(long time);
	long getPackageTime();
	void setLastPackageTime(long time);
	long getLastPackageTime();

	default boolean getBoolean(String key, boolean def) {
		Object value = config(key);
		if(null == value) {
			return def;
		}
		return BasicUtil.parseBoolean(value, def);
	}
	default String getString(String key, String def) {
		Object value = config(key);
		if(null != value) {
			return value.toString();
		}
		return def;
	}
	default String getString(String key) {
		return getString(key, null);
	}
	default int getInt(String key, int def) {
		Object value = config(key);
		if(null == value) {
			return def;
		}
		return BasicUtil.parseInt(value, def);
	}
	default long getLong(String key, long def) {
		Object value = config(key);
		if(null == value) {
			return def;
		}
		return BasicUtil.parseLong(value, def);
	}
	ConfigStore clone();

	/**
	 * 过滤不存在的列
	 * @param metadatas 可用范围
	 */
	default void filter(LinkedHashMap<String, Column> metadatas) {
		ConfigChain chain = getConfigChain();
		if(null != chain) {
			chain.filter(metadatas);
		}
		OrderStore orders = getOrders();
		if(null != orders) {
			orders.filter(metadatas);
		}
	}
	
	/**
	 * 是否显示SQL
	 * @return boolean
	 */
	default boolean IS_LOG_SQL() {
		return getBoolean("IS_LOG_SQL", ConfigTable.IS_LOG_SQL);
	}
	default ConfigStore IS_LOG_SQL(boolean value) {
		return config("IS_LOG_SQL", value);
	}

	/**
	 * 是否显示慢SQL
	 * @return boolean
	 */
	default boolean IS_LOG_SLOW_SQL() {
		return getBoolean("IS_LOG_SLOW_SQL", ConfigTable.IS_LOG_SLOW_SQL);
	}
	default ConfigStore IS_LOG_SLOW_SQL(boolean value) {
		return config("IS_LOG_SLOW_SQL", value);
	}

	/**
	 * 查询结果输出日志
	 * @return boolean
	 */
	default boolean IS_LOG_QUERY_RESULT() {
		return getBoolean("IS_LOG_QUERY_RESULT", ConfigTable.IS_LOG_QUERY_RESULT);
	}
	default ConfigStore IS_LOG_QUERY_RESULT(boolean value) {
		return config("IS_LOG_QUERY_RESULT", value);
	}

	/**
	 * 查询结果输出日志时 是否过滤元数据查询结果
	 * @return boolean
	 */
	default boolean IS_LOG_QUERY_RESULT_EXCLUDE_METADATA() {
		return getBoolean("IS_LOG_QUERY_RESULT_EXCLUDE_METADATA", ConfigTable.IS_LOG_QUERY_RESULT_EXCLUDE_METADATA);
	}
	default ConfigStore IS_LOG_QUERY_RESULT_EXCLUDE_METADATA(boolean value) {
		return config("IS_LOG_QUERY_RESULT_EXCLUDE_METADATA", value);
	}

	/**
	 * 异常时是否显示SQL
	 * @return boolean
	 */
	default boolean IS_LOG_SQL_WHEN_ERROR() {
		return getBoolean("IS_LOG_SQL_WHEN_ERROR", ConfigTable.IS_LOG_SQL_WHEN_ERROR);
	}
	default ConfigStore IS_LOG_SQL_WHEN_ERROR(boolean value) {
		return config("IS_LOG_SQL_WHEN_ERROR", value);
	}

	/**
	 * 是否输出异常堆栈日志
	 * @return boolean
	 */
	default boolean IS_PRINT_EXCEPTION_STACK_TRACE() {
		return getBoolean("IS_PRINT_EXCEPTION_STACK_TRACE", ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE);
	}
	default ConfigStore IS_PRINT_EXCEPTION_STACK_TRACE(boolean value) {
		return config("IS_PRINT_EXCEPTION_STACK_TRACE", value);
	}

	/**
	 * 是否显示SQL参数(占位符模式下有效)
	 * @return boolean
	 */
	default boolean IS_LOG_SQL_PARAM() {
		return getBoolean("IS_LOG_SQL_PARAM", ConfigTable.IS_LOG_SQL_PARAM);
	}
	default boolean IS_LOG_BATCH_SQL_PARAM() {
		return getBoolean("IS_LOG_BATCH_SQL_PARAM", ConfigTable.IS_LOG_BATCH_SQL_PARAM);
	}
	default ConfigStore IS_LOG_SQL_PARAM(boolean value) {
		return config("IS_LOG_SQL_PARAM", value);
	}

	/**
	 * 异常时是否显示SQL参数(占位符模式下有效)
	 * @return boolean
	 */
	default boolean IS_LOG_SQL_PARAM_WHEN_ERROR() {
		return getBoolean("IS_LOG_SQL_PARAM_WHEN_ERROR", ConfigTable.IS_LOG_SQL_PARAM_WHEN_ERROR);
	}
	default ConfigStore IS_LOG_SQL_PARAM_WHEN_ERROR(boolean value) {
		return config("IS_LOG_SQL_PARAM_WHEN_ERROR", value);
	}

	/**
	 * 是否显示SQL执行时间
	 * @return boolean
	 */
	default boolean IS_LOG_SQL_TIME() {
		return getBoolean("IS_LOG_SQL_TIME", ConfigTable.IS_LOG_SQL_TIME);
	}
	default ConfigStore IS_LOG_SQL_TIME(boolean value) {
		return config("IS_LOG_SQL_TIME", value);
	}

	/**
	 * 是否抛出查询异常
	 * @return boolean
	 */
	default boolean IS_THROW_SQL_QUERY_EXCEPTION() {
		return getBoolean("IS_THROW_SQL_QUERY_EXCEPTION", ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION);
	}
	default ConfigStore IS_THROW_SQL_QUERY_EXCEPTION(boolean value) {
		return config("IS_THROW_SQL_QUERY_EXCEPTION", value);
	}

	/**
	 * 是否抛出更新异常
	 * @return boolean
	 */
	default boolean IS_THROW_SQL_UPDATE_EXCEPTION() {
		return getBoolean("IS_THROW_SQL_UPDATE_EXCEPTION", ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION);
	}
	default ConfigStore IS_THROW_SQL_UPDATE_EXCEPTION(boolean value) {
		return config("IS_THROW_SQL_UPDATE_EXCEPTION", value);
	}

	/**
	 * SQL日志是否启用占位符
	 * @return boolean
	 */
	default boolean IS_SQL_LOG_PLACEHOLDER() {
		return getBoolean("IS_SQL_LOG_PLACEHOLDER", ConfigTable.IS_SQL_LOG_PLACEHOLDER);
	}
	default ConfigStore IS_SQL_LOG_PLACEHOLDER(boolean value) {
		return config("IS_SQL_LOG_PLACEHOLDER", value);
	}

	/**
	 * insert update 时是否自动检测表结构(删除表中不存在的属性)
	 * @return boolean
	 */
	default boolean IS_AUTO_CHECK_METADATA() {
		return getBoolean("IS_AUTO_CHECK_METADATA", ConfigTable.IS_AUTO_CHECK_METADATA);
	}
	default ConfigStore IS_AUTO_CHECK_METADATA(boolean value) {
		return config("IS_AUTO_CHECK_METADATA", value);
	}

	/**
	 * 是否自动检测el值
	 * @return boolean
	 */
	default boolean IS_AUTO_CHECK_EL_VALUE() {
		return getBoolean("IS_AUTO_CHECK_EL_VALUE", ConfigTable.IS_AUTO_CHECK_EL_VALUE);
	}
	default ConfigStore IS_AUTO_CHECK_EL_VALUE(boolean value) {
		return config("IS_AUTO_CHECK_EL_VALUE", value);
	}
	/**
	 * 查询返回空DataSet时，是否检测元数据信息
	 * @return boolean
	 */
	default boolean IS_CHECK_EMPTY_SET_METADATA() {
		return getBoolean("IS_CHECK_EMPTY_SET_METADATA", ConfigTable.IS_CHECK_EMPTY_SET_METADATA);
	}
	default ConfigStore IS_CHECK_EMPTY_SET_METADATA(boolean value) {
		return config("IS_CHECK_EMPTY_SET_METADATA", value);
	}

	/**
	 * 慢SQL判断标准(ms)
	 * @return long
	 */
	default long SLOW_SQL_MILLIS() {
		return getLong("SLOW_SQL_MILLIS", ConfigTable.SLOW_SQL_MILLIS);
	}
	default ConfigStore SLOW_SQL_MILLIS(long value) {
		return config("SLOW_SQL_MILLIS", value);
	}

	/**
	 * DataRow是否更新nul值的列(针对DataRow)
	 * @return boolean
	 */
	default boolean IS_UPDATE_NULL_COLUMN() {
		return getBoolean("IS_UPDATE_NULL_COLUMN", ConfigTable.IS_UPDATE_NULL_COLUMN);
	}
	default ConfigStore IS_UPDATE_NULL_COLUMN(boolean value) {
		return config("IS_UPDATE_NULL_COLUMN", value);
	}

	/**
	 * DataRow是否更新空值的列
	 * @return boolean
	 */
	default boolean IS_UPDATE_EMPTY_COLUMN() {
		return getBoolean("IS_UPDATE_EMPTY_COLUMN", ConfigTable.IS_UPDATE_EMPTY_COLUMN);
	}
	default ConfigStore IS_UPDATE_EMPTY_COLUMN(boolean value) {
		return config("IS_UPDATE_EMPTY_COLUMN", value);
	}

	/**
	 * DataRow是否插入nul值的列
	 * @return boolean
	 */
	default boolean IS_INSERT_NULL_COLUMN() {
		return getBoolean("IS_INSERT_NULL_COLUMN", ConfigTable.IS_INSERT_NULL_COLUMN);
	}
	default ConfigStore IS_INSERT_NULL_COLUMN(boolean value) {
		return config("IS_INSERT_NULL_COLUMN", value);
	}

	/**
	 * DataRow是否插入空值的列
	 * @return boolean
	 */
	default boolean IS_INSERT_EMPTY_COLUMN() {
		return getBoolean("IS_INSERT_EMPTY_COLUMN", ConfigTable.IS_INSERT_EMPTY_COLUMN);
	}
	default ConfigStore IS_INSERT_EMPTY_COLUMN(boolean value) {
		return config("IS_INSERT_EMPTY_COLUMN", value);
	}

	/**
	 * Entity是否更新nul值的属性(针对Entity)
	 * @return boolean
	 */
	default boolean IS_UPDATE_NULL_FIELD() {
		return getBoolean("IS_UPDATE_NULL_FIELD", ConfigTable.IS_UPDATE_NULL_FIELD);
	}
	default ConfigStore IS_UPDATE_NULL_FIELD(boolean value) {
		return config("IS_UPDATE_NULL_FIELD", value);
	}

	/**
	 * Entity是否更新空值的属性
	 * @return boolean
	 */
	default boolean IS_UPDATE_EMPTY_FIELD() {
		return getBoolean("IS_UPDATE_EMPTY_FIELD", ConfigTable.IS_UPDATE_EMPTY_FIELD);
	}
	default ConfigStore IS_UPDATE_EMPTY_FIELD(boolean value) {
		return config("IS_UPDATE_EMPTY_FIELD", value);
	}

	/**
	 * Entity是否更新nul值的属性
	 * @return boolean
	 */
	default boolean IS_INSERT_NULL_FIELD() {
		return getBoolean("IS_INSERT_NULL_FIELD", ConfigTable.IS_INSERT_NULL_FIELD);
	}
	default ConfigStore IS_INSERT_NULL_FIELD(boolean value) {
		return config("IS_INSERT_NULL_FIELD", value);
	}

	/**
	 * Entity是否更新空值的属性
	 * @return boolean
	 */
	default boolean IS_INSERT_EMPTY_FIELD() {
		return getBoolean("IS_INSERT_EMPTY_FIELD", ConfigTable.IS_INSERT_EMPTY_FIELD);
	}
	default ConfigStore IS_INSERT_EMPTY_FIELD(boolean value) {
		return config("IS_INSERT_EMPTY_FIELD", value);
	}
	default boolean IS_LOG_SQL_WARN() {
		return getBoolean("IS_LOG_SQL_WARN", ConfigTable.IS_LOG_SQL_WARN);
	}
	default ConfigStore IS_LOG_SQL_WARN(boolean value) {
		return config("IS_LOG_SQL_WARN", value);
	}
	default boolean IS_REPLACE_EMPTY_NULL() {
		return getBoolean("IS_REPLACE_EMPTY_NULL", ConfigTable.IS_REPLACE_EMPTY_NULL);
	}

	default ConfigStore IS_REPLACE_EMPTY_NULL(boolean value) {
		return config("IS_REPLACE_EMPTY_NULL", value);
	}

	default int SQL_QUERY_TIMEOUT() {
		return getInt("SQL_QUERY_TIMEOUT", ConfigTable.SQL_QUERY_TIMEOUT);
	}
	default ConfigStore SQL_QUERY_TIMEOUT(int s) {
		return config("SQL_QUERY_TIMEOUT", s);
	}
	default int SQL_UPDATE_TIMEOUT() {
		return getInt("SQL_UPDATE_TIMEOUT", ConfigTable.SQL_UPDATE_TIMEOUT);
	}

	default ConfigStore SQL_UPDATE_TIMEOUT(int s) {
		return config("SQL_UPDATE_TIMEOUT", s);
	}
	default ConfigStore IGNORE_GRAPH_QUERY_RESULT_TOP_KEY(int s) {
		return config("IGNORE_GRAPH_QUERY_RESULT_TOP_KEY", s);
	}
	default int IGNORE_GRAPH_QUERY_RESULT_TOP_KEY() {
		return getInt("IGNORE_GRAPH_QUERY_RESULT_TOP_KEY", ConfigTable.IGNORE_GRAPH_QUERY_RESULT_TOP_KEY);
	}
	default ConfigStore IGNORE_GRAPH_QUERY_RESULT_TABLE(int s) {
		return config("IGNORE_GRAPH_QUERY_RESULT_TABLE", s);
	}
	default int IGNORE_GRAPH_QUERY_RESULT_TABLE() {
		return getInt("IGNORE_GRAPH_QUERY_RESULT_TABLE", ConfigTable.IGNORE_GRAPH_QUERY_RESULT_TABLE);
	}
	default ConfigStore MERGE_GRAPH_QUERY_RESULT_TABLE(int s) {
		return config("MERGE_GRAPH_QUERY_RESULT_TABLE", s);
	}
	default int MERGE_GRAPH_QUERY_RESULT_TABLE() {
		return getInt("MERGE_GRAPH_QUERY_RESULT_TABLE", ConfigTable.MERGE_GRAPH_QUERY_RESULT_TABLE);
	}
	default ConfigStore IS_ENABLE_PLACEHOLDER_REGEX_EXT(boolean s) {
		return config("IS_ENABLE_PLACEHOLDER_REGEX_EXT", s);
	}
	default boolean IS_ENABLE_PLACEHOLDER_REGEX_EXT() {
		return getBoolean("IS_ENABLE_PLACEHOLDER_REGEX_EXT", ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT);
	}
	default ConfigStore IS_ENABLE_SQL_DATATYPE_CONVERT(boolean s) {
		return config("IS_ENABLE_SQL_DATATYPE_CONVERT", s);
	}
	default boolean IS_ENABLE_SQL_DATATYPE_CONVERT() {
		return getBoolean("IS_ENABLE_SQL_DATATYPE_CONVERT", ConfigTable.IS_ENABLE_SQL_DATATYPE_CONVERT);
	}
	default ConfigStore IS_CHECK_ALL_INSERT_COLUMN(boolean s) {
		return config("IS_CHECK_ALL_INSERT_COLUMN", s);
	}
	default boolean IS_CHECK_ALL_INSERT_COLUMN() {
		return getBoolean("IS_CHECK_ALL_INSERT_COLUMN", ConfigTable.IS_CHECK_ALL_INSERT_COLUMN);
	}
	default ConfigStore IS_CHECK_ALL_UPDATE_COLUMN(boolean s) {
		return config("IS_CHECK_ALL_UPDATE_COLUMN", s);
	}
	default boolean IS_CHECK_ALL_UPDATE_COLUMN() {
		return getBoolean("IS_CHECK_ALL_UPDATE_COLUMN", ConfigTable.IS_CHECK_ALL_UPDATE_COLUMN);
	}
	/**
	 * 关闭所有SQL日志
	 * @return ConfigStore
	 */
	default ConfigStore closeAllSqlLog() {
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
	default ConfigStore openAllSqlLog() {
		config("IS_LOG_SQL", true);
		config("IS_LOG_SQL_PARAM", true);
		config("IS_LOG_SQL_WHEN_ERROR", true);
		config("IS_LOG_SQL_PARAM_WHEN_ERROR", true);
		config("IS_LOG_SQL_TIME", true);
		config("IS_LOG_SLOW_SQL", true);
		return this;
	}

	/**
	 * 是否输出SQL日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_SQL(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_SQL();
		}
		return ConfigTable.IS_LOG_SQL;
	}

	/**
	 * insert update 时是否自动检测表结构(删除表中不存在的属性)
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_AUTO_CHECK_METADATA(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_AUTO_CHECK_METADATA();
		}
		return ConfigTable.IS_AUTO_CHECK_METADATA;
	}

	static boolean IS_AUTO_CHECK_EL_VALUE(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_AUTO_CHECK_EL_VALUE();
		}
		return ConfigTable.IS_AUTO_CHECK_EL_VALUE;
	}

	/**
	 * 查询返回空DataSet时，是否检测元数据信息
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_CHECK_EMPTY_SET_METADATA(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_CHECK_EMPTY_SET_METADATA();
		}
		return ConfigTable.IS_CHECK_EMPTY_SET_METADATA;
	}

	/**
	 * 是否输出慢SQL日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_SLOW_SQL(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_SLOW_SQL();
		}
		return ConfigTable.IS_LOG_SLOW_SQL;
	}

	/**
	 * 查询结果输出日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_QUERY_RESULT(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_QUERY_RESULT();
		}
		return ConfigTable.IS_LOG_QUERY_RESULT;
	}

	/**
	 * 查询结果输出日志时 是否过滤元数据查询结果
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_QUERY_RESULT_EXCLUDE_METADATA(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_QUERY_RESULT_EXCLUDE_METADATA();
		}
		return ConfigTable.IS_LOG_QUERY_RESULT_EXCLUDE_METADATA;
	}

	/**
	 * 是否输出SQL参数日志(占位符模式下有效)
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_SQL_PARAM(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_SQL_PARAM();
		}
		return ConfigTable.IS_LOG_SQL_PARAM;
	}

	/**
	 * 是否输出SQL参数日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_BATCH_SQL_PARAM(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_BATCH_SQL_PARAM();
		}
		return ConfigTable.IS_LOG_BATCH_SQL_PARAM;
	}

	/**
	 * 异常时是否输出SQL日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_SQL_WHEN_ERROR(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_SQL_WHEN_ERROR();
		}
		return ConfigTable.IS_LOG_SQL_WHEN_ERROR;
	}

	/**
	 * 是否输出异常堆栈日志
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_PRINT_EXCEPTION_STACK_TRACE(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_PRINT_EXCEPTION_STACK_TRACE();
		}
		return ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE;
	}

	/**
	 * 异常时是否输出SQL参数日志(占位符模式下有效)
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_SQL_PARAM_WHEN_ERROR(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_SQL_PARAM_WHEN_ERROR();
		}
		return ConfigTable.IS_LOG_SQL_PARAM_WHEN_ERROR;
	}
	static boolean IS_SQL_LOG_PLACEHOLDER(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_SQL_LOG_PLACEHOLDER();
		}
		return ConfigTable.IS_SQL_LOG_PLACEHOLDER;
	}

	/**
	 * 是否显示SQL耗时
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_LOG_SQL_TIME(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_SQL_TIME();
		}
		return ConfigTable.IS_LOG_SQL_TIME;
	}

	/**
	 * 慢SQL判断标准
	 * @param configs ConfigStore
	 * @return long
	 */
	static long SLOW_SQL_MILLIS(ConfigStore configs) {
		if(null != configs) {
			return configs.SLOW_SQL_MILLIS();
		}
		return ConfigTable.SLOW_SQL_MILLIS;
	}

	/**
	 * 是否抛出查询异常
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_THROW_SQL_QUERY_EXCEPTION(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_THROW_SQL_QUERY_EXCEPTION();
		}
		return ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION;
	}

	/**
	 * 是否抛出更新异常
	 * @param configs ConfigStore
	 * @return boolean
	 */
	static boolean IS_THROW_SQL_UPDATE_EXCEPTION(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_THROW_SQL_UPDATE_EXCEPTION();
		}
		return ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION;
	}
	static boolean IS_UPDATE_NULL_COLUMN(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_UPDATE_NULL_COLUMN();
		}
		return ConfigTable.IS_UPDATE_NULL_COLUMN;
	}
	static boolean IS_UPDATE_EMPTY_COLUMN(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_UPDATE_EMPTY_COLUMN();
		}
		return ConfigTable.IS_UPDATE_EMPTY_COLUMN;
	}

	static boolean IS_UPDATE_NULL_FIELD(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_UPDATE_NULL_FIELD();
		}
		return ConfigTable.IS_UPDATE_NULL_FIELD;
	}

	static boolean IS_UPDATE_EMPTY_FIELD(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_UPDATE_EMPTY_FIELD();
		}
		return ConfigTable.IS_UPDATE_EMPTY_FIELD;
	}

	static boolean IS_INSERT_NULL_FIELD(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_INSERT_NULL_FIELD();
		}
		return ConfigTable.IS_INSERT_NULL_FIELD;
	}

	static boolean IS_INSERT_EMPTY_FIELD(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_INSERT_EMPTY_FIELD();
		}
		return ConfigTable.IS_INSERT_EMPTY_FIELD;
	}

	static boolean IS_INSERT_NULL_COLUMN(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_INSERT_NULL_COLUMN();
		}
		return ConfigTable.IS_INSERT_NULL_COLUMN;
	}
	static boolean IS_INSERT_EMPTY_COLUMN(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_INSERT_EMPTY_COLUMN();
		}
		return ConfigTable.IS_INSERT_EMPTY_COLUMN;
	}
	static boolean IS_LOG_SQL_WARN(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_LOG_SQL_WARN();
		}
		return ConfigTable.IS_LOG_SQL_WARN;
	}
	static boolean IS_REPLACE_EMPTY_NULL(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_REPLACE_EMPTY_NULL();
		}
		return ConfigTable.IS_REPLACE_EMPTY_NULL;
	}
	static int SQL_QUERY_TIMEOUT(ConfigStore configs) {
		if(null != configs) {
			return configs.SQL_QUERY_TIMEOUT();
		}
		return ConfigTable.SQL_QUERY_TIMEOUT;
	}
	static int SQL_UPDATE_TIMEOUT(ConfigStore configs) {
		if(null != configs) {
			return configs.SQL_UPDATE_TIMEOUT();
		}
		return ConfigTable.SQL_UPDATE_TIMEOUT;
	}
	static int IGNORE_GRAPH_QUERY_RESULT_TOP_KEY(ConfigStore configs) {
		if(null != configs) {
			return configs.IGNORE_GRAPH_QUERY_RESULT_TOP_KEY();
		}
		return ConfigTable.IGNORE_GRAPH_QUERY_RESULT_TOP_KEY;
	}
	static int IGNORE_GRAPH_QUERY_RESULT_TABLE(ConfigStore configs) {
		if(null != configs) {
			return configs.IGNORE_GRAPH_QUERY_RESULT_TABLE();
		}
		return ConfigTable.IGNORE_GRAPH_QUERY_RESULT_TABLE;
	}
	static int MERGE_GRAPH_QUERY_RESULT_TABLE(ConfigStore configs) {
		if(null != configs) {
			return configs.MERGE_GRAPH_QUERY_RESULT_TABLE();
		}
		return ConfigTable.MERGE_GRAPH_QUERY_RESULT_TABLE;
	}
	static boolean IS_ENABLE_PLACEHOLDER_REGEX_EXT(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_ENABLE_PLACEHOLDER_REGEX_EXT();
		}
		return ConfigTable.IS_ENABLE_PLACEHOLDER_REGEX_EXT;
	}
	static boolean IS_ENABLE_SQL_DATATYPE_CONVERT(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_ENABLE_SQL_DATATYPE_CONVERT();
		}
		return ConfigTable.IS_ENABLE_SQL_DATATYPE_CONVERT;
	}
	static boolean IS_CHECK_ALL_INSERT_COLUMN(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_CHECK_ALL_INSERT_COLUMN();
		}
		return ConfigTable.IS_CHECK_ALL_INSERT_COLUMN;
	}
	static boolean IS_CHECK_ALL_UPDATE_COLUMN(ConfigStore configs) {
		if(null != configs) {
			return configs.IS_CHECK_ALL_UPDATE_COLUMN();
		}
		return ConfigTable.IS_CHECK_ALL_UPDATE_COLUMN;
	}
} 
 
 
