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

package org.anyline.data.jdbc.clickhouse;

import org.anyline.metadata.type.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum ClickHouseTypeMetadataAlias implements TypeMetadataAlias {
	ARRAY                         ("ARRAY"                          ,StandardTypeMetadata.ARRAY                     , "Arry"            , "Arry"                , null   , null   , null   ,   2,   2,   2,  -1,  -1,  -1),
	DATE                          ("DATE"                           ,StandardTypeMetadata.DATE                      , "Date32"          , "Date32"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	DATE32                        ("DATE32"                         ,StandardTypeMetadata.DATE32                    , "Date32"          , "Date32"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	DATETIME                      ("DATETIME"                       ,StandardTypeMetadata.DATETIME64                , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	DATETIME64                    ("DATETIME64"                     ,StandardTypeMetadata.DATETIME64                , "DateTime64"      , "DateTime64"          , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	DECIMAL                       ("DECIMAL"                        ,StandardTypeMetadata.DECIMAL                   , "Decimal"         , "Decimal({P},{S})"    , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECIMAL128                    ("DECIMAL128"                     ,StandardTypeMetadata.DECIMAL128                , "Decimal128"      , "Decimal128({P},{S})" , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECIMAL256                    ("DECIMAL256"                     ,StandardTypeMetadata.DECIMAL256                , "Decimal256"      , "Decimal256({P},{S})" , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECIMAL32                     ("DECIMAL32"                      ,StandardTypeMetadata.DECIMAL32                 , "Decimal32"       , "Decimal32({P},{S})"  , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECIMAL64                     ("DECIMAL64"                      ,StandardTypeMetadata.DECIMAL64                 , "Decimal64"       , "Decimal64({P},{S})"  , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	ENUM                          ("ENUM"                           ,StandardTypeMetadata.ENUM                      ,   1,   1,   1,  -1,  -1,  -1),
	FIXEDSTRING                   ("FixedString"                    ,StandardTypeMetadata.FixedString               ,   0,   1,   1,  -1,  -1,  -1),
	FLOAT32                       ("FLOAT32"                        ,StandardTypeMetadata.FLOAT32                   , "Float32"         , "Float32"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	FLOAT64                       ("FLOAT64"                        ,StandardTypeMetadata.FLOAT64                   , "Float64"         , "Float64"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT                           ("INT"                            ,StandardTypeMetadata.INT32                     , "Int32"           , "Int32"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT128                        ("INT128"                         ,StandardTypeMetadata.INT128                    , "Int128"          , "Int128"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT16                         ("INT16"                          ,StandardTypeMetadata.INT16                     , "Int16"           , "Int16"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT256                        ("INT256"                         ,StandardTypeMetadata.INT256                    , "Int256"          , "Int256"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT32                         ("INT32"                          ,StandardTypeMetadata.INT32                     , "Int32"           , "Int32"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT64                         ("INT64"                          ,StandardTypeMetadata.INT64                     , "Int64"           , "Int64"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	IPV4                          ("IPV4"                           ,StandardTypeMetadata.IPV4                      , "IPv4"            , "IPv4"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	IPV6                          ("IPV6"                           ,StandardTypeMetadata.IPV6                      , "IPv6"            , "IPv6"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	LOWCARDINALITY                ("LowCardinality"                 ,StandardTypeMetadata.LowCardinality            , "LowCardinality"  , "LowCardinality"      , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	MAP                           ("MAP"                            ,StandardTypeMetadata.MAP                       , "Map"             , "Map"                 , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	MULTIPOLYGON                  ("MULTIPOLYGON"                   ,StandardTypeMetadata.MULTIPOLYGON              , "MultiPolygon"    , "MultiPolygon"        , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	POINT                         ("POINT"                          ,StandardTypeMetadata.POINT                     , "Point"           , "Point"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	POLYGON                       ("POLYGON"                        ,StandardTypeMetadata.POLYGON                   , "Polygon"         , "Polygon"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	RING                          ("RING"                           ,StandardTypeMetadata.RING                      ,   1,   1,   1,  -1,  -1,  -1),
	SIMPLEAGGREGATEFUNCTION       ("SimpleAggregateFunction"        ,StandardTypeMetadata.SimpleAggregateFunction   ,   1,   1,   1,  -1,  -1,  -1),
	STRING                        ("STRING"                         ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TUPLE                         ("TUPLE"                          ,StandardTypeMetadata.TUPLE                     ,   1,   1,   1,  -1,  -1,  -1),
	UINT128                       ("UINT128"                        ,StandardTypeMetadata.UINT128                   , "UInt128"         , "UInt128"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT16                        ("UINT16"                         ,StandardTypeMetadata.UINT16                    , "UInt16"          , "UInt16"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT256                       ("UINT256"                        ,StandardTypeMetadata.UINT256                   , "UInt256"         , "UInt256"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT32                        ("UINT32"                         ,StandardTypeMetadata.UINT32                    , "UInt32"          , "UInt32"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT64                        ("UINT64"                         ,StandardTypeMetadata.UINT64                    , "UInt64"          , "UInt64"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UINT8                         ("UINT8"                          ,StandardTypeMetadata.UINT8                     , "UInt8"           , "UInt8"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	UUID                          ("UUID"                           ,StandardTypeMetadata.UUID                      ,   1,   1,   1,  -1,  -1,  -1),
	ACLITEM                       ("ACLITEM"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	AGG_STATE                     ("AGG_STATE"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	AGGREGATE_METRIC_DOUBLE       ("aggregate_metric_double"        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	ALIAS                         ("alias"                          ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BFILE                         ("BFILE"                          ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BFLOAT16_VECTOR               ("BFloat16Vector"                 ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BIGINT                        ("BIGINT"                         ,StandardTypeMetadata.INT64                     , "Int64"           , "Int64"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BIGSERIAL                     ("BIGSERIAL"                      ,StandardTypeMetadata.INT64                     , "Int64"           , "Int64"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BINARY                        ("BINARY"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BINARY_DOUBLE                 ("BINARY_DOUBLE"                  ,StandardTypeMetadata.FLOAT64                   , "Float64"         , "Float64"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BINARY_FLOAT                  ("BINARY_FLOAT"                   ,StandardTypeMetadata.FLOAT32                   , "Float32"         , "Float32"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BINARY_INTEGER                ("BINARY_INTEGER"                 ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BINARY_VECTOR                 ("BinaryVector"                   ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BIT                           ("BIT"                            ,StandardTypeMetadata.INT8                      , "Int8"            , "Int8"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BIT_VARYING                   ("BIT VARYING"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BITMAP                        ("BITMAP"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BLOB                          ("BLOB"                           ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	BOOL                          ("BOOL"                           ,StandardTypeMetadata.INT8                      , "Int8"            , "Int8"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BOOLEAN                       ("BOOLEAN"                        ,StandardTypeMetadata.INT8                      , "Int8"            , "Int8"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BOX                           ("BOX"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BOX2D                         ("BOX2D"                          ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BPCHAR                        ("BPCHAR"                         ,StandardTypeMetadata.CHAR                      ,   0,   1,   1,  -1,  -1,  -1),
	BPCHARBYTE                    ("BPCHARBYTE"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	BYTE                          ("BYTE"                           ,StandardTypeMetadata.INT8                      , "Int8"            , "Int8"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	BYTEA                         ("BYTEA"                          ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	CHAR                          ("CHAR"                           ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	CHARACTER                     ("CHARACTER"                      ,StandardTypeMetadata.VARCHAR                   ,   0,   1,   1,  -1,  -1,  -1),
	CHARACTER_VARYING             ("CHARACTER VARYING"              ,StandardTypeMetadata.VARCHAR                   , "VARCHAR"         , "VARCHAR({L})"        , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	CID                           ("CID"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	CIDR                          ("CIDR"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	CIRCLE                        ("CIRCLE"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	CLOB                          ("CLOB"                           ,StandardTypeMetadata.STRING                    ,   1,   1,   1,  -1,  -1,  -1),
	COMPLETION                    ("completion"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	CURSOR                        ("CURSOR"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	DATE_NANOS                    ("date_nanos"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	DATERANGE                     ("DATERANGE"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	DATETIME_WITH_TIME_ZONE       ("DATETIME WITH TIME ZONE"        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	DATETIME2                     ("DATETIME2"                      ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	DATETIMEOFFSET                ("DATETIMEOFFSET"                 ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	DATETIMEV2                    ("DATETIMEV2"                     ,StandardTypeMetadata.DATETIME64                , "DateTime64"      , "DateTime64"          , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	DATEV2                        ("DATEV2"                         ,StandardTypeMetadata.DATE                      ,   1,   1,   1,  -1,  -1,  -1),
	DEC                           ("DEC"                            ,StandardTypeMetadata.DECIMAL                   , "Decimal"         , "Decimal({P},{S})"    , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	DECFLOAT                      ("DECFLOAT"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	DENSE_VECTOR                  ("dense_vector"                   ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	DOUBLE                        ("DOUBLE"                         ,StandardTypeMetadata.FLOAT64                   , "Float64"         , "Float64"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	DOUBLE_PRECISION              ("DOUBLE PRECISION"               ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	DSINTERVAL                    ("DSINTERVAL"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	DURATION                      ("DURATION"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	FIXED_STRING                  ("FIXED_STRING"                   ,StandardTypeMetadata.FixedString               , "FixedString"     , "FixedString"         , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	FLATTENED                     ("flattened"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	FLOAT                         ("FLOAT"                          ,StandardTypeMetadata.FLOAT32                   , "Float32"         , "Float32"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	FLOAT16_VECTOR                ("Float16Vector"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	FLOAT4                        ("FLOAT4"                         ,StandardTypeMetadata.FLOAT32                   , "Float32"         , "Float32"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	FLOAT8                        ("FLOAT8"                         ,StandardTypeMetadata.FLOAT32                   , "Float32"         , "Float32"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	FLOAT_VECTOR                  ("FloatVector"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEO_POINT                     ("geo_point"                      ,StandardTypeMetadata.POINT                     , "Point"           , "Point"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	GEO_SHAPE                     ("geo_shape"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOGRAPHY                     ("GEOGRAPHY"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOGRAPHY_POINT               ("GEOGRAPHY_POINT"                ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOMETRY                      ("GEOMETRY"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	GEOMETRYCOLLECTION            ("GEOMETRYCOLLECTION"             ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	GTSVECTOR                     ("GTSVECTOR"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	GUID                          ("GUID"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	HALF_FLOAT                    ("half_float"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	HIERARCHYID                   ("HIERARCHYID"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	HISTOGRAM                     ("histogram"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	HLL                           ("HLL"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	IMAGE                         ("IMAGE"                          ,StandardTypeMetadata.FixedString               , "FixedString"     , "FixedString"         , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	INET                          ("INET"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INT2                          ("INT2"                           ,StandardTypeMetadata.INT8                      , "Int8"            , "Int8"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT4                          ("INT4"                           ,StandardTypeMetadata.INT8                      , "Int8"            , "Int8"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT4RANGE                     ("INT4RANGE"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INT8                          ("INT8"                           ,StandardTypeMetadata.INT8                      , "Int8"            , "Int8"                , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INT8RANGE                     ("INT8RANGE"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTEGER                       ("INTEGER"                        ,StandardTypeMetadata.INT32                     , "Int32"           , "Int32"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	INTERVAL                      ("INTERVAL"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_DAY                  ("INTERVAL DAY"                   ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_DAY_HOUR             ("INTERVAL DAY TO HOUR"           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_DAY_MINUTE           ("INTERVAL DAY TO MINUTE"         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_DAY_SECOND           ("INTERVAL DAY TO SECOND"         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_HOUR                 ("INTERVAL HOUR"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_HOUR_MINUTE          ("INTERVAL HOUR TO MINUTE"        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_HOUR_SECOND          ("INTERVAL HOUR TO SECOND"        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_MINUTE               ("INTERVAL MINUTE"                ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_MINUTE_SECOND        ("INTERVAL MINUTE TO SECOND"      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_MONTH                ("INTERVAL MONTH"                 ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_SECOND               ("INTERVAL SECOND"                ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_YEAR                 ("INTERVAL YEAR"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	INTERVAL_YEAR_MONTH           ("INTERVAL YEAR TO MONTH"         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	IP                            ("ip"                             ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	JAVA_OBJECT                   ("JAVA_OBJECT"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	JOIN                          ("join"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	JSON                          ("JSON"                           ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	JSONB                         ("JSONB"                          ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	JSONPATH                      ("JSONPATH"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	KEYWORD                       ("KEYWORD"                        ,StandardTypeMetadata.VARCHAR                   ,   0,   1,   1,  -1,  -1,  -1),
	LARGEINT                      ("LARGEINT"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	LINE                          ("LINE"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	LINESTRING                    ("LINESTRING"                     ,StandardTypeMetadata.LINESTRING                , "LineString"      , "LineString"          , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	LIST                          ("LIST"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	LONG_TEXT                     ("LONG"                           ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	LONG                          ("long"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	LONGBLOB                      ("LONGBLOB"                       ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	LONGTEXT                      ("LONGTEXT"                       ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	LSEG                          ("LSEG"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	LVARCHAR                      ("LVARCHAR"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	MACADDR                       ("MACADDR"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	MACADDR8                      ("MACADDR8"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	MATCH_ONLY_TEXT               ("match_only_text "               ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	MEDIUMBLOB                    ("MEDIUMBLOB"                     ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	MEDIUMINT                     ("MEDIUMINT"                      ,StandardTypeMetadata.DECIMAL                   , "Decimal"         , "Decimal({P},{S})"    , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	MEDIUMTEXT                    ("MEDIUMTEXT"                     ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	MONEY                         ("MONEY"                          ,StandardTypeMetadata.DECIMAL                   , "Decimal"         , "Decimal({P},{S})"    , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	MULTILINESTRING               ("MULTILINESTRING"                ,StandardTypeMetadata.MULTILINESTRING           , "MultiLineString" , "MultiLineString"     , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	MULTIPOINT                    ("MULTIPOINT"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	MULTISET                      ("MULTISET"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	NATURALN                      ("NATURALN"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	NCHAR                         ("NCHAR"                          ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	NCLOB                         ("NCLOB"                          ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	NESTED                        ("nested"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	NTEXT                         ("NTEXT"                          ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	NUMBER                        ("NUMBER"                         ,StandardTypeMetadata.DECIMAL                   , "Decimal"         , "Decimal({P},{S})"    , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	NUMERIC                       ("NUMERIC"                        ,StandardTypeMetadata.DECIMAL                   , "Decimal"         , "Decimal({P},{S})"    , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	NUMRANGE                      ("NUMRANGE"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	NVARCHAR                      ("NVARCHAR"                       ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	NVARCHAR2                     ("NVARCHAR2"                      ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	OBJECT                        ("OBJECT"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	OID                           ("OID"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	ORA_DATE                      ("ORA_DATE"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	PATH                          ("PATH"                           ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	PERCOLATOR                    ("percolator"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	PG_SNAPSHOT                   ("PG_SNAPSHOT"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	POSITIVE                      ("POSITIVE"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	POSITIVEN                     ("POSITIVEN"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	RANGE                         ("Range"                          ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	RANK_FEATURE                  ("rank_feature"                   ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	RANK_FEATURES                 ("rank_features"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	RAW                           ("RAW"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REAL                          ("REAL"                           ,StandardTypeMetadata.FLOAT32                   , "Float32"         , "Float32"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	REFCURSOR                     ("REFCURSOR"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGCLASS                      ("REGCLASS"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGCONFIG                     ("REGCONFIG"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGDICTIONARY                 ("REGDICTIONARY"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGNAMESPACE                  ("REGNAMESPACE"                   ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGOPER                       ("REGOPER"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGOPERATOR                   ("REGOPERATOR"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGPROC                       ("REGPROC"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGPROCEDURE                  ("REGPROCEDURE"                   ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGROLE                       ("REGROLE"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	REGTYPE                       ("REGTYPE"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	ROW                           ("ROW"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	ROWID                         ("ROWID"                          ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SCALED_FLOAT                  ("scaled_float"                   ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SEARCH_AS_YOU_TYPE            ("search_as_you_type"             ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SECONDDATE                    ("SECONDDATE"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SERIAL                        ("SERIAL"                         ,StandardTypeMetadata.INT32                     , "Int32"           , "INT"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SERIAL2                       ("SERIAL2"                        ,StandardTypeMetadata.INT16                     , "Int16"           , "Int16"               , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SERIAL4                       ("SERIAL4"                        ,StandardTypeMetadata.INT32                     , "Int32"           , "INT"                 , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SERIAL8                       ("SERIAL8"                        ,StandardTypeMetadata.INT64                     , "Int64"           , "Int64"               , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SET                           ("SET"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SHAPE                         ("shape"                          ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SHORT                         ("SHORT"                          ,StandardTypeMetadata.INT16                     , "Int16"           , "Int16"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SIGNTYPE                      ("SIGNTYPE"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SIMPLE_DOUBLE                 ("SIMPLE_DOUBLE"                  ,StandardTypeMetadata.FLOAT64                   , "Float64"         , "Float64"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SIMPLE_FLOAT                  ("SIMPLE_FLOAT"                   ,StandardTypeMetadata.FLOAT32                   , "Float32"         , "Float32"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SIMPLE_INTEGER                ("SIMPLE_INTEGER"                 ,StandardTypeMetadata.INT32                     , "Int32"           , "Int32"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SMALLDATETIME                 ("SMALLDATETIME"                  ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	SMALLDECIMAL                  ("SMALLDECIMAL"                   ,StandardTypeMetadata.DECIMAL                   , "Decimal"         , "Decimal({P},{S})"    , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SMALLFLOAT                    ("SMALLFLOAT"                     ,StandardTypeMetadata.FLOAT32                   , "Float32"         , "Float32"             , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SMALLINT                      ("SMALLINT"                       ,StandardTypeMetadata.INT16                     , "Int16"           , "Int16"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SMALLMONEY                    ("SMALLMONEY"                     ,StandardTypeMetadata.DECIMAL                   , "Decimal"         , "Decimal({P},{S})"    , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	SMALLSERIAL                   ("SMALLSERIAL"                    ,StandardTypeMetadata.INT16                     , "Int16"           , "Int16"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	SPARSE_VECTOR                 ("sparse_vector"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SPARSE_FLOAT_VECTOR           ("SparseFloatVector"              ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SQL_DATETIMEOFFSET            ("SQL_DATETIMEOFFSET"             ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SQL_VARIANT                   ("SQL_VARIANT"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	ST_GEOMETRY                   ("ST_GEOMETRY"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	ST_POINT                      ("ST_POINT"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	STRUCT                        ("STRUCT"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	STRUCTS                       ("STRUCTS"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SYS_REFCURSOR                 ("SYS_REFCURSOR"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	SYSNAME                       ("SYSNAME"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	TEXT                          ("TEXT"                           ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TID                           ("TID"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	TIME                          ("TIME"                           ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	TIME_TZ_UNCONSTRAINED         ("TIME TZ UNCONSTRAINED"          ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIME_WITH_TIME_ZONE           ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIME_WITH_ZONE                ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIME_WITHOUT_TIME_ZONE        ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIME_WITHOUT_ZONE             ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIME_UNCONSTRAINED            ("TIME_UNCONSTRAINED"             ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP                     ("TIMESTAMP"                      ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	TIMESTAMP_WITH_LOCAL_ZONE     ("TIMESTAMP WITH LOCAL TIME ZONE" ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	TIMESTAMP_WITH_TIME_ZONE      ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	TIMESTAMP_WITH_ZONE           ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_WITHOUT_TIME_ZONE   ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_WITHOUT_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMP_NTZ                 ("TIMESTAMP_NTZ"                  ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMESTAMPTZ                   ("TIMESTAMPTZ"                    ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DateTime"            , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	TIMETZ                        ("TIMETZ"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	TIMEZ                         ("TIMEZ"                          ,StandardTypeMetadata.DATETIME                  , "DateTime"        , "DATETIME({S})"       , null   , null   , null   ,   1,   1,   2,  -1,  -1,  -1),
	TINYBLOB                      ("TINYBLOB"                       ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	TINYINT                       ("TINYINT"                        ,StandardTypeMetadata.INT8                      , "Int8"            , "Int8"                , null   , null   , null   ,   1,   0,   0,  -1,  -1,  -1),
	TINYTEXT                      ("TINYTEXT"                       ,StandardTypeMetadata.STRING                    ,   1,   1,   1,  -1,  -1,  -1),
	TOKEN_COUNT                   ("token_count"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	TSQUERY                       ("TSQUERY"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	TSRANGE                       ("TSRANGE"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	TSTZRANGE                     ("TSTZRANGE"                      ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	TSVECTOR                      ("TSVECTOR"                       ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	TXID_SNAPSHOT                 ("TXID_SNAPSHOT"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	UNIQUEIDENTIFIER              ("UNIQUEIDENTIFIER"               ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	UNSIGNED_LONG                 ("unsigned_long"                  ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	UROWID                        ("UROWID"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	VARBINARY                     ("VARBINARY"                      ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	VARBIT                        ("VARBIT"                         ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   0,   1,   1,  -1,  -1,  -1),
	VARBYTE                       ("VARBYTE"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	VARCHAR                       ("VARCHAR"                        ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	VARCHAR2                      ("VARCHAR2"                       ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	VARCHARBYTE                   ("VARCHARBYTE"                    ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	VECTOR                        ("VECTOR"                         ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	VERSION                       ("version"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	XID                           ("XID"                            ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	XML                           ("XML"                            ,StandardTypeMetadata.STRING                    , "String"          , "String"              , null   , null   , null   ,  -1,  -1,  -1,  -1,  -1,  -1),
	XMLTYPE                       ("XMLTYPE"                        ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1),
	YEAR                          ("YEAR"                           ,StandardTypeMetadata.UINT8                     , "UInt8"           , "UInt8"               , null   , null   , null   ,   1,   1,   1,  -1,  -1,  -1),
	YMINTERVAL                    ("YMINTERVAL"                     ,StandardTypeMetadata.NONE                      ,  -1,  -1,  -1,  -1,  -1,  -1);

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

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
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

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null , null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale, -1, -1, -1);
	}

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
		this(input, standard, null, null, null, null, null,  ignoreLength, ignorePrecision, ignoreScale, maxLength, maxPrecision, maxScale);
	}
	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale, -1, -1, -1);
	}

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	ClickHouseTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(null, standard, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	ClickHouseTypeMetadataAlias(String input, TypeMetadata standard) {
		this.input = input;
		this.standard = standard;
	}

	ClickHouseTypeMetadataAlias(TypeMetadata standard) {
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
