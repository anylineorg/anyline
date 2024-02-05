package org.anyline.data.jdbc.clickhouse;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;

public enum ClickhouseTypeMetadataAlias implements TypeMetadataAlias {
    //https://clickhouse.com/docs/en/sql-reference/data-types
    //select * from system.data_type_families
    ARRAY                       (StandardTypeMetadata.ARRAY),
    BFILE                       (StandardTypeMetadata.ILLEGAL),
    BINARY_DOUBLE               (StandardTypeMetadata.CLICKHOUSE_FLOAT64),
    BINARY_FLOAT                (StandardTypeMetadata.CLICKHOUSE_FLOAT32),
    BIGINT                      (StandardTypeMetadata.CLICKHOUSE_INT64),
    BIGSERIAL                   (StandardTypeMetadata.CLICKHOUSE_INT64),
    BINARY                      (StandardTypeMetadata.ILLEGAL),
    BIT                         (StandardTypeMetadata.BOOL),
    BLOB                        (StandardTypeMetadata.FixedString),
    BOOL                        (StandardTypeMetadata.BOOL),
    BOX                         (StandardTypeMetadata.ILLEGAL),
    BYTEA                       (StandardTypeMetadata.FixedString),
    CHAR                        (StandardTypeMetadata.STRING),
    CIDR                        (StandardTypeMetadata.ILLEGAL),
    CIRCLE                      (StandardTypeMetadata.ILLEGAL),
    CLOB                        (StandardTypeMetadata.STRING),
    DATE                        (StandardTypeMetadata.DATE),
    DATE32                      (StandardTypeMetadata.CLICKHOUSE_DATE32),
    DATETIME                    (StandardTypeMetadata.DATETIME),
    DATETIME64                  (StandardTypeMetadata.CLICKHOUSE_DATETIME64),
    DATETIME2                   (StandardTypeMetadata.DATETIME),
    DATETIMEOFFSET              (StandardTypeMetadata.DATETIME),
    DECIMAL                     (StandardTypeMetadata.DECIMAL),
    DECIMAL128                  (StandardTypeMetadata.CLICKHOUSE_DECIMAL128),
    DECIMAL256                  (StandardTypeMetadata.CLICKHOUSE_DECIMAL256),
    DECIMAL32                   (StandardTypeMetadata.CLICKHOUSE_DECIMAL32),
    DECIMAL64                   (StandardTypeMetadata.CLICKHOUSE_DECIMAL64),
    DOUBLE                      (StandardTypeMetadata.CLICKHOUSE_FLOAT64),
    ENUM                        (StandardTypeMetadata.ENUM),
    FixedString                 (StandardTypeMetadata.FixedString),
    FLOAT                       (StandardTypeMetadata.CLICKHOUSE_FLOAT32),
    FLOAT4                      (StandardTypeMetadata.CLICKHOUSE_FLOAT32),
    FLOAT8                      (StandardTypeMetadata.CLICKHOUSE_FLOAT32),
    GEOGRAPHY                   (StandardTypeMetadata.ILLEGAL),
    GEOMETRY                    (StandardTypeMetadata.ILLEGAL),
    GEOMETRYCOLLECTION          (StandardTypeMetadata.ILLEGAL),
    HIERARCHYID                 (StandardTypeMetadata.ILLEGAL),
    IMAGE                       (StandardTypeMetadata.FixedString),
    INET                        (StandardTypeMetadata.ILLEGAL),
    INTERVAL                    (StandardTypeMetadata.ILLEGAL),
    INT                         (StandardTypeMetadata.CLICKHOUSE_INT32),
    INT8                        (StandardTypeMetadata.CLICKHOUSE_INT8),
    INT16                       (StandardTypeMetadata.CLICKHOUSE_INT16),
    INT32                       (StandardTypeMetadata.CLICKHOUSE_INT32),
    INT64                       (StandardTypeMetadata.CLICKHOUSE_INT64),
    INT128                      (StandardTypeMetadata.CLICKHOUSE_INT128),
    INT256                      (StandardTypeMetadata.CLICKHOUSE_INT256),
    IPV4                        (StandardTypeMetadata.IPV4),
    IPV6                        (StandardTypeMetadata.IPV6),
    UINT8                       (StandardTypeMetadata.CLICKHOUSE_UINT8),
    UINT16                      (StandardTypeMetadata.CLICKHOUSE_UINT16),
    UINT32                      (StandardTypeMetadata.CLICKHOUSE_UINT32),
    UINT64                      (StandardTypeMetadata.CLICKHOUSE_UINT64),
    UINT128                     (StandardTypeMetadata.CLICKHOUSE_UINT128),
    UINT256                     (StandardTypeMetadata.CLICKHOUSE_UINT256),
    INTEGER                     (StandardTypeMetadata.INT32),
    JSON                        (StandardTypeMetadata.STRING),
    JSONB                       (StandardTypeMetadata.FixedString),
    LINE                        (StandardTypeMetadata.ILLEGAL),
    LONG                        (StandardTypeMetadata.INT64),
    LONGBLOB                    (StandardTypeMetadata.FixedString),
    LONGTEXT                    (StandardTypeMetadata.STRING),
    LowCardinality              (StandardTypeMetadata.LowCardinality),
    LSEG                        (StandardTypeMetadata.ILLEGAL),
    MACADDR                     (StandardTypeMetadata.ILLEGAL),
    MONEY                       (StandardTypeMetadata.DECIMAL),
    NUMBER                      (StandardTypeMetadata.DECIMAL),
    NCHAR                       (StandardTypeMetadata.STRING),
    NCLOB                       (StandardTypeMetadata.STRING),
    NTEXT                       (StandardTypeMetadata.STRING),
    NVARCHAR                    (StandardTypeMetadata.STRING),
    NVARCHAR2                   (StandardTypeMetadata.STRING),
    PATH                        (StandardTypeMetadata.ILLEGAL),
    MAP                         (StandardTypeMetadata.MAP),
    MEDIUMBLOB                  (StandardTypeMetadata.FixedString),
    MEDIUMINT                   (StandardTypeMetadata.DECIMAL),
    MEDIUMTEXT                  (StandardTypeMetadata.STRING),
    MULTILINESTRING             (StandardTypeMetadata.STRING),
    MULTIPOINT                  (StandardTypeMetadata.ILLEGAL),
    MULTIPOLYGON                (StandardTypeMetadata.MULTIPOLYGON),
    NUMERIC                     (StandardTypeMetadata.DECIMAL),
    POINT                       (StandardTypeMetadata.POINT),
    POLYGON                     (StandardTypeMetadata.POLYGON),
    REAL                        (StandardTypeMetadata.CLICKHOUSE_FLOAT32),
    RAW                         (StandardTypeMetadata.ILLEGAL),
    RING                        (StandardTypeMetadata.RING),
    ROWID                       (StandardTypeMetadata.ILLEGAL),
    SERIAL                      (StandardTypeMetadata.DECIMAL),
    SERIAL2                     (StandardTypeMetadata.DECIMAL),
    SERIAL4                     (StandardTypeMetadata.DECIMAL),
    SERIAL8                     (StandardTypeMetadata.DECIMAL),
    SET                         (StandardTypeMetadata.ILLEGAL),
    SMALLDATETIME               (StandardTypeMetadata.DATETIME),
    SMALLMONEY                  (StandardTypeMetadata.DECIMAL),
    SMALLINT                    (StandardTypeMetadata.DECIMAL),
    SMALLSERIAL                 (StandardTypeMetadata.DECIMAL),
    SQL_VARIANT                 (StandardTypeMetadata.ILLEGAL),
    SYSNAME                     (StandardTypeMetadata.ILLEGAL),
    STRING                      (StandardTypeMetadata.STRING),
    SimpleAggregateFunction     (StandardTypeMetadata.SimpleAggregateFunction),
    TEXT                        (StandardTypeMetadata.STRING),
    TIME                        (StandardTypeMetadata.DATETIME),
    TIMEZ                       (StandardTypeMetadata.DATETIME),
    TIMESTAMP                   (StandardTypeMetadata.DATETIME),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.DATETIME),
    TIMESTAMP_WITH_TIME_ZONE          (StandardTypeMetadata.DATETIME),
    TSQUERY                     (StandardTypeMetadata.ILLEGAL),
    TSVECTOR                    (StandardTypeMetadata.ILLEGAL),
    TUPLE                       (StandardTypeMetadata.TUPLE),
    TXID_SNAPSHOT               (StandardTypeMetadata.ILLEGAL),
    UNIQUEIDENTIFIER            (StandardTypeMetadata.ILLEGAL),
    UUID                        (StandardTypeMetadata.UUID),
    UROWID                      (StandardTypeMetadata.ILLEGAL),
    VARBIT                      (StandardTypeMetadata.FixedString),
    TINYBLOB                    (StandardTypeMetadata.FixedString),
    TINYINT                     (StandardTypeMetadata.DECIMAL),
    TINYTEXT                    (StandardTypeMetadata.STRING),
    VARBINARY                   (StandardTypeMetadata.FixedString),
    VARCHAR                     (StandardTypeMetadata.STRING),
    VARCHAR2                    (StandardTypeMetadata.STRING),
    XML                         (StandardTypeMetadata.ILLEGAL),
    YEAR                        (StandardTypeMetadata.INT);

    private final TypeMetadata standard;
    private int ignoreLength = -1;
    private int ignorePrecision = -1;
    private int ignoreScale = -1;
    private String lengthRefer;
    private String precisionRefer;
    private String scaleRefer;
    private TypeMetadata.Config config;

    ClickhouseTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    ClickhouseTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.lengthRefer = lengthRefer;
        this.precisionRefer = precisionRefer;
        this.scaleRefer = scaleRefer;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    ClickhouseTypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }

    @Override
    public TypeMetadata.Config config() {
        if(null == config){
            config = new TypeMetadata.Config();
            config.setLengthRefer(lengthRefer).setPrecisionRefer(precisionRefer).setScaleRefer(scaleRefer);
            config.setIgnoreLength(ignoreLength).setIgnorePrecision(ignorePrecision).setIgnoreScale(ignoreScale);
        }
        return config;
    }

}
