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
* LABEL_SECURITY 类函数定义
* 包含所有数据库的 LABEL_SECURITY 相关函数
*/
public enum LabelSecurityFunction implements META {
		ALTER_COLUMN_CONTROL("修改列级控制"),
		ALTER_COMPARTMENT("修改安全隔间全称名"),
		ALTER_LEVEL("修改级别全称名"),
		ALTER_POLICY("修改一个安全策略策略选项"),
		ALTER_ROW_CONTROL("修改行级控制选项"),
		ALTER_TAB_CONTROL_BASE_TIME("修改时间端访问间隔"),
		ALTER_TABLE_CONTROL("修改表级控制属性"),
		APPLY_COLUMN_CONTROL("应用列级控制"),
		APPLY_COLUMN_CONTROL_CONSTRAINT("应用列级控制推理约束"),
		APPLY_ROW_CONTROL("应用行级控制"),
		APPLY_TABLE_CONTROL("应用表级控制"),
		APPLY_TABLE_POLICY("应用安全策略到表"),
		CAN_ACCESS_COLUMN("column访问权限", "user,database,table,expression"),
		CAN_ACCESS_DATABASE("database访问权限", "user,database"),
		CAN_ACCESS_TABLE("table访问权限", "user,database,table"),
		CAN_ACCESS_USER("user访问权限", "user"),
		CAN_ACCESS_VIEW("view访问权限", "user,database,view"),
		CREATE_COMPARTMENT("创建一个安全隔间"),
		CREATE_LEVEL("创建一个安全级别"),
		CREATE_POLICY("创建一个安全策略"),
		DROP_COMPARTMENT_BY_NAME("删除一个安全隔间"),
		DROP_LEVEL_BY_NAME("删除一个级别"),
		DROP_POLICY("删除一个安全策略"),
		DROP_TAB_CONTROL_BASE_TIME("删除时间段访问"),
		ENABLE_POLICY("修改一个安全策略可用状态"),
		ENABLE_TABLE_POLICY("修改策略应用状态"),
		REMOVE_COLUMN_CONTROL("移除列级控制"),
		REMOVE_COLUMN_CONTROL_CONSTRAINT("移除列级控制推理约束"),
		REMOVE_ROW_CONTROL("移除行级控制"),
		REMOVE_TABLE_CONTROL("移除表级控制"),
		REMOVE_TABLE_POLICY("取消应用策略到表"),
		SET_TAB_CONTROL_BASE_TIME("基于时间段列集合访问控制")		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    LabelSecurityFunction(String title) {
        this(title, null, null);
    }
    LabelSecurityFunction(String title, String params) {
        this(title, params, null);
    }
    LabelSecurityFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    LabelSecurityFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.LABEL_SECURITY; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }
    
    
    
}