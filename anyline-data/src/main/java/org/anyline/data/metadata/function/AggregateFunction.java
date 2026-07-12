/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.metadata.function;

import org.anyline.metadata.SystemFunction.META;
import org.anyline.metadata.type.DatabaseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* AGGREGATE 类函数定义
* 包含所有数据库的 AGGREGATE 相关函数
*/
public enum AggregateFunction implements META {
		APPROX_COUNT("近似计数", "expression", DatabaseType.Oracle),
		APPROX_COUNT_DISTINCT("近似去重计数", "expression", DatabaseType.Oracle),
		APPROX_COUNT_DISTINCT_AGG("近似去重计数聚合", "expression", DatabaseType.Oracle),
		APPROX_COUNT_DISTINCT_DETAIL("近似去重计数详情", "expression", DatabaseType.Oracle),
		APPROX_MEDIAN("近似中位数", "expression", DatabaseType.Oracle),
		APPROX_PERCENTILE("近似百分位", "expression,percentile", DatabaseType.Oracle),
		APPROX_PERCENTILE_AGG("近似百分位聚合", "expression,percentile", DatabaseType.Oracle),
		APPROX_PERCENTILE_DETAIL("近似百分位详情", "expression,percentile", DatabaseType.Oracle),
		APPROX_RANK("近似排名", "expression", DatabaseType.Oracle),
		APPROX_SUM("近似求和", "expression", DatabaseType.Oracle),
		AVG("平均值", "expression"),
		BOOL_AND("按位与", "expression"),
		BOOL_OR("按位或", "expression"),
		COLLECT("收集为嵌套表", "expression", DatabaseType.Oracle),
		CORR("计算相关系数", "expression1,expression2"),
		CORR_K("肯德尔tau-b系数", "expression1,expression2"),
		CORR_S("斯皮尔曼ρ系数", "expression1,expression2"),
		COUNT("计数", "expression"),
		COVAR_POP("计算总体协方差", "expression1,expression2"),
		COVAR_SAMP("计算样本协方差", "expression1,expression2"),
		CUBE_TABLE("立方体表", "cube_name", DatabaseType.Oracle),
		CUME_DIST("累积分布", "window_spec"),
		CUME_DIST_FINAL("累积分布最终", DatabaseType.PostgreSQL),
		CV("当前值", DatabaseType.Oracle),
		DENSE_RANK("密集排名", "window_spec"),
		DENSE_RANK_FINAL("密集排名终", DatabaseType.PostgreSQL),
		EVERY("是否全部为真"),
		FIRST_FUNC("FIRST"),
		FIRST_ROW("第一行", "expression"),
		FIRST_VALUE("首值", "expression,window_spec"),
		GROUP_ID("分组ID", DatabaseType.Oracle),
		GROUPING("分组级别", "expression"),
		GROUPING_ID("分组层级ID", "expression", DatabaseType.Oracle),
		LAG("前导行", "expression,offset,default"),
		LAST_ROW("最后行"),
		LAST_VALUE("末值", "expression,window_spec"),
		LEAD("后续行", "expression,offset,default"),
		MAX("最大值", "expression"),
		MEDIAN("中位数", "expression"),
		MIN("最小值", "expression"),
		MODE("一个值与一组值之和比值"),
		MODE_FINAL("众数终", DatabaseType.PostgreSQL),
		NTH_VALUE("第N值", "n,expression"),
		NTILE("分桶", "n"),
		ORDERED_SET_TRANSITION("有序集过渡", DatabaseType.PostgreSQL),
		ORDERED_SET_TRANSITION_MULTI("有序集多参数过渡", DatabaseType.PostgreSQL),
		PERCENT_RANK("百分位排名", "window_spec"),
		PERCENT_RANK_FINAL("百分位排名终", DatabaseType.PostgreSQL),
		PERCENTILE_CONT("连续百分位数", "percentile,expression"),
		PERCENTILE_DISC("离散百分位数", "percentile,expression"),
		POWER_MULTIS_ET("嵌套表基数乘幂", "collection", DatabaseType.Oracle),
		POWER_MULTIS_ET_BY_CARDINALITY("嵌套表基数", "collection,cardinality", DatabaseType.Oracle),
		RANK("排名", "window_spec"),
		RANK_FINAL("排名最终", DatabaseType.PostgreSQL),
		RATIO_TO_REPORT("占比计算", "expression", DatabaseType.Oracle),
		REGR_AVGX("自变量平均值"),
		REGR_AVGY("因变量平均值"),
		REGR_COUNT("两个输入都为非空行数"),
		REGR_INTERCEPT("最小二乘法Y截距"),
		REGR_R2("系数平方"),
		REGR_SLOPE("最小二乘法斜率"),
		REGR_SXX("自变量“平方和”"),
		REGR_SXY("独立乘时因变量“乘积之和”"),
		REGR_SYY("因变量“平方和”"),
		ROW_NUMBER("行号", "window_spec"),
		SKIP_FUNC("SKIP"),
		SLOPE("斜率", "y"),
		STATS_MODE("众数", "expression", DatabaseType.Oracle),
		STD("标准差", "expression"),
		STDDEV("标准差", "expression"),
		STDDEV_POP("总体标准差", "expression"),
		STDDEV_SAMP("样本标准差", "expression"),
		SUM("求和", "expression"),
		UNIQUE_FUNC("UNIQUE(Informix兼容)"),
		VAR_POP("总体方差", "expression"),
		VAR_SAMP("样本方差", "expression"),
		VARIANCE("方差", "expression")		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    AggregateFunction(String title) {
        this(title, null, null);
    }
    AggregateFunction(String title, String params) {
        this(title, params, null);
    }
    AggregateFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    AggregateFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public META.Category category() { return META.Category.AGGREGATE; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}