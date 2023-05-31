package org.anyline.data.jdbc.postgresql;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.entity.metadata.ColumnType;

public enum PostresqlColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.ILLEGAL               ), //       ,oracle,
    BINARY_DOUBLE           (StandardColumnType.BINARY_DOUBLE         ), //       ,oracle,
    BINARY_FLOAT            (StandardColumnType.BINARY_FLOAT          ), //       ,oracle,
    BIGINT                  (StandardColumnType.INT8                  ), //mysql         ,mssql,
    BIGSERIAL               (StandardColumnType.BIGSERIAL             ), //     ,pg,
    BINARY                  (StandardColumnType.BIT                   ), //mysql         ,mssql,
    BIT                     (StandardColumnType.BIT                   ), //mysql,pg,     ,mssql,
    BLOB                    (StandardColumnType.BYTEA                 ), //mysql  ,oracle,   ,sqlite
    BOOL                    (StandardColumnType.BOOL                  ), //     ,pg
    BOX                     (StandardColumnType.ILLEGAL               ), //     ,pg
    BYTEA                   (StandardColumnType.BYTEA                 ), //     ,pg
    CHAR                    (StandardColumnType.CHAR                  ), //mysql,pg,oracle,mssql,
    CIDR                    (StandardColumnType.ILLEGAL               ), //      pg
    CIRCLE                  (StandardColumnType.ILLEGAL               ), //      pg
    CLOB                    (StandardColumnType.TEXT                  ), //       ,oracle
    DATE                    (StandardColumnType.DATE                  ), //mysql,pg,oracle,mssql
    DATETIME                (StandardColumnType.TIMESTAMP             ), //mysql         ,mssql
    DATETIME2               (StandardColumnType.TIMESTAMP             ), //mysql         ,mssql
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP             ), //mysql         ,mssql
    DECIMAL                 (StandardColumnType.DECIMAL               ), //mysql,pg,oracle,mssql
    DOUBLE                  (StandardColumnType.DECIMAL               ), //mysql,
    ENUM                    (StandardColumnType.ILLEGAL               ), //mysql,
    FLOAT                   (StandardColumnType.FLOAT4                ), //mysql  ,oracle,mssql
    FLOAT4                  (StandardColumnType.FLOAT4                ), //     ,pg
    FLOAT8                  (StandardColumnType.FLOAT8                ), //     ,pg
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), //              ,mssql
    GEOMETRY                (StandardColumnType.ILLEGAL               ), //mysql
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ), //mysql
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), //              ,mssql
    IMAGE                   (StandardColumnType.BYTEA                 ), //              ,mssql
    INET                    (StandardColumnType.INET                  ), //     ,pg
    INTERVAL                (StandardColumnType.INTERVAL              ), //     ,pg
    INT                     (StandardColumnType.INT4                  ), //mysql         ,mssql,
    INT2                    (StandardColumnType.INT2                  ), //     ,pg
    INT4                    (StandardColumnType.INT4                  ), //
    INT8                    (StandardColumnType.INT8                  ), //
    INTEGER                 (StandardColumnType.INT4                  ), //mysql                 ,,sqlite
    JSON                    (StandardColumnType.JSON                  ), //mysql,pg
    JSONB                   (StandardColumnType.JSONB                 ), //     ,pg
    LINE                    (StandardColumnType.LINE                  ), //     ,pg
    LINESTRING              (StandardColumnType.LINE                  ), //mysql
    LONG                    (StandardColumnType.INT8                  ), //       ,oracle
    LONGBLOB                (StandardColumnType.BYTEA                 ), //mysql
    LONGTEXT                (StandardColumnType.TEXT                  ), //mysql
    LSEG                    (StandardColumnType.LSEG                  ), //     ,pg
    MACADDR                 (StandardColumnType.MACADDR               ), //     ,pg
    MONEY                   (StandardColumnType.MONEY                 ), //     ,pg,     ,mssql
    NUMBER                  (StandardColumnType.DECIMAL               ), //       ,oracle
    NCHAR                   (StandardColumnType.VARCHAR               ), //       ,oracle,mssql
    NCLOB                   (StandardColumnType.BYTEA                 ), //       ,oracle
    NTEXT                   (StandardColumnType.TEXT                  ), //              ,mssql
    NVARCHAR                (StandardColumnType.VARCHAR               ), //              ,mssql
    NVARCHAR2               (StandardColumnType.VARCHAR               ), //       ,oracle
    PATH                    (StandardColumnType.PATH                  ), //     ,pg
    MEDIUMBLOB              (StandardColumnType.BYTEA                 ), //mysql,
    MEDIUMINT               (StandardColumnType.INT8                  ), //mysql,
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), //mysql,
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), //mysql,
    NUMERIC                 (StandardColumnType.DECIMAL               ), //mysql          ,mssql,sqlite
    POINT                   (StandardColumnType.POINT                 ), //mysql,pg
    POLYGON                 (StandardColumnType.POLYGON               ), //mysql,pg
    REAL                    (StandardColumnType.FLOAT4                ), //mysql         ,mssql,sqlite
    RAW                     (StandardColumnType.ILLEGAL               ), //       ,oracle
    ROWID                   (StandardColumnType.ILLEGAL               ), //       ,oracle
    SERIAL                  (StandardColumnType.SERIAL                ), //     ,pg,
    SERIAL2                 (StandardColumnType.SERIAL2               ), //     ,pg,
    SERIAL4                 (StandardColumnType.SERIAL4               ), //     ,pg,
    SERIAL8                 (StandardColumnType.SERIAL8               ), //     ,pg,
    SET                     (StandardColumnType.ILLEGAL               ), //mysql,
    SMALLDATETIME           (StandardColumnType.TIMESTAMP             ), //              ,mssql
    SMALLMONEY              (StandardColumnType.DECIMAL               ), //              ,mssql
    SMALLINT                (StandardColumnType.INT2                  ), //mysql,
    SMALLSERIAL             (StandardColumnType.SMALLSERIAL           ), //     ,pg,
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), //              ,mssql
    SYSNAME                 (StandardColumnType.ILLEGAL               ), //              ,mssql
    TEXT                    (StandardColumnType.TEXT                  ), //mysql,pg,     ,mssql,sqlite
    TIME                    (StandardColumnType.TIME                  ), //mysql,pg,     ,mssql
    TIMEZ                   (StandardColumnType.TIMEZ                 ), //     ,pg
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ), //mysql,pg,oracle,mssql
    TIMESTAMP_LOCAL_ZONE    (StandardColumnType.TIMESTAMP_LOCAL_ZONE  ), //     ,pg
    TIMESTAMP_ZONE          (StandardColumnType.TIMESTAMP_ZONE        ), //     ,pg
    TSQUERY                 (StandardColumnType.TSQUERY               ), //     ,pg
    TSVECTOR                (StandardColumnType.TSVECTOR              ), //     ,pg
    TXID_SNAPSHOT           (StandardColumnType.TXID_SNAPSHOT         ), //     ,pg
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), //       ，     ,mssql
    UUID                    (StandardColumnType.UUID                  ), //     ,pg
    UROWID                  (StandardColumnType.ILLEGAL               ), //       ,oracle
    VARBIT                  (StandardColumnType.VARBIT                ), //     ,pg
    TINYBLOB                (StandardColumnType.BYTEA                 ), //mysql,
    TINYINT                 (StandardColumnType.INT2                  ), //mysql         ,mssql
    TINYTEXT                (StandardColumnType.TEXT                  ), //mysql,
    VARBINARY               (StandardColumnType.VARBIT                ), //mysql         ,mssql
    VARCHAR                 (StandardColumnType.VARCHAR               ), //mysql,pg,oracle,mssql
    VARCHAR2                (StandardColumnType.VARCHAR               ), //       ,oracle,
    XML                     (StandardColumnType.XML                   ), //     ,pg，      ,mssql
    YEAR                    (StandardColumnType.DATE                  ); //mysql,
    private final ColumnType standard;
    private PostresqlColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }
}
