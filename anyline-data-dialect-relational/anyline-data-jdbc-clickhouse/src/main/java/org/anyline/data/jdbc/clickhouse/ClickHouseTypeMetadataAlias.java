/*
 * Copyright 2006-2023 www.anyline.org
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

package org.anyline.data.jdbc.clickhouse;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum ClickHouseTypeMetadataAlias implements TypeMetadataAlias {
	ARRAY                         ("ARRAY"                          ,StandardTypeMetadata.ARRAY                         ,   2,   2,   2),
	BOOL                          ("BOOL"                           ,StandardTypeMetadata.BOOL                          ,   1,   1,   1),
	DATE                          ("DATE"                           ,StandardTypeMetadata.DATE                          ,   1,   1,   1),
	DATE32                        ("DATE32"                         ,StandardTypeMetadata.CLICKHOUSE_DATE32             , "Date32"     , "Date32"        , null   , null   , null   ,   1,   1,   1),
	DATETIME                      ("DATETIME"                       ,StandardTypeMetadata.CLICKHOUSE_DATETIME64         , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	DATETIME64                    ("DATETIME64"                     ,StandardTypeMetadata.CLICKHOUSE_DATETIME64         , "DateTime64" , "DateTime64"    , null   , null   , null   ,   1,   1,   2),
	DECIMAL                       ("DECIMAL"                        ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	DECIMAL128                    ("DECIMAL128"                     ,StandardTypeMetadata.CLICKHOUSE_DECIMAL128         , "Decimal128" , "Decimal128"    , null   , null   , null   ,   1,   0,   2),
	DECIMAL256                    ("DECIMAL256"                     ,StandardTypeMetadata.CLICKHOUSE_DECIMAL256         , "Decimal256" , "Decimal256"    , null   , null   , null   ,   1,   0,   2),
	DECIMAL32                     ("DECIMAL32"                      ,StandardTypeMetadata.CLICKHOUSE_DECIMAL32          , "Decimal32"  , "Decimal32"     , null   , null   , null   ,   1,   0,   2),
	DECIMAL64                     ("DECIMAL64"                      ,StandardTypeMetadata.CLICKHOUSE_DECIMAL64          , "Decimal64"  , "Decimal64"     , null   , null   , null   ,   1,   0,   2),
	ENUM                          ("ENUM"                           ,StandardTypeMetadata.ENUM                          ,   1,   1,   1),
	FIXEDSTRING                   ("FixedString"                    ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	FLOAT32                       ("FLOAT32"                        ,StandardTypeMetadata.CLICKHOUSE_FLOAT32            , "Float32"    , "Float32"       , null   , null   , null   ,   1,   1,   1),
	FLOAT64                       ("FLOAT64"                        ,StandardTypeMetadata.CLICKHOUSE_FLOAT64            , "Float64"    , "Float64"       , null   , null   , null   ,   1,   1,   1),
	INT                           ("INT"                            ,StandardTypeMetadata.CLICKHOUSE_INT32              , "Int32"      , "Int32"         , null   , null   , null   ,   1,   1,   1),
	INT128                        ("INT128"                         ,StandardTypeMetadata.CLICKHOUSE_INT128             , "Int128"     , "Int128"        , null   , null   , null   ,   1,   1,   1),
	INT16                         ("INT16"                          ,StandardTypeMetadata.CLICKHOUSE_INT16              ,   1,   1,   1),
	INT256                        ("INT256"                         ,StandardTypeMetadata.CLICKHOUSE_INT256             , "Int256"     , "Int256"        , null   , null   , null   ,   1,   1,   1),
	INT32                         ("INT32"                          ,StandardTypeMetadata.CLICKHOUSE_INT32              , "Int32"      , "Int32"         , null   , null   , null   ,   1,   1,   1),
	INT64                         ("INT64"                          ,StandardTypeMetadata.CLICKHOUSE_INT64              , "Int64"      , "Int64"         , null   , null   , null   ,   1,   1,   1),
	IPV4                          ("IPV4"                           ,StandardTypeMetadata.IPV4                          ,   1,   1,   1),
	IPV6                          ("IPV6"                           ,StandardTypeMetadata.IPV6                          ,   1,   1,   1),
	LOWCARDINALITY                ("LowCardinality"                 ,StandardTypeMetadata.LowCardinality                ,   1,   1,   1),
	MAP                           ("MAP"                            ,StandardTypeMetadata.MAP                           ,   1,   1,   1),
	MULTIPOLYGON                  ("MULTIPOLYGON"                   ,StandardTypeMetadata.MULTIPOLYGON                  ,   1,   1,   1),
	POINT                         ("POINT"                          ,StandardTypeMetadata.POINT                         ,   1,   1,   1),
	POLYGON                       ("POLYGON"                        ,StandardTypeMetadata.POLYGON                       ,   1,   1,   1),
	RING                          ("RING"                           ,StandardTypeMetadata.RING                          ,   1,   1,   1),
	SIMPLEAGGREGATEFUNCTION       ("SimpleAggregateFunction"        ,StandardTypeMetadata.SimpleAggregateFunction       ,   1,   1,   1),
	STRING                        ("STRING"                         ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	TUPLE                         ("TUPLE"                          ,StandardTypeMetadata.TUPLE                         ,   1,   1,   1),
	UINT128                       ("UINT128"                        ,StandardTypeMetadata.CLICKHOUSE_UINT128            , "UInt128"    , "UInt128"       , null   , null   , null   ,   1,   1,   1),
	UINT16                        ("UINT16"                         ,StandardTypeMetadata.CLICKHOUSE_UINT16             , "UInt16"     , "UInt16"        , null   , null   , null   ,   1,   1,   1),
	UINT256                       ("UINT256"                        ,StandardTypeMetadata.CLICKHOUSE_UINT256            , "UInt256"    , "UInt256"       , null   , null   , null   ,   1,   1,   1),
	UINT32                        ("UINT32"                         ,StandardTypeMetadata.CLICKHOUSE_UINT32             , "UInt32"     , "UInt32"        , null   , null   , null   ,   1,   1,   1),
	UINT64                        ("UINT64"                         ,StandardTypeMetadata.CLICKHOUSE_UINT64             , "UInt64"     , "UInt64"        , null   , null   , null   ,   1,   1,   1),
	UINT8                         ("UINT8"                          ,StandardTypeMetadata.CLICKHOUSE_UINT8              , "UInt8"      , "UInt8"         , null   , null   , null   ,   1,   1,   1),
	UUID                          ("UUID"                           ,StandardTypeMetadata.UUID                          ,   1,   1,   1),
	ACLITEM                       ("ACLITEM"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	AGG_STATE                     ("AGG_STATE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	AGGREGATE_METRIC_DOUBLE       ("aggregate_metric_double"        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ALIAS                         ("alias"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BFILE                         ("BFILE"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BIGINT                        ("BIGINT"                         ,StandardTypeMetadata.CLICKHOUSE_INT64              , "Int64"      , "Int64"         , null   , null   , null   ,   1,   1,   1),
	BIGSERIAL                     ("BIGSERIAL"                      ,StandardTypeMetadata.CLICKHOUSE_INT64              , "Int64"      , "Int64"         , null   , null   , null   ,   1,   1,   1),
	BINARY                        ("BINARY"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BINARY_DOUBLE                 ("BINARY_DOUBLE"                  ,StandardTypeMetadata.CLICKHOUSE_FLOAT64            , "Float64"    , "Float64"       , null   , null   , null   ,   1,   1,   1),
	BINARY_FLOAT                  ("BINARY_FLOAT"                   ,StandardTypeMetadata.CLICKHOUSE_FLOAT32            , "Float32"    , "Float32"       , null   , null   , null   ,   1,   1,   1),
	BINARY_INTEGER                ("BINARY_INTEGER"                 ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BIT                           ("BIT"                            ,StandardTypeMetadata.BOOL                          ,   1,   1,   1),
	BIT_VARYING                   ("BIT VARYING"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BITMAP                        ("BITMAP"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BLOB                          ("BLOB"                           ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	BOOLEAN                       ("BOOLEAN"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BOX                           ("BOX"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BPCHAR                        ("BPCHAR"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BPCHARBYTE                    ("BPCHARBYTE"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BYTE                          ("BYTE"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BYTEA                         ("BYTEA"                          ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	CHAR                          ("CHAR"                           ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	CHARACTER                     ("CHARACTER"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CID                           ("CID"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CIDR                          ("CIDR"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CIRCLE                        ("CIRCLE"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLOB                          ("CLOB"                           ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	COMPLETION                    ("completion"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CURSOR                        ("CURSOR"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATE_NANOS                    ("date_nanos"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_DATE32             ("Date32"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATERANGE                     ("DATERANGE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_DATETIME64         ("DateTime"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATETIME_WITH_TIME_ZONE       ("DATETIME WITH TIME ZONE"        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATETIME2                     ("DATETIME2"                      ,StandardTypeMetadata.DATETIME                      , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	DATETIMEOFFSET                ("DATETIMEOFFSET"                 ,StandardTypeMetadata.DATETIME                      , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	DEC                           ("DEC"                            ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	DECFLOAT                      ("DECFLOAT"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_DECIMAL128         ("Decimal128"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_DECIMAL256         ("Decimal256"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_DECIMAL32          ("Decimal32"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_DECIMAL64          ("Decimal64"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DENSE_VECTOR                  ("dense_vector"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DOUBLE                        ("DOUBLE"                         ,StandardTypeMetadata.CLICKHOUSE_FLOAT64            , "Float64"    , "Float64"       , null   , null   , null   ,   1,   1,   1),
	DOUBLE_PRECISION              ("DOUBLE PRECISION"               ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DSINTERVAL                    ("DSINTERVAL"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DURATION                      ("DURATION"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FIXED_STRING                  ("FIXED_STRING"                   ,StandardTypeMetadata.CHAR                          ,   0,   1,   1),
	FLATTENED                     ("flattened"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FLOAT                         ("FLOAT"                          ,StandardTypeMetadata.CLICKHOUSE_FLOAT32            , "Float32"    , "Float32"       , null   , null   , null   ,   1,   1,   1),
	CLICKHOUSE_FLOAT32            ("Float32"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FLOAT4                        ("FLOAT4"                         ,StandardTypeMetadata.CLICKHOUSE_FLOAT32            , "Float32"    , "Float32"       , null   , null   , null   ,   1,   1,   1),
	CLICKHOUSE_FLOAT64            ("Float64"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FLOAT8                        ("FLOAT8"                         ,StandardTypeMetadata.CLICKHOUSE_FLOAT32            , "Float32"    , "Float32"       , null   , null   , null   ,   1,   1,   1),
	GEO_POINT                     ("geo_point"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEO_SHAPE                     ("geo_shape"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEOGRAPHY                     ("GEOGRAPHY"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEOGRAPHY_POINT               ("GEOGRAPHY_POINT"                ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEOMETRY                      ("GEOMETRY"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEOMETRYCOLLECTION            ("GEOMETRYCOLLECTION"             ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GTSVECTOR                     ("GTSVECTOR"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GUID                          ("GUID"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	HALF_FLOAT                    ("half_float"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	HIERARCHYID                   ("HIERARCHYID"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	HISTOGRAM                     ("histogram"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	HLL                           ("HLL"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	IMAGE                         ("IMAGE"                          ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	INET                          ("INET"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_INT128             ("Int128"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_INT16              ("Int16"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INT2                          ("INT2"                           ,StandardTypeMetadata.CLICKHOUSE_INT8               ,   1,   1,   1),
	CLICKHOUSE_INT256             ("Int256"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_INT32              ("Int32"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INT4                          ("INT4"                           ,StandardTypeMetadata.CLICKHOUSE_INT8               ,   1,   1,   1),
	INT4RANGE                     ("INT4RANGE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_INT64              ("Int64"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INT8                          ("INT8"                           ,StandardTypeMetadata.CLICKHOUSE_INT8               , "Int8"       , "Int8"          , null   , null   , null   ,   1,   1,   1),
	CLICKHOUSE_INT8               ("Int8"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INT8RANGE                     ("INT8RANGE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTEGER                       ("INTEGER"                        ,StandardTypeMetadata.CLICKHOUSE_INT32              ,   1,   1,   1),
	INTERVAL                      ("INTERVAL"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_DAY                  ("INTERVAL DAY"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_DAY_HOUR             ("INTERVAL DAY TO HOUR"           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_DAY_MINUTE           ("INTERVAL DAY TO MINUTE"         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_DAY_SECOND           ("INTERVAL DAY TO SECOND"         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_HOUR                 ("INTERVAL HOUR"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_HOUR_MINUTE          ("INTERVAL HOUR TO MINUTE"        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_HOUR_SECOND          ("INTERVAL HOUR TO SECOND"        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_MINUTE               ("INTERVAL MINUTE"                ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_MINUTE_SECOND        ("INTERVAL MINUTE TO SECOND"      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_MONTH                ("INTERVAL MONTH"                 ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_SECOND               ("INTERVAL SECOND"                ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_YEAR                 ("INTERVAL YEAR"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INTERVAL_YEAR_MONTH           ("INTERVAL YEAR TO MONTH"         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	IP                            ("ip"                             ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	JAVA_OBJECT                   ("JAVA_OBJECT"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	JOIN                          ("join"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	JSON                          ("JSON"                           ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	JSONB                         ("JSONB"                          ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	JSONPATH                      ("JSONPATH"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	KEYWORD                       ("KEYWORD"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LARGEINT                      ("LARGEINT"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LINE                          ("LINE"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LINESTRING                    ("LINESTRING"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LIST                          ("LIST"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LONG_TEXT                     ("LONG"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LONG                          ("long"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LONGBLOB                      ("LONGBLOB"                       ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	LONGTEXT                      ("LONGTEXT"                       ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	LSEG                          ("LSEG"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LVARCHAR                      ("LVARCHAR"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MACADDR                       ("MACADDR"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MACADDR8                      ("MACADDR8"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MATCH_ONLY_TEXT               ("match_only_text "               ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MEDIUMBLOB                    ("MEDIUMBLOB"                     ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	MEDIUMINT                     ("MEDIUMINT"                      ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	MEDIUMTEXT                    ("MEDIUMTEXT"                     ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	MONEY                         ("MONEY"                          ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	MULTILINESTRING               ("MULTILINESTRING"                ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	MULTIPOINT                    ("MULTIPOINT"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MULTISET                      ("MULTISET"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	NATURALN                      ("NATURALN"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	NCHAR                         ("NCHAR"                          ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	NCLOB                         ("NCLOB"                          ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	NESTED                        ("nested"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	NTEXT                         ("NTEXT"                          ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	NUMBER                        ("NUMBER"                         ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	NUMERIC                       ("NUMERIC"                        ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	NUMRANGE                      ("NUMRANGE"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	NVARCHAR                      ("NVARCHAR"                       ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	NVARCHAR2                     ("NVARCHAR2"                      ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	OBJECT                        ("OBJECT"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	OID                           ("OID"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ORA_DATE                      ("ORA_DATE"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	PATH                          ("PATH"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	PERCOLATOR                    ("percolator"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	PG_SNAPSHOT                   ("PG_SNAPSHOT"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	POSITIVE                      ("POSITIVE"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	POSITIVEN                     ("POSITIVEN"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	RANGE                         ("Range"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	RANK_FEATURE                  ("rank_feature"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	RANK_FEATURES                 ("rank_features"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	RAW                           ("RAW"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REAL                          ("REAL"                           ,StandardTypeMetadata.CLICKHOUSE_FLOAT32            , "Float32"    , "Float32"       , null   , null   , null   ,   1,   1,   1),
	REFCURSOR                     ("REFCURSOR"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGCLASS                      ("REGCLASS"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGCONFIG                     ("REGCONFIG"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGDICTIONARY                 ("REGDICTIONARY"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGNAMESPACE                  ("REGNAMESPACE"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGOPER                       ("REGOPER"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGOPERATOR                   ("REGOPERATOR"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGPROC                       ("REGPROC"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGPROCEDURE                  ("REGPROCEDURE"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGROLE                       ("REGROLE"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REGTYPE                       ("REGTYPE"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ROW                           ("ROW"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ROWID                         ("ROWID"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SCALED_FLOAT                  ("scaled_float"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SEARCH_AS_YOU_TYPE            ("search_as_you_type"             ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SECONDDATE                    ("SECONDDATE"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SERIAL                        ("SERIAL"                         ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	SERIAL2                       ("SERIAL2"                        ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	SERIAL4                       ("SERIAL4"                        ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	SERIAL8                       ("SERIAL8"                        ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	SET                           ("SET"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SHAPE                         ("shape"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SHORT                         ("SHORT"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIGNTYPE                      ("SIGNTYPE"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIMPLE_DOUBLE                 ("SIMPLE_DOUBLE"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIMPLE_FLOAT                  ("SIMPLE_FLOAT"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIMPLE_INTEGER                ("SIMPLE_INTEGER"                 ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SMALLDATETIME                 ("SMALLDATETIME"                  ,StandardTypeMetadata.DATETIME                      , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	SMALLDECIMAL                  ("SMALLDECIMAL"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SMALLFLOAT                    ("SMALLFLOAT"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SMALLINT                      ("SMALLINT"                       ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	SMALLMONEY                    ("SMALLMONEY"                     ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	SMALLSERIAL                   ("SMALLSERIAL"                    ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	SPARSE_VECTOR                 ("sparse_vector"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SQL_DATETIMEOFFSET            ("SQL_DATETIMEOFFSET"             ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SQL_VARIANT                   ("SQL_VARIANT"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ST_GEOMETRY                   ("ST_GEOMETRY"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ST_POINT                      ("ST_POINT"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	STRUCT                        ("STRUCT"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	STRUCTS                       ("STRUCTS"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SYS_REFCURSOR                 ("SYS_REFCURSOR"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SYSNAME                       ("SYSNAME"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TEXT                          ("TEXT"                           ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	TID                           ("TID"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TIME                          ("TIME"                           ,StandardTypeMetadata.DATETIME                      , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	TIME_TZ_UNCONSTRAINED         ("TIME TZ UNCONSTRAINED"          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TIME_WITH_TIME_ZONE           ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.TIME_WITH_TIME_ZONE           ,   1,   1,   1),
	TIME_WITH_ZONE                ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.TIME_WITH_TIME_ZONE           ,   1,   1,   1),
	TIME_WITHOUT_TIME_ZONE        ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE        ,   1,   1,   1),
	TIME_WITHOUT_ZONE             ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE        ,   1,   1,   1),
	TIME_UNCONSTRAINED            ("TIME_UNCONSTRAINED"             ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TIMESTAMP                     ("TIMESTAMP"                      ,StandardTypeMetadata.DATETIME                      , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	TIMESTAMP_WITH_LOCAL_ZONE     ("TIMESTAMP WITH LOCAL TIME ZONE" ,StandardTypeMetadata.DATETIME                      , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	TIMESTAMP_WITH_TIME_ZONE      ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.DATETIME                      , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	TIMESTAMP_WITH_ZONE           ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.TIMESTAMP_WITH_TIME_ZONE      ,   1,   1,   2),
	TIMESTAMP_WITHOUT_TIME_ZONE   ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE   ,   1,   2,   1),
	TIMESTAMP_WITHOUT_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE   ,   1,   2,   1),
	TIMESTAMPTZ                   ("TIMESTAMPTZ"                    ,StandardTypeMetadata.NONE                          ,   1,  -1,  -1),
	TIMEZ                         ("TIMEZ"                          ,StandardTypeMetadata.DATETIME                      , "DATETIME"   , "DATETIME({S})" , null   , null   , null   ,   1,   1,   2),
	TINYBLOB                      ("TINYBLOB"                       ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	TINYINT                       ("TINYINT"                        ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	TINYTEXT                      ("TINYTEXT"                       ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	TOKEN_COUNT                   ("token_count"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TSQUERY                       ("TSQUERY"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TSRANGE                       ("TSRANGE"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TSTZRANGE                     ("TSTZRANGE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TSVECTOR                      ("TSVECTOR"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TXID_SNAPSHOT                 ("TXID_SNAPSHOT"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_UINT128            ("UInt128"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_UINT16             ("UInt16"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_UINT256            ("UInt256"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_UINT32             ("UInt32"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_UINT64             ("UInt64"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLICKHOUSE_UINT8              ("UInt8"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UNIQUEIDENTIFIER              ("UNIQUEIDENTIFIER"               ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UNSIGNED_LONG                 ("unsigned_long"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UROWID                        ("UROWID"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	VARBINARY                     ("VARBINARY"                      ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	VARBIT                        ("VARBIT"                         ,StandardTypeMetadata.FixedString                   ,   0,   1,   1),
	VARCHAR                       ("VARCHAR"                        ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	VARCHAR2                      ("VARCHAR2"                       ,StandardTypeMetadata.STRING                        ,   1,   1,   1),
	VARCHARBYTE                   ("VARCHARBYTE"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	VECTOR                        ("VECTOR"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	VERSION                       ("version"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	XID                           ("XID"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	XML                           ("XML"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	YEAR                          ("YEAR"                           ,StandardTypeMetadata.CLICKHOUSE_UINT8              ,   1,   1,   1),
	YMINTERVAL                    ("YMINTERVAL"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1);

	private String input                     ; // 输入名称(根据输入名称转换成标准类型)(名称与枚举名不一致的需要,如带空格的)
	private final TypeMetadata standard      ; // 标准类型
	private String meta                      ; // SQL数据类型名称
	private String formula                   ; // SQL最终数据类型公式
	private int ignoreLength            = -1 ; // 是否忽略长度
	private int ignorePrecision         = -1 ; // 是否忽略有效位数
	private int ignoreScale             = -1 ; // 是否忽略小数位数
	private String lengthRefer               ; // 读取元数据依据-长度
	private String precisionRefer            ; // 读取元数据依据-有效位数
	private String scaleRefer                ; // 读取元数据依据-小数位数
	private TypeMetadata.Refer refer       ; // 集成元数据读写配置

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
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
	}

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, null , null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	ClickHouseTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(null, standard, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard){
		this.input = input;
		this.standard = standard;
	}

	ClickHouseTypeMetadataAlias(TypeMetadata standard){
		this.standard = standard;
	}

	@Override
	public String input(){
		if(null == input){
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
		if(null == refer){
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
				refer.setIgnoreLength(ignoreLength);
			}
			if(-1 != ignorePrecision) {
				refer.setIgnorePrecision(ignorePrecision);
			}
			if(-1 != ignoreScale) {
				refer.setIgnoreScale(ignoreScale);
			}
		}
		return refer;
	}
}
