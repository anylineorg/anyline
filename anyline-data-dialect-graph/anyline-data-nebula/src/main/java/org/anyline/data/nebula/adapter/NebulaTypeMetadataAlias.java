package org.anyline.data.nebula.adapter;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum NebulaTypeMetadataAlias implements TypeMetadataAlias {
	BOOL                               ("BOOL"                             ,StandardTypeMetadata.BOOL                               ),
	DATE                               ("DATE"                             ,StandardTypeMetadata.DATE                               ),
	DATETIME                           ("DATETIME"                         ,StandardTypeMetadata.DATETIME                           ),
	DOUBLE                             ("DOUBLE"                           ,StandardTypeMetadata.DOUBLE                             , 1, 1, 1),
	DURATION                           ("DURATION"                         ,StandardTypeMetadata.DURATION                           ),
	FIXED_STRING                       ("FIXED_STRING"                     ,StandardTypeMetadata.FIXED_STRING                       , "FIXED_STRING"                     , null                               , 0, 1, 1),
	FLOAT                              ("FLOAT"                            ,StandardTypeMetadata.FLOAT                              , 1, 1, 1),
	INT                                ("INT"                              ,StandardTypeMetadata.INT                                ),
	INT16                              ("INT16"                            ,StandardTypeMetadata.INT16                              ),
	INT32                              ("INT32"                            ,StandardTypeMetadata.INT32                              ),
	INT64                              ("INT64"                            ,StandardTypeMetadata.INT64                              ),
	INT8                               ("INT8"                             ,StandardTypeMetadata.INT8                               ),
	LINESTRING                         ("LINESTRING"                       ,StandardTypeMetadata.LINESTRING                         ),
	LIST                               ("LIST"                             ,StandardTypeMetadata.LIST                               ),
	MAP                                ("MAP"                              ,StandardTypeMetadata.MAP                                ),
	POINT                              ("POINT"                            ,StandardTypeMetadata.POINT                              ),
	POLYGON                            ("POLYGON"                          ,StandardTypeMetadata.POLYGON                            ),
	SET                                ("SET"                              ,StandardTypeMetadata.SET                                ),
	STRING                             ("STRING"                           ,StandardTypeMetadata.STRING                             ),
	TIME                               ("TIME"                             ,StandardTypeMetadata.TIME                               ),
	TIMESTAMP                          ("TIMESTAMP"                        ,StandardTypeMetadata.TIMESTAMP                          ),
	ACLITEM                            ("ACLITEM"                          ,StandardTypeMetadata.ILLEGAL                            ),
	AGG_STATE                          ("AGG_STATE"                        ,StandardTypeMetadata.ILLEGAL                            ),
	AGGREGATE_METRIC_DOUBLE            ("aggregate_metric_double"          ,StandardTypeMetadata.NONE                               ),
	ALIAS                              ("alias"                            ,StandardTypeMetadata.NONE                               ),
	ARRAY                              ("ARRAY"                            ,StandardTypeMetadata.LIST                               ),
	BFILE                              ("BFILE"                            ,StandardTypeMetadata.ILLEGAL                            ),
	BIGINT                             ("BIGINT"                           ,StandardTypeMetadata.INT64                              ),
	BIGSERIAL                          ("BIGSERIAL"                        ,StandardTypeMetadata.INT64                              ),
	BINARY                             ("BINARY"                           ,StandardTypeMetadata.ILLEGAL                            ),
	BINARY_DOUBLE                      ("BINARY_DOUBLE"                    ,StandardTypeMetadata.DOUBLE                             ),
	BINARY_FLOAT                       ("BINARY_FLOAT"                     ,StandardTypeMetadata.FLOAT                              ),
	BINARY_INTEGER                     ("BINARY_INTEGER"                   ,StandardTypeMetadata.ILLEGAL                            ),
	BIT                                ("BIT"                              ,StandardTypeMetadata.ILLEGAL                            ),
	BIT_VARYING                        ("BIT VARYING"                      ,StandardTypeMetadata.ILLEGAL                            ),
	BITMAP                             ("BITMAP"                           ,StandardTypeMetadata.ILLEGAL                            ),
	BLOB                               ("BLOB"                             ,StandardTypeMetadata.ILLEGAL                            ),
	BOOLEAN                            ("BOOLEAN"                          ,StandardTypeMetadata.BOOL                               ),
	BOX                                ("BOX"                              ,StandardTypeMetadata.ILLEGAL                            ),
	BPCHAR                             ("BPCHAR"                           ,StandardTypeMetadata.ILLEGAL                            ),
	BPCHARBYTE                         ("BPCHARBYTE"                       ,StandardTypeMetadata.ILLEGAL                            ),
	BYTE                               ("BYTE"                             ,StandardTypeMetadata.ILLEGAL                            ),
	BYTEA                              ("BYTEA"                            ,StandardTypeMetadata.NONE                               ),
	CHAR                               ("CHAR"                             ,StandardTypeMetadata.NONE                               ),
	CHARACTER                          ("CHARACTER"                        ,StandardTypeMetadata.NONE                               ),
	CID                                ("CID"                              ,StandardTypeMetadata.ILLEGAL                            ),
	CIDR                               ("CIDR"                             ,StandardTypeMetadata.ILLEGAL                            ),
	CIRCLE                             ("CIRCLE"                           ,StandardTypeMetadata.ILLEGAL                            ),
	CLOB                               ("CLOB"                             ,StandardTypeMetadata.STRING                             ),
	COMPLETION                         ("completion"                       ,StandardTypeMetadata.NONE                               ),
	CURSOR                             ("CURSOR"                           ,StandardTypeMetadata.ILLEGAL                            ),
	CLICKHOUSE_DATE32                  ("DATE32"                           ,StandardTypeMetadata.DATE                               ),
	DATERANGE                          ("DATERANGE"                        ,StandardTypeMetadata.ILLEGAL                            ),
	DATETIME2                          ("DATETIME2"                        ,StandardTypeMetadata.DATETIME                           ),
	CLICKHOUSE_DATETIME64              ("DATETIME64"                       ,StandardTypeMetadata.DATETIME                           ),
	DATETIMEOFFSET                     ("DATETIMEOFFSET"                   ,StandardTypeMetadata.ILLEGAL                            ),
	DEC                                ("DEC"                              ,StandardTypeMetadata.DECIMAL                            ),
	DECFLOAT                           ("DECFLOAT"                         ,StandardTypeMetadata.FLOAT                              ),
	DECIMAL                            ("DECIMAL"                          ,StandardTypeMetadata.DOUBLE                             ),
	CLICKHOUSE_DECIMAL128              ("DECIMAL128"                       ,StandardTypeMetadata.DOUBLE                             ),
	CLICKHOUSE_DECIMAL256              ("DECIMAL256"                       ,StandardTypeMetadata.DOUBLE                             ),
	CLICKHOUSE_DECIMAL32               ("DECIMAL32"                        ,StandardTypeMetadata.DOUBLE                             ),
	CLICKHOUSE_DECIMAL64               ("DECIMAL64"                        ,StandardTypeMetadata.DOUBLE                             ),
	DENSE_VECTOR                       ("dense_vector"                     ,StandardTypeMetadata.NONE                               ),
	DOUBLE_PRECISION                   ("DOUBLE PRECISION"                 ,StandardTypeMetadata.DOUBLE                             , 1, 1, 1),
	DSINTERVAL                         ("DSINTERVAL"                       ,StandardTypeMetadata.ILLEGAL                            ),
	ENUM                               ("ENUM"                             ,StandardTypeMetadata.ILLEGAL                            ),
	FIXEDSTRING                        ("FixedString"                      ,StandardTypeMetadata.FIXED_STRING                       , "FIXED_STRING"                     , null                               , 0, 1, 1),
	FLATTENED                          ("flattened"                        ,StandardTypeMetadata.NONE                               ),
	CLICKHOUSE_FLOAT32                 ("FLOAT32"                          ,StandardTypeMetadata.FLOAT                              ),
	FLOAT4                             ("FLOAT4"                           ,StandardTypeMetadata.FLOAT                              , 1, 1, 1),
	CLICKHOUSE_FLOAT64                 ("FLOAT64"                          ,StandardTypeMetadata.FLOAT                              ),
	FLOAT8                             ("FLOAT8"                           ,StandardTypeMetadata.FLOAT                              , 1, 1, 1),
	GEO_POINT                          ("geo_point"                        ,StandardTypeMetadata.NONE                               ),
	GEO_SHAPE                          ("geo_shape"                        ,StandardTypeMetadata.NONE                               ),
	GEOGRAPHY                          ("GEOGRAPHY"                        ,StandardTypeMetadata.ILLEGAL                            ),
	GEOGRAPHY_POINT                    ("GEOGRAPHY_POINT"                  ,StandardTypeMetadata.ILLEGAL                            ),
	GEOMETRY                           ("GEOMETRY"                         ,StandardTypeMetadata.ILLEGAL                            ),
	GEOMETRYCOLLECTION                 ("GEOMETRYCOLLECTION"               ,StandardTypeMetadata.ILLEGAL                            ),
	GTSVECTOR                          ("GTSVECTOR"                        ,StandardTypeMetadata.ILLEGAL                            ),
	GUID                               ("GUID"                             ,StandardTypeMetadata.ILLEGAL                            ),
	HALF_FLOAT                         ("half_float"                       ,StandardTypeMetadata.NONE                               ),
	HIERARCHYID                        ("HIERARCHYID"                      ,StandardTypeMetadata.ILLEGAL                            ),
	HISTOGRAM                          ("histogram"                        ,StandardTypeMetadata.NONE                               ),
	HLL                                ("HLL"                              ,StandardTypeMetadata.ILLEGAL                            ),
	IMAGE                              ("IMAGE"                            ,StandardTypeMetadata.ILLEGAL                            ),
	INET                               ("INET"                             ,StandardTypeMetadata.ILLEGAL                            ),
	INT128                             ("INT128"                           ,StandardTypeMetadata.INT64                              ),
	CLICKHOUSE_INT128                  ("INT128"                           ,StandardTypeMetadata.INT64                              ),
	CLICKHOUSE_INT16                   ("INT16"                            ,StandardTypeMetadata.INT16                              ),
	INT2                               ("INT2"                             ,StandardTypeMetadata.INT                                ),
	CLICKHOUSE_INT256                  ("INT256"                           ,StandardTypeMetadata.INT64                              ),
	INT256                             ("INT256"                           ,StandardTypeMetadata.INT64                              ),
	CLICKHOUSE_INT32                   ("INT32"                            ,StandardTypeMetadata.INT32                              ),
	INT4                               ("INT4"                             ,StandardTypeMetadata.INT                                ),
	INT4RANGE                          ("INT4RANGE"                        ,StandardTypeMetadata.ILLEGAL                            ),
	CLICKHOUSE_INT64                   ("INT64"                            ,StandardTypeMetadata.INT64                              ),
	CLICKHOUSE_INT8                    ("INT8"                             ,StandardTypeMetadata.INT8                               ),
	INT8RANGE                          ("INT8RANGE"                        ,StandardTypeMetadata.ILLEGAL                            ),
	INTEGER                            ("INTEGER"                          ,StandardTypeMetadata.INT                                ),
	INTERVAL                           ("INTERVAL"                         ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_DAY                       ("INTERVAL DAY"                     ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_DAY_HOUR                  ("INTERVAL DAY TO HOUR"             ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_DAY_MINUTE                ("INTERVAL DAY TO MINUTE"           ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_DAY_SECOND                ("INTERVAL DAY TO SECOND"           ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_HOUR                      ("INTERVAL HOUR"                    ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_HOUR_MINUTE               ("INTERVAL HOUR TO MINUTE"          ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_HOUR_SECOND               ("INTERVAL HOUR TO SECOND"          ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_MINUTE                    ("INTERVAL MINUTE"                  ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_MINUTE_SECOND             ("INTERVAL MINUTE TO SECOND"        ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_MONTH                     ("INTERVAL MONTH"                   ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_SECOND                    ("INTERVAL SECOND"                  ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_YEAR                      ("INTERVAL YEAR"                    ,StandardTypeMetadata.ILLEGAL                            ),
	INTERVAL_YEAR_MONTH                ("INTERVAL YEAR TO MONTH"           ,StandardTypeMetadata.ILLEGAL                            ),
	IP                                 ("ip"                               ,StandardTypeMetadata.NONE                               ),
	IPV4                               ("IPV4"                             ,StandardTypeMetadata.ILLEGAL                            ),
	IPV6                               ("IPV6"                             ,StandardTypeMetadata.ILLEGAL                            ),
	JAVA_OBJECT                        ("JAVA_OBJECT"                      ,StandardTypeMetadata.ILLEGAL                            ),
	JOIN                               ("join"                             ,StandardTypeMetadata.NONE                               ),
	JSON                               ("JSON"                             ,StandardTypeMetadata.STRING                             ),
	JSONB                              ("JSONB"                            ,StandardTypeMetadata.STRING                             ),
	JSONPATH                           ("JSONPATH"                         ,StandardTypeMetadata.ILLEGAL                            ),
	KEYWORD                            ("KEYWORD"                          ,StandardTypeMetadata.STRING                             ),
	LARGEINT                           ("LARGEINT"                         ,StandardTypeMetadata.STRING                             ),
	LINE                               ("LINE"                             ,StandardTypeMetadata.LINESTRING                         ),
	LONG                               ("long"                             ,StandardTypeMetadata.NONE                               ),
	LONG_TEXT                          ("LONG"                             ,StandardTypeMetadata.INT64                              ),
	LONGBLOB                           ("LONGBLOB"                         ,StandardTypeMetadata.ILLEGAL                            ),
	LONGTEXT                           ("LONGTEXT"                         ,StandardTypeMetadata.STRING                             ),
	LOWCARDINALITY                     ("LowCardinality"                   ,StandardTypeMetadata.ILLEGAL                            ),
	LSEG                               ("LSEG"                             ,StandardTypeMetadata.ILLEGAL                            ),
	LVARCHAR                           ("LVARCHAR"                         ,StandardTypeMetadata.ILLEGAL                            ),
	MACADDR                            ("MACADDR"                          ,StandardTypeMetadata.ILLEGAL                            ),
	MACADDR8                           ("MACADDR8"                         ,StandardTypeMetadata.ILLEGAL                            ),
	MEDIUMBLOB                         ("MEDIUMBLOB"                       ,StandardTypeMetadata.ILLEGAL                            ),
	MEDIUMINT                          ("MEDIUMINT"                        ,StandardTypeMetadata.INT8                               ),
	MEDIUMTEXT                         ("MEDIUMTEXT"                       ,StandardTypeMetadata.STRING                             ),
	MONEY                              ("MONEY"                            ,StandardTypeMetadata.DOUBLE                             ),
	MULTILINESTRING                    ("MULTILINESTRING"                  ,StandardTypeMetadata.ILLEGAL                            ),
	MULTIPOINT                         ("MULTIPOINT"                       ,StandardTypeMetadata.ILLEGAL                            ),
	MULTIPOLYGON                       ("MULTIPOLYGON"                     ,StandardTypeMetadata.ILLEGAL                            ),
	MULTISET                           ("MULTISET"                         ,StandardTypeMetadata.ILLEGAL                            ),
	NATURALN                           ("NATURALN"                         ,StandardTypeMetadata.ILLEGAL                            ),
	NCHAR                              ("NCHAR"                            ,StandardTypeMetadata.FIXED_STRING                       ),
	NCLOB                              ("NCLOB"                            ,StandardTypeMetadata.STRING                             ),
	NESTED                             ("nested"                           ,StandardTypeMetadata.NONE                               ),
	NTEXT                              ("NTEXT"                            ,StandardTypeMetadata.STRING                             ),
	NUMBER                             ("NUMBER"                           ,StandardTypeMetadata.DOUBLE                             ),
	NUMERIC                            ("NUMERIC"                          ,StandardTypeMetadata.DOUBLE                             ),
	NUMRANGE                           ("NUMRANGE"                         ,StandardTypeMetadata.ILLEGAL                            ),
	NVARCHAR                           ("NVARCHAR"                         ,StandardTypeMetadata.STRING                             ),
	NVARCHAR2                          ("NVARCHAR2"                        ,StandardTypeMetadata.STRING                             ),
	OBJECT                             ("OBJECT"                           ,StandardTypeMetadata.ILLEGAL                            ),
	OID                                ("OID"                              ,StandardTypeMetadata.ILLEGAL                            ),
	ORA_DATE                           ("ORA_DATE"                         ,StandardTypeMetadata.ILLEGAL                            ),
	PATH                               ("PATH"                             ,StandardTypeMetadata.ILLEGAL                            ),
	PERCOLATOR                         ("percolator"                       ,StandardTypeMetadata.NONE                               ),
	PG_SNAPSHOT                        ("PG_SNAPSHOT"                      ,StandardTypeMetadata.ILLEGAL                            ),
	POSITIVE                           ("POSITIVE"                         ,StandardTypeMetadata.ILLEGAL                            ),
	POSITIVEN                          ("POSITIVEN"                        ,StandardTypeMetadata.ILLEGAL                            ),
	RANGE                              ("Range"                            ,StandardTypeMetadata.NONE                               ),
	RANK_FEATURE                       ("rank_feature"                     ,StandardTypeMetadata.NONE                               ),
	RANK_FEATURES                      ("rank_features"                    ,StandardTypeMetadata.NONE                               ),
	RAW                                ("RAW"                              ,StandardTypeMetadata.ILLEGAL                            ),
	REAL                               ("REAL"                             ,StandardTypeMetadata.ILLEGAL                            ),
	REFCURSOR                          ("REFCURSOR"                        ,StandardTypeMetadata.ILLEGAL                            ),
	REGCLASS                           ("REGCLASS"                         ,StandardTypeMetadata.ILLEGAL                            ),
	REGCONFIG                          ("REGCONFIG"                        ,StandardTypeMetadata.ILLEGAL                            ),
	REGDICTIONARY                      ("REGDICTIONARY"                    ,StandardTypeMetadata.ILLEGAL                            ),
	REGNAMESPACE                       ("REGNAMESPACE"                     ,StandardTypeMetadata.ILLEGAL                            ),
	REGOPER                            ("REGOPER"                          ,StandardTypeMetadata.ILLEGAL                            ),
	REGOPERATOR                        ("REGOPERATOR"                      ,StandardTypeMetadata.ILLEGAL                            ),
	REGPROC                            ("REGPROC"                          ,StandardTypeMetadata.ILLEGAL                            ),
	REGPROCEDURE                       ("REGPROCEDURE"                     ,StandardTypeMetadata.ILLEGAL                            ),
	REGROLE                            ("REGROLE"                          ,StandardTypeMetadata.ILLEGAL                            ),
	REGTYPE                            ("REGTYPE"                          ,StandardTypeMetadata.ILLEGAL                            ),
	RING                               ("RING"                             ,StandardTypeMetadata.ILLEGAL                            ),
	ROW                                ("ROW"                              ,StandardTypeMetadata.ILLEGAL                            ),
	ROWID                              ("ROWID"                            ,StandardTypeMetadata.ILLEGAL                            ),
	SCALED_FLOAT                       ("scaled_float"                     ,StandardTypeMetadata.NONE                               ),
	SEARCH_AS_YOU_TYPE                 ("search_as_you_type"               ,StandardTypeMetadata.NONE                               ),
	SECONDDATE                         ("SECONDDATE"                       ,StandardTypeMetadata.ILLEGAL                            ),
	SERIAL                             ("SERIAL"                           ,StandardTypeMetadata.INT                                ),
	SERIAL2                            ("SERIAL2"                          ,StandardTypeMetadata.INT                                ),
	SERIAL4                            ("SERIAL4"                          ,StandardTypeMetadata.INT                                ),
	SERIAL8                            ("SERIAL8"                          ,StandardTypeMetadata.INT64                              ),
	SHAPE                              ("shape"                            ,StandardTypeMetadata.NONE                               ),
	SHORT                              ("SHORT"                            ,StandardTypeMetadata.INT16                              ),
	SIGNTYPE                           ("SIGNTYPE"                         ,StandardTypeMetadata.ILLEGAL                            ),
	SIMPLE_DOUBLE                      ("SIMPLE_DOUBLE"                    ,StandardTypeMetadata.DOUBLE                             ),
	SIMPLE_FLOAT                       ("SIMPLE_FLOAT"                     ,StandardTypeMetadata.FLOAT                              ),
	SIMPLE_INTEGER                     ("SIMPLE_INTEGER"                   ,StandardTypeMetadata.ILLEGAL                            ),
	SIMPLEAGGREGATEFUNCTION            ("SimpleAggregateFunction"          ,StandardTypeMetadata.ILLEGAL                            ),
	SMALLDATETIME                      ("SMALLDATETIME"                    ,StandardTypeMetadata.DATETIME                           ),
	SMALLDECIMAL                       ("SMALLDECIMAL"                     ,StandardTypeMetadata.DOUBLE                             ),
	SMALLFLOAT                         ("SMALLFLOAT"                       ,StandardTypeMetadata.FLOAT                              ),
	SMALLINT                           ("SMALLINT"                         ,StandardTypeMetadata.INT16                              ),
	SMALLMONEY                         ("SMALLMONEY"                       ,StandardTypeMetadata.DOUBLE                             ),
	SMALLSERIAL                        ("SMALLSERIAL"                      ,StandardTypeMetadata.INT16                              ),
	SPARSE_VECTOR                      ("sparse_vector"                    ,StandardTypeMetadata.NONE                               ),
	SQL_DATETIMEOFFSET                 ("SQL_DATETIMEOFFSET"               ,StandardTypeMetadata.ILLEGAL                            ),
	SQL_VARIANT                        ("SQL_VARIANT"                      ,StandardTypeMetadata.ILLEGAL                            ),
	ST_GEOMETRY                        ("ST_GEOMETRY"                      ,StandardTypeMetadata.ILLEGAL                            ),
	ST_POINT                           ("ST_POINT"                         ,StandardTypeMetadata.POINT                              ),
	STRUCT                             ("STRUCT"                           ,StandardTypeMetadata.ILLEGAL                            ),
	STRUCTS                            ("STRUCTS"                          ,StandardTypeMetadata.ILLEGAL                            ),
	SYS_REFCURSOR                      ("SYS_REFCURSOR"                    ,StandardTypeMetadata.ILLEGAL                            ),
	SYSNAME                            ("SYSNAME"                          ,StandardTypeMetadata.ILLEGAL                            ),
	TEXT                               ("TEXT"                             ,StandardTypeMetadata.STRING                             ),
	TID                                ("TID"                              ,StandardTypeMetadata.ILLEGAL                            ),
	TIME_TZ_UNCONSTRAINED              ("TIME TZ UNCONSTRAINED"            ,StandardTypeMetadata.TIME                               ),
	TIME_WITH_ZONE                     ("TIME WITH TIME ZONE"              ,StandardTypeMetadata.TIME_WITH_TIME_ZONE                ),
	TIME_WITH_TIME_ZONE                ("TIME WITH TIME ZONE"              ,StandardTypeMetadata.TIME_WITH_TIME_ZONE                ),
	TIME_WITHOUT_ZONE                  ("TIME WITHOUT TIME ZONE"           ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE             ),
	TIME_WITHOUT_TIME_ZONE             ("TIME WITHOUT TIME ZONE"           ,StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE             ),
	TIME_UNCONSTRAINED                 ("TIME_UNCONSTRAINED"               ,StandardTypeMetadata.TIME                               ),
	TIMESTAMP_WITH_LOCAL_ZONE          ("TIMESTAMP WITH LOCAL TIME ZONE"   ,StandardTypeMetadata.TIMESTAMP                          ),
	TIMESTAMP_WITH_TIME_ZONE           ("TIMESTAMP WITH TIME ZONE"         ,StandardTypeMetadata.TIMESTAMP                          ),
	TIMESTAMP_WITH_ZONE                ("TIMESTAMP WITH TIME ZONE"         ,StandardTypeMetadata.TIMESTAMP_WITH_TIME_ZONE           ),
	TIMESTAMP_WITHOUT_TIME_ZONE        ("TIMESTAMP WITHOUT TIME ZONE"      ,StandardTypeMetadata.TIMESTAMP                          ),
	TIMESTAMP_WITHOUT_ZONE             ("TIMESTAMP WITHOUT TIME ZONE"      ,StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE        ),
	TIMESTAMPTZ                        ("TIMESTAMPTZ"                      ,StandardTypeMetadata.TIMESTAMP                          ),
	TIMEZ                              ("TIMEZ"                            ,StandardTypeMetadata.TIME                               ),
	TINYBLOB                           ("TINYBLOB"                         ,StandardTypeMetadata.ILLEGAL                            ),
	TINYINT                            ("TINYINT"                          ,StandardTypeMetadata.INT8                               ),
	TINYTEXT                           ("TINYTEXT"                         ,StandardTypeMetadata.STRING                             ),
	TOKEN_COUNT                        ("token_count"                      ,StandardTypeMetadata.NONE                               ),
	TSQUERY                            ("TSQUERY"                          ,StandardTypeMetadata.ILLEGAL                            ),
	TSRANGE                            ("TSRANGE"                          ,StandardTypeMetadata.ILLEGAL                            ),
	TSTZRANGE                          ("TSTZRANGE"                        ,StandardTypeMetadata.ILLEGAL                            ),
	TSVECTOR                           ("TSVECTOR"                         ,StandardTypeMetadata.ILLEGAL                            ),
	TUPLE                              ("TUPLE"                            ,StandardTypeMetadata.ILLEGAL                            ),
	TXID_SNAPSHOT                      ("TXID_SNAPSHOT"                    ,StandardTypeMetadata.ILLEGAL                            ),
	CLICKHOUSE_UINT128                 ("UINT128"                          ,StandardTypeMetadata.INT64                              ),
	CLICKHOUSE_UINT16                  ("UINT16"                           ,StandardTypeMetadata.INT16                              ),
	CLICKHOUSE_UINT256                 ("UINT256"                          ,StandardTypeMetadata.INT64                              ),
	CLICKHOUSE_UINT32                  ("UINT32"                           ,StandardTypeMetadata.INT32                              ),
	CLICKHOUSE_UINT64                  ("UINT64"                           ,StandardTypeMetadata.INT64                              ),
	CLICKHOUSE_UINT8                   ("UINT8"                            ,StandardTypeMetadata.INT8                               ),
	UNIQUEIDENTIFIER                   ("UNIQUEIDENTIFIER"                 ,StandardTypeMetadata.ILLEGAL                            ),
	UNSIGNED_LONG                      ("unsigned_long"                    ,StandardTypeMetadata.NONE                               ),
	UROWID                             ("UROWID"                           ,StandardTypeMetadata.ILLEGAL                            ),
	UUID                               ("UUID"                             ,StandardTypeMetadata.ILLEGAL                            ),
	VARBINARY                          ("VARBINARY"                        ,StandardTypeMetadata.ILLEGAL                            ),
	VARBIT                             ("VARBIT"                           ,StandardTypeMetadata.ILLEGAL                            ),
	VARCHAR                            ("VARCHAR"                          ,StandardTypeMetadata.STRING                             ),
	VARCHAR2                           ("VARCHAR2"                         ,StandardTypeMetadata.STRING                             ),
	VARCHARBYTE                        ("VARCHARBYTE"                      ,StandardTypeMetadata.ILLEGAL                            ),
	VERSION                            ("version"                          ,StandardTypeMetadata.NONE                               ),
	XID                                ("XID"                              ,StandardTypeMetadata.ILLEGAL                            ),
	XML                                ("XML"                              ,StandardTypeMetadata.STRING                             ),
	YEAR                               ("YEAR"                             ,StandardTypeMetadata.INT                                ),
	YMINTERVAL                         ("YMINTERVAL"                       ,StandardTypeMetadata.ILLEGAL                            );

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

	NebulaTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
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

	NebulaTypeMetadataAlias(String input, TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, null , null, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	NebulaTypeMetadataAlias(String input, TypeMetadata standard, String meta, String formula, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, meta, formula, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	NebulaTypeMetadataAlias(String input, TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(input, standard, null, null, null, ignoreLength, ignorePrecision, ignoreScale);
	}

	NebulaTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
		this(null, standard, lengthRefer, precisionRefer, scaleRefer, ignoreLength, ignorePrecision, ignoreScale);
	}

	NebulaTypeMetadataAlias(String input, TypeMetadata standard){
		this.input = input;
		this.standard = standard;
	}

	NebulaTypeMetadataAlias(TypeMetadata standard){
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
