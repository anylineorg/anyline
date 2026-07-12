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

import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.SystemFunction.META;
import org.anyline.metadata.type.DatabaseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DataMiningFunction implements META {
		CLUSTER_DETAILS("聚类详情", "expression", DatabaseType.Oracle),
		CLUSTER_DISTANCE("聚类距离", "expression1,expression2", DatabaseType.Oracle),
		CLUSTER_ID("聚类ID", "expression", DatabaseType.Oracle),
		CLUSTER_PROBABILITY("聚类概率", "expression", DatabaseType.Oracle),
		CLUSTER_SET("聚类集", "expression", DatabaseType.Oracle),
		FEATURE_COMPARE("特征比较", "expression1,expression2", DatabaseType.Oracle),
		FEATURE_DETAILS("特征详情", "expression", DatabaseType.Oracle),
		FEATURE_ID("特征ID", "expression", DatabaseType.Oracle),
		FEATURE_SET("特征集", "expression", DatabaseType.Oracle),
		FEATURE_VALUE("特征值", "expression", DatabaseType.Oracle),
		PREDICTION("预测", "model_name", DatabaseType.Oracle),
		PREDICTION_BOUNDS("预测边界", "model_name", DatabaseType.Oracle),
		PREDICTION_COST("预测成本", "model_name", DatabaseType.Oracle),
		PREDICTION_DETAILS("预测详情", "model_name", DatabaseType.Oracle),
		PREDICTION_PROBABILITY("预测概率", "model_name", DatabaseType.Oracle),
		PREDICTION_SET("预测集", "model_name", DatabaseType.Oracle),
		STATS_BINOMIAL_TEST("二项式检验", "expression", DatabaseType.Oracle),
		STATS_CROSS_TAB("交叉表分析", "expression", DatabaseType.Oracle),
		STATS_F_TEST("F检验", "expression", DatabaseType.Oracle),
		STATS_KS_TEST("KS检验", "expression", DatabaseType.Oracle),
		STATS_MW_TEST("Mann-Whitney检验", "expression", DatabaseType.Oracle),
		STATS_ONE_WAY_ANOVA("单因素方差分析", "expression", DatabaseType.Oracle),
		STATS_T_TEST_INDEP("独立T检验", "expression", DatabaseType.Oracle),
		STATS_T_TEST_INDEPU("不等方差独立T检验", "expression", DatabaseType.Oracle),
		STATS_T_TEST_ONE("单样本T检验", "expression", DatabaseType.Oracle),
		STATS_T_TEST_PAIRED("配对T检验", "expression", DatabaseType.Oracle),
		STATS_WSR_TEST("Wilcoxon检验", "expression", DatabaseType.Oracle)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    DataMiningFunction(String title) { this(title, null, null); }
    DataMiningFunction(String title, String params) { this(title, params, null); }
    DataMiningFunction(String title, DatabaseType database) { this(title, null, database); }
    DataMiningFunction(String title, String params, DatabaseType database) {
        this.title = title; this.database = database;
        if(null!=params) this.params.addAll(Arrays.asList(params.split(",")));
    }

    public String title() { return title; }
    public SystemFunction.META.Category category() { return META.Category.DATA_MINING; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }
    public META meta() { return this; }
    public void params(List<String> p) { params.clear(); if(p!=null) params.addAll(p); }
    public void support(boolean s) {}
}