package org.anyline.data.jdbc.mssql;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum MSSQLColumnTypeAlias implements ColumnTypeAlias {
    BFILE                   (ColumnType.ILLEGAL                       ), //     ,  ,oracle,
    BINARY_DOUBLE           (StandardColumnType.NUMERIC               ), //     ,  ,oracle,
    BINARY_FLOAT            (StandardColumnType.FLOAT_MSSQL              ), //     ,  ,oracle,
    BIGINT                  (StandardColumnType.BIGINT                ), //mysql,  ,      ,mssql,
    BIGSERIAL               (StandardColumnType.BIGINT                ), //     ,pg,
    BINARY                  (StandardColumnType.BINARY                ), //mysql,  ,      ,mssql,
    BIT                     (StandardColumnType.BIT                   ), //mysql,pg,      ,mssql,
    BLOB                    (StandardColumnType.VARBINARY             ), //mysql,  ,oracle,     ,sqlite
    BOOL                    (StandardColumnType.BIT                   ), //     ,pg
    BOX                     (StandardColumnType.ILLEGAL               ), //     ,pg
    BYTEA                   (StandardColumnType.VARBINARY             ), //     ,pg
    CHAR                    (StandardColumnType.CHAR                  ), //mysql,pg,oracle,mssql,
    CIDR                    (StandardColumnType.ILLEGAL               ), //      pg
    CIRCLE                  (StandardColumnType.ILLEGAL               ), //      pg
    CLOB                    (StandardColumnType.TEXT                  ), //     ,  ,oracle
    DATE                    (StandardColumnType.DATE                  ), //mysql,pg,oracle,mssql
    DATETIME                (StandardColumnType.DATETIME              ), //mysql,  ,      ,mssql
    DATETIME2               (StandardColumnType.DATETIME2             ), //mysql,  ,      ,mssql
    DATETIMEOFFSET          (StandardColumnType.DATETIMEOFFSET        ), //mysql,  ,      ,mssql
    DECIMAL                 (StandardColumnType.DECIMAL               ), //mysql,pg,oracle,mssql
    DOUBLE                  (StandardColumnType.DECIMAL               ), //mysql,
    ENUM                    (StandardColumnType.ILLEGAL               ), //mysql,
    FLOAT                   (StandardColumnType.FLOAT_MSSQL                 ), //mysql,  ,oracle,mssql
    FLOAT4                  (StandardColumnType.FLOAT_MSSQL                 ), //     ,pg
    FLOAT8                  (StandardColumnType.FLOAT_MSSQL                 ), //     ,pg
    GEOGRAPHY               (StandardColumnType.GEOGRAPHY             ), //     ,  ,      ,mssql
    GEOMETRY                (StandardColumnType.ILLEGAL               ), //mysql
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ), //mysql
    HIERARCHYID             (StandardColumnType.HIERARCHYID           ), //     ,  ,      ,mssql
    IMAGE                   (StandardColumnType.IMAGE                 ), //     ,  ,      ,mssql
    INET                    (StandardColumnType.ILLEGAL               ), //     ,pg
    INTERVAL                (StandardColumnType.ILLEGAL               ), //     ,pg
    INT                     (StandardColumnType.INT                   ), //mysql,  ,      ,mssql,
    INT2                    (StandardColumnType.INT                   ), //     ,pg
    INT4                    (StandardColumnType.INT                   ), //     ,pg
    INT8                    (StandardColumnType.BIGINT                ), //     ,pg
    INTEGER                 (StandardColumnType.INT                   ), //mysql                 ,sqlite
    JSON                    (StandardColumnType.ILLEGAL               ), //mysql,pg
    JSONB                   (StandardColumnType.ILLEGAL               ), //     ,pg
    LINE                    (StandardColumnType.ILLEGAL               ), //mysql,pg
    LONG                    (StandardColumnType.BIGINT                ), //     ,  ,oracle
    LONGBLOB                (StandardColumnType.VARBINARY             ), //mysql
    LONGTEXT                (StandardColumnType.TEXT                  ), //mysql
    LSEG                    (StandardColumnType.ILLEGAL               ), //     ,pg
    MACADDR                 (StandardColumnType.ILLEGAL               ), //     ,pg
    MONEY                   (StandardColumnType.MONEY                 ), //     ,pg,      ,mssql
    NUMBER                  (StandardColumnType.NUMERIC               ), //     ,  ,oracle
    NCHAR                   (StandardColumnType.NCHAR                 ), //     ,  ,oracle,mssql
    NCLOB                   (StandardColumnType.VARBINARY             ), //     ,  ,oracle
    NTEXT                   (StandardColumnType.NTEXT                 ), //     ,  ,      ,mssql
    NVARCHAR                (StandardColumnType.NVARCHAR              ), //     ,  ,      ,mssql
    NVARCHAR2               (StandardColumnType.NVARCHAR              ), //     ,  ,oracle
    PATH                    (StandardColumnType.ILLEGAL               ), //     ,pg
    MEDIUMBLOB              (StandardColumnType.VARBINARY             ), //mysql,
    MEDIUMINT               (StandardColumnType.INT                   ), //mysql,
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), //mysql,
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), //mysql,
    NUMERIC                 (StandardColumnType.NUMERIC               ), //mysql,  ,       ,mssql,sqlite
    POINT                   (StandardColumnType.ILLEGAL               ), //mysql,pg
    POLYGON                 (StandardColumnType.ILLEGAL               ), //mysql,pg
    REAL                    (StandardColumnType.REAL                  ), //mysql,  ,      ,mssql,sqlite
    RAW                     (StandardColumnType.ILLEGAL               ), //     ,  ,oracle
    ROWID                   (StandardColumnType.ILLEGAL               ), //     ,  ,oracle
    SERIAL                  (StandardColumnType.INT                   ), //     ,pg,
    SERIAL2                 (StandardColumnType.TINYINT               ), //     ,pg,
    SERIAL4                 (StandardColumnType.INT                   ), //     ,pg,
    SERIAL8                 (StandardColumnType.BIGINT                ), //     ,pg,
    SET                     (StandardColumnType.ILLEGAL               ), //mysql,
    SMALLDATETIME           (StandardColumnType.SMALLDATETIME         ), //     ,  ,      ,mssql
    SMALLMONEY              (StandardColumnType.SMALLMONEY            ), //     ,  ,      ,mssql
    SMALLINT                (StandardColumnType.INT                   ), //mysql,
    SMALLSERIAL             (StandardColumnType.INT                   ), //     ,pg,
    SQL_VARIANT             (StandardColumnType.SQL_VARIANT           ), //     ,  ,      ,mssql
    SYSNAME                 (StandardColumnType.SYSNAME               ), //     ,  ,      ,mssql
    TEXT                    (StandardColumnType.TEXT                  ), //mysql,pg,      ,mssql,sqlite
    TIME                    (StandardColumnType.TIME                  ), //mysql,pg,      ,mssql
    TIMEZ                   (StandardColumnType.TIME                  ), //     ,pg
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ), //mysql,pg,oracle,mssql
    TIMESTAMP_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), //     ,pg
    TIMESTAMP_ZONE          (StandardColumnType.TIMESTAMP             ), //     ,pg
    TSQUERY                 (StandardColumnType.ILLEGAL               ), //     ,pg
    TSVECTOR                (StandardColumnType.ILLEGAL               ), //     ,pg
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), //     ,pg
    UNIQUEIDENTIFIER        (StandardColumnType.UNIQUEIDENTIFIER      ), //     ,  ，     ,mssql
    UUID                    (StandardColumnType.ILLEGAL               ), //     ,pg
    UROWID                  (StandardColumnType.ILLEGAL               ), //     ,  ,oracle
    VARBIT                  (StandardColumnType.VARBINARY             ), //     ,pg
    TINYBLOB                (StandardColumnType.VARBINARY             ), //mysql,
    TINYINT                 (StandardColumnType.TINYINT               ), //mysql,  ,      ,mssql
    TINYTEXT                (StandardColumnType.TEXT                  ), //mysql,
    VARBINARY               (StandardColumnType.VARBINARY             ), //mysql,  ,      ,mssql
    VARCHAR                 (StandardColumnType.VARCHAR               ), //mysql,pg,oracle,mssql
    VARCHAR2                (StandardColumnType.VARCHAR               ), //     ,  ,oracle,
    XML                     (StandardColumnType.XML                   ), //     ,pg，      ,mssql
    YEAR                    (StandardColumnType.DATE                  ); //mysql,
    private final ColumnType standard;
    private MSSQLColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }
}
