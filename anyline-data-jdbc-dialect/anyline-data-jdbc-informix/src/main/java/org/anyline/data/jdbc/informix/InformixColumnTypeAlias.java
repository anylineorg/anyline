package org.anyline.data.jdbc.informix;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum InformixColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.ILLEGAL               ), //       ,oracle,
    BINARY_DOUBLE           (StandardColumnType.BINARY_DOUBLE         ), //       ,oracle,
    BINARY_FLOAT            (StandardColumnType.BINARY_FLOAT          ), //       ,oracle,
    BIGINT                  (StandardColumnType.BIGINT                ), //mysql         ,mssql,     ,Informix
    BIGSERIAL               (StandardColumnType.BIGSERIAL             ), //     ,pg,                 ,Informix
    BINARY                  (StandardColumnType.BYTE                  ), //mysql         ,mssql,
    BIT                     (StandardColumnType.BYTE                  ), //mysql,pg,     ,mssql,
    BLOB                    (StandardColumnType.BLOB                  ), //mysql  ,oracle,   ,sqlite ,Informix
    BOOL                    (StandardColumnType.BOOLEAN               ), //     ,pg
    BOOLEAN                 (StandardColumnType.BOOLEAN               ), //                          ,Informix
    BOX                     (StandardColumnType.ILLEGAL               ), //     ,pg
    BYTE                    (StandardColumnType.BYTE                  ), //                          ,Informix
    BYTEA                   (StandardColumnType.BYTE                  ), //     ,pg
    CHAR                    (StandardColumnType.CHAR                  ), //mysql,pg,oracle,mssql,Informix
    CIDR                    (StandardColumnType.ILLEGAL               ), //      pg
    CIRCLE                  (StandardColumnType.ILLEGAL               ), //      pg
    CLOB                    (StandardColumnType.CLOB                  ), //       ,oracle,            ,Informix
    DATE                    (StandardColumnType.DATE                  ), //mysql,pg,oracle,mssql      ,Informix
    DATETIME                (StandardColumnType.DATETIME              ), //mysql         ,mssql       ,Informix
    DATETIME2               (StandardColumnType.DATETIME              ), //mysql         ,mssql
    DATETIMEOFFSET          (StandardColumnType.DATETIME              ), //mysql         ,mssql
    DECIMAL                 (StandardColumnType.DECIMAL               ), //mysql,pg,oracle,mssql       ,Informix
    DOUBLE                  (StandardColumnType.DOUBLE                ), //mysql,                      ,Informix
    ENUM                    (StandardColumnType.ILLEGAL               ), //mysql,
    FLOAT                   (StandardColumnType.FLOAT_INFORMIX        ), //mysql  ,oracle,mssql        ,Informix
    FLOAT4                  (StandardColumnType.FLOAT_INFORMIX        ), //     ,pg
    FLOAT8                  (StandardColumnType.FLOAT_INFORMIX        ), //     ,pg
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), //              ,mssql
    GEOMETRY                (StandardColumnType.ILLEGAL               ), //mysql
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ), //mysql
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), //              ,mssql
    IMAGE                   (StandardColumnType.BYTE                  ), //              ,mssql
    INET                    (StandardColumnType.ILLEGAL               ), //     ,pg
    INTERVAL                (StandardColumnType.INTERVAL              ), //     ,pg                     ,Informix
    INT                     (StandardColumnType.INT                   ), //mysql         ,mssql,        ,Informix
    INT2                    (StandardColumnType.INT                   ), //     ,pg
    INT4                    (StandardColumnType.INT                   ), //
    INT8                    (StandardColumnType.INT8                  ), //                             ,Informix
    INTEGER                 (StandardColumnType.INFORMIX_INTEGER      ), //mysql               ,sqlite ,Informix
    JSON                    (StandardColumnType.TEXT                  ), //mysql,pg
    JSONB                   (StandardColumnType.TEXT                  ), //     ,pg
    LINE                    (StandardColumnType.ILLEGAL               ), //mysql,pg
    LONG                    (StandardColumnType.BIGINT                ), //       ,oracle
    LONGBLOB                (StandardColumnType.BLOB                  ), //mysql
    LONGTEXT                (StandardColumnType.TEXT                  ), //mysql
    LSEG                    (StandardColumnType.ILLEGAL               ), //     ,pg
    MACADDR                 (StandardColumnType.ILLEGAL               ), //     ,pg
    MONEY                   (StandardColumnType.MONEY                 ), //     ,pg,     ,mssql          ,Informix
    NUMBER                  (StandardColumnType.DECIMAL               ), //       ,oracle
    NCHAR                   (StandardColumnType.NCHAR                 ), //       ,oracle,mssql
    NCLOB                   (StandardColumnType.CLOB                  ), //       ,oracle
    NTEXT                   (StandardColumnType.TEXT                  ), //              ,mssql
    NVARCHAR                (StandardColumnType.VARCHAR               ), //              ,mssql
    NVARCHAR2               (StandardColumnType.VARCHAR               ), //       ,oracle
    PATH                    (StandardColumnType.ILLEGAL               ), //     ,pg
    MEDIUMBLOB              (StandardColumnType.ILLEGAL               ), //mysql,
    MEDIUMINT               (StandardColumnType.INT                   ), //mysql,
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), //mysql,
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), //mysql,
    NUMERIC                 (StandardColumnType.DECIMAL               ), //mysql          ,mssql,sqlite ,Informix
    POINT                   (StandardColumnType.ILLEGAL               ), //mysql,pg
    POLYGON                 (StandardColumnType.ILLEGAL               ), //mysql,pg
    REAL                    (StandardColumnType.FLOAT_INFORMIX        ), //mysql         ,mssql,sqlite  ,Informix
    RAW                     (StandardColumnType.ILLEGAL               ), //       ,oracle
    ROWID                   (StandardColumnType.ILLEGAL               ), //       ,oracle
    SERIAL                  (StandardColumnType.SERIAL                ), //     ,pg,                    ,Informix
    SERIAL2                 (StandardColumnType.SERIAL                ), //     ,pg,
    SERIAL4                 (StandardColumnType.SERIAL                ), //     ,pg,
    SERIAL8                 (StandardColumnType.SERIAL8               ), //     ,pg,                    ,Informix
    SET                     (StandardColumnType.ILLEGAL               ), //mysql,
    SMALLDATETIME           (StandardColumnType.DATETIME              ), //              ,mssql
    SMALLFLOAT              (StandardColumnType.FLOAT_INFORMIX        ), //              ,              ,Informix
    SMALLMONEY              (StandardColumnType.DECIMAL               ), //              ,mssql
    SMALLINT                (StandardColumnType.INT                   ), //mysql,                       ,Informix
    SMALLSERIAL             (StandardColumnType.SERIAL                ), //     ,pg,
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), //              ,mssql
    SYSNAME                 (StandardColumnType.ILLEGAL               ), //              ,mssql
    TEXT                    (StandardColumnType.TEXT                  ), //mysql,pg,     ,mssql,sqlite   ,Informix
    TIME                    (StandardColumnType.DATETIME              ), //mysql,pg,     ,mssql
    TIMEZ                   (StandardColumnType.DATETIME              ), //     ,pg
    TIMESTAMP               (StandardColumnType.DATETIME              ), //mysql,pg,oracle,mssql
    TIMESTAMP_LOCAL_ZONE    (StandardColumnType.DATETIME              ), //     ,pg
    TIMESTAMP_ZONE          (StandardColumnType.DATETIME              ), //     ,pg
    TSQUERY                 (StandardColumnType.ILLEGAL               ), //     ,pg
    TSVECTOR                (StandardColumnType.ILLEGAL               ), //     ,pg
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), //     ,pg
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), //       ，     ,mssql
    UUID                    (StandardColumnType.ILLEGAL               ), //     ,pg
    UROWID                  (StandardColumnType.ILLEGAL               ), //       ,oracle
    VARBIT                  (StandardColumnType.BYTEA                 ), //     ,pg
    TINYBLOB                (StandardColumnType.BYTE                  ), //mysql,
    TINYINT                 (StandardColumnType.INT                   ), //mysql         ,mssql
    TINYTEXT                (StandardColumnType.TEXT                  ), //mysql,
    VARBINARY               (StandardColumnType.BYTE                  ), //mysql         ,mssql
    VARCHAR                 (StandardColumnType.VARCHAR               ), //mysql,pg,oracle,mssql        ,Informix
    LVARCHAR                (StandardColumnType.LVARCHAR              ), //                              ,Informix
    VARCHAR2                (StandardColumnType.VARCHAR               ), //       ,oracle,
    XML                     (StandardColumnType.TEXT                  ), //     ,pg，      ,mssql
    YEAR                    (StandardColumnType.DATETIME              ); //mysql,
    private final ColumnType standard;
    private InformixColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }
}
