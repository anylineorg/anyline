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


package org.anyline.data.jdbc.sqlite;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum SQLiteColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.ILLEGAL            ), //      , oracle,
    BINARY_DOUBLE           (StandardColumnType.REAL               ), //      , oracle,
    BINARY_FLOAT            (StandardColumnType.ILLEGAL            ), //      , oracle,
    BIGINT                  (StandardColumnType.INTEGER            ), //mysql,   , mssql,
    BIGSERIAL               (StandardColumnType.INTEGER            ), //    , pg,
    BINARY                  (StandardColumnType.BLOB               ), //mysql,   , mssql,
    BIT                     (StandardColumnType.INTEGER            ), //mysql, pg, , mssql,
    BLOB                    (StandardColumnType.BLOB               ), //mysql , oracle,     , sqlite
    BOOL                    (StandardColumnType.INTEGER            ), //    , pg
    BOX                     (StandardColumnType.ILLEGAL            ), //    , pg
    BYTEA                   (StandardColumnType.BLOB               ), //    , pg
    CHAR                    (StandardColumnType.TEXT               ), //mysql, pg, oracle, mssql,
    CIDR                    (StandardColumnType.ILLEGAL            ), //      pg
    CIRCLE                  (StandardColumnType.TEXT               ), //      pg
    CLOB                    (StandardColumnType.TEXT               ), //      , oracle
    DATE                    (StandardColumnType.TEXT               ), //mysql, pg, oracle, mssql
    DATETIME                (StandardColumnType.TEXT               ), //mysql,   , mssql
    DATETIME2               (StandardColumnType.TEXT               ), //mysql,   , mssql
    DATETIMEOFFSET          (StandardColumnType.INTEGER            ), //mysql,   , mssql
    DECIMAL                 (StandardColumnType.REAL               ), //mysql, pg, oracle, mssql
    DOUBLE                  (StandardColumnType.REAL               ), //mysql,
    ENUM                    (StandardColumnType.ILLEGAL            ), //mysql,
    FLOAT                   (StandardColumnType.REAL               ), //mysql , oracle, mssql
    FLOAT4                  (StandardColumnType.REAL               ), //    , pg
    FLOAT8                  (StandardColumnType.REAL               ), //    , pg
    GEOGRAPHY               (StandardColumnType.ILLEGAL            ), //            , mssql
    GEOMETRY                (StandardColumnType.REAL               ), //mysql
    GEOMETRYCOLLECTION       (StandardColumnType.ILLEGAL            ), //mysql
    HIERARCHYID             (StandardColumnType.ILLEGAL            ), //            , mssql
    IMAGE                   (StandardColumnType.BLOB               ), //            , mssql
    INET                    (StandardColumnType.ILLEGAL            ), //    , pg
    INTERVAL                (StandardColumnType.ILLEGAL            ), //    , pg
    INT                     (StandardColumnType.INTEGER            ), //mysql,   , mssql,
    INT2                    (StandardColumnType.INTEGER            ), //    , pg
    INT4                    (StandardColumnType.INTEGER            ), //    , pg
    INT8                    (StandardColumnType.INTEGER            ), //    , pg
    INTEGER                 (StandardColumnType.INTEGER            ), //mysql                , sqlite
    JSON                    (StandardColumnType.TEXT               ), //mysql, pg
    JSONB                   (StandardColumnType.BLOB               ), //    , pg
    LINE                    (StandardColumnType.ILLEGAL            ), //mysql, pg
    LONG                    (StandardColumnType.INTEGER            ), //      , oracle
    LONGBLOB                (StandardColumnType.BLOB               ), //mysql
    LONGTEXT                (StandardColumnType.TEXT               ), //mysql
    LSEG                    (StandardColumnType.ILLEGAL            ), //    ,pg
    MACADDR                 (StandardColumnType.ILLEGAL            ), //    ,pg
    MONEY                   (StandardColumnType.REAL               ), //    ,pg, ,mssql
    NUMBER                  (StandardColumnType.REAL               ), //      ,oracle
    NCHAR                   (StandardColumnType.TEXT               ), //      ,oracle,mssql
    NCLOB                   (StandardColumnType.BLOB               ), //      ,oracle
    NTEXT                   (StandardColumnType.TEXT               ), //            ,mssql
    NVARCHAR                (StandardColumnType.TEXT               ), //            ,mssql
    NVARCHAR2               (StandardColumnType.TEXT               ), //      ,oracle
    PATH                    (StandardColumnType.ILLEGAL            ), //    ,pg
    MEDIUMBLOB              (StandardColumnType.BLOB               ), //mysql,
    MEDIUMINT               (StandardColumnType.INTEGER            ), //mysql,
    MEDIUMTEXT              (StandardColumnType.TEXT               ), //mysql,
    MULTILINESTRING         (StandardColumnType.ILLEGAL            ), //mysql,
    MULTIPOINT              (StandardColumnType.ILLEGAL            ), //mysql,
    MULTIPOLYGON            (StandardColumnType.ILLEGAL            ), //mysql,
    NUMERIC                 (StandardColumnType.REAL               ), //mysql,   ,mssql,sqlite
    POINT                   (StandardColumnType.ILLEGAL            ), //mysql,pg
    POLYGON                 (StandardColumnType.ILLEGAL            ), //mysql,pg
    REAL                    (StandardColumnType.REAL               ), //mysql,  ,mssql,sqlite
    RAW                     (StandardColumnType.ILLEGAL            ), //      ,oracle
    ROWID                   (StandardColumnType.ILLEGAL            ), //      ,oracle
    SERIAL                  (StandardColumnType.INTEGER            ), //    ,pg,
    SERIAL2                 (StandardColumnType.INTEGER            ), //    ,pg,
    SERIAL4                 (StandardColumnType.INTEGER            ), //    ,pg,
    SERIAL8                 (StandardColumnType.INTEGER            ), //    ,pg,
    SET                     (StandardColumnType.ILLEGAL            ), //mysql,
    SMALLDATETIME           (StandardColumnType.TEXT               ), //            ,mssql
    SMALLMONEY              (StandardColumnType.TEXT               ), //            ,mssql
    SMALLINT                (StandardColumnType.INTEGER            ), //mysql,
    SMALLSERIAL             (StandardColumnType.INTEGER            ), //    ,pg,
    SQL_VARIANT             (StandardColumnType.ILLEGAL            ), //            ,mssql
    SYSNAME                 (StandardColumnType.ILLEGAL            ), //            ,mssql
    TEXT                    (StandardColumnType.TEXT               ), //mysql,pg, ,mssql,sqlite
    TIME                    (StandardColumnType.TEXT               ), //mysql,pg, ,mssql
    TIMEZ                   (StandardColumnType.TEXT               ), //    ,pg
    TIMESTAMP               (StandardColumnType.INTEGER            ), //mysql,pg,oracle,mssql
    TIMESTAMP_LOCAL_ZONE    (StandardColumnType.INTEGER            ), //    ,pg
    TIMESTAMP_ZONE          (StandardColumnType.INTEGER            ), //    ,pg
    TSQUERY                 (StandardColumnType.ILLEGAL            ), //    ,pg
    TSVECTOR                (StandardColumnType.ILLEGAL            ), //    ,pg
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL            ), //    ,pg
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL            ), //       ，    ,mssql
    UUID                    (StandardColumnType.ILLEGAL            ), //    ,pg
    UROWID                  (StandardColumnType.ILLEGAL            ), //      ,oracle
    VARBIT                  (StandardColumnType.BLOB               ), //    ,pg
    TINYBLOB                (StandardColumnType.BLOB               ), //mysql,
    TINYINT                 (StandardColumnType.INTEGER            ), //mysql,  ,mssql
    TINYTEXT                (StandardColumnType.TEXT               ), //mysql,
    VARBINARY               (StandardColumnType.BLOB               ), //mysql,  ,mssql
    VARCHAR                 (StandardColumnType.TEXT               ), //mysql,pg,oracle,mssql
    VARCHAR2                (StandardColumnType.TEXT               ), //      ,oracle,
    XML                     (StandardColumnType.TEXT               ), //    ,pg，     ,mssql
    YEAR                    (StandardColumnType.INTEGER            ); //mysql,
    private final ColumnType standard;
    private SQLiteColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }

}
