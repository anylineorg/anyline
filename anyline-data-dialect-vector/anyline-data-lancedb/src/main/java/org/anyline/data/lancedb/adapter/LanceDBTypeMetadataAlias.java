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


package org.anyline.data.lancedb.adapter;

import org.anyline.metadata.type.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum LanceDBTypeMetadataAlias implements TypeMetadataAlias {
	INT                       ("INT"                       ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BIGINT                    ("BIGINT"                    ,StandardTypeMetadata.BIGINT                 , "BigInt"           , "BigInt"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	SMALLINT                  ("SMALLINT"                  ,StandardTypeMetadata.SMALLINT               , "SmallInt"         , "SmallInt"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TINYINT                   ("TINYINT"                   ,StandardTypeMetadata.TINYINT                , "TinyInt"          , "TinyInt"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	FLOAT                     ("FLOAT"                     ,StandardTypeMetadata.FLOAT                  , "Float"            , "Float"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DOUBLE                    ("DOUBLE"                    ,StandardTypeMetadata.DOUBLE                 , "Double"           , "Double"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BOOLEAN                   ("BOOLEAN"                   ,StandardTypeMetadata.BOOLEAN                , "Bool"             , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	VARCHAR                   ("VARCHAR"                   ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TEXT                      ("TEXT"                      ,StandardTypeMetadata.TEXT                   , "LargeUtf8"        , "LargeUtf8"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BINARY                    ("BINARY"                    ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BLOB                      ("BLOB"                      ,StandardTypeMetadata.BLOB                   , "LargeBinary"      , "LargeBinary"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATE                      ("DATE"                      ,StandardTypeMetadata.DATE                   , "Date32"           , "Date32"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIMESTAMP                 ("TIMESTAMP"                 ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	ARRAY                     ("ARRAY"                     ,StandardTypeMetadata.ARRAY                  , "List"             , "List({P})"              , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1,  -1,  -1),
	JSON                      ("JSON"                      ,StandardTypeMetadata.JSON                   , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),

	FLOAT_VECTOR              ("FLOAT_VECTOR"              ,StandardTypeMetadata.FLOAT_VECTOR           , "FixedSizeList"    , "FixedSizeList({P})"      , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1,  -1,  -1),
	FLOAT16_VECTOR            ("FLOAT16_VECTOR"            ,StandardTypeMetadata.FLOAT16_VECTOR         , "FixedSizeList"    , "FixedSizeList({P})"      , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1,  -1,  -1),
	BFLOAT16_VECTOR           ("BFLOAT16_VECTOR"           ,StandardTypeMetadata.BFLOAT16_VECTOR        , "FixedSizeList"    , "FixedSizeList({P})"      , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1,  -1,  -1),
	BINARY_VECTOR             ("BINARY_VECTOR"             ,StandardTypeMetadata.BINARY_VECTOR          , "FixedSizeList"    , "FixedSizeList({P})"      , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1,  -1,  -1),
	SPARSE_FLOAT_VECTOR       ("SPARSE_FLOAT_VECTOR"       ,StandardTypeMetadata.SPARSE_FLOAT_VECTOR    , "NONE"             ,                           -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),

	ACLITEM                   ("ACLITEM"                   ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	AGG_STATE                 ("AGG_STATE"                 ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	AGGREGATE_METRIC_DOUBLE   ("aggregate_metric_double"   ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	ALIAS                     ("alias"                     ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	BOX                       ("BOX"                       ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	BOX2D                     ("BOX2D"                     ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	CID                       ("CID"                       ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	CIDR                      ("CIDR"                      ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	CIRCLE                    ("CIRCLE"                    ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	CURSOR                    ("CURSOR"                    ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOMETRY                  ("GEOMETRY"                  ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL                  ("INTERVAL"                  ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	ROWID                     ("ROWID"                     ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	SQL_VARIANT               ("SQL_VARIANT"               ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),

	INT1                      ("INT1"                      ,StandardTypeMetadata.TINYINT                , "TinyInt"          , "TinyInt"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	INT2                      ("INT2"                      ,StandardTypeMetadata.SMALLINT               , "SmallInt"         , "SmallInt"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	INT4                      ("INT4"                      ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	INT8                      ("INT8"                      ,StandardTypeMetadata.BIGINT                 , "BigInt"           , "BigInt"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	INT16                     ("INT16"                     ,StandardTypeMetadata.SMALLINT               , "SmallInt"         , "SmallInt"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	INT32                     ("INT32"                     ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	INT64                     ("INT64"                     ,StandardTypeMetadata.BIGINT                 , "BigInt"           , "BigInt"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	INTEGER                   ("INTEGER"                   ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	MEDIUMINT                 ("MEDIUMINT"                 ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	SIGNED                    ("SIGNED"                    ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	UNSIGNED                  ("UNSIGNED"                  ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	SERIAL                    ("SERIAL"                    ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BIGSERIAL                 ("BIGSERIAL"                 ,StandardTypeMetadata.BIGINT                 , "BigInt"           , "BigInt"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	UINT8                     ("UInt8"                     ,StandardTypeMetadata.TINYINT                , "TinyInt"          , "TinyInt"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	UINT16                    ("UInt16"                    ,StandardTypeMetadata.SMALLINT               , "SmallInt"         , "SmallInt"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	UINT32                    ("UInt32"                    ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	UINT64                    ("UInt64"                    ,StandardTypeMetadata.BIGINT                 , "BigInt"           , "BigInt"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	LONG                      ("LONG"                      ,StandardTypeMetadata.BIGINT                 , "BigInt"           , "BigInt"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BLONG                     ("BLONG"                     ,StandardTypeMetadata.BIGINT                 , "BigInt"           , "BigInt"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	LONG_RAW                  ("LONG RAW"                  ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),

	FLOAT4                    ("FLOAT4"                    ,StandardTypeMetadata.FLOAT                  , "Float"            , "Float"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	FLOAT8                    ("FLOAT8"                    ,StandardTypeMetadata.DOUBLE                 , "Double"           , "Double"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	FLOAT32                   ("FLOAT32"                   ,StandardTypeMetadata.FLOAT                  , "Float"            , "Float"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	FLOAT64                   ("FLOAT64"                   ,StandardTypeMetadata.DOUBLE                 , "Double"           , "Double"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	REAL                      ("REAL"                      ,StandardTypeMetadata.FLOAT                  , "Float"            , "Float"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DOUBLE_PRECISION          ("DOUBLE PRECISION"          ,StandardTypeMetadata.DOUBLE                 , "Double"           , "Double"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BINARY_FLOAT              ("BINARY_FLOAT"              ,StandardTypeMetadata.FLOAT                  , "Float"            , "Float"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BINARY_DOUBLE             ("BINARY_DOUBLE"             ,StandardTypeMetadata.DOUBLE                 , "Double"           , "Double"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BINARY_INTEGER            ("BINARY_INTEGER"            ,StandardTypeMetadata.INT                    , "Int"              , "Int"                    , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DECIMAL128                ("Decimal128"                ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),

	DEC                       ("DEC"                       ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	DECIMAL                   ("DECIMAL"                   ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	NUMERIC                   ("NUMERIC"                   ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	NUMBER                    ("NUMBER"                    ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	DECIMAL_V3                ("DecimalV3"                 ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	DECIMAL32                 ("Decimal32"                 ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	DECIMAL64                 ("Decimal64"                 ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	DECIMAL256                ("Decimal256"                ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	DECFLOAT                  ("DECFLOAT"                  ,StandardTypeMetadata.FLOAT                  , "Float"            , "Float"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	MONEY                     ("MONEY"                     ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),
	SMALLMONEY                ("SMALLMONEY"                ,StandardTypeMetadata.DECIMAL                ,                          1,   2,   2,  -1,  -1,  -1,  -1,  -1),

	CHAR                      ("CHAR"                      ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	NCHAR                     ("NCHAR"                     ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	NVARCHAR                  ("NVARCHAR"                  ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	NVARCHAR2                 ("NVARCHAR2"                 ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	VARCHAR2                  ("VARCHAR2"                  ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	STRING                    ("String"                    ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BPCHAR                    ("BPCHAR"                    ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BPCHARBYTE                ("BPCHARBYTE"                ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	CHARACTER                 ("CHARACTER"                 ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	CHARACTER_VARYING         ("CHARACTER VARYING"         ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	CLOB                      ("CLOB"                      ,StandardTypeMetadata.TEXT                   , "LargeUtf8"        , "LargeUtf8"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	NCLOB                     ("NCLOB"                     ,StandardTypeMetadata.TEXT                   , "LargeUtf8"        , "LargeUtf8"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	LONGTEXT                  ("LONGTEXT"                  ,StandardTypeMetadata.TEXT                   , "LargeUtf8"        , "LargeUtf8"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	MEDIUMTEXT                ("MEDIUMTEXT"                ,StandardTypeMetadata.TEXT                   , "LargeUtf8"        , "LargeUtf8"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TINYTEXT                  ("TINYTEXT"                  ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	NTEXT                     ("NTEXT"                     ,StandardTypeMetadata.TEXT                   , "LargeUtf8"        , "LargeUtf8"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	FIXED_STRING              ("FixedString"               ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	UUID                      ("UUID"                      ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	UNIQUEIDENTIFIER          ("UNIQUEIDENTIFIER"          ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	ENUM                      ("ENUM"                      ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),

	VARBINARY                 ("VARBINARY"                 ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	RAW                       ("RAW"                       ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	LONGBLOB                  ("LONGBLOB"                  ,StandardTypeMetadata.BLOB                   , "LargeBinary"      , "LargeBinary"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	MEDIUMBLOB                ("MEDIUMBLOB"                ,StandardTypeMetadata.BLOB                   , "LargeBinary"      , "LargeBinary"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TINYBLOB                  ("TINYBLOB"                  ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BYTEA                     ("BYTEA"                     ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BYTE                      ("BYTE"                      ,StandardTypeMetadata.TINYINT                , "TinyInt"          , "TinyInt"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	IMAGE                     ("IMAGE"                     ,StandardTypeMetadata.BLOB                   , "LargeBinary"      , "LargeBinary"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),

	BOOL                      ("BOOL"                      ,StandardTypeMetadata.BOOLEAN                , "Bool"             , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BIT                       ("BIT"                       ,StandardTypeMetadata.BOOLEAN                , "Bool"             , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BIT_VARYING               ("BIT VARYING"               ,StandardTypeMetadata.BOOLEAN                , "Bool"             , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	ISNOTNULL                 ("IS NOT NULL"               ,StandardTypeMetadata.BOOLEAN                , "Bool"             , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),

	DATETIME                  ("DATETIME"                  ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATETIME2                 ("DATETIME2"                 ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATETIMEOFFSET            ("DATETIMEOFFSET"            ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	SMALLDATETIME             ("SMALLDATETIME"             ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATE32                    ("Date32"                    ,StandardTypeMetadata.DATE                   , "Date32"           , "Date32"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATETIME64                ("DateTime64"                ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIME                      ("TIME"                      ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIME_NANOS                ("time_nanos"                ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATE_NANOS                ("date_nanos"                ,StandardTypeMetadata.DATE                   , "Date32"           , "Date32"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIME_WITH_TIME_ZONE       ("TIME WITH TIME ZONE"       ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIMESTAMP_WITH_TIME_ZONE  ("TIMESTAMP WITH TIME ZONE"  ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIMESTAMP_WITH_LOCAL_TZ   ("TIMESTAMP WITH LOCAL TZ"   ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIMESTAMPTZ               ("TIMESTAMPTZ"               ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIMESTAMP_LTZ             ("TIMESTAMP_LTZ"             ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	TIMESTAMP_NTZ             ("TIMESTAMP_NTZ"             ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	YEAR                      ("YEAR"                      ,StandardTypeMetadata.SMALLINT               , "SmallInt"         , "SmallInt"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATETIMEV2                ("DATETIMEV2"                ,StandardTypeMetadata.TIMESTAMP              , "Timestamp"        , "Timestamp({S})"         , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATEV2                    ("DATEV2"                    ,StandardTypeMetadata.DATE                   , "Date32"           , "Date32"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DATERANGE                 ("DATERANGE"                 ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DSINTERVAL                ("DSINTERVAL"                ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	DURATION                  ("DURATION"                  ,StandardTypeMetadata.BIGINT                 , "BigInt"           , "BigInt"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),

	XML                       ("XML"                       ,StandardTypeMetadata.TEXT                   , "LargeUtf8"        , "LargeUtf8"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),

	GEO_POINT                 ("geo_point"                 ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	GEO_SHAPE                 ("geo_shape"                 ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOGRAPHY                 ("GEOGRAPHY"                 ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOGRAPHY_POINT           ("GEOGRAPHY_POINT"           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	LINESTRING                ("LINESTRING"                ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	LSEG                      ("LSEG"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	MACADDR                   ("MACADDR"                   ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	MACADDR8                  ("MACADDR8"                  ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	MULTILINESTRING           ("MULTILINESTRING"           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	MULTIPOINT                ("MULTIPOINT"                ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	MULTIPOLYGON              ("MULTIPOLYGON"              ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	PATH                      ("PATH"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	POINT                     ("POINT"                     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	POLYGON                   ("POLYGON"                   ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	TSQUERY                   ("TSQUERY"                   ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	TSVECTOR                  ("TSVECTOR"                  ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	TXID_SNAPSHOT             ("TXID_SNAPSHOT"             ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	XID                       ("XID"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),

	COMPLETION                ("completion"                ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	DENSE_VECTOR              ("dense_vector"              ,StandardTypeMetadata.FLOAT_VECTOR           , "FixedSizeList"    , "FixedSizeList({P})"      , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1,  -1,  -1),
	DENSE_VECTOR_BIGINT       ("dense_vector_bigint"       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	DENSE_VECTOR_SMALLINT     ("dense_vector_smallint"     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	FLATTENED                 ("flattened"                 ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	HISTOGRAM                 ("histogram"                 ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	IP                        ("ip"                        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	KEYWORD                   ("keyword"                   ,StandardTypeMetadata.VARCHAR                , "Utf8"             , "Utf8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	NESTED                    ("nested"                    ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	RANK_FEATURE              ("rank_feature"              ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	SCALED_FLOAT              ("scaled_float"              ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	SEARCH_AS_YOU_TYPE        ("search_as_you_type"        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	SPARSE_VECTOR             ("sparse_vector"             ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	TOKEN_COUNT               ("token_count"               ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	WILDCARD                  ("wildcard"                  ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),

	BFILE                     ("BFILE"                     ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),
	BITMAP                    ("BITMAP"                    ,StandardTypeMetadata.BINARY                 , "Binary"           , "Binary"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1,  -1,  -1),

	MAP                       ("MAP"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	OBJECT                    ("Object"                    ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	TUPLE                     ("TUPLE"                     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	VARIANT                   ("VARIANT"                   ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1),
	;

	private String input                     ;
	private final TypeMetadata standard      ;
	private String meta                      ;
	private String formula                   ;
	private int ignoreLength            = -1 ;
	private int ignorePrecision         = -1 ;
	private int ignoreScale             = -1 ;
	private int supportTimeZone         = -1 ;
	private int supportLocalTimeZone    = -1 ;
	private int maxLength               = -1 ;
	private int maxPrecision            = -1 ;
	private int maxScale                = -1 ;
	private String lengthRefer               ;
	private String precisionRefer            ;
	private String scaleRefer                ;
	private TypeMetadata.Refer refer         ;

	LanceDBTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale, int supportTimeZone, int supportLocalTimeZone) {
		this.input = input;
		this.standard = standard;
		this.meta = meta;
		this.formula = formula;
		this.lengthRefer = lengthRefer;
		this.precisionRefer = precisionRefer;
		this.scaleRefer = scaleRefer;
		this.ignoreLength = ignoreLength;
		this.ignorePrecision = ignorePrecision;
		this.ignoreScale = ignoreScale;
		this.supportTimeZone = supportTimeZone;
		this.supportLocalTimeZone = supportLocalTimeZone;
		this.maxLength = maxLength;
		this.maxPrecision = maxPrecision;
		this.maxScale = maxScale;
	}

	LanceDBTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
		this(input, standard, meta, formula, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale, maxLength, maxPrecision, maxScale, -1, -1);
	}

	LanceDBTypeMetadataAlias(String input, TypeMetadata standard, String meta, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale, int supportTimeZone, int supportLocalTimeZone) {
		this(input, standard, meta, null, null, null, null, ignoreLength, ignorePrecision, ignoreScale, maxLength, maxPrecision, maxScale, supportTimeZone, supportLocalTimeZone);
	}

	LanceDBTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale, int supportTimeZone, int supportLocalTimeZone) {
		this(input, standard, null, null, null, null, null,  ignoreLength, ignorePrecision, ignoreScale, maxLength, maxPrecision, maxScale, supportTimeZone, supportLocalTimeZone);
	}

	LanceDBTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
		this(input, standard, null, null, null, null, null,  ignoreLength, ignorePrecision, ignoreScale, maxLength, maxPrecision, maxScale);
	}

	LanceDBTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale, -1, -1, -1);
	}

	LanceDBTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null, null, null, null, null, ignoreLength, ignorePrecision, ignoreScale, -1, -1, -1);
	}

	LanceDBTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(null, standard, null, null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale, -1, -1, -1);
	}

	LanceDBTypeMetadataAlias(String input, TypeMetadata standard) {
		this.input = input;
		this.standard = standard;
	}

	LanceDBTypeMetadataAlias(TypeMetadata standard) {
		this.standard = standard;
	}

	@Override
	public String input() {
		if(null == input) {
			input = name();
		}
		return input;
	}

	@Override
	public TypeMetadata standard() {
		return standard;
	}

	@Override
	public TypeMetadata.Refer refer() {
		if(null == refer) {
			refer = new TypeMetadata.Refer();
			if(null != meta) {
				refer.setMeta(meta);
			}
			if(null != formula) {
				refer.setFormula(formula);
			}
			if(null != lengthRefer) {
				refer.setLengthRefer(lengthRefer);
			}
			if(null != precisionRefer) {
				refer.setPrecisionRefer(precisionRefer);
			}
			if(null != scaleRefer) {
				refer.setScaleRefer(scaleRefer);
			}
			if(-1 != ignoreLength) {
				refer.ignoreLength(ignoreLength);
			}
			if(-1 != ignorePrecision) {
				refer.ignorePrecision(ignorePrecision);
			}
			if(-1 != ignoreScale) {
				refer.ignoreScale(ignoreScale);
			}
			if(-1 != supportTimeZone) {
				refer.supportTimeZone(supportTimeZone);
			}
			if(-1 != supportLocalTimeZone) {
				refer.supportLocalTimeZone(supportLocalTimeZone);
			}
			if(-1 != maxLength) {
				refer.maxLength(maxLength);
			}
			if(-1 != maxPrecision) {
				refer.maxPrecision(maxPrecision);
			}
			if(-1 != maxScale) {
				refer.maxScale(maxScale);
			}
		}
		return refer;
	}

	public String metadata() {
		return meta;
	}

	public String formula() {
		return formula;
	}

	public int ignoreLength() {
		return ignoreLength;
	}

	public int ignorePrecision() {
		return ignorePrecision;
	}

	public int ignoreScale() {
		return ignoreScale;
	}

	public int supportTimeZone() {
		return supportTimeZone;
	}

	public int supportLocalTimeZone() {
		return supportLocalTimeZone;
	}

	public int maxLength() {
		return maxLength;
	}

	public int maxPrecision() {
		return maxPrecision;
	}

	public int maxScale() {
		return maxScale;
	}
}