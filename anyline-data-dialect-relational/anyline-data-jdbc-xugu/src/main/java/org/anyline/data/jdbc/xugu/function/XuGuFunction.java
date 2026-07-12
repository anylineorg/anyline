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


package org.anyline.data.jdbc.xugu.function;

import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseType;

import java.util.List;

/**
 * XuguDB 函数定义
 * <p>
 * XuguDB 虚谷数据库，基于 PostgreSQL 深度优化，全面兼容 Oracle 语法和函数。
 * 兼容 Oracle 9i/10g/11g/12c/19c/21c 版本的主要内置函数。
 * <p>
 * 参考：XuguDB官方文档 https://help.xugudb.com/content/reference/function/function
 */
public enum XuGuFunction implements SystemFunction {
	;


	private final String title;
	private final String formula;
	private final META meta;
	private boolean support = true;
	private List<String> params = null;
	private List<String> orders = null;

	XuGuFunction(META meta, String title, String formula) {
		this(meta, title, formula, true);
	}
	XuGuFunction(META meta, String title, String formula, boolean support) {
		this.meta = meta;
		this.title = title;
		this.formula = formula;
		this.support = support;
	}
	XuGuFunction(META meta, boolean support) {
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
		return DatabaseType.XuGu;
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
		if (orders != null && !orders.isEmpty()) {
			return orders;
		}
		String formula = formula();
		if (formula != null && formula.contains("${")) {
			orders = SystemFunction.extractPlaceholders(formula);
		}
		return orders;
	}
}