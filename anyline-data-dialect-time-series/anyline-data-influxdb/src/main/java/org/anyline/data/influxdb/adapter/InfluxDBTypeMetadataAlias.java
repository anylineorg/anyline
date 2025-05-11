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

package org.anyline.data.influxdb.adapter;

import org.anyline.metadata.type.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum InfluxDBTypeMetadataAlias implements TypeMetadataAlias {
	ACLITEM                       ("ACLITEM"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	AGG_STATE                     ("AGG_STATE"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	AGGREGATE_METRIC_DOUBLE       ("aggregate_metric_double"        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	ALIAS                         ("alias"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	ARRAY                         ("ARRAY"                          ,StandardTypeMetadata.ARRAY                         , "Array"    , "Array"    , null   , null   , null   ,   2,   2,   2),
	BFILE                         ("BFILE"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	BFLOAT16_VECTOR               ("BFloat16Vector"                 ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BIGINT                        ("BIGINT"                         ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	BIGSERIAL                     ("BIGSERIAL"                      ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	BINARY                        ("BINARY"                         ,StandardTypeMetadata.BYTES                         , "Bytes"    , "Bytes"    , null   , null   , null   ,  -1,  -1,  -1),
	BINARY_DOUBLE                 ("BINARY_DOUBLE"                  ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	BINARY_FLOAT                  ("BINARY_FLOAT"                   ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	BINARY_INTEGER                ("BINARY_INTEGER"                 ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	BINARY_VECTOR                 ("BinaryVector"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BIT                           ("BIT"                            ,StandardTypeMetadata.BOOLEAN                       , "Boolean"  , "Boolean"  , null   , null   , null   ,   1,   1,   1),
	BIT_VARYING                   ("BIT VARYING"                    ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	BITMAP                        ("BITMAP"                         ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	BLOB                          ("BLOB"                           ,StandardTypeMetadata.BYTES                         , "Bytes"    , "Bytes"    , null   , null   , null   ,  -1,  -1,  -1),
	BOOL                          ("BOOL"                           ,StandardTypeMetadata.BOOLEAN                       , "Boolean"  , "Boolean"  , null   , null   , null   ,   1,   1,   1),
	BOOLEAN                       ("BOOLEAN"                        ,StandardTypeMetadata.BOOLEAN                       , "Boolean"  , "Boolean"  , null   , null   , null   ,   1,   1,   1),
	BOX                           ("BOX"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	BOX2D                         ("BOX2D"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BPCHAR                        ("BPCHAR"                         ,StandardTypeMetadata.CHAR                          ,   0,   1,   1),
	BPCHARBYTE                    ("BPCHARBYTE"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BYTE                          ("BYTE"                           ,StandardTypeMetadata.BOOLEAN                       , "Boolean"  , "Boolean"  , null   , null   , null   ,   1,   1,   1),
	BYTEA                         ("BYTEA"                          ,StandardTypeMetadata.BYTES                         , "Bytes"    , "Bytes"    , null   , null   , null   ,  -1,  -1,  -1),
	CHAR                          ("CHAR"                           ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	CHARACTER                     ("CHARACTER"                      ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	CID                           ("CID"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	CIDR                          ("CIDR"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	CIRCLE                        ("CIRCLE"                         ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	CLOB                          ("CLOB"                           ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	COMPLETION                    ("completion"                     ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	CURSOR                        ("CURSOR"                         ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	DATE                          ("DATE"                           ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	DATE_NANOS                    ("date_nanos"                     ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	DATE32                        ("Date32"                         ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	DATERANGE                     ("DATERANGE"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	DATETIME                      ("DATETIME"                       ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	DATETIME64                    ("DateTime"                       ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	DATETIME_WITH_TIME_ZONE       ("DATETIME WITH TIME ZONE"        ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	DATETIME2                     ("DATETIME2"                      ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	DATETIMEOFFSET                ("DATETIMEOFFSET"                 ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	DATETIMEV2                    ("DATETIMEV2"                     ,StandardTypeMetadata.DATETIME                      ,   1,   1,   1),
	DATEV2                        ("DATEV2"                         ,StandardTypeMetadata.DATETIME                      ,   1,   1,   1),
	DEC                           ("DEC"                            ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DECFLOAT                      ("DECFLOAT"                       ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DECIMAL                       ("DECIMAL"                        ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DECIMAL128                    ("Decimal128"                     ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DECIMAL256                    ("Decimal256"                     ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DECIMAL32                     ("Decimal32"                      ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DECIMAL64                     ("Decimal64"                      ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DENSE_VECTOR                  ("dense_vector"                   ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	DOUBLE                        ("DOUBLE"                         ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DOUBLE_PRECISION              ("DOUBLE PRECISION"               ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DSINTERVAL                    ("DSINTERVAL"                     ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	DURATION                      ("DURATION"                       ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	ENUM                          ("ENUM"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	FIXED_STRING                  ("FIXED_STRING"                   ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	FIXEDSTRING                   ("FixedString"                    ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	FLATTENED                     ("flattened"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	FLOAT                         ("FLOAT"                          ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	FLOAT16_VECTOR                ("Float16Vector"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FLOAT32                       ("Float32"                        ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	FLOAT4                        ("FLOAT4"                         ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   1,   1),
	FLOAT64                       ("Float64"                        ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	FLOAT8                        ("FLOAT8"                         ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   1,   1),
	FLOAT_VECTOR                  ("FloatVector"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEO_POINT                     ("geo_point"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	GEO_SHAPE                     ("geo_shape"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	GEOGRAPHY                     ("GEOGRAPHY"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	GEOGRAPHY_POINT               ("GEOGRAPHY_POINT"                ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	GEOMETRY                      ("GEOMETRY"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	GEOMETRYCOLLECTION            ("GEOMETRYCOLLECTION"             ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	GTSVECTOR                     ("GTSVECTOR"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	GUID                          ("GUID"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	HALF_FLOAT                    ("half_float"                     ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	HIERARCHYID                   ("HIERARCHYID"                    ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	HISTOGRAM                     ("histogram"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	HLL                           ("HLL"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	IMAGE                         ("IMAGE"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	INET                          ("INET"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	INT                           ("INT"                            ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT128                        ("INT128"                         ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT16                         ("INT16"                          ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT2                          ("INT2"                           ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT256                        ("INT256"                         ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT32                         ("INT32"                          ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT4                          ("INT4"                           ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT4RANGE                     ("INT4RANGE"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	INT64                         ("INT64"                          ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT8                          ("INT8"                           ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INT8RANGE                     ("INT8RANGE"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	INTEGER                       ("INTEGER"                        ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	INTERVAL                      ("INTERVAL"                       ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_DAY                  ("INTERVAL DAY"                   ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_DAY_HOUR             ("INTERVAL DAY TO HOUR"           ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_DAY_MINUTE           ("INTERVAL DAY TO MINUTE"         ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_DAY_SECOND           ("INTERVAL DAY TO SECOND"         ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_HOUR                 ("INTERVAL HOUR"                  ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_HOUR_MINUTE          ("INTERVAL HOUR TO MINUTE"        ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_HOUR_SECOND          ("INTERVAL HOUR TO SECOND"        ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_MINUTE               ("INTERVAL MINUTE"                ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_MINUTE_SECOND        ("INTERVAL MINUTE TO SECOND"      ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_MONTH                ("INTERVAL MONTH"                 ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_SECOND               ("INTERVAL SECOND"                ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_YEAR                 ("INTERVAL YEAR"                  ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	INTERVAL_YEAR_MONTH           ("INTERVAL YEAR TO MONTH"         ,StandardTypeMetadata.DURATION                      , "Duration" , "Duration" , null   , null   , null   ,   1,   1,   1),
	IP                            ("ip"                             ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	IPV4                          ("IPV4"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	IPV6                          ("IPV6"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	JAVA_OBJECT                   ("JAVA_OBJECT"                    ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	JOIN                          ("join"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	JSON                          ("JSON"                           ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	JSONB                         ("JSONB"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	JSONPATH                      ("JSONPATH"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	KEYWORD                       ("KEYWORD"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	LARGEINT                      ("LARGEINT"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	LINE                          ("LINE"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	LINESTRING                    ("LINESTRING"                     ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	LIST                          ("LIST"                           ,StandardTypeMetadata.ARRAY                         , "Array"    , "Array"    , null   , null   , null   ,   2,   2,   2),
	LONG                          ("long"                           ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	LONG_TEXT                     ("LONG"                           ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	LONGBLOB                      ("LONGBLOB"                       ,StandardTypeMetadata.BYTES                         , "Bytes"    , "Bytes"    , null   , null   , null   ,  -1,  -1,  -1),
	LONGTEXT                      ("LONGTEXT"                       ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	LOWCARDINALITY                ("LowCardinality"                 ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	LSEG                          ("LSEG"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	LVARCHAR                      ("LVARCHAR"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	MACADDR                       ("MACADDR"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	MACADDR8                      ("MACADDR8"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	MAP                           ("MAP"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	MATCH_ONLY_TEXT               ("match_only_text "               ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	MEDIUMBLOB                    ("MEDIUMBLOB"                     ,StandardTypeMetadata.BYTES                         , "Bytes"    , "Bytes"    , null   , null   , null   ,  -1,  -1,  -1),
	MEDIUMINT                     ("MEDIUMINT"                      ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	MEDIUMTEXT                    ("MEDIUMTEXT"                     ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	MONEY                         ("MONEY"                          ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	MULTILINESTRING               ("MULTILINESTRING"                ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	MULTIPOINT                    ("MULTIPOINT"                     ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	MULTIPOLYGON                  ("MULTIPOLYGON"                   ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	MULTISET                      ("MULTISET"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	NATURALN                      ("NATURALN"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	NCHAR                         ("NCHAR"                          ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	NCLOB                         ("NCLOB"                          ,StandardTypeMetadata.BYTES                         , "Bytes"    , "Bytes"    , null   , null   , null   ,  -1,  -1,  -1),
	NESTED                        ("nested"                         ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	NTEXT                         ("NTEXT"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	NUMBER                        ("NUMBER"                         ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	NUMERIC                       ("NUMERIC"                        ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	NUMRANGE                      ("NUMRANGE"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	NVARCHAR                      ("NVARCHAR"                       ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	NVARCHAR2                     ("NVARCHAR2"                      ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	OBJECT                        ("OBJECT"                         ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	OID                           ("OID"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	ORA_DATE                      ("ORA_DATE"                       ,StandardTypeMetadata.TIME                          ,   1,   1,   1),
	PATH                          ("PATH"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	PERCOLATOR                    ("percolator"                     ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	PG_SNAPSHOT                   ("PG_SNAPSHOT"                    ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	POINT                         ("POINT"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	POLYGON                       ("POLYGON"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	POSITIVE                      ("POSITIVE"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	POSITIVEN                     ("POSITIVEN"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	RANGE                         ("Range"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	RANK_FEATURE                  ("rank_feature"                   ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	RANK_FEATURES                 ("rank_features"                  ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	RAW                           ("RAW"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REAL                          ("REAL"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REFCURSOR                     ("REFCURSOR"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGCLASS                      ("REGCLASS"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGCONFIG                     ("REGCONFIG"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGDICTIONARY                 ("REGDICTIONARY"                  ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGNAMESPACE                  ("REGNAMESPACE"                   ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGOPER                       ("REGOPER"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGOPERATOR                   ("REGOPERATOR"                    ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGPROC                       ("REGPROC"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGPROCEDURE                  ("REGPROCEDURE"                   ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGROLE                       ("REGROLE"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	REGTYPE                       ("REGTYPE"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	RING                          ("RING"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	ROW                           ("ROW"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	ROWID                         ("ROWID"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SCALED_FLOAT                  ("scaled_float"                   ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	SEARCH_AS_YOU_TYPE            ("search_as_you_type"             ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SECONDDATE                    ("SECONDDATE"                     ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SERIAL                        ("SERIAL"                         ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	SERIAL2                       ("SERIAL2"                        ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	SERIAL4                       ("SERIAL4"                        ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	SERIAL8                       ("SERIAL8"                        ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	SET                           ("SET"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SHAPE                         ("shape"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SHORT                         ("SHORT"                          ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	SIGNTYPE                      ("SIGNTYPE"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SIMPLE_DOUBLE                 ("SIMPLE_DOUBLE"                  ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	SIMPLE_FLOAT                  ("SIMPLE_FLOAT"                   ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	SIMPLE_INTEGER                ("SIMPLE_INTEGER"                 ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	SIMPLEAGGREGATEFUNCTION       ("SimpleAggregateFunction"        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SMALLDATETIME                 ("SMALLDATETIME"                  ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	SMALLDECIMAL                  ("SMALLDECIMAL"                   ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	SMALLFLOAT                    ("SMALLFLOAT"                     ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	SMALLINT                      ("SMALLINT"                       ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	SMALLMONEY                    ("SMALLMONEY"                     ,StandardTypeMetadata.FLOAT                         , "Float"    , "Float"    , null   , null   , null   ,   1,   2,   3),
	SMALLSERIAL                   ("SMALLSERIAL"                    ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	SPARSE_VECTOR                 ("sparse_vector"                  ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SPARSE_FLOAT_VECTOR           ("SparseFloatVector"              ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SQL_DATETIMEOFFSET            ("SQL_DATETIMEOFFSET"             ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SQL_VARIANT                   ("SQL_VARIANT"                    ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	ST_GEOMETRY                   ("ST_GEOMETRY"                    ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	ST_POINT                      ("ST_POINT"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	STRING                        ("STRING"                         ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	STRUCT                        ("STRUCT"                         ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	STRUCTS                       ("STRUCTS"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SYS_REFCURSOR                 ("SYS_REFCURSOR"                  ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	SYSNAME                       ("SYSNAME"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	TEXT                          ("TEXT"                           ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	TID                           ("TID"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	TIME                          ("TIME"                           ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIME_TZ_UNCONSTRAINED         ("TIME TZ UNCONSTRAINED"          ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIME_WITH_TIME_ZONE           ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIME_WITH_ZONE                ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIME_WITHOUT_TIME_ZONE        ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIME_WITHOUT_ZONE             ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIME_UNCONSTRAINED            ("TIME_UNCONSTRAINED"             ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP                     ("TIMESTAMP"                      ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITH_LOCAL_ZONE     ("TIMESTAMP WITH LOCAL TIME ZONE" ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITH_TIME_ZONE      ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITH_ZONE           ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITHOUT_TIME_ZONE   ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITHOUT_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_NTZ                 ("TIMESTAMP_NTZ"                  ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE   ,   1,   2,   1),
	TIMESTAMPTZ                   ("TIMESTAMPTZ"                    ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TIMETZ                        ("TIMETZ"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TIMEZ                         ("TIMEZ"                          ,StandardTypeMetadata.DATETIME                      , "Time"     , "Time"     , null   , null   , null   ,   1,   1,   1),
	TINYBLOB                      ("TINYBLOB"                       ,StandardTypeMetadata.BYTES                         , "Bytes"    , "Bytes"    , null   , null   , null   ,  -1,  -1,  -1),
	TINYINT                       ("TINYINT"                        ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	TINYTEXT                      ("TINYTEXT"                       ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	TOKEN_COUNT                   ("token_count"                    ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	TSQUERY                       ("TSQUERY"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	TSRANGE                       ("TSRANGE"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	TSTZRANGE                     ("TSTZRANGE"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	TSVECTOR                      ("TSVECTOR"                       ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	TUPLE                         ("TUPLE"                          ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	TXID_SNAPSHOT                 ("TXID_SNAPSHOT"                  ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	UINT128                       ("UInt128"                        ,StandardTypeMetadata.INT32                         , "integer"  , "integer"  , null   , null   , null   ,   1,   1,   1),
	UINT16                        ("UInt16"                         ,StandardTypeMetadata.INT32                         , "integer"  , "integer"  , null   , null   , null   ,   1,   1,   1),
	UINT256                       ("UInt256"                        ,StandardTypeMetadata.INT32                         , "integer"  , "integer"  , null   , null   , null   ,   1,   1,   1),
	UINT32                        ("UInt32"                         ,StandardTypeMetadata.INT32                         , "integer"  , "integer"  , null   , null   , null   ,   1,   1,   1),
	UINT64                        ("UInt64"                         ,StandardTypeMetadata.INT32                         , "integer"  , "integer"  , null   , null   , null   ,   1,   1,   1),
	UINT8                         ("UInt8"                          ,StandardTypeMetadata.INT32                         , "integer"  , "integer"  , null   , null   , null   ,   1,   1,   1),
	UNIQUEIDENTIFIER              ("UNIQUEIDENTIFIER"               ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	UNSIGNED_LONG                 ("unsigned_long"                  ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	UROWID                        ("UROWID"                         ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	UUID                          ("UUID"                           ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	VARBINARY                     ("VARBINARY"                      ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	VARBIT                        ("VARBIT"                         ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	VARBYTE                       ("VARBYTE"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	VARCHAR                       ("VARCHAR"                        ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	VARCHAR2                      ("VARCHAR2"                       ,StandardTypeMetadata.STRING                        , "String"   , "String"   , null   , null   , null   ,   1,   1,   1),
	VARCHARBYTE                   ("VARCHARBYTE"                    ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	VECTOR                        ("VECTOR"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	VERSION                       ("version"                        ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	XID                           ("XID"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	XML                           ("XML"                            ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1),
	XMLTYPE                       ("XMLTYPE"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	YEAR                          ("YEAR"                           ,StandardTypeMetadata.INTEGER                       , "Integer"  , "Integer"  , null   , null   , null   ,   1,   1,   1),
	YMINTERVAL                    ("YMINTERVAL"                     ,StandardTypeMetadata.ILLEGAL                       ,  -1,  -1,  -1);

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
	private TypeMetadata.Refer refer         ; // 集成元数据读写配置

	InfluxDBTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
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

	InfluxDBTypeMetadataAlias(String input, TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null , null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	InfluxDBTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	InfluxDBTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	InfluxDBTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(null, standard, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	InfluxDBTypeMetadataAlias(String input, TypeMetadata standard) {
		this.input = input;
		this.standard = standard;
	}

	InfluxDBTypeMetadataAlias(TypeMetadata standard) {
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
