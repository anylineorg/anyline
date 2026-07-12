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
* NETWORK 类函数定义
* 包含所有数据库的 NETWORK 相关函数
*/
public enum NetworkFunction implements META {
		ABBREV("缩写IP地址", "address"),
		BROADCAST("网络地址:broadcast", "address", DatabaseType.PostgreSQL),
		CIDEQ("CIDR相等", DatabaseType.PostgreSQL),
		CIDIN("CIDR输入", DatabaseType.PostgreSQL),
		CIDOUT("CIDR输出", DatabaseType.PostgreSQL),
		CIDR("CIDR地址", DatabaseType.PostgreSQL),
		CIDR_IN("输入转换", DatabaseType.PostgreSQL),
		CIDR_OUT("输出转换", DatabaseType.PostgreSQL),
		CIDR_RECV("二进制接收", DatabaseType.PostgreSQL),
		CIDR_SEND("二进制发送", DatabaseType.PostgreSQL),
		CIDRECV("CIDR地址接收", DatabaseType.PostgreSQL),
		CIDSEND("CIDR发送", DatabaseType.PostgreSQL),
		FAMILY("网络地址:family", "address", DatabaseType.PostgreSQL),
		HOST("提取主机地址部分", "address"),
		HOST_MASK("从IP地址中提取主机掩码", "inet"),
		HOSTMASK("网络地址:hostmask", "address", DatabaseType.PostgreSQL),
		INET_ATON("IPv4转数字", "ip"),
		INET_CLIENT_ADDR("当前客户端IP地址", DatabaseType.PostgreSQL),
		INET_CLIENT_PORT("当前客户端IP端口号", DatabaseType.PostgreSQL),
		INET_GIST_COMPRESS("索引压缩", DatabaseType.PostgreSQL),
		INET_GIST_CONSISTENT("索引一致性检查", DatabaseType.PostgreSQL),
		INET_GIST_FETCH("索引取回", DatabaseType.PostgreSQL),
		INET_GIST_PENALTY("索引代价惩罚", DatabaseType.PostgreSQL),
		INET_GIST_PICKSPLIT("索引分裂策略", DatabaseType.PostgreSQL),
		INET_GIST_SAME("索引相等性检查", DatabaseType.PostgreSQL),
		INET_IN("输入转换", DatabaseType.PostgreSQL),
		INET_MERGE("合并网络地址", "inet1", DatabaseType.PostgreSQL),
		INET_NTOA("数字转IPv4", "ip"),
		INET_OUT("输出转换", DatabaseType.PostgreSQL),
		INET_RECV("二进制接收", DatabaseType.PostgreSQL),
		INET_SAME_FAMILY("两个地址是否属于相同IP系列", "inet1", DatabaseType.PostgreSQL),
		INET_SEND("二进制发送", DatabaseType.PostgreSQL),
		INET_SERVER_ADDR("服务器接受当前连接IP地址", DatabaseType.PostgreSQL),
		INET_SERVER_PORT("服务器接受当前连接IP端口号", DatabaseType.PostgreSQL),
		INET_SPG_CHOOSE("索引节点选择", DatabaseType.PostgreSQL),
		INET_SPG_CONFIG("索引配置", DatabaseType.PostgreSQL),
		INET_SPG_INNER_CONSISTENT("索引一致性检查", DatabaseType.PostgreSQL),
		INET_SPG_LEAF_CONSISTENT("索引一致性检查", DatabaseType.PostgreSQL),
		INET_SPG_PICKSPLIT("索引分裂策略", DatabaseType.PostgreSQL),
		INETAND("网络地址按位与", DatabaseType.PostgreSQL),
		INETMI("网络地址减法", DatabaseType.PostgreSQL),
		INETMI_INT8("网络地址减整数", DatabaseType.PostgreSQL),
		INETNOT("网络地址按位非", DatabaseType.PostgreSQL),
		INETOR("网络地址按位或", DatabaseType.PostgreSQL),
		INETPL("网络地址加整数", DatabaseType.PostgreSQL),
		IP_ABBREV("缩写显示格式创建为文", DatabaseType.PostgreSQL),
		IP_BROADCAST("地址网络广播地址", DatabaseType.PostgreSQL),
		IP_HOST("IP地址文本", DatabaseType.PostgreSQL),
		IP_HOST_MASK("地址网络主机掩码", DatabaseType.PostgreSQL),
		IP_INET_MERGE("包含两个给定网络最小网络", DatabaseType.PostgreSQL),
		IP_INET_SAME_FAMILY("测试地址是否属于同一IP系列", DatabaseType.PostgreSQL),
		IP_MASK_LENGTH("网络掩码长度（以位为单位）", DatabaseType.PostgreSQL),
		IP_NETMASK("地址网络网络掩码", DatabaseType.PostgreSQL),
		IP_NETWORK("网络地址归零", DatabaseType.PostgreSQL),
		IP_SET_MASK_LENGTH("值网络掩码长度。地址部分不变", DatabaseType.PostgreSQL),
		IP_TEXT("IP地址及掩码", DatabaseType.PostgreSQL),
		IP_VERSION("IP版本", DatabaseType.PostgreSQL),
		MACADDR("MAC地址", DatabaseType.PostgreSQL),
		MACADDR_AND("按位与", DatabaseType.PostgreSQL),
		MACADDR_CMP("比较", DatabaseType.PostgreSQL),
		MACADDR_EQ("相等判断", DatabaseType.PostgreSQL),
		MACADDR_GE("大于等于判断", DatabaseType.PostgreSQL),
		MACADDR_GT("大于判断", DatabaseType.PostgreSQL),
		MACADDR_IN("输入转换", DatabaseType.PostgreSQL),
		MACADDR_LE("小于等于判断", DatabaseType.PostgreSQL),
		MACADDR_LT("小于判断", DatabaseType.PostgreSQL),
		MACADDR_NE("不等判断", DatabaseType.PostgreSQL),
		MACADDR_NOT("按位非", DatabaseType.PostgreSQL),
		MACADDR_OR("按位或", DatabaseType.PostgreSQL),
		MACADDR_OUT("输出转换", DatabaseType.PostgreSQL),
		MACADDR_RECV("二进制接收", DatabaseType.PostgreSQL),
		MACADDR_SEND("二进制发送", DatabaseType.PostgreSQL),
		MACADDR_SORTSUPPORT("排序支持", DatabaseType.PostgreSQL),
		MACADDR8("MAC地址8", DatabaseType.PostgreSQL),
		MACADDR8_AND("按位与", DatabaseType.PostgreSQL),
		MACADDR8_CMP("比较", DatabaseType.PostgreSQL),
		MACADDR8_EQ("相等判断", DatabaseType.PostgreSQL),
		MACADDR8_GE("大于等于判断", DatabaseType.PostgreSQL),
		MACADDR8_GT("大于判断", DatabaseType.PostgreSQL),
		MACADDR8_IN("输入转换", DatabaseType.PostgreSQL),
		MACADDR8_LE("小于等于判断", DatabaseType.PostgreSQL),
		MACADDR8_LT("小于判断", DatabaseType.PostgreSQL),
		MACADDR8_NE("不等判断", DatabaseType.PostgreSQL),
		MACADDR8_NOT("按位非", DatabaseType.PostgreSQL),
		MACADDR8_OR("按位或", DatabaseType.PostgreSQL),
		MACADDR8_OUT("输出转换", DatabaseType.PostgreSQL),
		MACADDR8_RECV("二进制接收", DatabaseType.PostgreSQL),
		MACADDR8_SEND("二进制发送", DatabaseType.PostgreSQL),
		MACADDR8_SET7BIT("设置值", DatabaseType.PostgreSQL),
		MASK_LEN("网络地址掩码长度(位数)", "inet"),
		MASKLEN("网络掩码长度", "address", DatabaseType.PostgreSQL),
		NET_MASK("网络地址网络掩码", "inet"),
		NET_NETWORK("网络地址网络部分", "inet"),
		NETMASK("网络地址:netmask", "address", DatabaseType.PostgreSQL),
		NETWORK("network操作符", DatabaseType.PostgreSQL),
		NETWORK_CMP("比较", DatabaseType.PostgreSQL),
		NETWORK_EQ("相等判断", DatabaseType.PostgreSQL),
		NETWORK_GE("大于等于判断", DatabaseType.PostgreSQL),
		NETWORK_GT("大于判断", DatabaseType.PostgreSQL),
		NETWORK_LARGER("取较大值", "address1", DatabaseType.PostgreSQL),
		NETWORK_LE("小于等于判断", DatabaseType.PostgreSQL),
		NETWORK_LT("小于判断", DatabaseType.PostgreSQL),
		NETWORK_NE("不等判断", DatabaseType.PostgreSQL),
		NETWORK_OVERLAP("重叠判断", DatabaseType.PostgreSQL),
		NETWORK_SMALLER("取较小值", "address1", DatabaseType.PostgreSQL),
		NETWORK_SORTSUPPORT("排序支持", DatabaseType.PostgreSQL),
		NETWORK_SUB("减法", DatabaseType.PostgreSQL),
		NETWORK_SUBEQ("减法", DatabaseType.PostgreSQL),
		NETWORK_SUBSET_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		NETWORK_SUP("network操作符", DatabaseType.PostgreSQL),
		NETWORK_SUPEQ("network操作符", DatabaseType.PostgreSQL),
		NETWORKJOINSEL("network操作符", DatabaseType.PostgreSQL),
		NETWORKSEL("network操作符", DatabaseType.PostgreSQL),
		SET_MASK_LEN("网络地址掩码长度", "inet"),
		SET_MASKLEN("设置网络掩码长度", "address", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    NetworkFunction(String title) {
        this(title, null, null);
    }
    NetworkFunction(String title, String params) {
        this(title, params, null);
    }
    NetworkFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    NetworkFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.NETWORK; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}