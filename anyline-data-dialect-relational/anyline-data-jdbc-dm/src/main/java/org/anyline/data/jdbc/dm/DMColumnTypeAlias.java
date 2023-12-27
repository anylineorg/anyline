/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.data.jdbc.dm;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum DMColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.BFILE                 ), //       , oracle,
    BINARY_DOUBLE           (StandardColumnType.BINARY_DOUBLE         ), //       , oracle,
    BINARY_FLOAT            (StandardColumnType.FLOAT_ORACLE          ), //       , oracle,
    BIGINT                  (StandardColumnType.BIGINT                ), //mysql         , mssql,
    BIGSERIAL               (StandardColumnType.BIGINT                ), //     , pg,
    BINARY                  (StandardColumnType.BLOB                  ), //mysql         , mssql,
    BIT                     (StandardColumnType.NUMBER                ), //mysql, pg, , mssql,
    BLOB                    (StandardColumnType.BLOB                  ), //mysql  , oracle, , sqlite
    BOOL                    (StandardColumnType.NUMBER                ), //     , pg
    BOX                     (StandardColumnType.ILLEGAL               ), //     , pg
    BYTEA                   (StandardColumnType.BLOB                  ), //     , pg
    CHAR                    (StandardColumnType.CHAR                  ), //mysql, pg, oracle, mssql,
    CIDR                    (StandardColumnType.ILLEGAL               ), //      pg
    CIRCLE                  (StandardColumnType.ILLEGAL               ), //      pg
    CLOB                    (StandardColumnType.CLOB                  ), //       , oracle
    DATE                    (StandardColumnType.DATE                  ), //mysql, pg, oracle, mssql
    DATETIME                (StandardColumnType.TIMESTAMP             ), //mysql         , mssql
    DATETIME2               (StandardColumnType.TIMESTAMP             ), //mysql         , mssql
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP             ), //mysql         , mssql
    DECIMAL                 (StandardColumnType.NUMBER                ), //mysql, pg, oracle, mssql
    DOUBLE                  (StandardColumnType.NUMBER                ), //mysql,
    ENUM                    (StandardColumnType.ILLEGAL               ), //mysql,
    FLOAT                   (StandardColumnType.FLOAT_ORACLE          ), //mysql  , oracle, mssql
    FLOAT4                  (StandardColumnType.FLOAT_ORACLE          ), //     , pg
    FLOAT8                  (StandardColumnType.FLOAT_ORACLE          ), //     , pg
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), //              , mssql
    GEOMETRY                (StandardColumnType.ILLEGAL               ), //mysql
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ), //mysql
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), //              , mssql
    IMAGE                   (StandardColumnType.BLOB                  ), //              , mssql
    INET                    (StandardColumnType.ILLEGAL               ), //     , pg
    INTERVAL                (StandardColumnType.ILLEGAL               ), //     , pg
    INT                     (StandardColumnType.INT                   ), //mysql         , mssql,
    INT2                    (StandardColumnType.INT                   ), //     , pg
    INT4                    (StandardColumnType.INT                   ), //     , pg
    INT8                    (StandardColumnType.BIGINT                ), //     , pg
    INTEGER                 (StandardColumnType.INT                   ), //mysql                 , sqlite
    JSON                    (StandardColumnType.CLOB                  ), //mysql, pg
    JSONB                   (StandardColumnType.BLOB                  ), //     , pg
    LINE                    (StandardColumnType.ILLEGAL               ), //mysql, pg
    LONG                    (StandardColumnType.LONG                  ), //       , oracle
    LONGBLOB                (StandardColumnType.BLOB                  ), //mysql
    LONGTEXT                (StandardColumnType.TEXT                  ), //mysql
    LSEG                    (StandardColumnType.ILLEGAL               ), //     , pg
    MACADDR                 (StandardColumnType.ILLEGAL               ), //     , pg
    MONEY                   (StandardColumnType.NUMBER                ), //     , pg, , mssql
    NUMBER                  (StandardColumnType.NUMBER                ), //       , oracle
    NCHAR                   (StandardColumnType.NCHAR                 ), //       , oracle, mssql
    NCLOB                   (StandardColumnType.NCLOB                 ), //       , oracle
    NTEXT                   (StandardColumnType.NCLOB                 ), //              , mssql
    NVARCHAR                (StandardColumnType.NVARCHAR2             ), //              , mssql
    NVARCHAR2               (StandardColumnType.NVARCHAR2             ), //       , oracle
    PATH                    (StandardColumnType.ILLEGAL               ), //     , pg
    MEDIUMBLOB              (StandardColumnType.BLOB                  ), //mysql,
    MEDIUMINT               (StandardColumnType.NUMBER                ), //mysql,
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), //mysql,
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), //mysql,
    NUMERIC                 (StandardColumnType.NUMBER                ), //mysql          , mssql, sqlite
    POINT                   (StandardColumnType.ILLEGAL               ), //mysql, pg
    POLYGON                 (StandardColumnType.ILLEGAL               ), //mysql, pg
    REAL                    (StandardColumnType.FLOAT_ORACLE          ), //mysql         , mssql, sqlite
    RAW                     (StandardColumnType.RAW                   ), //       , oracle
    ROWID                   (StandardColumnType.ROWID                 ), //       , oracle
    SERIAL                  (StandardColumnType.INT                   ), //     , pg,
    SERIAL2                 (StandardColumnType.INT                   ), //     , pg,
    SERIAL4                 (StandardColumnType.INT                   ), //     , pg,
    SERIAL8                 (StandardColumnType.BIGINT                ), //     , pg,
    SET                     (StandardColumnType.ILLEGAL               ), //mysql,
    SMALLDATETIME           (StandardColumnType.TIMESTAMP             ), //              , mssql
    SMALLMONEY              (StandardColumnType.NUMBER                ), //              , mssql
    SMALLINT                (StandardColumnType.NUMBER                ), //mysql,
    SMALLSERIAL             (StandardColumnType.NUMBER                ), //     , pg,
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), //              , mssql
    SYSNAME                 (StandardColumnType.ILLEGAL               ), //              , mssql
    TEXT                    (StandardColumnType.TEXT                  ), //mysql, pg, , mssql, sqlite
    TIME                    (StandardColumnType.TIMESTAMP             ), //mysql, pg, , mssql
    TIMEZ                   (StandardColumnType.TIMESTAMP             ), //     , pg
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ), //mysql, pg, oracle, mssql
    TIMESTAMP_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), //     , pg
    TIMESTAMP_ZONE          (StandardColumnType.TIMESTAMP             ), //     , pg
    TSQUERY                 (StandardColumnType.ILLEGAL               ), //     , pg
    TSVECTOR                (StandardColumnType.ILLEGAL               ), //     , pg
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), //     , pg
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), //       ，     , mssql
    UUID                    (StandardColumnType.ILLEGAL               ), //     , pg
    UROWID                  (StandardColumnType.UROWID                ), //       , oracle
    VARBIT                  (StandardColumnType.BLOB                  ), //     , pg
    TINYBLOB                (StandardColumnType.BLOB                  ), //mysql,
    TINYINT                 (StandardColumnType.NUMBER                ), //mysql         , mssql
    TINYTEXT                (StandardColumnType.TEXT                  ), //mysql,
    VARBINARY               (StandardColumnType.BLOB                  ), //mysql         , mssql
    VARCHAR                 (StandardColumnType.VARCHAR               ), //mysql, pg, oracle, mssql
    VARCHAR2                (StandardColumnType.VARCHAR2               ), //        , oracle,
    XML                     (StandardColumnType.ILLEGAL               ), //     , pg，      , mssql
    YEAR                    (StandardColumnType.DATE                  ); //mysql,
    private final ColumnType standard;
    private DMColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }
}
