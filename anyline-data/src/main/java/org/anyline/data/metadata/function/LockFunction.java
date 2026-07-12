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
* LOCK 类函数定义
* 包含所有数据库的 LOCK 相关函数
*/
public enum LockFunction implements META {
		ADVISORY_LOCK("独占会话级咨询锁", "lockid", DatabaseType.PostgreSQL),
		ADVISORY_LOCK_SHARED("共享会话级咨询锁", "lockid", DatabaseType.PostgreSQL),
		ADVISORY_UNLOCK("释放独占咨询锁", "lockid", DatabaseType.PostgreSQL),
		ADVISORY_UNLOCK_ALL("释放所有会话锁", DatabaseType.PostgreSQL),
		ADVISORY_UNLOCK_SHARED("释放共享咨询锁", "lockid", DatabaseType.PostgreSQL),
		ADVISORY_XACT_LOCK("获得专属交易级咨询锁", "lockid", DatabaseType.PostgreSQL),
		ADVISORY_XACT_LOCK_SHARED("获得共享交易级咨询锁", "lockid", DatabaseType.PostgreSQL),
		BLOCKING_PIDS("阻止进程获取锁列表", DatabaseType.PostgreSQL),
		GET_LOCK("获取命名锁", "name,timeout"),
		IS_FREE_LOCK("命名锁是否空闲", "name", DatabaseType.MySQL),
		IS_USED_LOCK("命名锁是否正在使用中", "name", DatabaseType.MySQL),
		RELEASE_ALL_LOCKS("释放所有当前命名锁", DatabaseType.MySQL),
		RELEASE_LOCK("释放命名锁", "lock"),
		SAFE_SNAPSHOT_BLOCKING_PIDS("阻止进程获取安全快照列表", DatabaseType.PostgreSQL),
		TRY_ADVISORY_LOCK("获得独占会话级咨询锁", "key", DatabaseType.PostgreSQL),
		TRY_ADVISORY_LOCK_SHARED("共享会话级咨询锁", "key", DatabaseType.PostgreSQL),
		TRY_ADVISORY_XACT_LOCK("获得专属交易级咨询锁", DatabaseType.PostgreSQL),
		TRY_ADVISORY_XACT_LOCK_SHARED("共享事务级咨询锁", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    LockFunction(String title) {
        this(title, null, null);
    }
    LockFunction(String title, String params) {
        this(title, params, null);
    }
    LockFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    LockFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.LOCK; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }
    
    
    
}