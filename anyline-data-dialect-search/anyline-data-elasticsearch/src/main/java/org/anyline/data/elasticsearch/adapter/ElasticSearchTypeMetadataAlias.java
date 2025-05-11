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

package org.anyline.data.elasticsearch.adapter;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum ElasticSearchTypeMetadataAlias implements TypeMetadataAlias {
	AGGREGATE_METRIC_DOUBLE       ("aggregate_metric_double"        ,StandardTypeMetadata.AGGREGATE_METRIC_DOUBLE       ,   1,   1,   1),
	ALIAS                         ("alias"                          ,StandardTypeMetadata.ALIAS                         ,  -1,  -1,  -1),
	BINARY                        ("BINARY"                         ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	BOOLEAN                       ("BOOLEAN"                        ,StandardTypeMetadata.BOOLEAN                       , "boolean"       , "boolean"       , null   , null   , null   ,   1,   1,   1),
	BYTE                          ("BYTE"                           ,StandardTypeMetadata.BYTE                          , "byte"          , "byte"          , null   , null   , null   ,   1,   1,   1),
	COMPLETION                    ("completion"                     ,StandardTypeMetadata.COMPLETION                    ,  -1,  -1,  -1),
	DATE                          ("DATE"                           ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	DATE_NANOS                    ("date_nanos"                     ,StandardTypeMetadata.DATE_NANOS                    ,   1,   1,   1),
	DENSE_VECTOR                  ("dense_vector"                   ,StandardTypeMetadata.DENSE_VECTOR                  ,   1,   0,   1),
	DOUBLE                        ("DOUBLE"                         ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	FLATTENED                     ("flattened"                      ,StandardTypeMetadata.FLATTENED                     , "nested"        , "nested"        , null   , null   , null   ,  -1,  -1,  -1),
	FLOAT                         ("FLOAT"                          ,StandardTypeMetadata.FLOAT                         , "float"         , "float"         , null   , null   , null   ,   1,   1,   1),
	GEO_POINT                     ("geo_point"                      ,StandardTypeMetadata.GEO_POINT                     ,  -1,  -1,  -1),
	GEO_SHAPE                     ("geo_shape"                      ,StandardTypeMetadata.GEO_SHAPE                     ,  -1,  -1,  -1),
	HALF_FLOAT                    ("half_float"                     ,StandardTypeMetadata.HALF_FLOAT                    , "half_float"    , "half_float"    , null   , null   , null   ,  -1,  -1,  -1),
	HISTOGRAM                     ("histogram"                      ,StandardTypeMetadata.HISTOGRAM                     ,  -1,  -1,  -1),
	INTEGER                       ("INTEGER"                        ,StandardTypeMetadata.INTEGER                       , "integer"       , "integer"       , null   , null   , null   ,   1,   1,   1),
	IP                            ("ip"                             ,StandardTypeMetadata.IP                            , "ip"            , "ip"            , null   , null   , null   ,  -1,  -1,  -1),
	JOIN                          ("join"                           ,StandardTypeMetadata.JOIN                          , "join"          , "join"          , null   , null   , null   ,  -1,  -1,  -1),
	KEYWORD                       ("KEYWORD"                        ,StandardTypeMetadata.KEYWORD                       , "keyword"       , "keyword"       , null   , null   , null   ,   1,   1,   1),
	LONG_TEXT                     ("LONG"                           ,StandardTypeMetadata.LONG_TEXT                     , "long"          , "long"          , null   , null   , null   ,   1,   1,   1),
	LONG                          ("long"                           ,StandardTypeMetadata.LONG_TEXT                     ,   1,   1,   1),
	MATCH_ONLY_TEXT               ("match_only_text "               ,StandardTypeMetadata.MATCH_ONLY_TEXT               ,  -1,  -1,  -1),
	NESTED                        ("nested"                         ,StandardTypeMetadata.NESTED                        ,  -1,  -1,  -1),
	OBJECT                        ("OBJECT"                         ,StandardTypeMetadata.OBJECT                        , "object"        , "object"        , null   , null   , null   ,   1,   1,   1),
	PERCOLATOR                    ("percolator"                     ,StandardTypeMetadata.PERCOLATOR                    ,  -1,  -1,  -1),
	RANGE                         ("Range"                          ,StandardTypeMetadata.RANGE                         ,  -1,  -1,  -1),
	RANK_FEATURE                  ("rank_feature"                   ,StandardTypeMetadata.RANK_FEATURE                  ,  -1,  -1,  -1),
	RANK_FEATURES                 ("rank_features"                  ,StandardTypeMetadata.RANK_FEATURES                 ,  -1,  -1,  -1),
	SCALED_FLOAT                  ("scaled_float"                   ,StandardTypeMetadata.SCALED_FLOAT                  , "scaled_float"  , "scaled_float"  , null   , null   , null   ,  -1,  -1,  -1),
	SHAPE                         ("shape"                          ,StandardTypeMetadata.SHAPE                         ,  -1,  -1,  -1),
	SPARSE_VECTOR                 ("sparse_vector"                  ,StandardTypeMetadata.SPARSE_VECTOR                 ,   1,   0,   1),
	TEXT                          ("TEXT"                           ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	TOKEN_COUNT                   ("token_count"                    ,StandardTypeMetadata.TOKEN_COUNT                   ,  -1,  -1,  -1),
	UNSIGNED_LONG                 ("unsigned_long"                  ,StandardTypeMetadata.UNSIGNED_LONG                 , "unsigned_long" , "unsigned_long" , null   , null   , null   ,  -1,  -1,  -1),
	VERSION                       ("version"                        ,StandardTypeMetadata.VERSION                       ,  -1,  -1,  -1),
	ACLITEM                       ("ACLITEM"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	AGG_STATE                     ("AGG_STATE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ARRAY                         ("ARRAY"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BFILE                         ("BFILE"                          ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	BFLOAT16_VECTOR               ("BFloat16Vector"                 ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BIGINT                        ("BIGINT"                         ,StandardTypeMetadata.LONG                          , "long"          , "long"          , null   , null   , null   ,   1,   1,   1),
	BIGSERIAL                     ("BIGSERIAL"                      ,StandardTypeMetadata.LONG                          , "long"          , "long"          , null   , null   , null   ,   1,   1,   1),
	BINARY_DOUBLE                 ("BINARY_DOUBLE"                  ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	BINARY_FLOAT                  ("BINARY_FLOAT"                   ,StandardTypeMetadata.FLOAT                         , "float"         , "float"         , null   , null   , null   ,   1,   1,   1),
	BINARY_INTEGER                ("BINARY_INTEGER"                 ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BINARY_VECTOR                 ("BinaryVector"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BIT                           ("BIT"                            ,StandardTypeMetadata.BYTE                          , "byte"          , "byte"          , null   , null   , null   ,   1,   1,   1),
	BIT_VARYING                   ("BIT VARYING"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BITMAP                        ("BITMAP"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BLOB                          ("BLOB"                           ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	BOOL                          ("BOOL"                           ,StandardTypeMetadata.BOOLEAN                       , "boolean"       , "boolean"       , null   , null   , null   ,   1,   1,   1),
	BOX                           ("BOX"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BOX2D                         ("BOX2D"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BPCHAR                        ("BPCHAR"                         ,StandardTypeMetadata.CHAR                          ,   0,   1,   1),
	BPCHARBYTE                    ("BPCHARBYTE"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	BYTEA                         ("BYTEA"                          ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	CHAR                          ("CHAR"                           ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	CHARACTER                     ("CHARACTER"                      ,StandardTypeMetadata.VARCHAR                       ,   0,   1,   1),
	CID                           ("CID"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CIDR                          ("CIDR"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CIRCLE                        ("CIRCLE"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	CLOB                          ("CLOB"                           ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	CURSOR                        ("CURSOR"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATE32                        ("Date32"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATERANGE                     ("DATERANGE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATETIME64                    ("DateTime"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATETIME                      ("DATETIME"                       ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	DATETIME_WITH_TIME_ZONE       ("DATETIME WITH TIME ZONE"        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DATETIME2                     ("DATETIME2"                      ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	DATETIMEOFFSET                ("DATETIMEOFFSET"                 ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	DATETIMEV2                    ("DATETIMEV2"                     ,StandardTypeMetadata.DATE                          ,   1,   1,   1),
	DATEV2                        ("DATEV2"                         ,StandardTypeMetadata.DATE                          ,   1,   1,   1),
	DEC                           ("DEC"                            ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	DECFLOAT                      ("DECFLOAT"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DECIMAL                       ("DECIMAL"                        ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	DECIMAL128                    ("Decimal128"                     ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	DECIMAL256                    ("Decimal256"                     ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	DECIMAL32                     ("Decimal32"                      ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	DECIMAL64                     ("Decimal64"                      ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	DOUBLE_PRECISION              ("DOUBLE PRECISION"               ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DSINTERVAL                    ("DSINTERVAL"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	DURATION                      ("DURATION"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ENUM                          ("ENUM"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FIXED_STRING                  ("FIXED_STRING"                   ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	FIXEDSTRING                   ("FixedString"                    ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	FLOAT16_VECTOR                ("Float16Vector"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FLOAT32                       ("Float32"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FLOAT4                        ("FLOAT4"                         ,StandardTypeMetadata.FLOAT                         , "half_float"    , "half_float"    , null   , null   , null   ,   1,   1,   1),
	FLOAT64                       ("Float64"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	FLOAT8                        ("FLOAT8"                         ,StandardTypeMetadata.FLOAT                         , "half_float"    , "half_float"    , null   , null   , null   ,   1,   1,   1),
	FLOAT_VECTOR                  ("FloatVector"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEOGRAPHY                     ("GEOGRAPHY"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEOGRAPHY_POINT               ("GEOGRAPHY_POINT"                ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEOMETRY                      ("GEOMETRY"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GEOMETRYCOLLECTION            ("GEOMETRYCOLLECTION"             ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GTSVECTOR                     ("GTSVECTOR"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	GUID                          ("GUID"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	HIERARCHYID                   ("HIERARCHYID"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	HLL                           ("HLL"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	IMAGE                         ("IMAGE"                          ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	INET                          ("INET"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INT                           ("INT"                            ,StandardTypeMetadata.INTEGER                       , "integer"       , "integer"       , null   , null   , null   ,   1,   1,   1),
	INT128                        ("INT128"                         ,StandardTypeMetadata.LONG                          , "long"          , "long"          , null   , null   , null   ,  -1,  -1,  -1),
	INT16                         ("INT16"                          ,StandardTypeMetadata.SHORT                         , "short"         , "short"         , null   , null   , null   ,   1,   1,   1),
	INT2                          ("INT2"                           ,StandardTypeMetadata.SHORT                         , "short"         , "short"         , null   , null   , null   ,   1,   1,   1),
	INT256                        ("INT256"                         ,StandardTypeMetadata.LONG                          , "long"          , "long"          , null   , null   , null   ,  -1,  -1,  -1),
	INT32                         ("INT32"                          ,StandardTypeMetadata.LONG                          , "long"          , "long"          , null   , null   , null   ,  -1,  -1,  -1),
	INT4                          ("INT4"                           ,StandardTypeMetadata.INTEGER                       , "integer"       , "integer"       , null   , null   , null   ,   1,   1,   1),
	INT4RANGE                     ("INT4RANGE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	INT64                         ("INT64"                          ,StandardTypeMetadata.LONG                          , "long"          , "long"          , null   , null   , null   ,  -1,  -1,  -1),
	INT8                          ("INT8"                           ,StandardTypeMetadata.LONG_TEXT                     , "long"          , "long"          , null   , null   , null   ,   1,   1,   1),
	INT8RANGE                     ("INT8RANGE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
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
	IPV4                          ("IPV4"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	IPV6                          ("IPV6"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	JAVA_OBJECT                   ("JAVA_OBJECT"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	JSON                          ("JSON"                           ,StandardTypeMetadata.OBJECT                        , "object"        , "object"        , null   , null   , null   ,   1,   1,   1),
	JSONB                         ("JSONB"                          ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	JSONPATH                      ("JSONPATH"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LARGEINT                      ("LARGEINT"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LINE                          ("LINE"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LINESTRING                    ("LINESTRING"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LIST                          ("LIST"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LONGBLOB                      ("LONGBLOB"                       ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	LONGTEXT                      ("LONGTEXT"                       ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	LOWCARDINALITY                ("LowCardinality"                 ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LSEG                          ("LSEG"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	LVARCHAR                      ("LVARCHAR"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MACADDR                       ("MACADDR"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MACADDR8                      ("MACADDR8"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MAP                           ("MAP"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MEDIUMBLOB                    ("MEDIUMBLOB"                     ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	MEDIUMINT                     ("MEDIUMINT"                      ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	MEDIUMTEXT                    ("MEDIUMTEXT"                     ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	MONEY                         ("MONEY"                          ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	MULTILINESTRING               ("MULTILINESTRING"                ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MULTIPOINT                    ("MULTIPOINT"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MULTIPOLYGON                  ("MULTIPOLYGON"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	MULTISET                      ("MULTISET"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	NATURALN                      ("NATURALN"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	NCHAR                         ("NCHAR"                          ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	NCLOB                         ("NCLOB"                          ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	NTEXT                         ("NTEXT"                          ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	NUMBER                        ("NUMBER"                         ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	NUMERIC                       ("NUMERIC"                        ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	NUMRANGE                      ("NUMRANGE"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	NVARCHAR                      ("NVARCHAR"                       ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	NVARCHAR2                     ("NVARCHAR2"                      ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	OID                           ("OID"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ORA_DATE                      ("ORA_DATE"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	PATH                          ("PATH"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	PG_SNAPSHOT                   ("PG_SNAPSHOT"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	POINT                         ("POINT"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	POLYGON                       ("POLYGON"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	POSITIVE                      ("POSITIVE"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	POSITIVEN                     ("POSITIVEN"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	RAW                           ("RAW"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	REAL                          ("REAL"                           ,StandardTypeMetadata.FLOAT                         , "float"         , "float"         , null   , null   , null   ,   1,   1,   1),
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
	RING                          ("RING"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ROW                           ("ROW"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ROWID                         ("ROWID"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SECONDDATE                    ("SECONDDATE"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SERIAL                        ("SERIAL"                         ,StandardTypeMetadata.SHORT                         , "short"         , "short"         , null   , null   , null   ,   1,   1,   1),
	SERIAL2                       ("SERIAL2"                        ,StandardTypeMetadata.SHORT                         , "short"         , "short"         , null   , null   , null   ,   1,   1,   1),
	SERIAL4                       ("SERIAL4"                        ,StandardTypeMetadata.SHORT                         , "short"         , "short"         , null   , null   , null   ,   1,   1,   1),
	SERIAL8                       ("SERIAL8"                        ,StandardTypeMetadata.LONG                          , "long"          , "long"          , null   , null   , null   ,   1,   1,   1),
	SET                           ("SET"                            ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	SHORT                         ("SHORT"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIGNTYPE                      ("SIGNTYPE"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIMPLE_DOUBLE                 ("SIMPLE_DOUBLE"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIMPLE_FLOAT                  ("SIMPLE_FLOAT"                   ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIMPLE_INTEGER                ("SIMPLE_INTEGER"                 ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SIMPLEAGGREGATEFUNCTION       ("SimpleAggregateFunction"        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SMALLDATETIME                 ("SMALLDATETIME"                  ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	SMALLDECIMAL                  ("SMALLDECIMAL"                   ,StandardTypeMetadata.DECIMAL                       ,   1,   0,   0),
	SMALLFLOAT                    ("SMALLFLOAT"                     ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SMALLINT                      ("SMALLINT"                       ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	SMALLMONEY                    ("SMALLMONEY"                     ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	SMALLSERIAL                   ("SMALLSERIAL"                    ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	SPARSE_FLOAT_VECTOR           ("SparseFloatVector"              ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SQL_DATETIMEOFFSET            ("SQL_DATETIMEOFFSET"             ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SQL_VARIANT                   ("SQL_VARIANT"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ST_GEOMETRY                   ("ST_GEOMETRY"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	ST_POINT                      ("ST_POINT"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	STRING                        ("STRING"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	STRUCT                        ("STRUCT"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	STRUCTS                       ("STRUCTS"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SYS_REFCURSOR                 ("SYS_REFCURSOR"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	SYSNAME                       ("SYSNAME"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TID                           ("TID"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TIME                          ("TIME"                           ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIME_TZ_UNCONSTRAINED         ("TIME TZ UNCONSTRAINED"          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TIME_WITH_TIME_ZONE           ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIME_WITH_ZONE                ("TIME WITH TIME ZONE"            ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIME_WITHOUT_TIME_ZONE        ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIME_WITHOUT_ZONE             ("TIME WITHOUT TIME ZONE"         ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIME_UNCONSTRAINED            ("TIME_UNCONSTRAINED"             ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TIMESTAMP                     ("TIMESTAMP"                      ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITH_LOCAL_ZONE     ("TIMESTAMP WITH LOCAL TIME ZONE" ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITH_ZONE           ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITH_TIME_ZONE      ("TIMESTAMP WITH TIME ZONE"       ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITHOUT_TIME_ZONE   ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_WITHOUT_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"    ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TIMESTAMP_NTZ                 ("TIMESTAMP_NTZ"                  ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE   ,   1,   2,   1),
	TIMESTAMPTZ                   ("TIMESTAMPTZ"                    ,StandardTypeMetadata.NONE                          ,   1,  -1,  -1),
	TIMETZ                        ("TIMETZ"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TIMEZ                         ("TIMEZ"                          ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
	TINYBLOB                      ("TINYBLOB"                       ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	TINYINT                       ("TINYINT"                        ,StandardTypeMetadata.DOUBLE                        , "double"        , "double"        , null   , null   , null   ,   1,   0,   0),
	TINYTEXT                      ("TINYTEXT"                       ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	TSQUERY                       ("TSQUERY"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TSRANGE                       ("TSRANGE"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TSTZRANGE                     ("TSTZRANGE"                      ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TSVECTOR                      ("TSVECTOR"                       ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TUPLE                         ("TUPLE"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	TXID_SNAPSHOT                 ("TXID_SNAPSHOT"                  ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UINT128                       ("UInt128"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UINT16                        ("UInt16"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UINT256                       ("UInt256"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UINT32                        ("UInt32"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UINT64                        ("UInt64"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UINT8                         ("UInt8"                          ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UNIQUEIDENTIFIER              ("UNIQUEIDENTIFIER"               ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UROWID                        ("UROWID"                         ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	UUID                          ("UUID"                           ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	VARBINARY                     ("VARBINARY"                      ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	VARBIT                        ("VARBIT"                         ,StandardTypeMetadata.BINARY                        , "binary"        , "binary"        , null   , null   , null   ,   0,   1,   1),
	VARBYTE                       ("VARBYTE"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	VARCHAR                       ("VARCHAR"                        ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	VARCHAR2                      ("VARCHAR2"                       ,StandardTypeMetadata.TEXT                          , "text"          , "text"          , null   , null   , null   ,   1,   1,   1),
	VARCHARBYTE                   ("VARCHARBYTE"                    ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	VECTOR                        ("VECTOR"                         ,StandardTypeMetadata.DENSE_VECTOR                  ,   1,   0,   1),
	XID                           ("XID"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	XML                           ("XML"                            ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	XMLTYPE                       ("XMLTYPE"                        ,StandardTypeMetadata.NONE                          ,  -1,  -1,  -1),
	YEAR                          ("YEAR"                           ,StandardTypeMetadata.DATE                          , "date"          , "date"          , null   , null   , null   ,   1,   1,   1),
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
	private TypeMetadata.Refer refer         ; // 集成元数据读写配置

	ElasticSearchTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
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

	ElasticSearchTypeMetadataAlias(String input, TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null , null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	ElasticSearchTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	ElasticSearchTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(input, standard, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	ElasticSearchTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
		this(null, standard, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	ElasticSearchTypeMetadataAlias(String input, TypeMetadata standard) {
		this.input = input;
		this.standard = standard;
	}

	ElasticSearchTypeMetadataAlias(TypeMetadata standard) {
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
