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


package org.anyline.data.elasticsearch.adapter;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum ElasticSearchColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.BINARY                 ), //       ,oracle,
    BINARY_DOUBLE           (StandardColumnType.BINARY         ), //       ,oracle,
    BINARY_FLOAT            (StandardColumnType.FLOAT          ), //       ,oracle,
    BIGINT                  (StandardColumnType.LONG                ), //mysql         ,mssql,
    BIGSERIAL               (StandardColumnType.LONG                ), //     ,pg,
    BINARY                  (StandardColumnType.BINARY                  ), //mysql         ,mssql,
    BIT                     (StandardColumnType.BYTE                ), //mysql,pg,     ,mssql,
    BLOB                    (StandardColumnType.BINARY                  ), //mysql  ,oracle,   ,sqlite
    BOOL                    (StandardColumnType.BOOLEAN                ), //     ,pg
    BOX                     (StandardColumnType.ILLEGAL               ), //     ,pg
    BYTEA                   (StandardColumnType.BINARY                  ), //     ,pg
    CHAR                    (StandardColumnType.TEXT                  ), //mysql,pg,oracle,mssql,
    CIDR                    (StandardColumnType.ILLEGAL               ), //      pg
    CIRCLE                  (StandardColumnType.ILLEGAL               ), //      pg
    CLOB                    (StandardColumnType.TEXT                  ), //       ,oracle
    DATE                    (StandardColumnType.DATE                  ), //mysql,pg,oracle,mssql
    DATETIME                (StandardColumnType.DATE             ), //mysql         ,mssql
    DATETIME2               (StandardColumnType.DATE             ), //mysql         ,mssql
    DATETIMEOFFSET          (StandardColumnType.DATE             ), //mysql         ,mssql
    DECIMAL                 (StandardColumnType.DOUBLE                ), //mysql,pg,oracle,mssql
    DOUBLE                  (StandardColumnType.DOUBLE                ), //mysql,
    ENUM                    (StandardColumnType.ILLEGAL               ), //mysql,
    FLOAT                   (StandardColumnType.FLOAT          ), //mysql  ,oracle,mssql
    FLOAT4                  (StandardColumnType.FLOAT          ), //     ,pg
    FLOAT8                  (StandardColumnType.FLOAT          ), //     ,pg
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), //              ,mssql
    GEOMETRY                (StandardColumnType.ILLEGAL               ), //mysql
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ), //mysql
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), //              ,mssql
    IMAGE                   (StandardColumnType.BINARY                  ), //              ,mssql
    INET                    (StandardColumnType.ILLEGAL               ), //     ,pg
    INTERVAL                (StandardColumnType.ILLEGAL               ), //     ,pg
    INT                     (StandardColumnType.INTEGER                   ), //mysql         ,mssql,
    INT2                    (StandardColumnType.INTEGER                   ), //     ,pg
    INT4                    (StandardColumnType.INTEGER                   ), //     ,pg
    INT8                    (StandardColumnType.LONG                ), //     ,pg
    INTEGER                 (StandardColumnType.INTEGER                   ), //mysql                 ,sqlite
    JSON                    (StandardColumnType.OBJECT                  ), //mysql,pg
    JSONB                   (StandardColumnType.BINARY                  ), //     ,pg
    LINE                    (StandardColumnType.ILLEGAL               ), //mysql,pg
    LONG                    (StandardColumnType.LONG                  ), //       ,oracle
    LONGBLOB                (StandardColumnType.BINARY                  ), //mysql
    LONGTEXT                (StandardColumnType.TEXT                  ), //mysql
    LSEG                    (StandardColumnType.ILLEGAL               ), //     ,pg
    MACADDR                 (StandardColumnType.ILLEGAL               ), //     ,pg
    MONEY                   (StandardColumnType.DOUBLE                ), //     ,pg,     ,mssql
    NUMBER                  (StandardColumnType.DOUBLE                ), //       ,oracle
    NCHAR                   (StandardColumnType.TEXT                 ), //       ,oracle,mssql
    NCLOB                   (StandardColumnType.TEXT                 ), //       ,oracle
    NTEXT                   (StandardColumnType.TEXT                 ), //              ,mssql
    NVARCHAR                (StandardColumnType.TEXT             ), //              ,mssql
    NVARCHAR2               (StandardColumnType.TEXT             ), //       ,oracle
    PATH                    (StandardColumnType.ILLEGAL               ), //     ,pg
    MEDIUMBLOB              (StandardColumnType.BINARY                  ), //mysql,
    MEDIUMINT               (StandardColumnType.DOUBLE                ), //mysql,
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), //mysql,
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), //mysql,
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), //mysql,
    NUMERIC                 (StandardColumnType.DOUBLE                ), //mysql          ,mssql,sqlite
    POINT                   (StandardColumnType.ILLEGAL               ), //mysql,pg
    POLYGON                 (StandardColumnType.ILLEGAL               ), //mysql,pg
    REAL                    (StandardColumnType.FLOAT          ), //mysql         ,mssql,sqlite
    RAW                     (StandardColumnType.ILLEGAL                   ), //       ,oracle
    ROWID                   (StandardColumnType.ILLEGAL                 ), //       ,oracle
    SERIAL                  (StandardColumnType.INTEGER), //     ,pg,
    SERIAL2                 (StandardColumnType.INTEGER                   ), //     ,pg,
    SERIAL4                 (StandardColumnType.INTEGER                   ), //     ,pg,
    SERIAL8                 (StandardColumnType.LONG                ), //     ,pg,
    SET                     (StandardColumnType.DATE               ), //mysql,
    SMALLDATETIME           (StandardColumnType.DATE             ), //              ,mssql
    SMALLMONEY              (StandardColumnType.DOUBLE                ), //              ,mssql
    SMALLINT                (StandardColumnType.DOUBLE                ), //mysql,
    SMALLSERIAL             (StandardColumnType.DOUBLE                ), //     ,pg,
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), //              ,mssql
    SYSNAME                 (StandardColumnType.ILLEGAL               ), //              ,mssql
    TEXT                    (StandardColumnType.TEXT                  ), //mysql,pg,     ,mssql,sqlite
    TIME                    (StandardColumnType.DATE             ), //mysql,pg,     ,mssql
    TIMEZ                   (StandardColumnType.DATE             ), //     ,pg
    TIMESTAMP               (StandardColumnType.DATE             ), //mysql,pg,oracle,mssql
    TIMESTAMP_LOCAL_ZONE    (StandardColumnType.DATE             ), //     ,pg
    TIMESTAMP_ZONE          (StandardColumnType.DATE             ), //     ,pg
    TSQUERY                 (StandardColumnType.ILLEGAL               ), //     ,pg
    TSVECTOR                (StandardColumnType.ILLEGAL               ), //     ,pg
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), //     ,pg
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), //       ，     ,mssql
    UUID                    (StandardColumnType.ILLEGAL               ), //     ,pg
    UROWID                  (StandardColumnType.ILLEGAL                ), //       ,oracle
    VARBIT                  (StandardColumnType.BINARY                  ), //     ,pg
    TINYBLOB                (StandardColumnType.BINARY                  ), //mysql,
    TINYINT                 (StandardColumnType.DOUBLE                ), //mysql         ,mssql
    TINYTEXT                (StandardColumnType.TEXT                  ), //mysql,
    VARBINARY               (StandardColumnType.BINARY                  ), //mysql         ,mssql
    VARCHAR                 (StandardColumnType.TEXT               ), //mysql,pg,oracle,mssql
    VARCHAR2                (StandardColumnType.TEXT               ), //        ,oracle,
    XML                     (StandardColumnType.ILLEGAL               ), //     ,pg，      ,mssql
    YEAR                    (StandardColumnType.DATE                  ), //mysql,
    OBJECT                  (StandardColumnType.OBJECT                  ), //mysql,
    KEYWORD                 (StandardColumnType.KEYWORD                  ); //mysql,
    private final ColumnType standard;
    private ElasticSearchColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }
}
