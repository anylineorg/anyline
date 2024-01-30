package org.anyline.data.jdbc.clickhouse;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;

public enum ClickhouseTypeMetadataAlias implements TypeMetadataAlias {
    //https://clickhouse.com/docs/en/sql-reference/data-types
    BFILE                   (StandardTypeMetadata.BFILE),
    BINARY_DOUBLE           (StandardTypeMetadata.BINARY_DOUBLE),
    BINARY_FLOAT            (StandardTypeMetadata.FLOAT_ORACLE),
    BIGINT                  (StandardTypeMetadata.NUMBER),
    BIGSERIAL               (StandardTypeMetadata.NUMBER),
    BINARY                  (StandardTypeMetadata.BLOB),
    BIT                     (StandardTypeMetadata.NUMBER),
    BLOB                    (StandardTypeMetadata.BLOB),
    BOOL                    (StandardTypeMetadata.NUMBER),
    BOX                     (StandardTypeMetadata.ILLEGAL),
    BYTEA                   (StandardTypeMetadata.BLOB),
    CHAR                    (StandardTypeMetadata.CHAR),
    CIDR                    (StandardTypeMetadata.ILLEGAL),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL),
    CLOB                    (StandardTypeMetadata.CLOB),
    DATE                    (StandardTypeMetadata.DATE),
    DATETIME                (StandardTypeMetadata.TIMESTAMP),
    DATETIME2               (StandardTypeMetadata.TIMESTAMP),
    DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP),
    DECIMAL                 (StandardTypeMetadata.NUMBER),
    DOUBLE                  (StandardTypeMetadata.CLICKHOUSE_FLOAT64),
    ENUM                    (StandardTypeMetadata.ILLEGAL),
    FLOAT                   (StandardTypeMetadata.CLICKHOUSE_FLOAT32),
    FLOAT4                  (StandardTypeMetadata.CLICKHOUSE_FLOAT32),
    FLOAT8                  (StandardTypeMetadata.CLICKHOUSE_FLOAT32),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL),
    IMAGE                   (StandardTypeMetadata.BLOB),
    INET                    (StandardTypeMetadata.ILLEGAL),
    INTERVAL                (StandardTypeMetadata.ILLEGAL),
    INT                     (StandardTypeMetadata.CLICKHOUSE_INT32),
    INT8                    (StandardTypeMetadata.CLICKHOUSE_INT8),
    INT16                   (StandardTypeMetadata.CLICKHOUSE_INT16),
    INT32                   (StandardTypeMetadata.CLICKHOUSE_INT32),
    INT64                   (StandardTypeMetadata.CLICKHOUSE_INT64),
    INT128                  (StandardTypeMetadata.CLICKHOUSE_INT128),
    INT256                  (StandardTypeMetadata.CLICKHOUSE_INT256),
    UINT8                    (StandardTypeMetadata.CLICKHOUSE_UINT8),
    UINT16                   (StandardTypeMetadata.CLICKHOUSE_UINT16),
    UINT32                   (StandardTypeMetadata.CLICKHOUSE_UINT32),
    UINT64                   (StandardTypeMetadata.CLICKHOUSE_UINT64),
    UINT128                  (StandardTypeMetadata.CLICKHOUSE_UINT128),
    UINT256                  (StandardTypeMetadata.CLICKHOUSE_UINT256),
    INTEGER                 (StandardTypeMetadata.NUMBER),
    JSON                    (StandardTypeMetadata.CLOB),
    JSONB                   (StandardTypeMetadata.BLOB),
    LINE                    (StandardTypeMetadata.ILLEGAL),
    LONG                    (StandardTypeMetadata.LONG_TEXT),
    LONGBLOB                (StandardTypeMetadata.BLOB),
    LONGTEXT                (StandardTypeMetadata.CLOB),
    LSEG                    (StandardTypeMetadata.ILLEGAL),
    MACADDR                 (StandardTypeMetadata.ILLEGAL),
    MONEY                   (StandardTypeMetadata.NUMBER),
    NUMBER                  (StandardTypeMetadata.NUMBER),
    NCHAR                   (StandardTypeMetadata.NCHAR),
    NCLOB                   (StandardTypeMetadata.NCLOB),
    NTEXT                   (StandardTypeMetadata.NCLOB),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR2),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR2),
    PATH                    (StandardTypeMetadata.ILLEGAL),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB),
    MEDIUMINT               (StandardTypeMetadata.NUMBER),
    MEDIUMTEXT              (StandardTypeMetadata.CLOB),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL),
    NUMERIC                 (StandardTypeMetadata.NUMBER),
    POINT                   (StandardTypeMetadata.ILLEGAL),
    POLYGON                 (StandardTypeMetadata.ILLEGAL),
    REAL                    (StandardTypeMetadata.FLOAT_ORACLE),
    RAW                     (StandardTypeMetadata.RAW),
    ROWID                   (StandardTypeMetadata.ROWID),
    SERIAL                  (StandardTypeMetadata.NUMBER),
    SERIAL2                 (StandardTypeMetadata.NUMBER),
    SERIAL4                 (StandardTypeMetadata.NUMBER),
    SERIAL8                 (StandardTypeMetadata.NUMBER),
    SET                     (StandardTypeMetadata.ILLEGAL),
    SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP),
    SMALLMONEY              (StandardTypeMetadata.NUMBER),
    SMALLINT                (StandardTypeMetadata.NUMBER),
    SMALLSERIAL             (StandardTypeMetadata.NUMBER),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL),
    TEXT                    (StandardTypeMetadata.CLOB),
    TIME                    (StandardTypeMetadata.TIMESTAMP),
    TIMEZ                   (StandardTypeMetadata.TIMESTAMP),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP_WITH_LOCAL_ZONE),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP_WITH_ZONE),
    TSQUERY                     (StandardTypeMetadata.ILLEGAL),
    TSVECTOR                    (StandardTypeMetadata.ILLEGAL),
    TXID_SNAPSHOT               (StandardTypeMetadata.ILLEGAL),
    UNIQUEIDENTIFIER            (StandardTypeMetadata.ILLEGAL),
    UUID                        (StandardTypeMetadata.ILLEGAL),
    UROWID                      (StandardTypeMetadata.UROWID),
    VARBIT                      (StandardTypeMetadata.BLOB),
    TINYBLOB                    (StandardTypeMetadata.BLOB),
    TINYINT                     (StandardTypeMetadata.NUMBER),
    TINYTEXT                    (StandardTypeMetadata.CLOB),
    VARBINARY                   (StandardTypeMetadata.BLOB),
    VARCHAR                     (StandardTypeMetadata.VARCHAR2),
    VARCHAR2                    (StandardTypeMetadata.VARCHAR2),
    XML                         (StandardTypeMetadata.ILLEGAL),
    YEAR                        (StandardTypeMetadata.DATE);

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
