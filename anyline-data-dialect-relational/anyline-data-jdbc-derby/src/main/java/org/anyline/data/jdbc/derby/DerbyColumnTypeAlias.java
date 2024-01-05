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


package org.anyline.data.jdbc.derby;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum DerbyColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL               ), //    , , oracle,
        BINARY_DOUBLE           (StandardColumnType.DOUBLE                ), //    , , oracle,
        BINARY_FLOAT            (StandardColumnType.FLOAT           ), //    , , oracle,
        BIGINT                  (StandardColumnType.BIGINT                ), //mysql, , , mssql,
        BIGSERIAL               (StandardColumnType.BIGINT                ), //    , pg,
        BINARY                  (StandardColumnType.BLOB                ), //mysql, , , mssql,
        BIT                     (StandardColumnType.SMALLINT                   ), //mysql, pg, , mssql,
        BLOB                    (StandardColumnType.BLOB                  ), //mysql, , oracle, , sqlite
        BOOL                    (StandardColumnType.BIT                   ), //    , pg
        BOX                     (StandardColumnType.ILLEGAL               ), //    , pg
        BYTEA                   (StandardColumnType.CLOB             ), //    , pg
        CHAR                    (StandardColumnType.CHAR                  ), //mysql, pg, oracle, mssql,
        CIDR                    (StandardColumnType.ILLEGAL               ), //      pg
        CIRCLE                  (StandardColumnType.ILLEGAL               ), //      pg
        CLOB                    (StandardColumnType.CLOB                  ), //    , , oracle
        DATE                    (StandardColumnType.DATE                  ), //mysql, pg, oracle, mssql
        DATETIME                (StandardColumnType.TIMESTAMP              ), //mysql, , , mssql
        DATETIME2               (StandardColumnType.TIMESTAMP             ), //mysql, , , mssql
        DATETIMEOFFSET          (StandardColumnType.TIMESTAMP        ), //mysql, , , mssql
        DECIMAL                 (StandardColumnType.DECIMAL               ), //mysql, pg, oracle, mssql
        DOUBLE                  (StandardColumnType.DOUBLE                ), //mysql,
        ENUM                    (StandardColumnType.ILLEGAL                  ), //mysql,
        FLOAT                   (StandardColumnType.FLOAT           ), //mysql, , oracle, mssql
        FLOAT4                  (StandardColumnType.FLOAT           ), //    , pg
        FLOAT8                  (StandardColumnType.FLOAT           ), //    , pg
        GEOGRAPHY               (StandardColumnType.ILLEGAL               ), //    , , , mssql
        GEOMETRY                (StandardColumnType.ILLEGAL              ), //mysql
        GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL    ), //mysql
        HIERARCHYID             (StandardColumnType.ILLEGAL               ), //    , , , mssql
        IMAGE                   (StandardColumnType.BLOB                  ), //    , , , mssql
        INET                    (StandardColumnType.ILLEGAL               ), //    , pg
        INTERVAL                (StandardColumnType.ILLEGAL               ), //    , pg
        INT                     (StandardColumnType.INT                   ), //mysql, , , mssql,
        INT2                    (StandardColumnType.INT                   ), //    , pg
        INT4                    (StandardColumnType.INT                   ), //    , pg
        INT8                    (StandardColumnType.BIGINT                ), //    , pg
        INTEGER                 (StandardColumnType.INT                   ), //mysql                , sqlite
        JSON                    (StandardColumnType.CLOB                  ), //mysql, pg
        JSONB                   (StandardColumnType.BLOB                  ), //    , pg
        LINE                    (StandardColumnType.ILLEGAL            ), //    , pg
        LINESTRING              (StandardColumnType.ILLEGAL            ), //mysql
        LONG                    (StandardColumnType.BIGINT                ), //    , , oracle
        LONGBLOB                (StandardColumnType.CLOB             ), //mysql
        LONGTEXT                (StandardColumnType.CLOB              ), //mysql
        LSEG                    (StandardColumnType.ILLEGAL               ), //    , pg
        MACADDR                 (StandardColumnType.ILLEGAL               ), //    , pg
        MONEY                   (StandardColumnType.DECIMAL               ), //    , pg, , mssql
        NUMBER                  (StandardColumnType.DECIMAL               ), //    , , oracle
        NCHAR                   (StandardColumnType.VARCHAR               ), //    , , oracle, mssql
        NCLOB                   (StandardColumnType.CLOB                  ), //    , , oracle
        NTEXT                   (StandardColumnType.CLOB                  ), //    , , , mssql
        NVARCHAR                (StandardColumnType.VARCHAR               ), //    , , , mssql
        NVARCHAR2               (StandardColumnType.VARCHAR               ), //    , , oracle
        PATH                    (StandardColumnType.ILLEGAL               ), //    , pg
        MEDIUMBLOB              (StandardColumnType.BLOB            ), //mysql,
        MEDIUMINT               (StandardColumnType.ILLEGAL             ), //mysql,
        MEDIUMTEXT              (StandardColumnType.ILLEGAL            ), //mysql,
        MULTILINE               (StandardColumnType.ILLEGAL       ), //mysql,
        MULTILINESTRING         (StandardColumnType.ILLEGAL       ), //mysql,
        MULTIPOINT              (StandardColumnType.ILLEGAL            ), //mysql,
        MULTIPOLYGON            (StandardColumnType.ILLEGAL          ), //mysql,
        NUMERIC                 (StandardColumnType.DECIMAL               ), //mysql, , , mssql, sqlite
        POINT                   (StandardColumnType.ILLEGAL                 ), //mysql, pg
        GEOGRAPHY_POINT         (StandardColumnType.ILLEGAL                 ), //voltdb
        POLYGON                 (StandardColumnType.ILLEGAL               ), //mysql, pg
        REAL                    (StandardColumnType.REAL                  ), //mysql, , , mssql, sqlite
        RAW                     (StandardColumnType.ILLEGAL               ), //    , , oracle
        ROWID                   (StandardColumnType.ILLEGAL               ), //    , , oracle
        SERIAL                  (StandardColumnType.SMALLINT               ), //    , pg,
        SERIAL2                 (StandardColumnType.SMALLINT               ), //    , pg,
        SERIAL4                 (StandardColumnType.INT                   ), //    , pg,
        SERIAL8                 (StandardColumnType.BIGINT                ), //    , pg,
        SET                     (StandardColumnType.ILLEGAL                   ), //mysql,
        SMALLDATETIME           (StandardColumnType.TIMESTAMP              ), //    , , , mssql
        SMALLMONEY              (StandardColumnType.DECIMAL               ), //    , , , mssql
        SMALLINT                (StandardColumnType.SMALLINT               ), //mysql,
        SMALLSERIAL             (StandardColumnType.SMALLINT               ), //    , pg,
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ), //    , , , mssql
        SYSNAME                 (StandardColumnType.ILLEGAL               ), //    , , , mssql
        TEXT                    (StandardColumnType.CLOB                  ), //mysql, pg, , mssql, sqlite
        TIME                    (StandardColumnType.TIME                  ), //mysql, pg, , mssql
        TIMEZ                   (StandardColumnType.TIME                  ), //    , pg
        TIMESTAMP               (StandardColumnType.TIMESTAMP             ), //mysql, pg, oracle, mssql
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), //    , pg
        TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP             ), //    , pg
        TSQUERY                 (StandardColumnType.ILLEGAL               ), //    , pg
        TSVECTOR                (StandardColumnType.ILLEGAL               ), //    , pg
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), //    , pg
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), //       ，    , mssql
        UUID                    (StandardColumnType.ILLEGAL               ), //    , pg
        UROWID                  (StandardColumnType.ILLEGAL               ), //    , , oracle
        VARBIT                  (StandardColumnType.CLOB             ), //    , pg
        TINYBLOB                (StandardColumnType.BLOB              ), //mysql,
        TINYINT                 (StandardColumnType.SMALLINT               ), //mysql, , , mssql
        TINYTEXT                (StandardColumnType.CLOB              ), //mysql,
        VARBINARY               (StandardColumnType.CLOB             ), //mysql, , , mssql
        VARCHAR                 (StandardColumnType.VARCHAR               ), //mysql, pg, oracle, mssql
        VARCHAR2                (StandardColumnType.VARCHAR               ), //    , , oracle,
        XML                     (StandardColumnType.CLOB                  ), //    , pg，     , mssql
        YEAR                    (StandardColumnType.DATE                  ); //mysql,
        private final TypeMetadata standard;
        DerbyColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
