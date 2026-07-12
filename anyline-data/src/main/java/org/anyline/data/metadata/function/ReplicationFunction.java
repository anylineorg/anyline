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
* REPLICATION 类函数定义
* 包含所有数据库的 REPLICATION 相关函数
*/
public enum ReplicationFunction implements META {
		ASYNCHRONOUS_CONNECTION_FAILOVER_ADD_MANAGED("复制通道添加组成员", "channel,host,port", DatabaseType.MySQL),
		ASYNCHRONOUS_CONNECTION_FAILOVER_ADD_SOURCE("复制通道添加源服务器", "channel,host,port", DatabaseType.MySQL),
		ASYNCHRONOUS_CONNECTION_FAILOVER_DELETE_MANAGED("复制通道删除组", "channel,host,port", DatabaseType.MySQL),
		ASYNCHRONOUS_CONNECTION_FAILOVER_DELETE_SOURCE("复制通道删除源", "channel,host,port", DatabaseType.MySQL),
		ASYNCHRONOUS_CONNECTION_FAILOVER_RESET("清除异步组故障转移", "channel", DatabaseType.MySQL),
		AVAILABLE_WAL_SUMMARIES("关数据目录中存在WAL摘要文件", DatabaseType.PostgreSQL),
		COPY_LOGICAL_REPLICATION_SLOT("复制逻辑槽", "slot_name", DatabaseType.PostgreSQL),
		COPY_PHYSICAL_REPLICATION_SLOT("复制物理槽", "slot_name", DatabaseType.PostgreSQL),
		CREATE_LOGICAL_REPLICATION_SLOT("创建逻辑复制槽", "slot_name,output_plugin", DatabaseType.PostgreSQL),
		CREATE_PHYSICAL_REPLICATION_SLOT("创建新物理复制槽", "slot_name", DatabaseType.PostgreSQL),
		CURRENT_SNAPSHOT("当前快照", DatabaseType.PostgreSQL),
		CURRENT_WAL_FLUSH_LSN("当前WAL日志刷新位置", DatabaseType.PostgreSQL),
		CURRENT_WAL_INSERT_LSN("当前WAL日志插入位置", DatabaseType.PostgreSQL),
		CURRENT_WAL_LSN("当前WAL日志写入位置", DatabaseType.PostgreSQL),
		CURRENT_XACT_ID("当前事务的 ID", DatabaseType.PostgreSQL),
		CURRENT_XACT_ID_IF_ASSIGNED("当前事务的 ID", DatabaseType.PostgreSQL),
		DROP_REPLICATION_SLOT("删除物理或逻辑复制槽", "slot_name", DatabaseType.PostgreSQL),
		EXPORT_SNAPSHOT("保存事务快照", DatabaseType.PostgreSQL),
		GET_WAL_REPLAY_PAUSE_STATE("恢复暂停状态", DatabaseType.PostgreSQL),
		GET_WAL_RESOURCE_MANAGERS("系统中当前加载WAL资源管理器", DatabaseType.PostgreSQL),
		GET_WAL_SUMMARIZER_STATE("WAL摘要器进度", DatabaseType.PostgreSQL),
		GROUP_REPLICATION_DISABLE_MEMBER_ACTION("禁用指定事件成员作", "action,stage", DatabaseType.MySQL),
		GROUP_REPLICATION_ENABLE_MEMBER_ACTION("为指定事件启用成员作", "action,stage", DatabaseType.MySQL),
		GROUP_REPLICATION_GET_COMMUNICATION_PROTOCOL("当前组复制通信协议版本", DatabaseType.MySQL),
		GROUP_REPLICATION_GET_WRITE_CONCURRENCY("当前为组设置最大共识实例数", DatabaseType.MySQL),
		GROUP_REPLICATION_RESET_MEMBER_ACTIONS("重置所有成员", DatabaseType.MySQL),
		GROUP_REPLICATION_SET_AS_PRIMARY("特定组成员设为主要组成员", "member", DatabaseType.MySQL),
		GROUP_REPLICATION_SET_COMMUNICATION_PROTOCOL("要使用组复制通信协议版本", "protocol", DatabaseType.MySQL),
		GROUP_REPLICATION_SET_WRITE_CONCURRENCY("设置可在 平行", "concurrency", DatabaseType.MySQL),
		GROUP_REPLICATION_SWITCH_TO_MULTI_PRIMARY_MODE("切换为多主模式", DatabaseType.MySQL),
		GROUP_REPLICATION_SWITCH_TO_SINGLE_PRIMARY_MODE("切换为单主模式", "member", DatabaseType.MySQL),
		GTID_SUBSET("减法", "subset"),
		GTID_SUBTRACT("减法", "set"),
		IS_WAL_REPLAY_PAUSED("请求恢复是否暂停", DatabaseType.PostgreSQL),
		LAST_WAL_RECEIVE_LSN("WAL最后接收位置", DatabaseType.PostgreSQL),
		LAST_WAL_REPLAY_LSN("WAL恢复位置", DatabaseType.PostgreSQL),
		LOGICAL_SLOT_GET_BINARY_CHANGES("大于等于判断", DatabaseType.PostgreSQL),
		LOGICAL_SLOT_GET_CHANGES("槽变更", DatabaseType.PostgreSQL),
		LOGICAL_SLOT_PEEK_BINARY_CHANGES("逻辑槽查看二进制", DatabaseType.PostgreSQL),
		LOGICAL_SLOT_PEEK_CHANGES("逻辑槽查看变更", DatabaseType.PostgreSQL),
		LS_WAL_DIR("WAL日志文件列表", DatabaseType.PostgreSQL),
		MASTER_POS_WAIT("等待副本同步", "master_log,relay_log", DatabaseType.MySQL),
		REPLICATE("重复字符串"),
		REPLICATION_ORIGIN_ADVANCE("设置复制进度", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_CREATE("创建复制源", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_DROP("删除以前创建复制源", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_OID("查找复制源", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_PROGRESS("给定复制源重放位置", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_SESSION_IS_SETUP("当前会话中选择了复制源", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_SESSION_PROGRESS("当前会话中选择复制源重放位置", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_SESSION_RESET("按位或", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_SESSION_SETUP("当前会话标记为从给定源重播", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_XACT_RESET("按位或", DatabaseType.PostgreSQL),
		REPLICATION_ORIGIN_XACT_SETUP("标记事务为已重播", DatabaseType.PostgreSQL),
		REPLICATION_SLOT_ADVANCE("前进复制槽当前确认位置", DatabaseType.PostgreSQL),
		SHOW_BINLOG_EVENTS("获得指定Binlog文件详细内容", DatabaseType.MySQL),
		SNAPSHOT_XIP("快照中包含一组正在进行事务ID", DatabaseType.PostgreSQL),
		SNAPSHOT_XMAX("快照的XMAX", DatabaseType.PostgreSQL),
		SNAPSHOT_XMIN("快照的XMIN", DatabaseType.PostgreSQL),
		SOURCE_POS_WAIT("等待副本同步", "master_log,relay_log,timeout", DatabaseType.MySQL),
		SPLIT_WAL_FILE_NAME("提取序列号和时间线ID", DatabaseType.PostgreSQL),
		SWITCH_WAL("强制服务器切换到新WAL日志文件", DatabaseType.PostgreSQL),
		SYNC_REPLICATION_SLOTS("复制槽故障转移同步", DatabaseType.PostgreSQL),
		TXID_SNAPSHOT_IN("输入转换", DatabaseType.PostgreSQL),
		TXID_SNAPSHOT_OUT("输出转换", DatabaseType.PostgreSQL),
		TXID_SNAPSHOT_RECV("二进制接收", DatabaseType.PostgreSQL),
		TXID_SNAPSHOT_SEND("二进制发送", DatabaseType.PostgreSQL),
		VISIBLE_IN_SNAPSHOT("事务ID是否可见", DatabaseType.PostgreSQL),
		WAIT_FOR_EXECUTED_GTID_SET("等待给定GTID在副本上执行", "gtid_set,timeout", DatabaseType.MySQL),
		WAL_FILE_NAME("WAL位置转文件名", DatabaseType.PostgreSQL),
		WAL_FILE_NAME_OFFSET("WAL位置转偏移", DatabaseType.PostgreSQL),
		WAL_LSN_DIFF("两个WAL日志位置之间字节差", DatabaseType.PostgreSQL),
		WAL_REPLAY_PAUSE("请求暂停恢复", DatabaseType.PostgreSQL),
		WAL_REPLAY_RESUME("重新启动恢复", DatabaseType.PostgreSQL),
		WAL_SUMMARY_CONTENTS("WAL摘要详情", DatabaseType.PostgreSQL),
		XACT_COMMIT_TIMESTAMP("事务的提交时间戳", DatabaseType.PostgreSQL),
		XACT_COMMIT_TIMESTAMP_ORIGIN("事务提交时间戳和复制源", DatabaseType.PostgreSQL),
		XACT_STATUS("最近事务提交状态", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    ReplicationFunction(String title) {
        this(title, null, null);
    }
    ReplicationFunction(String title, String params) {
        this(title, params, null);
    }
    ReplicationFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    ReplicationFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.REPLICATION; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}