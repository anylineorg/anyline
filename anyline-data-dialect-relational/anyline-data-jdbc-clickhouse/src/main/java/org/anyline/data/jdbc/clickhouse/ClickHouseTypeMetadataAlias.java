package org.anyline.data.jdbc.clickhouse;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum ClickHouseTypeMetadataAlias implements TypeMetadataAlias {
	ARRAY                              ("ARRAY"                            ,StandardTypeMetadata.ARRAY                              , 2, 2, 2),
	BOOL                               ("BOOL"                             ,StandardTypeMetadata.BOOL                               , 1, 1, 1),
	DATE                               ("DATE"                             ,StandardTypeMetadata.DATE                               , 1, 1, 1),
	CLICKHOUSE_DATE32                  ("DATE32"                           ,StandardTypeMetadata.CLICKHOUSE_DATE32                  , "Date32"                           , null                               , 1, 1, 1),
	DATETIME                           ("DATETIME"                         ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	CLICKHOUSE_DATETIME64              ("DATETIME64"                       ,StandardTypeMetadata.CLICKHOUSE_DATETIME64              , "DateTime64"                       , null                               , 1, 1, 1),
	DECIMAL                            ("DECIMAL"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	CLICKHOUSE_DECIMAL128              ("DECIMAL128"                       ,StandardTypeMetadata.CLICKHOUSE_DECIMAL128              , "Decimal128"                       , null                               , 1, 0, 2),
	CLICKHOUSE_DECIMAL256              ("DECIMAL256"                       ,StandardTypeMetadata.CLICKHOUSE_DECIMAL256              , "Decimal256"                       , null                               , 1, 0, 2),
	CLICKHOUSE_DECIMAL32               ("DECIMAL32"                        ,StandardTypeMetadata.CLICKHOUSE_DECIMAL32               , "Decimal32"                        , null                               , 1, 0, 2),
	CLICKHOUSE_DECIMAL64               ("DECIMAL64"                        ,StandardTypeMetadata.CLICKHOUSE_DECIMAL64               , "Decimal64"                        , null                               , 1, 0, 2),
	ENUM                               ("ENUM"                             ,StandardTypeMetadata.ENUM                               , 1, 1, 1),
	FIXEDSTRING                        ("FixedString"                      ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	CLICKHOUSE_FLOAT32                 ("FLOAT32"                          ,StandardTypeMetadata.CLICKHOUSE_FLOAT32                 , "Float32"                          , null                               , 1, 1, 1),
	CLICKHOUSE_FLOAT64                 ("FLOAT64"                          ,StandardTypeMetadata.CLICKHOUSE_FLOAT64                 , "Float64"                          , null                               , 1, 1, 1),
	CLICKHOUSE_INT128                  ("INT128"                           ,StandardTypeMetadata.CLICKHOUSE_INT128                  , "Int128"                           , null                               , 1, 1, 1),
	CLICKHOUSE_INT16                   ("INT16"                            ,StandardTypeMetadata.CLICKHOUSE_INT16                   , "Int16"                            , null                               , 1, 1, 1),
	CLICKHOUSE_INT256                  ("INT256"                           ,StandardTypeMetadata.CLICKHOUSE_INT256                  , "Int256"                           , null                               , 1, 1, 1),
	CLICKHOUSE_INT32                   ("INT32"                            ,StandardTypeMetadata.CLICKHOUSE_INT32                   , "Int32"                            , null                               , 1, 1, 1),
	CLICKHOUSE_INT64                   ("INT64"                            ,StandardTypeMetadata.CLICKHOUSE_INT64                   , "Int64"                            , null                               , 1, 1, 1),
	CLICKHOUSE_INT8                    ("INT8"                             ,StandardTypeMetadata.CLICKHOUSE_INT8                    , "Int8"                             , null                               , 1, 1, 1),
	IPV4                               ("IPV4"                             ,StandardTypeMetadata.IPV4                               , 1, 1, 1),
	IPV6                               ("IPV6"                             ,StandardTypeMetadata.IPV6                               , 1, 1, 1),
	LOWCARDINALITY                     ("LowCardinality"                   ,StandardTypeMetadata.LowCardinality                     , 1, 1, 1),
	MAP                                ("MAP"                              ,StandardTypeMetadata.MAP                                , 1, 1, 1),
	MULTIPOLYGON                       ("MULTIPOLYGON"                     ,StandardTypeMetadata.MULTIPOLYGON                       , 1, 1, 1),
	POINT                              ("POINT"                            ,StandardTypeMetadata.POINT                              , 1, 1, 1),
	POLYGON                            ("POLYGON"                          ,StandardTypeMetadata.POLYGON                            , 1, 1, 1),
	RING                               ("RING"                             ,StandardTypeMetadata.RING                               , 1, 1, 1),
	SIMPLEAGGREGATEFUNCTION            ("SimpleAggregateFunction"          ,StandardTypeMetadata.SimpleAggregateFunction            , 1, 1, 1),
	STRING                             ("STRING"                           ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	TUPLE                              ("TUPLE"                            ,StandardTypeMetadata.TUPLE                              , 1, 1, 1),
	CLICKHOUSE_UINT128                 ("UINT128"                          ,StandardTypeMetadata.CLICKHOUSE_UINT128                 , "UInt128"                          , null                               , 1, 1, 1),
	CLICKHOUSE_UINT16                  ("UINT16"                           ,StandardTypeMetadata.CLICKHOUSE_UINT16                  , "UInt16"                           , null                               , 1, 1, 1),
	CLICKHOUSE_UINT256                 ("UINT256"                          ,StandardTypeMetadata.CLICKHOUSE_UINT256                 , "UInt256"                          , null                               , 1, 1, 1),
	CLICKHOUSE_UINT32                  ("UINT32"                           ,StandardTypeMetadata.CLICKHOUSE_UINT32                  , "UInt32"                           , null                               , 1, 1, 1),
	CLICKHOUSE_UINT64                  ("UINT64"                           ,StandardTypeMetadata.CLICKHOUSE_UINT64                  , "UInt64"                           , null                               , 1, 1, 1),
	CLICKHOUSE_UINT8                   ("UINT8"                            ,StandardTypeMetadata.CLICKHOUSE_UINT8                   , "UInt8"                            , null                               , 1, 1, 1),
	UUID                               ("UUID"                             ,StandardTypeMetadata.UUID                               , 1, 1, 1),
	ACLITEM                            ("ACLITEM"                          ,StandardTypeMetadata.NONE                               ),
	AGG_STATE                          ("AGG_STATE"                        ,StandardTypeMetadata.NONE                               ),
	AGGREGATE_METRIC_DOUBLE            ("aggregate_metric_double"          ,StandardTypeMetadata.NONE                               ),
	ALIAS                              ("alias"                            ,StandardTypeMetadata.NONE                               ),
	BFILE                              ("BFILE"                            ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	BIGINT                             ("BIGINT"                           ,StandardTypeMetadata.CLICKHOUSE_INT64                   , "Int64"                            , null                               , 1, 1, 1),
	BIGSERIAL                          ("BIGSERIAL"                        ,StandardTypeMetadata.CLICKHOUSE_INT64                   , "Int64"                            , null                               , 1, 1, 1),
	BINARY                             ("BINARY"                           ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	BINARY_DOUBLE                      ("BINARY_DOUBLE"                    ,StandardTypeMetadata.CLICKHOUSE_FLOAT64                 , "Float64"                          , null                               , 1, 1, 1),
	BINARY_FLOAT                       ("BINARY_FLOAT"                     ,StandardTypeMetadata.CLICKHOUSE_FLOAT32                 , "Float32"                          , null                               , 1, 1, 1),
	BINARY_INTEGER                     ("BINARY_INTEGER"                   ,StandardTypeMetadata.NONE                               ),
	BIT                                ("BIT"                              ,StandardTypeMetadata.BOOL                               , 1, 1, 1),
	BIT_VARYING                        ("BIT VARYING"                      ,StandardTypeMetadata.NONE                               ),
	BITMAP                             ("BITMAP"                           ,StandardTypeMetadata.NONE                               ),
	BLOB                               ("BLOB"                             ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	BOOLEAN                            ("BOOLEAN"                          ,StandardTypeMetadata.NONE                               ),
	BOX                                ("BOX"                              ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	BPCHAR                             ("BPCHAR"                           ,StandardTypeMetadata.NONE                               ),
	BPCHARBYTE                         ("BPCHARBYTE"                       ,StandardTypeMetadata.NONE                               ),
	BYTE                               ("BYTE"                             ,StandardTypeMetadata.NONE                               ),
	BYTEA                              ("BYTEA"                            ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	CHAR                               ("CHAR"                             ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	CHARACTER                          ("CHARACTER"                        ,StandardTypeMetadata.NONE                               ),
	CID                                ("CID"                              ,StandardTypeMetadata.NONE                               ),
	CIDR                               ("CIDR"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	CIRCLE                             ("CIRCLE"                           ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	CLOB                               ("CLOB"                             ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	COMPLETION                         ("completion"                       ,StandardTypeMetadata.NONE                               ),
	CURSOR                             ("CURSOR"                           ,StandardTypeMetadata.NONE                               ),
	DATERANGE                          ("DATERANGE"                        ,StandardTypeMetadata.NONE                               ),
	DATETIME2                          ("DATETIME2"                        ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	DATETIMEOFFSET                     ("DATETIMEOFFSET"                   ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	DEC                                ("DEC"                              ,StandardTypeMetadata.DECIMAL                            ),
	DECFLOAT                           ("DECFLOAT"                         ,StandardTypeMetadata.NONE                               ),
	DENSE_VECTOR                       ("dense_vector"                     ,StandardTypeMetadata.NONE                               ),
	DOUBLE                             ("DOUBLE"                           ,StandardTypeMetadata.CLICKHOUSE_FLOAT64                 , "Float64"                          , null                               , 1, 1, 1),
	DOUBLE_PRECISION                   ("DOUBLE PRECISION"                 ,StandardTypeMetadata.NONE                               ),
	DSINTERVAL                         ("DSINTERVAL"                       ,StandardTypeMetadata.NONE                               ),
	DURATION                           ("DURATION"                         ,StandardTypeMetadata.NONE                               ),
	FIXED_STRING                       ("FIXED_STRING"                     ,StandardTypeMetadata.CHAR                               ),
	FLATTENED                          ("flattened"                        ,StandardTypeMetadata.NONE                               ),
	FLOAT                              ("FLOAT"                            ,StandardTypeMetadata.CLICKHOUSE_FLOAT32                 , "Float32"                          , null                               , 1, 1, 1),
	FLOAT4                             ("FLOAT4"                           ,StandardTypeMetadata.CLICKHOUSE_FLOAT32                 , "Float32"                          , null                               , 1, 1, 1),
	FLOAT8                             ("FLOAT8"                           ,StandardTypeMetadata.CLICKHOUSE_FLOAT32                 , "Float32"                          , null                               , 1, 1, 1),
	GEO_POINT                          ("geo_point"                        ,StandardTypeMetadata.NONE                               ),
	GEO_SHAPE                          ("geo_shape"                        ,StandardTypeMetadata.NONE                               ),
	GEOGRAPHY                          ("GEOGRAPHY"                        ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	GEOGRAPHY_POINT                    ("GEOGRAPHY_POINT"                  ,StandardTypeMetadata.NONE                               ),
	GEOMETRY                           ("GEOMETRY"                         ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	GEOMETRYCOLLECTION                 ("GEOMETRYCOLLECTION"               ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	GTSVECTOR                          ("GTSVECTOR"                        ,StandardTypeMetadata.NONE                               ),
	GUID                               ("GUID"                             ,StandardTypeMetadata.NONE                               ),
	HALF_FLOAT                         ("half_float"                       ,StandardTypeMetadata.NONE                               ),
	HIERARCHYID                        ("HIERARCHYID"                      ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	HISTOGRAM                          ("histogram"                        ,StandardTypeMetadata.NONE                               ),
	HLL                                ("HLL"                              ,StandardTypeMetadata.NONE                               ),
	IMAGE                              ("IMAGE"                            ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	INET                               ("INET"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	INT                                ("INT"                              ,StandardTypeMetadata.CLICKHOUSE_INT32                   , "Int32"                            , null                               , 1, 1, 1),
	INT128                             ("INT128"                           ,StandardTypeMetadata.CLICKHOUSE_INT128                  , "Int128"                           , null                               , 1, 1, 1),
	INT16                              ("INT16"                            ,StandardTypeMetadata.NONE                               ),
	INT2                               ("INT2"                             ,StandardTypeMetadata.NONE                               ),
	INT256                             ("INT256"                           ,StandardTypeMetadata.CLICKHOUSE_INT256                  , "Int256"                           , null                               , 1, 1, 1),
	INT32                              ("INT32"                            ,StandardTypeMetadata.CLICKHOUSE_INT32                   , "Int32"                            , null                               , 1, 1, 1),
	INT4                               ("INT4"                             ,StandardTypeMetadata.NONE                               ),
	INT4RANGE                          ("INT4RANGE"                        ,StandardTypeMetadata.NONE                               ),
	INT64                              ("INT64"                            ,StandardTypeMetadata.CLICKHOUSE_INT64                   , "Int64"                            , null                               , 1, 1, 1),
	INT8                               ("INT8"                             ,StandardTypeMetadata.CLICKHOUSE_INT8                    , "Int8"                             , null                               , 1, 1, 1),
	INT8RANGE                          ("INT8RANGE"                        ,StandardTypeMetadata.NONE                               ),
	INTEGER                            ("INTEGER"                          ,StandardTypeMetadata.INT32                              , 1, 1, 1),
	INTERVAL                           ("INTERVAL"                         ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	INTERVAL_DAY                       ("INTERVAL DAY"                     ,StandardTypeMetadata.NONE                               ),
	INTERVAL_DAY_HOUR                  ("INTERVAL DAY TO HOUR"             ,StandardTypeMetadata.NONE                               ),
	INTERVAL_DAY_MINUTE                ("INTERVAL DAY TO MINUTE"           ,StandardTypeMetadata.NONE                               ),
	INTERVAL_DAY_SECOND                ("INTERVAL DAY TO SECOND"           ,StandardTypeMetadata.NONE                               ),
	INTERVAL_HOUR                      ("INTERVAL HOUR"                    ,StandardTypeMetadata.NONE                               ),
	INTERVAL_HOUR_MINUTE               ("INTERVAL HOUR TO MINUTE"          ,StandardTypeMetadata.NONE                               ),
	INTERVAL_HOUR_SECOND               ("INTERVAL HOUR TO SECOND"          ,StandardTypeMetadata.NONE                               ),
	INTERVAL_MINUTE                    ("INTERVAL MINUTE"                  ,StandardTypeMetadata.NONE                               ),
	INTERVAL_MINUTE_SECOND             ("INTERVAL MINUTE TO SECOND"        ,StandardTypeMetadata.NONE                               ),
	INTERVAL_MONTH                     ("INTERVAL MONTH"                   ,StandardTypeMetadata.NONE                               ),
	INTERVAL_SECOND                    ("INTERVAL SECOND"                  ,StandardTypeMetadata.NONE                               ),
	INTERVAL_YEAR                      ("INTERVAL YEAR"                    ,StandardTypeMetadata.NONE                               ),
	INTERVAL_YEAR_MONTH                ("INTERVAL YEAR TO MONTH"           ,StandardTypeMetadata.NONE                               ),
	IP                                 ("ip"                               ,StandardTypeMetadata.NONE                               ),
	JAVA_OBJECT                        ("JAVA_OBJECT"                      ,StandardTypeMetadata.NONE                               ),
	JOIN                               ("join"                             ,StandardTypeMetadata.NONE                               ),
	JSON                               ("JSON"                             ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	JSONB                              ("JSONB"                            ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	JSONPATH                           ("JSONPATH"                         ,StandardTypeMetadata.NONE                               ),
	KEYWORD                            ("KEYWORD"                          ,StandardTypeMetadata.NONE                               ),
	LARGEINT                           ("LARGEINT"                         ,StandardTypeMetadata.NONE                               ),
	LINE                               ("LINE"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	LINESTRING                         ("LINESTRING"                       ,StandardTypeMetadata.NONE                               ),
	LIST                               ("LIST"                             ,StandardTypeMetadata.NONE                               ),
	LONG_TEXT                          ("LONG"                             ,StandardTypeMetadata.NONE                               ),
	LONG                               ("long"                             ,StandardTypeMetadata.NONE                               ),
	LONGBLOB                           ("LONGBLOB"                         ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	LONGTEXT                           ("LONGTEXT"                         ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	LSEG                               ("LSEG"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	LVARCHAR                           ("LVARCHAR"                         ,StandardTypeMetadata.NONE                               ),
	MACADDR                            ("MACADDR"                          ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	MACADDR8                           ("MACADDR8"                         ,StandardTypeMetadata.NONE                               ),
	MEDIUMBLOB                         ("MEDIUMBLOB"                       ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	MEDIUMINT                          ("MEDIUMINT"                        ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	MEDIUMTEXT                         ("MEDIUMTEXT"                       ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	MONEY                              ("MONEY"                            ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	MULTILINESTRING                    ("MULTILINESTRING"                  ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	MULTIPOINT                         ("MULTIPOINT"                       ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	MULTISET                           ("MULTISET"                         ,StandardTypeMetadata.NONE                               ),
	NATURALN                           ("NATURALN"                         ,StandardTypeMetadata.NONE                               ),
	NCHAR                              ("NCHAR"                            ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	NCLOB                              ("NCLOB"                            ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	NESTED                             ("nested"                           ,StandardTypeMetadata.NONE                               ),
	NTEXT                              ("NTEXT"                            ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	NUMBER                             ("NUMBER"                           ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	NUMERIC                            ("NUMERIC"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	NUMRANGE                           ("NUMRANGE"                         ,StandardTypeMetadata.NONE                               ),
	NVARCHAR                           ("NVARCHAR"                         ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	NVARCHAR2                          ("NVARCHAR2"                        ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	OBJECT                             ("OBJECT"                           ,StandardTypeMetadata.NONE                               ),
	OID                                ("OID"                              ,StandardTypeMetadata.NONE                               ),
	ORA_DATE                           ("ORA_DATE"                         ,StandardTypeMetadata.NONE                               ),
	PATH                               ("PATH"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	PERCOLATOR                         ("percolator"                       ,StandardTypeMetadata.NONE                               ),
	PG_SNAPSHOT                        ("PG_SNAPSHOT"                      ,StandardTypeMetadata.NONE                               ),
	POSITIVE                           ("POSITIVE"                         ,StandardTypeMetadata.NONE                               ),
	POSITIVEN                          ("POSITIVEN"                        ,StandardTypeMetadata.NONE                               ),
	RANGE                              ("Range"                            ,StandardTypeMetadata.NONE                               ),
	RANK_FEATURE                       ("rank_feature"                     ,StandardTypeMetadata.NONE                               ),
	RANK_FEATURES                      ("rank_features"                    ,StandardTypeMetadata.NONE                               ),
	RAW                                ("RAW"                              ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	REAL                               ("REAL"                             ,StandardTypeMetadata.CLICKHOUSE_FLOAT32                 , "Float32"                          , null                               , 1, 1, 1),
	REFCURSOR                          ("REFCURSOR"                        ,StandardTypeMetadata.NONE                               ),
	REGCLASS                           ("REGCLASS"                         ,StandardTypeMetadata.NONE                               ),
	REGCONFIG                          ("REGCONFIG"                        ,StandardTypeMetadata.NONE                               ),
	REGDICTIONARY                      ("REGDICTIONARY"                    ,StandardTypeMetadata.NONE                               ),
	REGNAMESPACE                       ("REGNAMESPACE"                     ,StandardTypeMetadata.NONE                               ),
	REGOPER                            ("REGOPER"                          ,StandardTypeMetadata.NONE                               ),
	REGOPERATOR                        ("REGOPERATOR"                      ,StandardTypeMetadata.NONE                               ),
	REGPROC                            ("REGPROC"                          ,StandardTypeMetadata.NONE                               ),
	REGPROCEDURE                       ("REGPROCEDURE"                     ,StandardTypeMetadata.NONE                               ),
	REGROLE                            ("REGROLE"                          ,StandardTypeMetadata.NONE                               ),
	REGTYPE                            ("REGTYPE"                          ,StandardTypeMetadata.NONE                               ),
	ROW                                ("ROW"                              ,StandardTypeMetadata.NONE                               ),
	ROWID                              ("ROWID"                            ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	SCALED_FLOAT                       ("scaled_float"                     ,StandardTypeMetadata.NONE                               ),
	SEARCH_AS_YOU_TYPE                 ("search_as_you_type"               ,StandardTypeMetadata.NONE                               ),
	SECONDDATE                         ("SECONDDATE"                       ,StandardTypeMetadata.NONE                               ),
	SERIAL                             ("SERIAL"                           ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	SERIAL2                            ("SERIAL2"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	SERIAL4                            ("SERIAL4"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	SERIAL8                            ("SERIAL8"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	SET                                ("SET"                              ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	SHAPE                              ("shape"                            ,StandardTypeMetadata.NONE                               ),
	SHORT                              ("SHORT"                            ,StandardTypeMetadata.NONE                               ),
	SIGNTYPE                           ("SIGNTYPE"                         ,StandardTypeMetadata.NONE                               ),
	SIMPLE_DOUBLE                      ("SIMPLE_DOUBLE"                    ,StandardTypeMetadata.NONE                               ),
	SIMPLE_FLOAT                       ("SIMPLE_FLOAT"                     ,StandardTypeMetadata.NONE                               ),
	SIMPLE_INTEGER                     ("SIMPLE_INTEGER"                   ,StandardTypeMetadata.NONE                               ),
	SMALLDATETIME                      ("SMALLDATETIME"                    ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	SMALLDECIMAL                       ("SMALLDECIMAL"                     ,StandardTypeMetadata.NONE                               ),
	SMALLFLOAT                         ("SMALLFLOAT"                       ,StandardTypeMetadata.NONE                               ),
	SMALLINT                           ("SMALLINT"                         ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	SMALLMONEY                         ("SMALLMONEY"                       ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	SMALLSERIAL                        ("SMALLSERIAL"                      ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	SPARSE_VECTOR                      ("sparse_vector"                    ,StandardTypeMetadata.NONE                               ),
	SQL_DATETIMEOFFSET                 ("SQL_DATETIMEOFFSET"               ,StandardTypeMetadata.NONE                               ),
	SQL_VARIANT                        ("SQL_VARIANT"                      ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	ST_GEOMETRY                        ("ST_GEOMETRY"                      ,StandardTypeMetadata.NONE                               ),
	ST_POINT                           ("ST_POINT"                         ,StandardTypeMetadata.NONE                               ),
	STRUCT                             ("STRUCT"                           ,StandardTypeMetadata.NONE                               ),
	STRUCTS                            ("STRUCTS"                          ,StandardTypeMetadata.NONE                               ),
	SYS_REFCURSOR                      ("SYS_REFCURSOR"                    ,StandardTypeMetadata.NONE                               ),
	SYSNAME                            ("SYSNAME"                          ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	TEXT                               ("TEXT"                             ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	TID                                ("TID"                              ,StandardTypeMetadata.NONE                               ),
	TIME                               ("TIME"                             ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	TIME_TZ_UNCONSTRAINED              ("TIME TZ UNCONSTRAINED"            ,StandardTypeMetadata.NONE                               ),
	TIME_WITH_TIME_ZONE                ("TIME WITH TIME ZONE"              ,StandardTypeMetadata.TIME_WITH_TIME_ZONE                ),
	TIME_WITH_ZONE                     ("TIME WITH TIME ZONE"              ,StandardTypeMetadata.TIME_WITH_TIME_ZONE                ),
	TIME_WITHOUT_ZONE                  ("TIME WITHOUT TIME ZONE"           ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE             ),
	TIME_WITHOUT_TIME_ZONE             ("TIME WITHOUT TIME ZONE"           ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE             ),
	TIME_UNCONSTRAINED                 ("TIME_UNCONSTRAINED"               ,StandardTypeMetadata.NONE                               ),
	TIMESTAMP                          ("TIMESTAMP"                        ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	TIMESTAMP_WITH_LOCAL_ZONE          ("TIMESTAMP WITH LOCAL TIME ZONE"   ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	TIMESTAMP_WITH_TIME_ZONE           ("TIMESTAMP WITH TIME ZONE"         ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	TIMESTAMP_WITH_ZONE                ("TIMESTAMP WITH TIME ZONE"         ,StandardTypeMetadata.TIMESTAMP_WITH_TIME_ZONE           ),
	TIMESTAMP_WITHOUT_TIME_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"      ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE        ),
	TIMESTAMP_WITHOUT_ZONE             ("TIMESTAMP WITHOUT TIME ZONE"      ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE        ),
	TIMESTAMPTZ                        ("TIMESTAMPTZ"                      ,StandardTypeMetadata.NONE                               ),
	TIMEZ                              ("TIMEZ"                            ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	TINYBLOB                           ("TINYBLOB"                         ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	TINYINT                            ("TINYINT"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	TINYTEXT                           ("TINYTEXT"                         ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	TOKEN_COUNT                        ("token_count"                      ,StandardTypeMetadata.NONE                               ),
	TSQUERY                            ("TSQUERY"                          ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	TSRANGE                            ("TSRANGE"                          ,StandardTypeMetadata.NONE                               ),
	TSTZRANGE                          ("TSTZRANGE"                        ,StandardTypeMetadata.NONE                               ),
	TSVECTOR                           ("TSVECTOR"                         ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	TXID_SNAPSHOT                      ("TXID_SNAPSHOT"                    ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	UNIQUEIDENTIFIER                   ("UNIQUEIDENTIFIER"                 ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	UNSIGNED_LONG                      ("unsigned_long"                    ,StandardTypeMetadata.NONE                               ),
	UROWID                             ("UROWID"                           ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	VARBINARY                          ("VARBINARY"                        ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	VARBIT                             ("VARBIT"                           ,StandardTypeMetadata.FixedString                        , 0, 1, 1),
	VARCHAR                            ("VARCHAR"                          ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	VARCHAR2                           ("VARCHAR2"                         ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	VARCHARBYTE                        ("VARCHARBYTE"                      ,StandardTypeMetadata.NONE                               ),
	VERSION                            ("version"                          ,StandardTypeMetadata.NONE                               ),
	XID                                ("XID"                              ,StandardTypeMetadata.NONE                               ),
	XML                                ("XML"                              ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	YEAR                               ("YEAR"                             ,StandardTypeMetadata.INT                                , 1, 1, 1),
	YMINTERVAL                         ("YMINTERVAL"                       ,StandardTypeMetadata.NONE                               );

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
	private TypeMetadata.Config config       ; // 集成元数据读写配置

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
	public TypeMetadata.Config config() {
		if(null == config){
			config = new TypeMetadata.Config();
			if(null != meta) {
				config.setMeta(meta);
			}
			if(null != formula) {
				config.setFormula(formula);
			}
			if(null != lengthRefer) {
				config.setLengthRefer(lengthRefer).setPrecisionRefer(precisionRefer).setScaleRefer(scaleRefer);
			}
			if(-1 != ignoreLength) {
				config.setIgnoreLength(ignoreLength).setIgnorePrecision(ignorePrecision).setIgnoreScale(ignoreScale);
			}
		}
		return config;
	}
}
