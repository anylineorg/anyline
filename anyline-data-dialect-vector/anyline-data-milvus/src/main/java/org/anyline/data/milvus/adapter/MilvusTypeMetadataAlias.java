/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.milvus.adapter;

import org.anyline.metadata.type.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum MilvusTypeMetadataAlias implements TypeMetadataAlias {
	ARRAY                         ("Array"                          ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	BFLOAT16_VECTOR               ("BFloat16Vector"                 ,StandardTypeMetadata.BFLOAT16_VECTOR       , "BFloat16Vector"    , "BFloat16Vector({P})"    , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1),
	BINARY_VECTOR                 ("BinaryVector"                   ,StandardTypeMetadata.BINARY_VECTOR         , "BinaryVector"      , "BinaryVector({p})"      , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1),
	DOUBLE                        ("DOUBLE"                         ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	FLOAT                         ("FLOAT"                          ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	INT16                         ("INT16"                          ,StandardTypeMetadata.INT16                 , "INT16"             , "INT16"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT32                         ("Int32"                          ,StandardTypeMetadata.INT32                 , "INT32"             , "INT32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT64                         ("Int64"                          ,StandardTypeMetadata.INT64                 , "INT64"             , "INT64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT8                          ("INT8"                           ,StandardTypeMetadata.INT8                  , "INT8"              , "INT8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	JSON                          ("JSON"                           ,StandardTypeMetadata.JSON                  , "JSON"              , "JSON"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SPARSE_FLOAT_VECTOR           ("SparseFloatVector"              ,StandardTypeMetadata.SPARSE_FLOAT_VECTOR   , "SparseFloatVector" , "SparseFloatVector({P})" , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1),
	ACLITEM                       ("ACLITEM"                        ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	AGG_STATE                     ("AGG_STATE"                      ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	AGGREGATE_METRIC_DOUBLE       ("aggregate_metric_double"        ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	ALIAS                         ("alias"                          ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	BFILE                         ("BFILE"                          ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	BIGINT                        ("BIGINT"                         ,StandardTypeMetadata.BIGINT                , "INT64"             , "INT64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BIGSERIAL                     ("BIGSERIAL"                      ,StandardTypeMetadata.BIGINT                , "Int64"             , "Int64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BINARY                        ("BINARY"                         ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	BINARY_DOUBLE                 ("BINARY_DOUBLE"                  ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	BINARY_FLOAT                  ("BINARY_FLOAT"                   ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	BINARY_INTEGER                ("BINARY_INTEGER"                 ,StandardTypeMetadata.INT2                  ,   1,   1,   1,  -1,  -1,  -1),
	BIT                           ("BIT"                            ,StandardTypeMetadata.BOOLEAN               , "Bool"              , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BIT_VARYING                   ("BIT VARYING"                    ,StandardTypeMetadata.BOOLEAN               , "Bool"              , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BITMAP                        ("BITMAP"                         ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	BLOB                          ("BLOB"                           ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	BOOL                          ("Bool"                           ,StandardTypeMetadata.BOOLEAN               , "Bool"              , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BOOLEAN                       ("BOOLEAN"                        ,StandardTypeMetadata.BOOLEAN               , "Bool"              , "Bool"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BOX                           ("BOX"                            ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	BOX2D                         ("BOX2D"                          ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	BPCHAR                        ("BPCHAR"                         ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	BPCHARBYTE                    ("BPCHARBYTE"                     ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	BYTE                          ("BYTE"                           ,StandardTypeMetadata.INT2                  ,   1,   1,   1,  -1,  -1,  -1),
	BYTEA                         ("BYTEA"                          ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	CHAR                          ("CHAR"                           ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	CHARACTER                     ("CHARACTER"                      ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	CHARACTER_VARYING             ("CHARACTER VARYING"              ,StandardTypeMetadata.VARCHAR               , "VARCHAR"           , "VARCHAR({L})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	CID                           ("CID"                            ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	CIDR                          ("CIDR"                           ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	CIRCLE                        ("CIRCLE"                         ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	CLOB                          ("CLOB"                           ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	COMPLETION                    ("completion"                     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	CURSOR                        ("CURSOR"                         ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	DATE                          ("Date"                           ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DATE_NANOS                    ("date_nanos"                     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	DATE32                        ("Date32"                         ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DATERANGE                     ("DATERANGE"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	DATETIME64                    ("DateTime"                       ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DATETIME                      ("DateTime"                       ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DATETIME_WITH_TIME_ZONE       ("DATETIME WITH TIME ZONE"        ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DATETIME2                     ("DATETIME2"                      ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DATETIMEOFFSET                ("DATETIMEOFFSET"                 ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DATETIMEV2                    ("DATETIMEV2"                     ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DATEV2                        ("DATEV2"                         ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	DEC                           ("DEC"                            ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	DECFLOAT                      ("DECFLOAT"                       ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	DECIMAL                       ("Decimal"                        ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECIMAL128                    ("Decimal128"                     ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECIMAL256                    ("Decimal256"                     ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECIMAL32                     ("Decimal32"                      ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECIMAL64                     ("Decimal64"                      ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DecimalV3                     ("DecimalV3"                      ,StandardTypeMetadata.DECIMAL               ,   1,   2,   2,  -1,  -1,  -1),
	DENSE_VECTOR                  ("dense_vector"                   ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	DOUBLE_PRECISION              ("DOUBLE PRECISION"               ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DSINTERVAL                    ("DSINTERVAL"                     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	DURATION                      ("DURATION"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	ENUM                          ("ENUM"                           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	FIXED_STRING                  ("FIXED_STRING"                   ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	FixedString                   ("FixedString"                    ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	FLATTENED                     ("flattened"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	FLOAT16_VECTOR                ("Float16Vector"                  ,StandardTypeMetadata.FLOAT16_VECTOR        , "Float16Vector"     , "Float16Vector({P})"     , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1),
	FLOAT32                       ("Float32"                        ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	FLOAT4                        ("FLOAT4"                         ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	FLOAT64                       ("Float64"                        ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	FLOAT8                        ("FLOAT8"                         ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	FLOAT_VECTOR                  ("FloatVector"                    ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEO_POINT                     ("geo_point"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEO_SHAPE                     ("geo_shape"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOGRAPHY                     ("GEOGRAPHY"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOGRAPHY_POINT               ("GEOGRAPHY_POINT"                ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOMETRY                      ("GEOMETRY"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOMETRYCOLLECTION            ("GEOMETRYCOLLECTION"             ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	GTSVECTOR                     ("GTSVECTOR"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	GUID                          ("GUID"                           ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	HALF_FLOAT                    ("half_float"                     ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	HIERARCHYID                   ("HIERARCHYID"                    ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	HISTOGRAM                     ("histogram"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	HLL                           ("HLL"                            ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	IMAGE                         ("IMAGE"                          ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	INET                          ("INET"                           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INT                           ("int"                            ,StandardTypeMetadata.INT32                 , "INT32"             , "INT32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT128                        ("Int128"                         ,StandardTypeMetadata.INT64                 , "INT64"             , "INT64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT2                          ("INT2"                           ,StandardTypeMetadata.INT16                 , "INT16"             , "INT16"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT256                        ("Int256"                         ,StandardTypeMetadata.INT64                 , "INT64"             , "INT64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT4                          ("INT4"                           ,StandardTypeMetadata.INT32                 , "INT32"             , "INT32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT4RANGE                     ("INT4RANGE"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INT8RANGE                     ("INT8RANGE"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTEGER                       ("INTEGER"                        ,StandardTypeMetadata.INT32                 , "INT32"             , "INT32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INTERVAL                      ("INTERVAL"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_DAY                  ("INTERVAL DAY"                   ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_DAY_HOUR             ("INTERVAL DAY TO HOUR"           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_DAY_MINUTE           ("INTERVAL DAY TO MINUTE"         ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_DAY_SECOND           ("INTERVAL DAY TO SECOND"         ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_HOUR                 ("INTERVAL HOUR"                  ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_HOUR_MINUTE          ("INTERVAL HOUR TO MINUTE"        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_HOUR_SECOND          ("INTERVAL HOUR TO SECOND"        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_MINUTE               ("INTERVAL MINUTE"                ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_MINUTE_SECOND        ("INTERVAL MINUTE TO SECOND"      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_MONTH                ("INTERVAL MONTH"                 ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_SECOND               ("INTERVAL SECOND"                ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_YEAR                 ("INTERVAL YEAR"                  ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_YEAR_MONTH           ("INTERVAL YEAR TO MONTH"         ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	IP                            ("ip"                             ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	IPV4                          ("IPV4"                           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	IPV6                          ("IPV6"                           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	JAVA_OBJECT                   ("JAVA_OBJECT"                    ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	JOIN                          ("join"                           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	JSONB                         ("JSONB"                          ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	JSONPATH                      ("JSONPATH"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	KEYWORD                       ("keyword"                        ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	LARGEINT                      ("LARGEINT"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	LINE                          ("LINE"                           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	LINESTRING                    ("LINESTRING"                     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	LIST                          ("LIST"                           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	LONG_TEXT                     ("LONG"                           ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	LONG                          ("long"                           ,StandardTypeMetadata.INT64                 , "Int64"             , "Int64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	LONGBLOB                      ("LONGBLOB"                       ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	LONGTEXT                      ("LONGTEXT"                       ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	LowCardinality                ("LowCardinality"                 ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	LSEG                          ("LSEG"                           ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	LVARCHAR                      ("LVARCHAR"                       ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	MACADDR                       ("MACADDR"                        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	MACADDR8                      ("MACADDR8"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	MAP                           ("MAP"                            ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	MATCH_ONLY_TEXT               ("match_only_text"                ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	MATCH_ONLY_TEXT               ("match_only_text "               ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	MEDIUMBLOB                    ("MEDIUMBLOB"                     ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	MEDIUMINT                     ("MEDIUMINT"                      ,StandardTypeMetadata.INT16                 , "INT16"             , "INT16"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	MEDIUMTEXT                    ("MEDIUMTEXT"                     ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	MONEY                         ("MONEY"                          ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	MULTILINESTRING               ("MULTILINESTRING"                ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	MULTIPOINT                    ("MULTIPOINT"                     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	MULTIPOLYGON                  ("MULTIPOLYGON"                   ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	MULTISET                      ("MULTISET"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	NATURALN                      ("NATURALN"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	NCHAR                         ("NCHAR"                          ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	NCLOB                         ("NCLOB"                          ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	NESTED                        ("nested"                         ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	NTEXT                         ("NTEXT"                          ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	NUMBER                        ("NUMBER"                         ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	NUMERIC                       ("NUMERIC"                        ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	NUMRANGE                      ("NUMRANGE"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	NVARCHAR                      ("NVARCHAR"                       ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	NVARCHAR2                     ("NVARCHAR2"                      ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	OBJECT                        ("OBJECT"                         ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	OID                           ("OID"                            ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	ORA_DATE                      ("ORA_DATE"                       ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	PATH                          ("PATH"                           ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	PERCOLATOR                    ("percolator"                     ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	PG_SNAPSHOT                   ("PG_SNAPSHOT"                    ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	POINT                         ("POINT"                          ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	POLYGON                       ("POLYGON"                        ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	POSITIVE                      ("POSITIVE"                       ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	POSITIVEN                     ("POSITIVEN"                      ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	RANGE                         ("Range"                          ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	RANK_FEATURE                  ("rank_feature"                   ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	RANK_FEATURES                 ("rank_features"                  ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	RAW                           ("RAW"                            ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REAL                          ("REAL"                           ,StandardTypeMetadata.INT2                  ,   1,   1,   1,  -1,  -1,  -1),
	REFCURSOR                     ("REFCURSOR"                      ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGCLASS                      ("REGCLASS"                       ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGCONFIG                     ("REGCONFIG"                      ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGDICTIONARY                 ("REGDICTIONARY"                  ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGNAMESPACE                  ("REGNAMESPACE"                   ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGOPER                       ("REGOPER"                        ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGOPERATOR                   ("REGOPERATOR"                    ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGPROC                       ("REGPROC"                        ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGPROCEDURE                  ("REGPROCEDURE"                   ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGROLE                       ("REGROLE"                        ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGTYPE                       ("REGTYPE"                        ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	RING                          ("RING"                           ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	ROW                           ("ROW"                            ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	ROWID                         ("ROWID"                          ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	SCALED_FLOAT                  ("scaled_float"                   ,StandardTypeMetadata.FLOAT                 ,   1,   2,   3,  -1,  -1,  -1),
	SECONDDATE                    ("SECONDDATE"                     ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	SERIAL                        ("SERIAL"                         ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SERIAL2                       ("SERIAL2"                        ,StandardTypeMetadata.INT16                 , "Int16"             , "Int16"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SERIAL4                       ("SERIAL4"                        ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SERIAL8                       ("SERIAL8"                        ,StandardTypeMetadata.INT64                 , "Int64"             , "Int64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SET                           ("SET"                            ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	SHAPE                         ("shape"                          ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	SHORT                         ("SHORT"                          ,StandardTypeMetadata.INT16                 , "IInt16"            , "IInt16"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SIGNTYPE                      ("SIGNTYPE"                       ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	SIMPLE_DOUBLE                 ("SIMPLE_DOUBLE"                  ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SIMPLE_FLOAT                  ("SIMPLE_FLOAT"                   ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	SIMPLE_INTEGER                ("SIMPLE_INTEGER"                 ,StandardTypeMetadata.INT32                 , "INT32"             , "INT32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SimpleAggregateFunction       ("SimpleAggregateFunction"        ,StandardTypeMetadata.ILLEGAL               ,  -1,  -1,  -1,  -1,  -1,  -1),
	SMALLDATETIME                 ("SMALLDATETIME"                  ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	SMALLDECIMAL                  ("SMALLDECIMAL"                   ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SMALLFLOAT                    ("SMALLFLOAT"                     ,StandardTypeMetadata.FLOAT                 , "Float"             , "Float"                  , null   , null   , null   ,   1,   2,   3,  -1,  -1,  -1),
	SMALLINT                      ("SMALLINT"                       ,StandardTypeMetadata.INT16                 , "INT16"             , "INT16"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SMALLMONEY                    ("SMALLMONEY"                     ,StandardTypeMetadata.DOUBLE                , "Double"            , "Double"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SMALLSERIAL                   ("SMALLSERIAL"                    ,StandardTypeMetadata.INT16                 , "IInt16"            , "IInt16"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SPARSE_VECTOR                 ("sparse_vector"                  ,StandardTypeMetadata.SPARSE_FLOAT_VECTOR   , "SparseFloatVector" , "SparseFloatVector({P})" , null   , null   , null   ,   1,   0,   1,  -1,  -1,  -1),
	SQL_DATETIMEOFFSET            ("SQL_DATETIMEOFFSET"             ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	SQL_VARIANT                   ("SQL_VARIANT"                    ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	ST_GEOMETRY                   ("ST_GEOMETRY"                    ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	ST_POINT                      ("ST_POINT"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	STRING                        ("STRING"                         ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	STRUCT                        ("STRUCT"                         ,StandardTypeMetadata.JSON                  , "JSON"              , "JSON"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	STRUCTS                       ("STRUCTS"                        ,StandardTypeMetadata.JSON                  , "JSON"              , "JSON"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SYS_REFCURSOR                 ("SYS_REFCURSOR"                  ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	SYSNAME                       ("SYSNAME"                        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	TEXT                          ("TEXT"                           ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TID                           ("TID"                            ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIME                          ("TIME"                           ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIME_TZ_UNCONSTRAINED         ("TIME TZ UNCONSTRAINED"          ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIME_WITH_TIME_ZONE           ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIME_WITH_ZONE                ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIME_WITHOUT_TIME_ZONE        ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIME_WITHOUT_ZONE             ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIME_UNCONSTRAINED            ("TIME_UNCONSTRAINED"             ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIMESTAMP                     ("TIMESTAMP"                      ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_WITH_LOCAL_ZONE     ("TIMESTAMP WITH LOCAL TIME ZONE" ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_WITH_TIME_ZONE      ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_WITH_ZONE           ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_WITHOUT_TIME_ZONE   ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_WITHOUT_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_NTZ                 ("TIMESTAMP_NTZ"                  ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMPTZ                   ("TIMESTAMPTZ"                    ,StandardTypeMetadata.INT32                 , "Int32"             , "Int32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMETZ                        ("TIMETZ"                         ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TIMEZ                         ("TIMEZ"                          ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TINYBLOB                      ("TINYBLOB"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	TINYINT                       ("TINYINT"                        ,StandardTypeMetadata.INT8                  , "INT8"              , "INT8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TINYTEXT                      ("TINYTEXT"                       ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TOKEN_COUNT                   ("token_count"                    ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	TSQUERY                       ("TSQUERY"                        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	TSRANGE                       ("TSRANGE"                        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	TSTZRANGE                     ("TSTZRANGE"                      ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	TSVECTOR                      ("TSVECTOR"                       ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	TUPLE                         ("TUPLE"                          ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	TXID_SNAPSHOT                 ("TXID_SNAPSHOT"                  ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	UINT128                       ("UInt128"                        ,StandardTypeMetadata.INT64                 , "INT64"             , "INT64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT16                        ("UInt16"                         ,StandardTypeMetadata.INT16                 , "INT16"             , "INT16"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT256                       ("UInt256"                        ,StandardTypeMetadata.INT64                 , "INT64"             , "INT64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT32                        ("UInt32"                         ,StandardTypeMetadata.INT32                 , "INT32"             , "INT32"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT64                        ("UInt64"                         ,StandardTypeMetadata.INT64                 , "INT64"             , "INT64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT8                         ("UInt8"                          ,StandardTypeMetadata.INT8                  , "INT8"              , "INT8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UNIQUEIDENTIFIER              ("UNIQUEIDENTIFIER"               ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	UNSIGNED_LONG                 ("unsigned_long"                  ,StandardTypeMetadata.INT64                 , "Int64"             , "Int64"                  , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UROWID                        ("UROWID"                         ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	UUID                          ("UUID"                           ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	VARBINARY                     ("VARBINARY"                      ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	VARBIT                        ("VARBIT"                         ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	VARBYTE                       ("VARBYTE"                        ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	VARCHAR                       ("VARCHAR"                        ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	VARCHAR2                      ("VARCHAR2"                       ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	VARCHARBYTE                   ("VARCHARBYTE"                    ,StandardTypeMetadata.ARRAY                 , "Array"             , "Array({P})"             , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	VECTOR                        ("VECTOR"                         ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	VERSION                       ("version"                        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	XID                           ("XID"                            ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	XML                           ("XML"                            ,StandardTypeMetadata.VARCHAR               , "VarChar"           , "VarChar({P})"           , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	XMLTYPE                       ("XMLTYPE"                        ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1),
	YEAR                          ("YEAR"                           ,StandardTypeMetadata.INT8                  , "Int8"              , "Int8"                   , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	YMINTERVAL                    ("YMINTERVAL"                     ,StandardTypeMetadata.NONE                  ,  -1,  -1,  -1,  -1,  -1,  -1);

	private String input                     ; // 输入名称(根据输入名称转换成标准类型)(名称与枚举名不一致的需要,如带空格的)
	private final TypeMetadata standard      ; // 标准类型
	private String meta                      ; // SQL数据类型名称
	private String formula                   ; // SQL最终数据类型公式
	private int ignoreLength            = -1 ; // 是否忽略长度
	private int ignorePrecision         = -1 ; // 是否忽略有效位数
	private int ignoreScale             = -1 ; // 是否忽略小数位数
	private int maxLength               = -1 ; // 最大长度
	private int maxPrecision            = -1 ; // 最大有效位数
	private int maxScale                = -1 ; // 最大小数位数
	private String lengthRefer               ; // 读取元数据依据-长度
	private String precisionRefer            ; // 读取元数据依据-有效位数
	private String scaleRefer                ; // 读取元数据依据-小数位数
	private TypeMetadata.Refer refer         ; // 集成元数据读写配置

	MilvusTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
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
		this.maxLength = maxLength;
		this.maxPrecision = maxPrecision;
		this.maxScale = maxScale;
	}

	MilvusTypeMetadataAlias(String input, TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null , null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale, -1, -1, -1);
	}

	MilvusTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
		this(input, standard, null, null, null, null, null,  ignoreLength, ignorePrecision, ignoreScale, maxLength, maxPrecision, maxScale);
	}
	MilvusTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale, -1, -1, -1);
	}

	MilvusTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	MilvusTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(null, standard, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	MilvusTypeMetadataAlias(String input, TypeMetadata standard) {
		this.input = input;
		this.standard = standard;
	}

	MilvusTypeMetadataAlias(TypeMetadata standard) {
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
		return refer;
	}
}
