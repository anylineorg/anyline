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


package org.anyline.data.jdbc.goldendb.function;

import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseType;

import java.util.List;

/**
 * GoldenDB 函数定义
 * <p>
 * GoldenDB 分布式数据库，核心兼容 MySQL 8.0.25。
 * MySQL 模式下完全兼容所有内置函数，
 * Oracle 模式(parse_mode=2)下额外支持约 97 个 Oracle 兼容函数。
 * <p>
 * 参考：GoldenDB产品手册 1.4.2 内置函数、5.10.5.1
 */
public enum GoldenDBFunction implements SystemFunction {
	;


	private final String title;    // 函数名
	private final String formula;   // 带参数的完整格式
	private final META meta;
	private boolean support = true;
	private List<String> params = null;
	private List<String> orders = null;

	GoldenDBFunction(META meta, String title, String formula) {
		this(meta, title, formula, true);
	}
	GoldenDBFunction(META meta, String title, String formula, boolean support) {
		this.meta = meta;
		this.title = title;
		this.formula = formula;
		this.support = support;
	}
	GoldenDBFunction(META meta, boolean support) {
		this(meta, null, null, support);
	}

	@Override
	public boolean support() {
		return support;
	}
	@Override
	public void support(boolean support) {
		this.support = support;
	}


	@Override
	public String title(){
		return title;
	}
	@Override
	public String formula() {
		return formula;
	}
	@Override
	public DatabaseType database() {
		return DatabaseType.GoldenDB;
	}

	@Override
	public META meta() {
		return meta;
	}

	@Override
	public List<String> params() {
		return params;
	}

	@Override
	public void params(List<String> params) {
		this.params = params;
	}


	@Override
	public List<String> orders() {
		// 如果 orders 已定义，直接返回
		if (orders != null && !orders.isEmpty()) {
			return orders;
		}
		// 否则从 formulatitle() 中提取参数顺序
		String formula = formula();
		if (formula != null && formula.contains("${")) {
			orders = SystemFunction.extractPlaceholders(formula);
		}
		return orders;
	}
}