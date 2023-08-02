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
 *
 *          
 */


package org.anyline.data.param;

import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.entity.Compare;
import org.anyline.entity.Order;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;

import java.util.List;
import java.util.Map;
 
 
/** 
 * 查询参数 
 * @author zh 
 * 
 */
public interface ConfigStore {
	/**
	 * 解析查询配置参数 
	 * @param config "COMPANY_CD:company","NM:nmEn% | NM:nmCn%","STATUS_VALUE:[status]" 
	 * @return Config
	 */ 
	Config parseConfig(String config); 
	ConfigStore setPageNavi(PageNavi navi);
	ConfigStore copyPageNavi(PageNavi navi);

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
	ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String col, Object ... values);
	ConfigStore ands(EMPTY_VALUE_SWITCH swt, String col, Object ... values);
	ConfigStore ands(String col, Object ... values);
	ConfigStore ands(Compare compare, String col, Object ... values);

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合 如果是集合生成IN条件
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, String var, Object value);
	ConfigStore and(String var, Object value);
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
	ConfigStore and(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore and(String id, String var, Object value, boolean overCondition, boolean overValue);

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,如果不覆盖则与原来的值合成新的集合
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore and(String var, Object value, boolean overCondition, boolean overValue);
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param text 可以是一条原生的SQL查询条件
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, String text);
	ConfigStore and(String text);

	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value);
	ConfigStore and(Compare compare, String var, Object value);
	/**
	 * 构造查询条件
	 * @param swt 遇到空值处理方式
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param compare 匹配方式
	 * @return ConfigStore
	 */
	ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value);
	ConfigStore and(Compare compare, String id, String var, Object value);
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
	ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore and(Compare compare, String var, Object value, boolean overCondition, boolean overValue);
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
	ConfigStore and(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue);

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
	ConfigStore and(EMPTY_VALUE_SWITCH swt, String id, String var, Object value);
	ConfigStore and(String id, String var, Object value);
	/**
	 * 构造查询条件
	 * @param config 查询条件
	 * @return ConfigStore
	 */
	ConfigStore and(Config config);
	ConfigStore and(ConfigStore config);

	ConfigStore eq(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore eq(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore eq(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore eq(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore eq(String var, Object value);


	ConfigStore gt(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore gt(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore gt(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore gt(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore gt(String var, Object value);

	ConfigStore ge(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ge(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ge(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ge(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ge(String var, Object value);


	ConfigStore lt(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore lt(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore lt(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore lt(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore lt(String var, Object value);

	ConfigStore le(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore le(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore le(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore le(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore le(String var, Object value);


	ConfigStore in(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore in(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore in(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore in(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore in(String var, Object value);


	ConfigStore like(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore like(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore like(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore like(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore like(String var, Object value);


	ConfigStore likePrefix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore likePrefix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore likePrefix(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore likePrefix(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore likePrefix(String var, Object value);

	ConfigStore startWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore startWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore startWith(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore startWith(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore startWith(String var, Object value);

	ConfigStore likeSuffix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore likeSuffix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore likeSuffix(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore likeSuffix(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore likeSuffix(String var, Object value);

	ConfigStore endWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore endWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore endWith(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore endWith(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore endWith(String var, Object value);

	ConfigStore findInSet(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSet(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSet(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSet(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSet(String var, Object value);

	ConfigStore findInSetOr(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSetOr(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSetOr(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSetOr(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSetOr(String var, Object value);

	ConfigStore findInSetAnd(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSetAnd(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSetAnd(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSetAnd(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore findInSetAnd(String var, Object value);


	ConfigStore between(EMPTY_VALUE_SWITCH swt, String id, String var, Object min, Object max, boolean overCondition, boolean overValue);
	ConfigStore between(EMPTY_VALUE_SWITCH swt, String var, Object min, Object max, boolean overCondition, boolean overValue);
	ConfigStore between(String id, String var, Object min, Object max, boolean overCondition, boolean overValue);
	ConfigStore between(String var, Object min, Object max, boolean overCondition, boolean overValue);
	ConfigStore between(String var, Object min, Object max);


	ConfigStore ne(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ne(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ne(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ne(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ne(String var, Object value);


	ConfigStore notIn(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notIn(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notIn(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notIn(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notIn(String var, Object value);

	ConfigStore notLike(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLike(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLike(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLike(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLike(String var, Object value);

	ConfigStore notLikePrefix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLikePrefix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLikePrefix(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLikePrefix(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLikePrefix(String var, Object value);

	ConfigStore notStartWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notStartWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notStartWith(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notStartWith(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notStartWith(String var, Object value);


	ConfigStore notLikeSuffix(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLikeSuffix(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLikeSuffix(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLikeSuffix(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notLikeSuffix(String var, Object value);

	ConfigStore notEndWith(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notEndWith(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notEndWith(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notEndWith(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore notEndWith(String var, Object value);


	/**
	 * 正则表达式(注意与like通配符不同,不是每个数据库都支持)
	 * @param swt 遇到空值处理方式
	 * @param id 表别名或XML中查询条件的ID
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
	 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
	 * @return ConfigStore
	 */
	ConfigStore regex(EMPTY_VALUE_SWITCH swt, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore regex(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore regex(String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore regex(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore regex(String var, Object value);

	ConfigStore or(ConfigStore config);


	/**
	 * 用来给占位符或自定义SQL中的参数赋值
	 * @param swt 遇到空值处理方式
	 * @param id 自定义查询条件ID或表名表别名
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @return ConfigStore
	 */
	ConfigStore param(EMPTY_VALUE_SWITCH swt, String id, String var, Object value);
	ConfigStore param(String id, String var, Object value);
	/**
	 * 用来给占位符或自定义SQL中的参数赋值
	 * @param swt 遇到空值处理方式
	 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果以var+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用
	 * @param value 值 可以是集合
	 * @return ConfigStore
	 */
	ConfigStore param(EMPTY_VALUE_SWITCH swt, String var, Object value);
	ConfigStore param(String var, Object value);
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
	ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value);
	ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value);
	ConfigStore or(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore or(Compare compare, String id, String var, Object value);
	ConfigStore or(Compare compare, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore or(Compare compare, String var, Object value);
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
	ConfigStore or(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore or(EMPTY_VALUE_SWITCH swt, String var, Object value);
	ConfigStore or(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore or(String var, Object value);
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
	ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String id, String var, Object value);
	ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String var, Object value);
	ConfigStore ors(Compare compare, String id, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ors(Compare compare, String id, String var, Object value);
	ConfigStore ors(Compare compare, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ors(Compare compare, String var, Object value);
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
	ConfigStore ors(EMPTY_VALUE_SWITCH swt, String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ors(EMPTY_VALUE_SWITCH swt, String var, Object value);
	ConfigStore ors(String var, Object value, boolean overCondition, boolean overValue);
	ConfigStore ors(String var, Object value);
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

} 
 
 
