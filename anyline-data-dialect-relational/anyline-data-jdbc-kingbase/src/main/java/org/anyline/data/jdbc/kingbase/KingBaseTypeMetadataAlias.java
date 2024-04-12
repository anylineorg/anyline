package org.anyline.data.jdbc.kingbase;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum KingBaseTypeMetadataAlias implements TypeMetadataAlias {
	ACLITEM                            ("ACLITEM"                          ,StandardTypeMetadata.ACLITEM                            , 1, 1, 1),
	BIGINT                             ("BIGINT"                           ,StandardTypeMetadata.BIGINT                             , 1, 1, 1),
	BIGSERIAL                          ("BIGSERIAL"                        ,StandardTypeMetadata.BIGSERIAL                          , 1, 1, 1),
	BINARY_DOUBLE                      ("BINARY_DOUBLE"                    ,StandardTypeMetadata.BINARY_DOUBLE                      , 1, 0, 0),
	BINARY_FLOAT                       ("BINARY_FLOAT"                     ,StandardTypeMetadata.BINARY_FLOAT                       , 1, 0, 0),
	BINARY_INTEGER                     ("BINARY_INTEGER"                   ,StandardTypeMetadata.BINARY_INTEGER                     , 1, 1, 1),
	BIT                                ("BIT"                              ,StandardTypeMetadata.BIT                                , 1, 1, 1),
	BIT_VARYING                        ("BIT VARYING"                      ,StandardTypeMetadata.BIT_VARYING                        , "BIT VARYING"                      , null                               , 1, 1, 1),
	BLOB                               ("BLOB"                             ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	BOOL                               ("BOOL"                             ,StandardTypeMetadata.BOOL                               , 1, 1, 1),
	BOX                                ("BOX"                              ,StandardTypeMetadata.BOX                                , 1, 1, 1),
	BPCHAR                             ("BPCHAR"                           ,StandardTypeMetadata.BPCHAR                             , 1, 1, 1),
	BPCHARBYTE                         ("BPCHARBYTE"                       ,StandardTypeMetadata.BPCHARBYTE                         , 1, 1, 1),
	BYTEA                              ("BYTEA"                            ,StandardTypeMetadata.BYTEA                              , 1, 1, 1),
	CHAR                               ("CHAR"                             ,StandardTypeMetadata.CHAR                               , 0, 1, 1),
	CHARACTER                          ("CHARACTER"                        ,StandardTypeMetadata.CHARACTER                          , 1, 1, 1),
	CID                                ("CID"                              ,StandardTypeMetadata.CID                                , 1, 1, 1),
	CIDR                               ("CIDR"                             ,StandardTypeMetadata.CIDR                               , 1, 1, 1),
	CIRCLE                             ("CIRCLE"                           ,StandardTypeMetadata.CIRCLE                             , 1, 1, 1),
	CLOB                               ("CLOB"                             ,StandardTypeMetadata.CLOB                               , 1, 1, 1),
	DATE                               ("DATE"                             ,StandardTypeMetadata.DATE                               , 1, 1, 1),
	DATERANGE                          ("DATERANGE"                        ,StandardTypeMetadata.DATERANGE                          , 1, 1, 1),
	DATETIME                           ("DATETIME"                         ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	DECIMAL                            ("DECIMAL"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	DOUBLE                             ("DOUBLE"                           ,StandardTypeMetadata.DOUBLE                             , 1, 1, 1),
	DOUBLE_PRECISION                   ("DOUBLE PRECISION"                 ,StandardTypeMetadata.DOUBLE_PRECISION                   , "DOUBLE PRECISION"                 , null                               , 1, 1, 1),
	DSINTERVAL                         ("DSINTERVAL"                       ,StandardTypeMetadata.DSINTERVAL                         , 1, 1, 1),
	FLOAT                              ("FLOAT"                            ,StandardTypeMetadata.FLOAT                              , 1, 2, 1),
	FLOAT4                             ("FLOAT4"                           ,StandardTypeMetadata.FLOAT4                             , 1, 2, 1),
	FLOAT8                             ("FLOAT8"                           ,StandardTypeMetadata.FLOAT8                             , 1, 2, 1),
	GTSVECTOR                          ("GTSVECTOR"                        ,StandardTypeMetadata.GTSVECTOR                          , 1, 1, 1),
	INET                               ("INET"                             ,StandardTypeMetadata.INET                               , 1, 1, 1),
	INT                                ("INT"                              ,StandardTypeMetadata.INT                                , 1, 1, 1),
	INT2                               ("INT2"                             ,StandardTypeMetadata.INT2                               , 1, 1, 1),
	INT4                               ("INT4"                             ,StandardTypeMetadata.INT4                               , 1, 1, 1),
	INT4RANGE                          ("INT4RANGE"                        ,StandardTypeMetadata.INT4RANGE                          , 1, 1, 1),
	INT8                               ("INT8"                             ,StandardTypeMetadata.INT8                               , 1, 1, 1),
	INT8RANGE                          ("INT8RANGE"                        ,StandardTypeMetadata.INT8RANGE                          , 1, 1, 1),
	INTEGER                            ("INTEGER"                          ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	INTERVAL_DAY                       ("INTERVAL DAY"                     ,StandardTypeMetadata.INTERVAL_DAY                       , "INTERVAL DAY"                     , null                               , 1, 2, 2),
	INTERVAL_DAY_HOUR                  ("INTERVAL DAY TO HOUR"             ,StandardTypeMetadata.INTERVAL_DAY_HOUR                  , "INTERVAL DAY TO HOUR"             , null                               , 1, 2, 2),
	INTERVAL_DAY_MINUTE                ("INTERVAL DAY TO MINUTE"           ,StandardTypeMetadata.INTERVAL_DAY_MINUTE                , "INTERVAL DAY TO MINUTE"           , null                               , 1, 2, 2),
	INTERVAL_DAY_SECOND                ("INTERVAL DAY TO SECOND"           ,StandardTypeMetadata.INTERVAL_DAY_SECOND                , "INTERVAL DAY TO SECOND"           , null                               , 1, 2, 2),
	INTERVAL_HOUR                      ("INTERVAL HOUR"                    ,StandardTypeMetadata.INTERVAL_HOUR                      , "INTERVAL HOUR"                    , null                               , 1, 2, 1),
	INTERVAL_HOUR_MINUTE               ("INTERVAL HOUR TO MINUTE"          ,StandardTypeMetadata.INTERVAL_HOUR_MINUTE               , "INTERVAL HOUR TO MINUTE"          , null                               , 1, 2, 2),
	INTERVAL_HOUR_SECOND               ("INTERVAL HOUR TO SECOND"          ,StandardTypeMetadata.INTERVAL_HOUR_SECOND               , "INTERVAL HOUR TO SECOND"          , null                               , 1, 2, 2),
	INTERVAL_MINUTE                    ("INTERVAL MINUTE"                  ,StandardTypeMetadata.INTERVAL_MINUTE                    , "INTERVAL MINUTE"                  , null                               , 1, 2, 1),
	INTERVAL_MINUTE_SECOND             ("INTERVAL MINUTE TO SECOND"        ,StandardTypeMetadata.INTERVAL_MINUTE_SECOND             , "INTERVAL MINUTE TO SECOND"        , null                               , 1, 2, 2),
	INTERVAL_MONTH                     ("INTERVAL MONTH"                   ,StandardTypeMetadata.INTERVAL_MONTH                     , "INTERVAL MONTH"                   , null                               , 1, 2, 1),
	INTERVAL_SECOND                    ("INTERVAL SECOND"                  ,StandardTypeMetadata.INTERVAL_SECOND                    , "INTERVAL SECOND"                  , null                               , 1, 2, 1),
	INTERVAL_YEAR                      ("INTERVAL YEAR"                    ,StandardTypeMetadata.INTERVAL_YEAR                      , "INTERVAL YEAR"                    , null                               , 1, 2, 1),
	INTERVAL_YEAR_MONTH                ("INTERVAL YEAR TO MONTH"           ,StandardTypeMetadata.INTERVAL_YEAR_MONTH                , "INTERVAL YEAR TO MONTH"           , null                               , 1, 2, 2),
	JSON                               ("JSON"                             ,StandardTypeMetadata.JSON                               , 1, 1, 1),
	JSONB                              ("JSONB"                            ,StandardTypeMetadata.JSONB                              , 1, 1, 1),
	JSONPATH                           ("JSONPATH"                         ,StandardTypeMetadata.JSONPATH                           , 1, 1, 1),
	LINE                               ("LINE"                             ,StandardTypeMetadata.LINE                               , 1, 1, 1),
	LONG_TEXT                          ("LONG"                             ,StandardTypeMetadata.LONG_TEXT                          , "LONG"                             , null                               , 1, 1, 1),
	LSEG                               ("LSEG"                             ,StandardTypeMetadata.LSEG                               , 1, 1, 1),
	MACADDR8                           ("MACADDR8"                         ,StandardTypeMetadata.MACADDR8                           , 1, 1, 1),
	MONEY                              ("MONEY"                            ,StandardTypeMetadata.MONEY                              , 1, 1, 1),
	NATURALN                           ("NATURALN"                         ,StandardTypeMetadata.NATURALN                           , 1, 1, 1),
	NCHAR                              ("NCHAR"                            ,StandardTypeMetadata.NCHAR                              , 0, 1, 1),
	NCLOB                              ("NCLOB"                            ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	NUMBER                             ("NUMBER"                           ,StandardTypeMetadata.NUMBER                             , 1, 2, 2),
	NUMRANGE                           ("NUMRANGE"                         ,StandardTypeMetadata.NUMRANGE                           , 1, 1, 1),
	NVARCHAR                           ("NVARCHAR"                         ,StandardTypeMetadata.NVARCHAR                           , 0, 1, 1),
	NVARCHAR2                          ("NVARCHAR2"                        ,StandardTypeMetadata.NVARCHAR2                          , 0, 1, 1),
	OID                                ("OID"                              ,StandardTypeMetadata.OID                                , 1, 1, 1),
	ORA_DATE                           ("ORA_DATE"                         ,StandardTypeMetadata.ORA_DATE                           , 1, 1, 1),
	PATH                               ("PATH"                             ,StandardTypeMetadata.PATH                               , 1, 1, 1),
	PG_SNAPSHOT                        ("PG_SNAPSHOT"                      ,StandardTypeMetadata.PG_SNAPSHOT                        , 1, 1, 1),
	POINT                              ("POINT"                            ,StandardTypeMetadata.POINT                              , 1, 1, 1),
	POLYGON                            ("POLYGON"                          ,StandardTypeMetadata.POLYGON                            , 1, 1, 1),
	POSITIVE                           ("POSITIVE"                         ,StandardTypeMetadata.POSITIVE                           , 1, 1, 1),
	POSITIVEN                          ("POSITIVEN"                        ,StandardTypeMetadata.POSITIVEN                          , 1, 1, 1),
	RAW                                ("RAW"                              ,StandardTypeMetadata.RAW                                , 1, 1, 1),
	REAL                               ("REAL"                             ,StandardTypeMetadata.REAL                               , 1, 0, 0),
	REFCURSOR                          ("REFCURSOR"                        ,StandardTypeMetadata.REFCURSOR                          , 1, 1, 1),
	REGCLASS                           ("REGCLASS"                         ,StandardTypeMetadata.REGCLASS                           , 1, 1, 1),
	REGCONFIG                          ("REGCONFIG"                        ,StandardTypeMetadata.REGCONFIG                          , 1, 1, 1),
	REGDICTIONARY                      ("REGDICTIONARY"                    ,StandardTypeMetadata.REGDICTIONARY                      , 1, 1, 1),
	REGNAMESPACE                       ("REGNAMESPACE"                     ,StandardTypeMetadata.REGNAMESPACE                       , 1, 1, 1),
	REGOPER                            ("REGOPER"                          ,StandardTypeMetadata.REGOPER                            , 1, 1, 1),
	REGOPERATOR                        ("REGOPERATOR"                      ,StandardTypeMetadata.REGOPERATOR                        , 1, 1, 1),
	REGPROC                            ("REGPROC"                          ,StandardTypeMetadata.REGPROC                            , 1, 1, 1),
	REGPROCEDURE                       ("REGPROCEDURE"                     ,StandardTypeMetadata.REGPROCEDURE                       , 1, 1, 1),
	REGROLE                            ("REGROLE"                          ,StandardTypeMetadata.REGROLE                            , 1, 1, 1),
	REGTYPE                            ("REGTYPE"                          ,StandardTypeMetadata.REGTYPE                            , 1, 1, 1),
	ROWID                              ("ROWID"                            ,StandardTypeMetadata.ROWID                              , 1, 1, 1),
	SERIAL                             ("SERIAL"                           ,StandardTypeMetadata.SERIAL                             , 1, 1, 1),
	SERIAL2                            ("SERIAL2"                          ,StandardTypeMetadata.SERIAL2                            , 1, 1, 1),
	SERIAL4                            ("SERIAL4"                          ,StandardTypeMetadata.SERIAL4                            , 1, 1, 1),
	SERIAL8                            ("SERIAL8"                          ,StandardTypeMetadata.SERIAL8                            , 1, 1, 1),
	SIGNTYPE                           ("SIGNTYPE"                         ,StandardTypeMetadata.SIGNTYPE                           , 1, 1, 1),
	SIMPLE_DOUBLE                      ("SIMPLE_DOUBLE"                    ,StandardTypeMetadata.SIMPLE_DOUBLE                      , 1, 1, 1),
	SIMPLE_FLOAT                       ("SIMPLE_FLOAT"                     ,StandardTypeMetadata.SIMPLE_FLOAT                       , 1, 2, 1),
	SIMPLE_INTEGER                     ("SIMPLE_INTEGER"                   ,StandardTypeMetadata.SIMPLE_INTEGER                     , 1, 1, 1),
	SMALLSERIAL                        ("SMALLSERIAL"                      ,StandardTypeMetadata.SMALLSERIAL                        , 1, 1, 1),
	STRING                             ("STRING"                           ,StandardTypeMetadata.STRING                             , 1, 1, 1),
	TEXT                               ("TEXT"                             ,StandardTypeMetadata.TEXT                               , 1, 1, 1),
	TID                                ("TID"                              ,StandardTypeMetadata.TID                                , 1, 1, 1),
	TIME_TZ_UNCONSTRAINED              ("TIME TZ UNCONSTRAINED"            ,StandardTypeMetadata.TIME_TZ_UNCONSTRAINED              , "TIME TZ UNCONSTRAINED"            , null                               , 1, 1, 1),
	TIME_WITH_TIME_ZONE                ("TIME WITH TIME ZONE"              ,StandardTypeMetadata.TIME_WITH_TIME_ZONE                , "TIME WITH TIME ZONE"              , null                               , 1, 1, 1),
	TIME_WITHOUT_TIME_ZONE             ("TIME WITHOUT TIME ZONE"           ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE             , "TIME WITHOUT TIME ZONE"           , null                               , 1, 1, 1),
	TIME_UNCONSTRAINED                 ("TIME_UNCONSTRAINED"               ,StandardTypeMetadata.TIME_UNCONSTRAINED                 , 1, 1, 1),
	TIMESTAMP                          ("TIMESTAMP"                        ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	TIMESTAMP_WITH_TIME_ZONE           ("TIMESTAMP WITH TIME ZONE"         ,StandardTypeMetadata.TIMESTAMP_WITH_TIME_ZONE           , "TIMESTAMP WITH TIME ZONE"         , null                               , 1, 1, 1),
	TIMESTAMP_WITHOUT_TIME_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"      ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE        , "TIMESTAMP WITHOUT TIME ZONE"      , null                               , 1, 1, 1),
	TIMESTAMPTZ                        ("TIMESTAMPTZ"                      ,StandardTypeMetadata.TIMESTAMPTZ                        , 1, 1, 1),
	TSQUERY                            ("TSQUERY"                          ,StandardTypeMetadata.TSQUERY                            , 1, 1, 1),
	TSRANGE                            ("TSRANGE"                          ,StandardTypeMetadata.TSRANGE                            , 1, 1, 1),
	TSTZRANGE                          ("TSTZRANGE"                        ,StandardTypeMetadata.TSTZRANGE                          , 1, 1, 1),
	TSVECTOR                           ("TSVECTOR"                         ,StandardTypeMetadata.TSVECTOR                           , 1, 1, 1),
	TXID_SNAPSHOT                      ("TXID_SNAPSHOT"                    ,StandardTypeMetadata.TXID_SNAPSHOT                      , 1, 1, 1),
	UROWID                             ("UROWID"                           ,StandardTypeMetadata.UROWID                             , 1, 1, 1),
	UUID                               ("UUID"                             ,StandardTypeMetadata.UUID                               , 1, 1, 1),
	VARBIT                             ("VARBIT"                           ,StandardTypeMetadata.VARBIT                             , 1, 1, 1),
	VARCHAR                            ("VARCHAR"                          ,StandardTypeMetadata.VARCHAR                            , 0, 1, 1),
	VARCHAR2                           ("VARCHAR2"                         ,StandardTypeMetadata.VARCHAR2                           , 0, 1, 1),
	VARCHARBYTE                        ("VARCHARBYTE"                      ,StandardTypeMetadata.VARCHARBYTE                        , 1, 1, 1),
	XID                                ("XID"                              ,StandardTypeMetadata.XID                                , 1, 1, 1),
	XML                                ("XML"                              ,StandardTypeMetadata.XML                                , 1, 1, 1),
	YMINTERVAL                         ("YMINTERVAL"                       ,StandardTypeMetadata.YMINTERVAL                         , 1, 1, 1),
	AGG_STATE                          ("AGG_STATE"                        ,StandardTypeMetadata.NONE                               ),
	AGGREGATE_METRIC_DOUBLE            ("aggregate_metric_double"          ,StandardTypeMetadata.NONE                               ),
	ALIAS                              ("alias"                            ,StandardTypeMetadata.NONE                               ),
	ARRAY                              ("ARRAY"                            ,StandardTypeMetadata.NONE                               ),
	BFILE                              ("BFILE"                            ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	BINARY                             ("BINARY"                           ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	BITMAP                             ("BITMAP"                           ,StandardTypeMetadata.NONE                               ),
	BOOLEAN                            ("BOOLEAN"                          ,StandardTypeMetadata.BOOL                               ),
	BYTE                               ("BYTE"                             ,StandardTypeMetadata.NONE                               ),
	COMPLETION                         ("completion"                       ,StandardTypeMetadata.NONE                               ),
	CURSOR                             ("CURSOR"                           ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_DATE32                  ("DATE32"                           ,StandardTypeMetadata.NONE                               ),
	DATETIME2                          ("DATETIME2"                        ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	CLICKHOUSE_DATETIME64              ("DATETIME64"                       ,StandardTypeMetadata.NONE                               ),
	DATETIMEOFFSET                     ("DATETIMEOFFSET"                   ,StandardTypeMetadata.DATETIME                           , 1, 1, 1),
	DEC                                ("DEC"                              ,StandardTypeMetadata.DECIMAL                            ),
	DECFLOAT                           ("DECFLOAT"                         ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_DECIMAL128              ("DECIMAL128"                       ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_DECIMAL256              ("DECIMAL256"                       ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_DECIMAL32               ("DECIMAL32"                        ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_DECIMAL64               ("DECIMAL64"                        ,StandardTypeMetadata.NONE                               ),
	DENSE_VECTOR                       ("dense_vector"                     ,StandardTypeMetadata.NONE                               ),
	DURATION                           ("DURATION"                         ,StandardTypeMetadata.NONE                               ),
	ENUM                               ("ENUM"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	FIXED_STRING                       ("FIXED_STRING"                     ,StandardTypeMetadata.CHAR                               ),
	FIXEDSTRING                        ("FixedString"                      ,StandardTypeMetadata.CHAR                               ),
	FLATTENED                          ("flattened"                        ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_FLOAT32                 ("FLOAT32"                          ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_FLOAT64                 ("FLOAT64"                          ,StandardTypeMetadata.NONE                               ),
	GEO_POINT                          ("geo_point"                        ,StandardTypeMetadata.NONE                               ),
	GEO_SHAPE                          ("geo_shape"                        ,StandardTypeMetadata.NONE                               ),
	GEOGRAPHY                          ("GEOGRAPHY"                        ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	GEOGRAPHY_POINT                    ("GEOGRAPHY_POINT"                  ,StandardTypeMetadata.POINT                              , 1, 1, 1),
	GEOMETRY                           ("GEOMETRY"                         ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	GEOMETRYCOLLECTION                 ("GEOMETRYCOLLECTION"               ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	GUID                               ("GUID"                             ,StandardTypeMetadata.NONE                               ),
	HALF_FLOAT                         ("half_float"                       ,StandardTypeMetadata.NONE                               ),
	HIERARCHYID                        ("HIERARCHYID"                      ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	HISTOGRAM                          ("histogram"                        ,StandardTypeMetadata.NONE                               ),
	HLL                                ("HLL"                              ,StandardTypeMetadata.NONE                               ),
	IMAGE                              ("IMAGE"                            ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	INT128                             ("INT128"                           ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_INT128                  ("INT128"                           ,StandardTypeMetadata.NONE                               ),
	INT16                              ("INT16"                            ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_INT16                   ("INT16"                            ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_INT256                  ("INT256"                           ,StandardTypeMetadata.NONE                               ),
	INT256                             ("INT256"                           ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_INT32                   ("INT32"                            ,StandardTypeMetadata.NONE                               ),
	INT32                              ("INT32"                            ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_INT64                   ("INT64"                            ,StandardTypeMetadata.NONE                               ),
	INT64                              ("INT64"                            ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_INT8                    ("INT8"                             ,StandardTypeMetadata.NONE                               ),
	INTERVAL                           ("INTERVAL"                         ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	IP                                 ("ip"                               ,StandardTypeMetadata.NONE                               ),
	IPV4                               ("IPV4"                             ,StandardTypeMetadata.NONE                               ),
	IPV6                               ("IPV6"                             ,StandardTypeMetadata.NONE                               ),
	JAVA_OBJECT                        ("JAVA_OBJECT"                      ,StandardTypeMetadata.NONE                               ),
	JOIN                               ("join"                             ,StandardTypeMetadata.NONE                               ),
	KEYWORD                            ("KEYWORD"                          ,StandardTypeMetadata.NONE                               ),
	LARGEINT                           ("LARGEINT"                         ,StandardTypeMetadata.NONE                               ),
	LINESTRING                         ("LINESTRING"                       ,StandardTypeMetadata.LINE                               , 1, 1, 1),
	LIST                               ("LIST"                             ,StandardTypeMetadata.NONE                               ),
	LONG                               ("long"                             ,StandardTypeMetadata.NONE                               ),
	LONGBLOB                           ("LONGBLOB"                         ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	LONGTEXT                           ("LONGTEXT"                         ,StandardTypeMetadata.CLOB                               , 1, 1, 1),
	LOWCARDINALITY                     ("LowCardinality"                   ,StandardTypeMetadata.NONE                               ),
	LVARCHAR                           ("LVARCHAR"                         ,StandardTypeMetadata.NONE                               ),
	MACADDR                            ("MACADDR"                          ,StandardTypeMetadata.MACADDR8                           , 1, 1, 1),
	MAP                                ("MAP"                              ,StandardTypeMetadata.NONE                               ),
	MEDIUMBLOB                         ("MEDIUMBLOB"                       ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	MEDIUMINT                          ("MEDIUMINT"                        ,StandardTypeMetadata.INT2                               , 1, 1, 1),
	MEDIUMTEXT                         ("MEDIUMTEXT"                       ,StandardTypeMetadata.TEXT                               , 1, 1, 1),
	MULTILINESTRING                    ("MULTILINESTRING"                  ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	MULTIPOINT                         ("MULTIPOINT"                       ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	MULTIPOLYGON                       ("MULTIPOLYGON"                     ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	MULTISET                           ("MULTISET"                         ,StandardTypeMetadata.NONE                               ),
	NESTED                             ("nested"                           ,StandardTypeMetadata.NONE                               ),
	NTEXT                              ("NTEXT"                            ,StandardTypeMetadata.TEXT                               , 1, 1, 1),
	NUMERIC                            ("NUMERIC"                          ,StandardTypeMetadata.NUMBER                             , 1, 2, 2),
	OBJECT                             ("OBJECT"                           ,StandardTypeMetadata.NONE                               ),
	PERCOLATOR                         ("percolator"                       ,StandardTypeMetadata.NONE                               ),
	RANGE                              ("Range"                            ,StandardTypeMetadata.NONE                               ),
	RANK_FEATURE                       ("rank_feature"                     ,StandardTypeMetadata.NONE                               ),
	RANK_FEATURES                      ("rank_features"                    ,StandardTypeMetadata.NONE                               ),
	RING                               ("RING"                             ,StandardTypeMetadata.NONE                               ),
	ROW                                ("ROW"                              ,StandardTypeMetadata.NONE                               ),
	SCALED_FLOAT                       ("scaled_float"                     ,StandardTypeMetadata.NONE                               ),
	SEARCH_AS_YOU_TYPE                 ("search_as_you_type"               ,StandardTypeMetadata.NONE                               ),
	SECONDDATE                         ("SECONDDATE"                       ,StandardTypeMetadata.NONE                               ),
	SET                                ("SET"                              ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	SHAPE                              ("shape"                            ,StandardTypeMetadata.NONE                               ),
	SHORT                              ("SHORT"                            ,StandardTypeMetadata.NONE                               ),
	SIMPLEAGGREGATEFUNCTION            ("SimpleAggregateFunction"          ,StandardTypeMetadata.NONE                               ),
	SMALLDATETIME                      ("SMALLDATETIME"                    ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	SMALLDECIMAL                       ("SMALLDECIMAL"                     ,StandardTypeMetadata.NONE                               ),
	SMALLFLOAT                         ("SMALLFLOAT"                       ,StandardTypeMetadata.NONE                               ),
	SMALLINT                           ("SMALLINT"                         ,StandardTypeMetadata.INT4                               , 1, 1, 1),
	SMALLMONEY                         ("SMALLMONEY"                       ,StandardTypeMetadata.MONEY                              , 1, 1, 1),
	SPARSE_VECTOR                      ("sparse_vector"                    ,StandardTypeMetadata.NONE                               ),
	SQL_DATETIMEOFFSET                 ("SQL_DATETIMEOFFSET"               ,StandardTypeMetadata.NONE                               ),
	SQL_VARIANT                        ("SQL_VARIANT"                      ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	ST_GEOMETRY                        ("ST_GEOMETRY"                      ,StandardTypeMetadata.NONE                               ),
	ST_POINT                           ("ST_POINT"                         ,StandardTypeMetadata.NONE                               ),
	STRUCT                             ("STRUCT"                           ,StandardTypeMetadata.NONE                               ),
	STRUCTS                            ("STRUCTS"                          ,StandardTypeMetadata.NONE                               ),
	SYS_REFCURSOR                      ("SYS_REFCURSOR"                    ,StandardTypeMetadata.NONE                               ),
	SYSNAME                            ("SYSNAME"                          ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	TIME                               ("TIME"                             ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	TIME_WITH_ZONE                     ("TIME WITH TIME ZONE"              ,StandardTypeMetadata.TIME_WITH_TIME_ZONE                ),
	TIME_WITHOUT_ZONE                  ("TIME WITHOUT TIME ZONE"           ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE             ),
	TIMESTAMP_WITH_LOCAL_ZONE          ("TIMESTAMP WITH LOCAL TIME ZONE"   ,StandardTypeMetadata.TIMESTAMP                          , "TIMESTAMP WITH LOCAL TIME ZONE"   , null                               , 1, 1, 1),
	TIMESTAMP_WITH_ZONE                ("TIMESTAMP WITH TIME ZONE"         ,StandardTypeMetadata.TIMESTAMP_WITH_TIME_ZONE           ),
	TIMESTAMP_WITHOUT_ZONE             ("TIMESTAMP WITHOUT TIME ZONE"      ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE        ),
	TIMEZ                              ("TIMEZ"                            ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	TINYBLOB                           ("TINYBLOB"                         ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	TINYINT                            ("TINYINT"                          ,StandardTypeMetadata.INT2                               , 1, 1, 1),
	TINYTEXT                           ("TINYTEXT"                         ,StandardTypeMetadata.TEXT                               , 1, 1, 1),
	TOKEN_COUNT                        ("token_count"                      ,StandardTypeMetadata.NONE                               ),
	TUPLE                              ("TUPLE"                            ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_UINT128                 ("UINT128"                          ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_UINT16                  ("UINT16"                           ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_UINT256                 ("UINT256"                          ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_UINT32                  ("UINT32"                           ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_UINT64                  ("UINT64"                           ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_UINT8                   ("UINT8"                            ,StandardTypeMetadata.NONE                               ),
	UNIQUEIDENTIFIER                   ("UNIQUEIDENTIFIER"                 ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	UNSIGNED_LONG                      ("unsigned_long"                    ,StandardTypeMetadata.NONE                               ),
	VARBINARY                          ("VARBINARY"                        ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	VERSION                            ("version"                          ,StandardTypeMetadata.NONE                               ),
	YEAR                               ("YEAR"                             ,StandardTypeMetadata.DATE                               , 1, 1, 1);

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

	KingBaseTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
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

	KingBaseTypeMetadataAlias(String input, TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, null , null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	KingBaseTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	KingBaseTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	KingBaseTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(null, standard, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	KingBaseTypeMetadataAlias(String input, TypeMetadata standard){
		this.input = input;
		this.standard = standard;
	}

	KingBaseTypeMetadataAlias(TypeMetadata standard){
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
