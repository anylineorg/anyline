package org.anyline.data.jdbc.hana;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum HanaTypeMetadataAlias implements TypeMetadataAlias {
	BIGINT                             ("BIGINT"                           ,StandardTypeMetadata.BIGINT                             , 1, 1, 1),
	BINARY                             ("BINARY"                           ,StandardTypeMetadata.BINARY                             , 0, 1, 1),
	BLOB                               ("BLOB"                             ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	BOOLEAN                            ("BOOLEAN"                          ,StandardTypeMetadata.BOOLEAN                            , 1, 1, 1),
	CHAR                               ("CHAR"                             ,StandardTypeMetadata.CHAR                               , 0, 1, 1),
	DATE                               ("DATE"                             ,StandardTypeMetadata.DATE                               , 1, 1, 1),
	DECIMAL                            ("DECIMAL"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	DOUBLE                             ("DOUBLE"                           ,StandardTypeMetadata.DOUBLE                             , 1, 0, 0),
	INTEGER                            ("INTEGER"                          ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	INTERVAL_DAY                       ("INTERVAL DAY"                     ,StandardTypeMetadata.INTERVAL_DAY                       ),
	INTERVAL_DAY_HOUR                  ("INTERVAL DAY TO HOUR"             ,StandardTypeMetadata.INTERVAL_DAY_HOUR                  ),
	INTERVAL_DAY_MINUTE                ("INTERVAL DAY TO MINUTE"           ,StandardTypeMetadata.INTERVAL_DAY_MINUTE                ),
	INTERVAL_DAY_SECOND                ("INTERVAL DAY TO SECOND"           ,StandardTypeMetadata.INTERVAL_DAY_SECOND                ),
	INTERVAL_HOUR                      ("INTERVAL HOUR"                    ,StandardTypeMetadata.INTERVAL_HOUR                      ),
	INTERVAL_HOUR_MINUTE               ("INTERVAL HOUR TO MINUTE"          ,StandardTypeMetadata.INTERVAL_HOUR_MINUTE               ),
	INTERVAL_HOUR_SECOND               ("INTERVAL HOUR TO SECOND"          ,StandardTypeMetadata.INTERVAL_HOUR_SECOND               ),
	INTERVAL_MINUTE                    ("INTERVAL MINUTE"                  ,StandardTypeMetadata.INTERVAL_MINUTE                    ),
	INTERVAL_MINUTE_SECOND             ("INTERVAL MINUTE TO SECOND"        ,StandardTypeMetadata.INTERVAL_MINUTE_SECOND             ),
	INTERVAL_MONTH                     ("INTERVAL MONTH"                   ,StandardTypeMetadata.INTERVAL_MONTH                     ),
	INTERVAL_SECOND                    ("INTERVAL SECOND"                  ,StandardTypeMetadata.INTERVAL_SECOND                    ),
	INTERVAL_YEAR                      ("INTERVAL YEAR"                    ,StandardTypeMetadata.INTERVAL_YEAR                      ),
	INTERVAL_YEAR_MONTH                ("INTERVAL YEAR TO MONTH"           ,StandardTypeMetadata.INTERVAL_YEAR_MONTH                ),
	LONG_TEXT                          ("LONG"                             ,StandardTypeMetadata.LONG_TEXT                          , "LONG"                             , null                               , 1, 1, 1),
	NCLOB                              ("NCLOB"                            ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	NVARCHAR                           ("NVARCHAR"                         ,StandardTypeMetadata.NVARCHAR                           , 0, 1, 1),
	REAL                               ("REAL"                             ,StandardTypeMetadata.REAL                               , 1, 0, 0),
	SECONDDATE                         ("SECONDDATE"                       ,StandardTypeMetadata.SECONDDATE                         , 1, 1, 1),
	SMALLDECIMAL                       ("SMALLDECIMAL"                     ,StandardTypeMetadata.SMALLDECIMAL                       , 1, 0, 0),
	SMALLINT                           ("SMALLINT"                         ,StandardTypeMetadata.SMALLINT                           , 1, 1, 1),
	ST_GEOMETRY                        ("ST_GEOMETRY"                      ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	ST_POINT                           ("ST_POINT"                         ,StandardTypeMetadata.ST_POINT                           , 1, 1, 1),
	TIME                               ("TIME"                             ,StandardTypeMetadata.TIME                               , 1, 1, 1),
	TIMESTAMP                          ("TIMESTAMP"                        ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	TINYINT                            ("TINYINT"                          ,StandardTypeMetadata.TINYINT                            , 1, 1, 1),
	VARBINARY                          ("VARBINARY"                        ,StandardTypeMetadata.VARBINARY                          , 0, 1, 1),
	VARCHAR                            ("VARCHAR"                          ,StandardTypeMetadata.VARCHAR                            , 0, 1, 1),
	ACLITEM                            ("ACLITEM"                          ,StandardTypeMetadata.NONE                               ),
	AGG_STATE                          ("AGG_STATE"                        ,StandardTypeMetadata.NONE                               ),
	AGGREGATE_METRIC_DOUBLE            ("aggregate_metric_double"          ,StandardTypeMetadata.NONE                               ),
	ALIAS                              ("alias"                            ,StandardTypeMetadata.NONE                               ),
	ARRAY                              ("ARRAY"                            ,StandardTypeMetadata.NONE                               ),
	BFILE                              ("BFILE"                            ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	BIGSERIAL                          ("BIGSERIAL"                        ,StandardTypeMetadata.BIGINT                             , 1, 1, 1),
	BINARY_DOUBLE                      ("BINARY_DOUBLE"                    ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	BINARY_FLOAT                       ("BINARY_FLOAT"                     ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	BINARY_INTEGER                     ("BINARY_INTEGER"                   ,StandardTypeMetadata.NONE                               ),
	BIT                                ("BIT"                              ,StandardTypeMetadata.BOOLEAN                            , 1, 1, 1),
	BIT_VARYING                        ("BIT VARYING"                      ,StandardTypeMetadata.NONE                               ),
	BITMAP                             ("BITMAP"                           ,StandardTypeMetadata.NONE                               ),
	BOOL                               ("BOOL"                             ,StandardTypeMetadata.BOOLEAN                            , 1, 1, 1),
	BOX                                ("BOX"                              ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	BPCHAR                             ("BPCHAR"                           ,StandardTypeMetadata.NONE                               ),
	BPCHARBYTE                         ("BPCHARBYTE"                       ,StandardTypeMetadata.NONE                               ),
	BYTE                               ("BYTE"                             ,StandardTypeMetadata.NONE                               ),
	BYTEA                              ("BYTEA"                            ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	CHARACTER                          ("CHARACTER"                        ,StandardTypeMetadata.NONE                               ),
	CID                                ("CID"                              ,StandardTypeMetadata.NONE                               ),
	CIDR                               ("CIDR"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	CIRCLE                             ("CIRCLE"                           ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	CLOB                               ("CLOB"                             ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	COMPLETION                         ("completion"                       ,StandardTypeMetadata.NONE                               ),
	CURSOR                             ("CURSOR"                           ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_DATE32                  ("DATE32"                           ,StandardTypeMetadata.NONE                               ),
	DATERANGE                          ("DATERANGE"                        ,StandardTypeMetadata.NONE                               ),
	DATETIME                           ("DATETIME"                         ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	DATETIME2                          ("DATETIME2"                        ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	CLICKHOUSE_DATETIME64              ("DATETIME64"                       ,StandardTypeMetadata.NUMBER                             ),
	DATETIMEOFFSET                     ("DATETIMEOFFSET"                   ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	DEC                                ("DEC"                              ,StandardTypeMetadata.DECIMAL                            ),
	DECFLOAT                           ("DECFLOAT"                         ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_DECIMAL128              ("DECIMAL128"                       ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_DECIMAL256              ("DECIMAL256"                       ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_DECIMAL32               ("DECIMAL32"                        ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_DECIMAL64               ("DECIMAL64"                        ,StandardTypeMetadata.NUMBER                             ),
	DENSE_VECTOR                       ("dense_vector"                     ,StandardTypeMetadata.NONE                               ),
	DOUBLE_PRECISION                   ("DOUBLE PRECISION"                 ,StandardTypeMetadata.FLOAT                              ),
	DSINTERVAL                         ("DSINTERVAL"                       ,StandardTypeMetadata.NONE                               ),
	DURATION                           ("DURATION"                         ,StandardTypeMetadata.NONE                               ),
	ENUM                               ("ENUM"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	FIXED_STRING                       ("FIXED_STRING"                     ,StandardTypeMetadata.CHAR                               ),
	FIXEDSTRING                        ("FixedString"                      ,StandardTypeMetadata.CHAR                               ),
	FLATTENED                          ("flattened"                        ,StandardTypeMetadata.NONE                               ),
	FLOAT                              ("FLOAT"                            ,StandardTypeMetadata.DOUBLE                             , 1, 0, 0),
	CLICKHOUSE_FLOAT32                 ("FLOAT32"                          ,StandardTypeMetadata.NUMBER                             ),
	FLOAT4                             ("FLOAT4"                           ,StandardTypeMetadata.DOUBLE                             , 1, 1, 1),
	CLICKHOUSE_FLOAT64                 ("FLOAT64"                          ,StandardTypeMetadata.NUMBER                             ),
	FLOAT8                             ("FLOAT8"                           ,StandardTypeMetadata.DOUBLE                             , 1, 1, 1),
	GEO_POINT                          ("geo_point"                        ,StandardTypeMetadata.NONE                               ),
	GEO_SHAPE                          ("geo_shape"                        ,StandardTypeMetadata.NONE                               ),
	GEOGRAPHY                          ("GEOGRAPHY"                        ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	GEOGRAPHY_POINT                    ("GEOGRAPHY_POINT"                  ,StandardTypeMetadata.NONE                               ),
	GEOMETRY                           ("GEOMETRY"                         ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	GEOMETRYCOLLECTION                 ("GEOMETRYCOLLECTION"               ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	GTSVECTOR                          ("GTSVECTOR"                        ,StandardTypeMetadata.NONE                               ),
	GUID                               ("GUID"                             ,StandardTypeMetadata.NONE                               ),
	HALF_FLOAT                         ("half_float"                       ,StandardTypeMetadata.NONE                               ),
	HIERARCHYID                        ("HIERARCHYID"                      ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	HISTOGRAM                          ("histogram"                        ,StandardTypeMetadata.NONE                               ),
	HLL                                ("HLL"                              ,StandardTypeMetadata.NONE                               ),
	IMAGE                              ("IMAGE"                            ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	INET                               ("INET"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	INT                                ("INT"                              ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	INT128                             ("INT128"                           ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_INT128                  ("INT128"                           ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_INT16                   ("INT16"                            ,StandardTypeMetadata.NUMBER                             ),
	INT16                              ("INT16"                            ,StandardTypeMetadata.NONE                               ),
	INT2                               ("INT2"                             ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	INT256                             ("INT256"                           ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_INT256                  ("INT256"                           ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_INT32                   ("INT32"                            ,StandardTypeMetadata.NUMBER                             ),
	INT32                              ("INT32"                            ,StandardTypeMetadata.NUMBER                             ),
	INT4                               ("INT4"                             ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	INT4RANGE                          ("INT4RANGE"                        ,StandardTypeMetadata.NUMBER                             ),
	INT64                              ("INT64"                            ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_INT64                   ("INT64"                            ,StandardTypeMetadata.NUMBER                             ),
	INT8                               ("INT8"                             ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	CLICKHOUSE_INT8                    ("INT8"                             ,StandardTypeMetadata.NUMBER                             ),
	INT8RANGE                          ("INT8RANGE"                        ,StandardTypeMetadata.NUMBER                             ),
	INTERVAL                           ("INTERVAL"                         ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	IP                                 ("ip"                               ,StandardTypeMetadata.NONE                               ),
	IPV4                               ("IPV4"                             ,StandardTypeMetadata.NONE                               ),
	IPV6                               ("IPV6"                             ,StandardTypeMetadata.NONE                               ),
	JAVA_OBJECT                        ("JAVA_OBJECT"                      ,StandardTypeMetadata.NONE                               ),
	JOIN                               ("join"                             ,StandardTypeMetadata.NONE                               ),
	JSON                               ("JSON"                             ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	JSONB                              ("JSONB"                            ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	JSONPATH                           ("JSONPATH"                         ,StandardTypeMetadata.NONE                               ),
	KEYWORD                            ("KEYWORD"                          ,StandardTypeMetadata.NONE                               ),
	LARGEINT                           ("LARGEINT"                         ,StandardTypeMetadata.NONE                               ),
	LINE                               ("LINE"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	LINESTRING                         ("LINESTRING"                       ,StandardTypeMetadata.NONE                               ),
	LIST                               ("LIST"                             ,StandardTypeMetadata.NONE                               ),
	LONG                               ("long"                             ,StandardTypeMetadata.NONE                               ),
	LONGBLOB                           ("LONGBLOB"                         ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	LONGTEXT                           ("LONGTEXT"                         ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	LOWCARDINALITY                     ("LowCardinality"                   ,StandardTypeMetadata.NONE                               ),
	LSEG                               ("LSEG"                             ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	LVARCHAR                           ("LVARCHAR"                         ,StandardTypeMetadata.NONE                               ),
	MACADDR                            ("MACADDR"                          ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	MACADDR8                           ("MACADDR8"                         ,StandardTypeMetadata.NONE                               ),
	MAP                                ("MAP"                              ,StandardTypeMetadata.NONE                               ),
	MEDIUMBLOB                         ("MEDIUMBLOB"                       ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	MEDIUMINT                          ("MEDIUMINT"                        ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	MEDIUMTEXT                         ("MEDIUMTEXT"                       ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	MONEY                              ("MONEY"                            ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	MULTILINESTRING                    ("MULTILINESTRING"                  ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	MULTIPOINT                         ("MULTIPOINT"                       ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	MULTIPOLYGON                       ("MULTIPOLYGON"                     ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	MULTISET                           ("MULTISET"                         ,StandardTypeMetadata.NONE                               ),
	NATURALN                           ("NATURALN"                         ,StandardTypeMetadata.NONE                               ),
	NCHAR                              ("NCHAR"                            ,StandardTypeMetadata.NVARCHAR                           , 0, 1, 1),
	NESTED                             ("nested"                           ,StandardTypeMetadata.NONE                               ),
	NTEXT                              ("NTEXT"                            ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	NUMBER                             ("NUMBER"                           ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	NUMERIC                            ("NUMERIC"                          ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	NUMRANGE                           ("NUMRANGE"                         ,StandardTypeMetadata.NONE                               ),
	NVARCHAR2                          ("NVARCHAR2"                        ,StandardTypeMetadata.NVARCHAR                           , 0, 1, 1),
	OBJECT                             ("OBJECT"                           ,StandardTypeMetadata.NONE                               ),
	OID                                ("OID"                              ,StandardTypeMetadata.NONE                               ),
	ORA_DATE                           ("ORA_DATE"                         ,StandardTypeMetadata.NONE                               ),
	PATH                               ("PATH"                             ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	PERCOLATOR                         ("percolator"                       ,StandardTypeMetadata.NONE                               ),
	PG_SNAPSHOT                        ("PG_SNAPSHOT"                      ,StandardTypeMetadata.NONE                               ),
	POINT                              ("POINT"                            ,StandardTypeMetadata.ST_POINT                           , 1, 1, 1),
	POLYGON                            ("POLYGON"                          ,StandardTypeMetadata.ST_GEOMETRY                        , 1, 1, 1),
	POSITIVE                           ("POSITIVE"                         ,StandardTypeMetadata.NONE                               ),
	POSITIVEN                          ("POSITIVEN"                        ,StandardTypeMetadata.NONE                               ),
	RANGE                              ("Range"                            ,StandardTypeMetadata.NONE                               ),
	RANK_FEATURE                       ("rank_feature"                     ,StandardTypeMetadata.NONE                               ),
	RANK_FEATURES                      ("rank_features"                    ,StandardTypeMetadata.NONE                               ),
	RAW                                ("RAW"                              ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
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
	RING                               ("RING"                             ,StandardTypeMetadata.NONE                               ),
	ROW                                ("ROW"                              ,StandardTypeMetadata.NONE                               ),
	ROWID                              ("ROWID"                            ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	SCALED_FLOAT                       ("scaled_float"                     ,StandardTypeMetadata.NONE                               ),
	SEARCH_AS_YOU_TYPE                 ("search_as_you_type"               ,StandardTypeMetadata.NONE                               ),
	SERIAL                             ("SERIAL"                           ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	SERIAL2                            ("SERIAL2"                          ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	SERIAL4                            ("SERIAL4"                          ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	SERIAL8                            ("SERIAL8"                          ,StandardTypeMetadata.BIGINT                             , 1, 1, 1),
	SET                                ("SET"                              ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	SHAPE                              ("shape"                            ,StandardTypeMetadata.NONE                               ),
	SHORT                              ("SHORT"                            ,StandardTypeMetadata.NONE                               ),
	SIGNTYPE                           ("SIGNTYPE"                         ,StandardTypeMetadata.NONE                               ),
	SIMPLE_DOUBLE                      ("SIMPLE_DOUBLE"                    ,StandardTypeMetadata.NONE                               ),
	SIMPLE_FLOAT                       ("SIMPLE_FLOAT"                     ,StandardTypeMetadata.NONE                               ),
	SIMPLE_INTEGER                     ("SIMPLE_INTEGER"                   ,StandardTypeMetadata.NONE                               ),
	SIMPLEAGGREGATEFUNCTION            ("SimpleAggregateFunction"          ,StandardTypeMetadata.NONE                               ),
	SMALLDATETIME                      ("SMALLDATETIME"                    ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	SMALLFLOAT                         ("SMALLFLOAT"                       ,StandardTypeMetadata.NONE                               ),
	SMALLMONEY                         ("SMALLMONEY"                       ,StandardTypeMetadata.DECIMAL                            , 1, 0, 0),
	SMALLSERIAL                        ("SMALLSERIAL"                      ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
	SPARSE_VECTOR                      ("sparse_vector"                    ,StandardTypeMetadata.NONE                               ),
	SQL_DATETIMEOFFSET                 ("SQL_DATETIMEOFFSET"               ,StandardTypeMetadata.NONE                               ),
	SQL_VARIANT                        ("SQL_VARIANT"                      ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	STRING                             ("STRING"                           ,StandardTypeMetadata.NONE                               ),
	STRUCT                             ("STRUCT"                           ,StandardTypeMetadata.NONE                               ),
	STRUCTS                            ("STRUCTS"                          ,StandardTypeMetadata.NONE                               ),
	SYS_REFCURSOR                      ("SYS_REFCURSOR"                    ,StandardTypeMetadata.NONE                               ),
	SYSNAME                            ("SYSNAME"                          ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	TEXT                               ("TEXT"                             ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	TID                                ("TID"                              ,StandardTypeMetadata.NONE                               ),
	TIME_TZ_UNCONSTRAINED              ("TIME TZ UNCONSTRAINED"            ,StandardTypeMetadata.NONE                               ),
	TIME_WITH_TIME_ZONE                ("TIME WITH TIME ZONE"              ,StandardTypeMetadata.TIME_WITH_TIME_ZONE                ),
	TIME_WITH_ZONE                     ("TIME WITH TIME ZONE"              ,StandardTypeMetadata.TIME_WITH_TIME_ZONE                ),
	TIME_WITHOUT_TIME_ZONE             ("TIME WITHOUT TIME ZONE"           ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE             ),
	TIME_WITHOUT_ZONE                  ("TIME WITHOUT TIME ZONE"           ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE             ),
	TIME_UNCONSTRAINED                 ("TIME_UNCONSTRAINED"               ,StandardTypeMetadata.NONE                               ),
	TIMESTAMP_WITH_LOCAL_ZONE          ("TIMESTAMP WITH LOCAL TIME ZONE"   ,StandardTypeMetadata.TIMESTAMP                          , "TIMESTAMP WITH LOCAL TIME ZONE"   , null                               , 1, 1, 1),
	TIMESTAMP_WITH_ZONE                ("TIMESTAMP WITH TIME ZONE"         ,StandardTypeMetadata.TIMESTAMP_WITH_TIME_ZONE           ),
	TIMESTAMP_WITH_TIME_ZONE           ("TIMESTAMP WITH TIME ZONE"         ,StandardTypeMetadata.TIMESTAMP                          , "TIMESTAMP WITH TIME ZONE"         , null                               , 1, 1, 1),
	TIMESTAMP_WITHOUT_TIME_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"      ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE        ),
	TIMESTAMP_WITHOUT_ZONE             ("TIMESTAMP WITHOUT TIME ZONE"      ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE        ),
	TIMESTAMPTZ                        ("TIMESTAMPTZ"                      ,StandardTypeMetadata.NONE                               ),
	TIMEZ                              ("TIMEZ"                            ,StandardTypeMetadata.TIMESTAMP                          , 1, 1, 1),
	TINYBLOB                           ("TINYBLOB"                         ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	TINYTEXT                           ("TINYTEXT"                         ,StandardTypeMetadata.NCLOB                              , 1, 1, 1),
	TOKEN_COUNT                        ("token_count"                      ,StandardTypeMetadata.NONE                               ),
	TSQUERY                            ("TSQUERY"                          ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	TSRANGE                            ("TSRANGE"                          ,StandardTypeMetadata.NONE                               ),
	TSTZRANGE                          ("TSTZRANGE"                        ,StandardTypeMetadata.NONE                               ),
	TSVECTOR                           ("TSVECTOR"                         ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	TUPLE                              ("TUPLE"                            ,StandardTypeMetadata.NONE                               ),
	TXID_SNAPSHOT                      ("TXID_SNAPSHOT"                    ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	CLICKHOUSE_UINT128                 ("UINT128"                          ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_UINT16                  ("UINT16"                           ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_UINT256                 ("UINT256"                          ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_UINT32                  ("UINT32"                           ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_UINT64                  ("UINT64"                           ,StandardTypeMetadata.NUMBER                             ),
	CLICKHOUSE_UINT8                   ("UINT8"                            ,StandardTypeMetadata.NUMBER                             ),
	UNIQUEIDENTIFIER                   ("UNIQUEIDENTIFIER"                 ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	UNSIGNED_LONG                      ("unsigned_long"                    ,StandardTypeMetadata.NONE                               ),
	UROWID                             ("UROWID"                           ,StandardTypeMetadata.VARCHAR                            , 0, 1, 1),
	UUID                               ("UUID"                             ,StandardTypeMetadata.ILLEGAL                            , -1, -1, -1),
	VARBIT                             ("VARBIT"                           ,StandardTypeMetadata.BLOB                               , 1, 1, 1),
	VARCHAR2                           ("VARCHAR2"                         ,StandardTypeMetadata.VARCHAR                            , 0, 1, 1),
	VARCHARBYTE                        ("VARCHARBYTE"                      ,StandardTypeMetadata.NONE                               ),
	VERSION                            ("version"                          ,StandardTypeMetadata.NONE                               ),
	XID                                ("XID"                              ,StandardTypeMetadata.NONE                               ),
	XML                                ("XML"                              ,StandardTypeMetadata.NVARCHAR                           , 0, 1, 1),
	YEAR                               ("YEAR"                             ,StandardTypeMetadata.INTEGER                            , 1, 1, 1),
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

	HanaTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
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

	HanaTypeMetadataAlias(String input, TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, null , null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	HanaTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	HanaTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	HanaTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(null, standard, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	HanaTypeMetadataAlias(String input, TypeMetadata standard){
		this.input = input;
		this.standard = standard;
	}

	HanaTypeMetadataAlias(TypeMetadata standard){
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
