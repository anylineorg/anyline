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


package org.anyline.data.jdbc.voltdb;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum VoltDBColumnTypeAlias implements ColumnTypeAlias {
        /*
        * 注意日期时间格式
        * For String variables, the text must be formatted as either YYYY-MM-DD hh.mm.ss.nnnnnn or just the date portion YYYY-MM-DD.
        * */
        BFILE                   (StandardColumnType.ILLEGAL               ), //    ,,oracle,
        BINARY_DOUBLE           (StandardColumnType.DECIMAL                ), //    ,,oracle,
        BINARY_FLOAT            (StandardColumnType.FLOAT           ), //    ,,oracle,
        BIGINT                  (StandardColumnType.BIGINT                ), //mysql,, ,mssql,
        BIGSERIAL               (StandardColumnType.BIGINT                ), //    ,pg,
        BINARY                  (StandardColumnType.VARBINARY                ), //mysql,, ,mssql,
        BIT                     (StandardColumnType.TINYINT                   ), //mysql,pg, ,mssql,
        BLOB                    (StandardColumnType.VARBINARY                  ), //mysql,,oracle, ,sqlite
        BOOL                    (StandardColumnType.TINYINT                   ), //    ,pg
        BOX                     (StandardColumnType.ILLEGAL               ), //    ,pg
        BYTEA                   (StandardColumnType.ILLEGAL             ), //    ,pg
        CHAR                    (StandardColumnType.VARCHAR                  ), //mysql,pg,oracle,mssql,
        CIDR                    (StandardColumnType.ILLEGAL               ), //      pg
        CIRCLE                  (StandardColumnType.ILLEGAL               ), //      pg
        CLOB                    (StandardColumnType.VARCHAR                  ), //    ,,oracle
        DATE                    (StandardColumnType.TIMESTAMP                  ), //mysql,pg,oracle,mssql
        DATETIME                (StandardColumnType.TIMESTAMP              ), //mysql,, ,mssql
        DATETIME2               (StandardColumnType.TIMESTAMP             ), //mysql,, ,mssql
        DATETIMEOFFSET          (StandardColumnType.TIMESTAMP        ), //mysql,, ,mssql
        DECIMAL                 (StandardColumnType.DECIMAL               ), //mysql,pg,oracle,mssql
        DOUBLE                  (StandardColumnType.DECIMAL                ), //mysql,
        ENUM                    (StandardColumnType.ILLEGAL                  ), //mysql,
        FLOAT                   (StandardColumnType.FLOAT           ), //mysql,,oracle,mssql
        FLOAT4                  (StandardColumnType.FLOAT           ), //    ,pg
        FLOAT8                  (StandardColumnType.FLOAT           ), //    ,pg
        GEOGRAPHY               (StandardColumnType.GEOGRAPHY               ), //    ,, ,mssql
        GEOMETRY                (StandardColumnType.GEOGRAPHY              ), //mysql
        GEOMETRYCOLLECTION      (StandardColumnType.GEOGRAPHY    ), //mysql
        HIERARCHYID             (StandardColumnType.ILLEGAL               ), //    ,, ,mssql
        IMAGE                   (StandardColumnType.VARBINARY                  ), //    ,, ,mssql
        INET                    (StandardColumnType.ILLEGAL               ), //    ,pg
        INTERVAL                (StandardColumnType.ILLEGAL               ), //    ,pg
        INT                     (StandardColumnType.INTEGER                   ), //mysql,, ,mssql,
        INT2                    (StandardColumnType.INTEGER                   ), //    ,pg
        INT4                    (StandardColumnType.INTEGER                   ), //    ,pg
        INT8                    (StandardColumnType.BIGINT                ), //    ,pg
        INTEGER                 (StandardColumnType.INTEGER                   ), //mysql                ,sqlite
        JSON                    (StandardColumnType.VARCHAR                  ), //mysql,pg
        JSONB                   (StandardColumnType.VARBINARY                  ), //    ,pg
        LINE                    (StandardColumnType.GEOGRAPHY            ), //    ,pg
        LINESTRING              (StandardColumnType.GEOGRAPHY            ), //mysql
        LONG                    (StandardColumnType.BIGINT                ), //    ,,oracle
        LONGBLOB                (StandardColumnType.VARBINARY             ), //mysql
        LONGTEXT                (StandardColumnType.VARCHAR              ), //mysql
        LSEG                    (StandardColumnType.ILLEGAL               ), //    ,pg
        MACADDR                 (StandardColumnType.ILLEGAL               ), //    ,pg
        MONEY                   (StandardColumnType.DECIMAL               ), //    ,pg, ,mssql
        NUMBER                  (StandardColumnType.DECIMAL               ), //    ,,oracle
        NCHAR                   (StandardColumnType.VARCHAR               ), //    ,,oracle,mssql
        NCLOB                   (StandardColumnType.VARCHAR                  ), //    ,,oracle
        NTEXT                   (StandardColumnType.VARCHAR                  ), //    ,, ,mssql
        NVARCHAR                (StandardColumnType.VARCHAR               ), //    ,, ,mssql
        NVARCHAR2               (StandardColumnType.VARCHAR               ), //    ,,oracle
        PATH                    (StandardColumnType.ILLEGAL               ), //    ,pg
        MEDIUMBLOB              (StandardColumnType.GEOGRAPHY            ), //mysql,
        MEDIUMINT               (StandardColumnType.GEOGRAPHY             ), //mysql,
        MEDIUMTEXT              (StandardColumnType.GEOGRAPHY            ), //mysql,
        MULTILINE               (StandardColumnType.GEOGRAPHY       ), //mysql,
        MULTILINESTRING         (StandardColumnType.GEOGRAPHY       ), //mysql,
        MULTIPOINT              (StandardColumnType.GEOGRAPHY            ), //mysql,
        MULTIPOLYGON            (StandardColumnType.GEOGRAPHY          ), //mysql,
        NUMERIC                 (StandardColumnType.DECIMAL               ), //mysql,, ,mssql,sqlite
        POINT                   (StandardColumnType.GEOGRAPHY_POINT                 ), //mysql,pg
        GEOGRAPHY_POINT         (StandardColumnType.GEOGRAPHY_POINT                 ), //voltdb
        POLYGON                 (StandardColumnType.POLYGON               ), //mysql,pg
        REAL                    (StandardColumnType.FLOAT                  ), //mysql,, ,mssql,sqlite
        RAW                     (StandardColumnType.ILLEGAL               ), //    ,,oracle
        ROWID                   (StandardColumnType.ILLEGAL               ), //    ,,oracle
        SERIAL                  (StandardColumnType.TINYINT               ), //    ,pg,
        SERIAL2                 (StandardColumnType.TINYINT               ), //    ,pg,
        SERIAL4                 (StandardColumnType.INTEGER                   ), //    ,pg,
        SERIAL8                 (StandardColumnType.BIGINT                ), //    ,pg,
        SET                     (StandardColumnType.ILLEGAL                   ), //mysql,
        SMALLDATETIME           (StandardColumnType.TIMESTAMP              ), //    ,, ,mssql
        SMALLMONEY              (StandardColumnType.DECIMAL               ), //    ,, ,mssql
        SMALLINT                (StandardColumnType.TINYINT               ), //mysql,
        SMALLSERIAL             (StandardColumnType.TINYINT               ), //    ,pg,
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ), //    ,, ,mssql
        SYSNAME                 (StandardColumnType.ILLEGAL               ), //    ,, ,mssql
        TEXT                    (StandardColumnType.VARCHAR                  ), //mysql,pg, ,mssql,sqlite
        TIME                    (StandardColumnType.TIMESTAMP                  ), //mysql,pg, ,mssql
        TIMEZ                   (StandardColumnType.TIMESTAMP                  ), //    ,pg
        TIMESTAMP               (StandardColumnType.TIMESTAMP             ), //mysql,pg,oracle,mssql
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), //    ,pg
        TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP             ), //    ,pg
        TSQUERY                 (StandardColumnType.ILLEGAL               ), //    ,pg
        TSVECTOR                (StandardColumnType.ILLEGAL               ), //    ,pg
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), //    ,pg
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), //       ，    ,mssql
        UUID                    (StandardColumnType.ILLEGAL               ), //    ,pg
        UROWID                  (StandardColumnType.ILLEGAL               ), //    ,,oracle
        VARBIT                  (StandardColumnType.VARBINARY             ), //    ,pg
        TINYBLOB                (StandardColumnType.VARBINARY              ), //mysql,
        TINYINT                 (StandardColumnType.TINYINT               ), //mysql,, ,mssql
        TINYTEXT                (StandardColumnType.VARCHAR              ), //mysql,
        VARBINARY               (StandardColumnType.VARBINARY             ), //mysql,, ,mssql
        VARCHAR                 (StandardColumnType.VARCHAR               ), //mysql,pg,oracle,mssql
        VARCHAR2                (StandardColumnType.VARCHAR               ), //    ,,oracle,
        XML                     (StandardColumnType.VARCHAR                  ), //    ,pg，     ,mssql
        YEAR                    (StandardColumnType.TIMESTAMP                  ); //mysql,
        private final ColumnType standard;
        VoltDBColumnTypeAlias(ColumnType standard){
                this.standard = standard;
        }

        @Override
        public ColumnType standard() {
                return standard;
        }
}
