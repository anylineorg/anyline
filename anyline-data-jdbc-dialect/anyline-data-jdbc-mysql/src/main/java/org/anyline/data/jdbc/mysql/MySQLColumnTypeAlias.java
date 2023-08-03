package org.anyline.data.jdbc.mysql;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum MySQLColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL               ), //     ,  ,oracle,
        BINARY_DOUBLE           (StandardColumnType.DOUBLE                ), //     ,  ,oracle,
        BINARY_FLOAT            (StandardColumnType.FLOAT_MYSQL           ), //     ,  ,oracle,
        BIGINT                  (StandardColumnType.BIGINT                ), //mysql,  ,     ,mssql,
        BIGSERIAL               (StandardColumnType.BIGINT                ), //     ,pg,
        BINARY                  (StandardColumnType.BINARY                ), //mysql,  ,     ,mssql,
        BIT                     (StandardColumnType.BIT                   ), //mysql,pg,     ,mssql,
        BLOB                    (StandardColumnType.BLOB                  ), //mysql,  ,oracle,   ,sqlite
        BOOL                    (StandardColumnType.BIT                   ), //     ,pg
        BOX                     (StandardColumnType.ILLEGAL               ), //     ,pg
        BYTEA                   (StandardColumnType.VARBINARY             ), //     ,pg
        CHAR                    (StandardColumnType.CHAR                  ), //mysql,pg,oracle,mssql,
        CIDR                    (StandardColumnType.ILLEGAL               ), //      pg
        CIRCLE                  (StandardColumnType.ILLEGAL               ), //      pg
        CLOB                    (StandardColumnType.TEXT                  ), //     ,  ,oracle
        DATE                    (StandardColumnType.DATE                  ), //mysql,pg,oracle,mssql
        DATETIME                (StandardColumnType.DATETIME              ), //mysql,  ,     ,mssql
        DATETIME2               (StandardColumnType.DATETIME2             ), //mysql,  ,     ,mssql
        DATETIMEOFFSET          (StandardColumnType.DATETIMEOFFSET        ), //mysql,  ,     ,mssql
        DECIMAL                 (StandardColumnType.DECIMAL               ), //mysql,pg,oracle,mssql
        DOUBLE                  (StandardColumnType.DOUBLE                ), //mysql,
        ENUM                    (StandardColumnType.ENUM                  ), //mysql,
        FLOAT                   (StandardColumnType.FLOAT_MYSQL           ), //mysql,  ,oracle,mssql
        FLOAT4                  (StandardColumnType.FLOAT_MYSQL           ), //     ,pg
        FLOAT8                  (StandardColumnType.FLOAT_MYSQL           ), //     ,pg
        GEOGRAPHY               (StandardColumnType.ILLEGAL               ), //     ,  ,     ,mssql
        GEOMETRY                (StandardColumnType.GEOMETRY              ), //mysql
        GEOMETRYCOLLECTION      (StandardColumnType.GEOMETRYCOLLECTION    ), //mysql
        HIERARCHYID             (StandardColumnType.ILLEGAL               ), //     ,  ,     ,mssql
        IMAGE                   (StandardColumnType.BLOB                  ), //     ,  ,     ,mssql
        INET                    (StandardColumnType.ILLEGAL               ), //     ,pg
        INTERVAL                (StandardColumnType.ILLEGAL               ), //     ,pg
        INT                     (StandardColumnType.INT                   ), //mysql,  ,     ,mssql,
        INT2                    (StandardColumnType.INT                   ), //     ,pg
        INT4                    (StandardColumnType.INT                   ), //     ,pg
        INT8                    (StandardColumnType.BIGINT                ), //     ,pg
        INTEGER                 (StandardColumnType.INT                   ), //mysql                 ,sqlite
        JSON                    (StandardColumnType.JSON                  ), //mysql,pg
        JSONB                   (StandardColumnType.BLOB                  ), //     ,pg
        LINE                    (StandardColumnType.LINESTRING            ), //     ,pg
        LINESTRING              (StandardColumnType.LINESTRING            ), //mysql
        LONG                    (StandardColumnType.BIGINT                ), //     ,  ,oracle
        LONGBLOB                (StandardColumnType.VARBINARY             ), //mysql
        LONGTEXT                (StandardColumnType.LONGTEXT              ), //mysql
        LSEG                    (StandardColumnType.ILLEGAL               ), //     ,pg
        MACADDR                 (StandardColumnType.ILLEGAL               ), //     ,pg
        MONEY                   (StandardColumnType.DECIMAL               ), //     ,pg,     ,mssql
        NUMBER                  (StandardColumnType.NUMERIC               ), //     ,  ,oracle
        NCHAR                   (StandardColumnType.VARCHAR               ), //     ,  ,oracle,mssql
        NCLOB                   (StandardColumnType.TEXT                  ), //     ,  ,oracle
        NTEXT                   (StandardColumnType.TEXT                  ), //     ,  ,     ,mssql
        NVARCHAR                (StandardColumnType.VARCHAR               ), //     ,  ,     ,mssql
        NVARCHAR2               (StandardColumnType.VARCHAR               ), //     ,  ,oracle
        PATH                    (StandardColumnType.ILLEGAL               ), //     ,pg
        MEDIUMBLOB              (StandardColumnType.MEDIUMBLOB            ), //mysql,
        MEDIUMINT               (StandardColumnType.MEDIUMINT             ), //mysql,
        MEDIUMTEXT              (StandardColumnType.MEDIUMTEXT            ), //mysql,
        MULTILINE               (StandardColumnType.MULTILINESTRING       ), //mysql,
        MULTILINESTRING         (StandardColumnType.MULTILINESTRING       ), //mysql,
        MULTIPOINT              (StandardColumnType.MULTIPOINT            ), //mysql,
        MULTIPOLYGON            (StandardColumnType.MULTIPOLYGON          ), //mysql,
        NUMERIC                 (StandardColumnType.NUMERIC               ), //mysql,  ,      ,mssql,sqlite
        POINT                   (StandardColumnType.POINT                 ), //mysql,pg
        POLYGON                 (StandardColumnType.POLYGON               ), //mysql,pg
        REAL                    (StandardColumnType.REAL                  ), //mysql,  ,     ,mssql,sqlite
        RAW                     (StandardColumnType.ILLEGAL               ), //     ,  ,oracle
        ROWID                   (StandardColumnType.ILLEGAL               ), //     ,  ,oracle
        SERIAL                  (StandardColumnType.TINYINT               ), //     ,pg,
        SERIAL2                 (StandardColumnType.TINYINT               ), //     ,pg,
        SERIAL4                 (StandardColumnType.INT                   ), //     ,pg,
        SERIAL8                 (StandardColumnType.BIGINT                ), //     ,pg,
        SET                     (StandardColumnType.SET                   ), //mysql,
        SMALLDATETIME           (StandardColumnType.DATETIME              ), //     ,  ,     ,mssql
        SMALLMONEY              (StandardColumnType.DECIMAL               ), //     ,  ,     ,mssql
        SMALLINT                (StandardColumnType.TINYINT               ), //mysql,
        SMALLSERIAL             (StandardColumnType.TINYINT               ), //     ,pg,
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ), //     ,  ,     ,mssql
        SYSNAME                 (StandardColumnType.ILLEGAL               ), //     ,  ,     ,mssql
        TEXT                    (StandardColumnType.TEXT                  ), //mysql,pg,     ,mssql,sqlite
        TIME                    (StandardColumnType.TIME                  ), //mysql,pg,     ,mssql
        TIMEZ                   (StandardColumnType.TIME                  ), //     ,pg
        TIMESTAMP               (StandardColumnType.TIMESTAMP             ), //mysql,pg,oracle,mssql
        TIMESTAMP_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), //     ,pg
        TIMESTAMP_ZONE          (StandardColumnType.TIMESTAMP             ), //     ,pg
        TSQUERY                 (StandardColumnType.ILLEGAL               ), //     ,pg
        TSVECTOR                (StandardColumnType.ILLEGAL               ), //     ,pg
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), //     ,pg
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), //       ，     ,mssql
        UUID                    (StandardColumnType.ILLEGAL               ), //     ,pg
        UROWID                  (StandardColumnType.ILLEGAL               ), //     ,  ,oracle
        VARBIT                  (StandardColumnType.VARBINARY             ), //     ,pg
        TINYBLOB                (StandardColumnType.TINYBLOB              ), //mysql,
        TINYINT                 (StandardColumnType.TINYINT               ), //mysql,  ,     ,mssql
        TINYTEXT                (StandardColumnType.TINYTEXT              ), //mysql,
        VARBINARY               (StandardColumnType.VARBINARY             ), //mysql,  ,     ,mssql
        VARCHAR                 (StandardColumnType.VARCHAR               ), //mysql,pg,oracle,mssql
        VARCHAR2                (StandardColumnType.VARCHAR               ), //     ,  ,oracle,
        XML                     (StandardColumnType.TEXT                  ), //     ,pg，      ,mssql
        YEAR                    (StandardColumnType.DATE                  ); //mysql,
        private final ColumnType standard;
        MySQLColumnTypeAlias(ColumnType standard){
                this.standard = standard;
        }

        @Override
        public ColumnType standard() {
                return standard;
        }
}
