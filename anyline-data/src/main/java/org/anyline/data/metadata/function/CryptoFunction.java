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
* CRYPTO 类函数定义
* 包含所有数据库的 CRYPTO 相关函数
*/
public enum CryptoFunction implements META {
		AES_DECRYPT("AES解密", "expression,key"),
		AES_ENCRYPT("AES加密", "expression,key"),
		CRC32("CRC32校验", "expression", DatabaseType.MySQL),
		CRC32C("二进制字符串CRC-32C值", "expression", DatabaseType.PostgreSQL),
		DECRYPT_AES("AES解密(GBase)", "expression"),
		DECRYPT_BIN("二进制解密"),
		DECRYPT_CHAR("解密为字符串"),
		DECRYPT_TDES("TDES解密", "expression"),
		DM_HASH("哈希", "expression"),
		ENCRYPT("加密"),
		ENCRYPT_AES("AES加密", "expression"),
		ENCRYPT_TDES("TDES加密", "expression"),
		GEN_RANDOM_UUID("生成随机UUID", DatabaseType.PostgreSQL),
		HASH("给定表达式哈希值", "expression"),
		HASH_ACLITEM("ACL项哈希", DatabaseType.PostgreSQL),
		HASH_ACLITEM_EXTENDED("ACL项哈希扩展", DatabaseType.PostgreSQL),
		HASH_ARRAY("hash数组", DatabaseType.PostgreSQL),
		HASH_ARRAY_EXTENDED("hash数组extended", DatabaseType.PostgreSQL),
		HASH_MULTIRANGE("乘法", DatabaseType.PostgreSQL),
		HASH_MULTIRANGE_EXTENDED("乘法", DatabaseType.PostgreSQL),
		HASH_NUMERIC("高精度哈希", DatabaseType.PostgreSQL),
		HASH_NUMERIC_EXTENDED("高精度哈希", DatabaseType.PostgreSQL),
		HASH_RANGE("hash范围", DatabaseType.PostgreSQL),
		HASH_RANGE_EXTENDED("hash范围extended", DatabaseType.PostgreSQL),
		HASH_RECORD("记录哈希", DatabaseType.PostgreSQL),
		HASH_RECORD_EXTENDED("记录哈希扩展", DatabaseType.PostgreSQL),
		HASHBPCHAR("定长字符哈希", DatabaseType.PostgreSQL),
		HASHBPCHAREXTENDED("定长字符哈希", DatabaseType.PostgreSQL),
		HASHCHAR("字符哈希", DatabaseType.PostgreSQL),
		HASHCHAREXTENDED("字符哈希", DatabaseType.PostgreSQL),
		HASHED_VALUE("计算哈希值"),
		HASHENUM("枚举哈希", DatabaseType.PostgreSQL),
		HASHENUMEXTENDED("枚举哈希扩展", DatabaseType.PostgreSQL),
		HASHFLOAT4("单精度哈希", DatabaseType.PostgreSQL),
		HASHFLOAT4EXTENDED("单精度哈希", DatabaseType.PostgreSQL),
		HASHFLOAT8("双精度哈希", DatabaseType.PostgreSQL),
		HASHFLOAT8EXTENDED("双精度哈希", DatabaseType.PostgreSQL),
		HASHHANDLER("hash索引处理器", DatabaseType.PostgreSQL),
		HASHINET("网络地址哈希", DatabaseType.PostgreSQL),
		HASHINETEXTENDED("网络地址哈希", DatabaseType.PostgreSQL),
		HASHINT2("双字节哈希", DatabaseType.PostgreSQL),
		HASHINT2EXTENDED("双字节哈希", DatabaseType.PostgreSQL),
		HASHINT4("四字节哈希", DatabaseType.PostgreSQL),
		HASHINT4EXTENDED("四字节哈希", DatabaseType.PostgreSQL),
		HASHINT8("八字节哈希", DatabaseType.PostgreSQL),
		HASHINT8EXTENDED("八字节哈希", DatabaseType.PostgreSQL),
		HASHMACADDR("MAC地址哈希", DatabaseType.PostgreSQL),
		HASHMACADDR8("MAC地址8哈希", DatabaseType.PostgreSQL),
		HASHMACADDR8EXTENDED("MAC地址8哈希扩展", DatabaseType.PostgreSQL),
		HASHMACADDREXTENDED("MAC地址哈希扩展", DatabaseType.PostgreSQL),
		HASHNAME("名称哈希", DatabaseType.PostgreSQL),
		HASHNAMEEXTENDED("名称哈希扩展", DatabaseType.PostgreSQL),
		HASHOID("OID哈希", DatabaseType.PostgreSQL),
		HASHOIDEXTENDED("OID哈希扩展", DatabaseType.PostgreSQL),
		HASHOIDVECTOR("OID向量哈希", DatabaseType.PostgreSQL),
		HASHOIDVECTOREXTENDED("OID向量哈希扩展", DatabaseType.PostgreSQL),
		HASHTEXT("文本哈希", DatabaseType.PostgreSQL),
		HASHTEXTEXTENDED("文本哈希", DatabaseType.PostgreSQL),
		HASHTID("TID哈希", DatabaseType.PostgreSQL),
		HASHTIDEXTENDED("TID哈希扩展", DatabaseType.PostgreSQL),
		HASHVARLENA("变长哈希", DatabaseType.PostgreSQL),
		HASHVARLENAEXTENDED("变长哈希扩展", DatabaseType.PostgreSQL),
		INTERVAL_HASH("哈希", DatabaseType.PostgreSQL),
		INTERVAL_HASH_EXTENDED("哈希", DatabaseType.PostgreSQL),
		JSONB_HASH("哈希", DatabaseType.PostgreSQL),
		JSONB_HASH_EXTENDED("哈希", DatabaseType.PostgreSQL),
		MD5("MD5哈希", "expression"),
		NUMERIC_AVG_DESERIALIZE("反序列化", DatabaseType.PostgreSQL),
		NUMERIC_DESERIALIZE("反序列化", DatabaseType.PostgreSQL),
		NUMERIC_POLY_DESERIALIZE("反序列化", DatabaseType.PostgreSQL),
		ORA_HASH("Oracle哈希", "expression"),
		PG_LSN_HASH("哈希", DatabaseType.PostgreSQL),
		PG_LSN_HASH_EXTENDED("哈希", DatabaseType.PostgreSQL),
		SHA1("SHA1哈希", "expression"),
		SHA2("SHA2哈希", "expression,hash_length"),
		SHA224("二进制字符串SHA-224哈希值"),
		SHA256("二进制字符串SHA-256哈希值"),
		SHA384("二进制字符串SHA-384哈希值"),
		SHA512("二进制字符串SHA-512哈希值"),
		SHOBJ_DESCRIPTION("共享对象描述", DatabaseType.PostgreSQL),
		STANDARD_HASH("给定表达式哈希值", "expression", DatabaseType.Oracle),
		TIME_HASH("哈希", DatabaseType.PostgreSQL),
		TIME_HASH_EXTENDED("哈希", DatabaseType.PostgreSQL),
		TIMESTAMP_HASH("哈希", DatabaseType.PostgreSQL),
		TIMESTAMP_HASH_EXTENDED("哈希", DatabaseType.PostgreSQL),
		TIMETZ_HASH("哈希", DatabaseType.PostgreSQL),
		TIMETZ_HASH_EXTENDED("哈希", DatabaseType.PostgreSQL),
		UUID_HASH("哈希", DatabaseType.PostgreSQL),
		UUID_HASH_EXTENDED("哈希", DatabaseType.PostgreSQL)		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    CryptoFunction(String title) {
        this(title, null, null);
    }
    CryptoFunction(String title, String params) {
        this(title, params, null);
    }
    CryptoFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    CryptoFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.CRYPTO; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}