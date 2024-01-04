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


package org.anyline.data.jdbc.tdengine;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum TDengineColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.ILLEGAL             ), //      , oracle,                ,
    BINARY_DOUBLE           (StandardColumnType.ILLEGAL             ), //      , oracle,
    BINARY_FLOAT            (StandardColumnType.ILLEGAL             ), //      , oracle,
    BIGINT                  (StandardColumnType.BIGINT              ), //mysql,   , mssql,     ,td
    BIGSERIAL               (StandardColumnType.BIGINT              ), //    ,pg,
    BINARY                  (StandardColumnType.BINARY              ), //mysql,  ,mssql,     ,td
    BIT                     (StandardColumnType.BOOL                ), //mysql,pg, ,mssql,
    BLOB                    (StandardColumnType.ILLEGAL             ), //mysql ,oracle,    ,sqlite
    BOOL                    (StandardColumnType.BOOL                ), //    ,pg                        ,td
    BOX                     (StandardColumnType.ILLEGAL             ), //    ,pg
    BYTEA                   (StandardColumnType.ILLEGAL             ), //    ,pg
    CHAR                    (StandardColumnType.NCHAR               ), //mysql,pg,oracle,mssql,
    CIDR                    (StandardColumnType.ILLEGAL             ), //      pg
    CIRCLE                  (StandardColumnType.ILLEGAL             ), //      pg
    CLOB                    (StandardColumnType.ILLEGAL             ), //      ,oracle
    DATE                    (StandardColumnType.TIMESTAMP           ), //mysql,pg,oracle,mssql
    DATETIME                (StandardColumnType.TIMESTAMP           ), //mysql,  ,mssql
    DATETIME2               (StandardColumnType.TIMESTAMP           ), //mysql,  ,mssql
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP           ), //mysql,  ,mssql
    DECIMAL                 (StandardColumnType.DOUBLE              ), //mysql,pg,oracle,mssql
    DOUBLE                  (StandardColumnType.DOUBLE              ), //mysql,                   ,td
    ENUM                    (StandardColumnType.ILLEGAL             ), //mysql,
    FLOAT                   (StandardColumnType.FLOAT_MySQL         ), //mysql  ,oracle,mssql         ,td
    FLOAT4                  (StandardColumnType.FLOAT_MySQL         ), //    ,pg
    FLOAT8                  (StandardColumnType.DOUBLE              ), //    ,pg
    GEOGRAPHY               (StandardColumnType.ILLEGAL             ), //            , mssql
    GEOMETRY                (StandardColumnType.ILLEGAL             ), //mysql
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL             ), //mysql
    HIERARCHYID             (StandardColumnType.ILLEGAL             ), //            ,mssql
    IMAGE                   (StandardColumnType.ILLEGAL             ), //            ,mssql
    INET                    (StandardColumnType.ILLEGAL             ), //    ,pg
    INTERVAL                (StandardColumnType.ILLEGAL             ), //    ,pg
    INT                     (StandardColumnType.INT                 ), //mysql,  ,mssql,   ,td
    INT2                    (StandardColumnType.INT                 ), //    ,pg
    INT4                    (StandardColumnType.INT                 ), //    ,pg
    INT8                    (StandardColumnType.INT                 ), //    ,pg
    INTEGER                 (StandardColumnType.INT                 ), //mysql                ,sqlite
    JSON                    (StandardColumnType.JSON                ), //mysql,pg                    ,td,
    JSONB                   (StandardColumnType.BLOB                ), //    ,pg
    LINE                    (StandardColumnType.ILLEGAL             ), //mysql,pg
    LONG                    (StandardColumnType.INT                 ), //      ,oracle
    LONGBLOB                (StandardColumnType.ILLEGAL             ), //mysql
    LONGTEXT                (StandardColumnType.NCHAR               ), //mysql
    LSEG                    (StandardColumnType.ILLEGAL             ), //    ,pg
    MACADDR                 (StandardColumnType.ILLEGAL             ), //    ,pg
    MONEY                   (StandardColumnType.DOUBLE              ), //    ,pg, ,mssql
    NUMBER                  (StandardColumnType.DOUBLE              ), //      ,oracle
    NCHAR                   (StandardColumnType.NCHAR               ), //      ,oracle,mssql        ,td
    NCLOB                   (StandardColumnType.NCHAR               ), //      ,oracle
    NTEXT                   (StandardColumnType.NCHAR               ), //            ,mssql
    NVARCHAR                (StandardColumnType.NCHAR               ), //            ,mssql
    NVARCHAR2               (StandardColumnType.NCHAR               ), //      ,oracle
    PATH                    (StandardColumnType.ILLEGAL             ), //    ,pg
    MEDIUMBLOB              (StandardColumnType.ILLEGAL             ), //mysql,
    MEDIUMINT               (StandardColumnType.ILLEGAL             ), //mysql,
    MEDIUMTEXT              (StandardColumnType.NCHAR               ), //mysql,
    MULTILINESTRING         (StandardColumnType.ILLEGAL             ), //mysql,
    MULTIPOINT              (StandardColumnType.ILLEGAL             ), //mysql,
    MULTIPOLYGON            (StandardColumnType.ILLEGAL             ), //mysql,
    NUMERIC                 (StandardColumnType.DOUBLE              ), //mysql,   ,mssql,sqlite
    POINT                   (StandardColumnType.ILLEGAL             ), //mysql,pg
    POLYGON                 (StandardColumnType.ILLEGAL             ), //mysql,pg
    REAL                    (StandardColumnType.DOUBLE              ), //mysql,  ,mssql,sqlite
    RAW                     (StandardColumnType.ILLEGAL             ), //      ,oracle
    ROWID                   (StandardColumnType.ILLEGAL             ), //      ,oracle
    SERIAL                  (StandardColumnType.INT                 ), //    ,pg,
    SERIAL2                 (StandardColumnType.INT                 ), //    ,pg,
    SERIAL4                 (StandardColumnType.INT                 ), //    ,pg,
    SERIAL8                 (StandardColumnType.INT                 ), //    ,pg,
    SET                     (StandardColumnType.ILLEGAL             ), //mysql,
    SMALLDATETIME           (StandardColumnType.TIMESTAMP           ), //            ,mssql
    SMALLMONEY              (StandardColumnType.DECIMAL             ), //            ,mssql
    SMALLINT                (StandardColumnType.INT                 ), //mysql,                ,td
    SMALLSERIAL             (StandardColumnType.INT                 ), //    ,pg,
    SQL_VARIANT             (StandardColumnType.ILLEGAL             ), //            ,mssql
    SYSNAME                 (StandardColumnType.ILLEGAL             ), //            ,mssql
    TEXT                    (StandardColumnType.NCHAR               ), //mysql,pg, ,mssql,sqlite
    TIME                    (StandardColumnType.TIMESTAMP           ), //mysql,pg, ,mssql
    TIMEZ                   (StandardColumnType.TIMESTAMP           ), //    ,pg
    TIMESTAMP               (StandardColumnType.TIMESTAMP           ), //mysql,pg,oracle,mssql      ,td
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP           ), //    ,pg
    TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP           ), //    ,pg
    TSQUERY                 (StandardColumnType.ILLEGAL             ), //    ,pg
    TSVECTOR                (StandardColumnType.ILLEGAL             ), //    ,pg
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL             ), //    ,pg
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL             ), //       ，    ,mssql
    UUID                    (StandardColumnType.ILLEGAL             ), //    ,pg
    UROWID                  (StandardColumnType.ILLEGAL             ), //      ,oracle
    VARBIT                  (StandardColumnType.ILLEGAL             ), //    ,pg
    TINYBLOB                (StandardColumnType.ILLEGAL             ), //mysql,
    TINYINT                 (StandardColumnType.INT                 ), //mysql,  ,mssql         ,td
    TINYTEXT                (StandardColumnType.NCHAR               ), //mysql,
    VARBINARY               (StandardColumnType.BLOB                ), //mysql,  ,mssql
    VARCHAR                 (StandardColumnType.NCHAR               ), //mysql,pg,oracle,mssql
    VARCHAR2                (StandardColumnType.NCHAR               ), //      ,oracle,
    XML                     (StandardColumnType.NCHAR               ), //    ,pg，     ,mssql
    YEAR                    (StandardColumnType.INT                 ); //mysql,
    private final ColumnType standard;
    private TDengineColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }

}
