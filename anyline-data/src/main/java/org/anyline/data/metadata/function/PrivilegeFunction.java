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
* PRIVILEGE 类函数定义
* 包含所有数据库的 PRIVILEGE 相关函数
*/
public enum PrivilegeFunction implements META {
		ACL_DEFAULT("构造默认访问权限", "aclitem", DatabaseType.PostgreSQL),
		ACL_EXPLODE("权限数组转为行集", "aclitem", DatabaseType.PostgreSQL),
		ACLCONTAINS("ACL中是否包含指定权限", DatabaseType.PostgreSQL),
		ACLDEFAULT("构造默认ACL数组", DatabaseType.PostgreSQL),
		ACLEXPLODE("ACL数组展开为行集", DatabaseType.PostgreSQL),
		ACLINSERT("向ACL插入权限", DatabaseType.PostgreSQL),
		ACLITEMEQ("ACL项比较", DatabaseType.PostgreSQL),
		ACLITEMIN("输入ACL项", DatabaseType.PostgreSQL),
		ACLITEMOUT("输出ACL项", DatabaseType.PostgreSQL),
		ACLREMOVE("从ACL删除权限", DatabaseType.PostgreSQL),
		AS_TABLESPACE_PRIVILEGE("用户是否具有表空间权限", "role", DatabaseType.PostgreSQL),
		GET_ACL("数据库对象ACL", DatabaseType.PostgreSQL),
		HAS_ANY_COLUMN_PRIVILEGE("用户是否对表任何列具有权限", DatabaseType.PostgreSQL),
		HAS_COLUMN_PRIVILEGE("用户是否对指定表列具有权限", DatabaseType.PostgreSQL),
		HAS_DATABASE_PRIVILEGE("用户是否具有数据库权限", DatabaseType.PostgreSQL),
		HAS_FOREIGN_DATA_WRAPPER_PRIVILEGE("外部包装器权限", DatabaseType.PostgreSQL),
		HAS_FUNCTION_PRIVILEGE("用户是否具有功能权限", DatabaseType.PostgreSQL),
		HAS_LANGUAGE_PRIVILEGE("用户是否具有语言权限", DatabaseType.PostgreSQL),
		HAS_LARGE_OBJECT_PRIVILEGE("用户是否具有大型对象权限", DatabaseType.PostgreSQL),
		HAS_PARAMETER_PRIVILEGE("用户是否具有配置参数权限", DatabaseType.PostgreSQL),
		HAS_ROLE("用户是否具有角色权限", DatabaseType.PostgreSQL),
		HAS_SCHEMA_PRIVILEGE("用户是否具有架构权限", DatabaseType.PostgreSQL),
		HAS_SEQUENCE_PRIVILEGE("用户是否具有序列权限", DatabaseType.PostgreSQL),
		HAS_SERVER_PRIVILEGE("用户是否具有外部服务器权限", DatabaseType.PostgreSQL),
		HAS_TABLE_PRIVILEGE("用户是否具有表权限", DatabaseType.PostgreSQL),
		HAS_TABLESPACE_PRIVILEGE("用户是否具有表空间权限", "user", DatabaseType.PostgreSQL),
		HAS_TYPE_PRIVILEGE("用户是否具有数据类型权限", DatabaseType.PostgreSQL),
		MAKE_ACL_ITEM("构造具有给定属性属性", DatabaseType.PostgreSQL),
		MAKEACLITEM("构造ACL项", DatabaseType.PostgreSQL),
		ROLES_GRAPHML("表示内存角色子图GraphML", DatabaseType.PostgreSQL),
		ROW_SECURITY_ACTIVE("在当前用户和当前环境上下文中", DatabaseType.PostgreSQL),
		SET_USER_PRIVILEGE("为用户授权特权", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    PrivilegeFunction(String title) {
        this(title, null, null);
    }
    PrivilegeFunction(String title, String params) {
        this(title, params, null);
    }
    PrivilegeFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    PrivilegeFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.PRIVILEGE; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}