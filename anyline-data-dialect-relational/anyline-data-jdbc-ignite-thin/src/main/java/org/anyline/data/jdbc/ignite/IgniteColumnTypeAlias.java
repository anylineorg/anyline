/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License,  Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,  software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,  either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.data.jdbc.ignite;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum IgniteColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL               ),  //    ,  , oracle,
        BINARY_DOUBLE           (StandardColumnType.ILLEGAL                ),  //    ,  , oracle,
        BINARY_FLOAT            (StandardColumnType.ILLEGAL           ),  //    ,  , oracle,
        BIGINT                  (StandardColumnType.BIGINT                ),  //mysql,  ,     , mssql,
        BIGSERIAL               (StandardColumnType.BIGINT                ),  //    , pg,
        BINARY                  (StandardColumnType.BINARY                ),  //mysql,  ,     , mssql,
        BIT                     (StandardColumnType.ILLEGAL                   ),  //mysql, pg,     , mssql,
        BLOB                    (StandardColumnType.BINARY                  ),  //mysql,  , oracle,   , sqlite
        BOOL                    (StandardColumnType.BOOLEAN                   ),  //    , pg
        BOX                     (StandardColumnType.ILLEGAL               ),  //    , pg
        BYTEA                   (StandardColumnType.ILLEGAL             ),  //    , pg
        CHAR                    (StandardColumnType.CHAR                  ),  //mysql, pg, oracle, mssql,
        CIDR                    (StandardColumnType.ILLEGAL               ),  //      pg
        CIRCLE                  (StandardColumnType.ILLEGAL               ),  //      pg
        CLOB                    (StandardColumnType.VARCHAR                  ),  //    ,  , oracle
        DATE                    (StandardColumnType.DATE                  ),  //mysql, pg, oracle, mssql
        DATETIME                (StandardColumnType.TIMESTAMP              ),  //mysql,  ,     , mssql
        DATETIME2               (StandardColumnType.TIMESTAMP             ),  //mysql,  ,     , mssql
        DATETIMEOFFSET          (StandardColumnType.TIMESTAMP        ),  //mysql,  ,     , mssql
        DECIMAL                 (StandardColumnType.DECIMAL               ),  //mysql, pg, oracle, mssql
        DOUBLE                  (StandardColumnType.DOUBLE                ),  //mysql,
        ENUM                    (StandardColumnType.ILLEGAL                  ),  //mysql,
        FLOAT                   (StandardColumnType.DOUBLE           ),  //mysql,  , oracle, mssql
        FLOAT4                  (StandardColumnType.DOUBLE           ),  //    , pg
        FLOAT8                  (StandardColumnType.DOUBLE           ),  //    , pg
        GEOGRAPHY               (StandardColumnType.ILLEGAL               ),  //    ,  ,     , mssql
        GEOMETRY                (StandardColumnType.GEOMETRY              ),  //mysql
        GEOMETRYCOLLECTION      (StandardColumnType.GEOMETRYCOLLECTION    ),  //mysql
        HIERARCHYID             (StandardColumnType.ILLEGAL               ),  //    ,  ,     , mssql
        IMAGE                   (StandardColumnType.BINARY                  ),  //    ,  ,     , mssql
        INET                    (StandardColumnType.ILLEGAL               ),  //    , pg
        INTERVAL                (StandardColumnType.ILLEGAL               ),  //    , pg
        INT                     (StandardColumnType.INT                   ),  //mysql,  ,     , mssql,
        INT2                    (StandardColumnType.INT                   ),  //    , pg
        INT4                    (StandardColumnType.INT                   ),  //    , pg
        INT8                    (StandardColumnType.BIGINT                ),  //    , pg
        INTEGER                 (StandardColumnType.INT                   ),  //mysql                , sqlite
        JSON                    (StandardColumnType.VARCHAR                  ),  //mysql, pg
        JSONB                   (StandardColumnType.BINARY                  ),  //    , pg
        LINE                    (StandardColumnType.LINESTRING            ),  //    , pg
        LINESTRING              (StandardColumnType.LINESTRING            ),  //mysql
        LONG                    (StandardColumnType.BIGINT                ),  //    ,  , oracle
        LONGBLOB                (StandardColumnType.BINARY             ),  //mysql
        LONGTEXT                (StandardColumnType.VARCHAR              ),  //mysql
        LSEG                    (StandardColumnType.ILLEGAL               ),  //    , pg
        MACADDR                 (StandardColumnType.ILLEGAL               ),  //    , pg
        MONEY                   (StandardColumnType.DECIMAL               ),  //    , pg,     , mssql
        NUMBER                  (StandardColumnType.DECIMAL               ),  //    ,  , oracle
        NCHAR                   (StandardColumnType.VARCHAR               ),  //    ,  , oracle, mssql
        NCLOB                   (StandardColumnType.VARCHAR                  ),  //    ,  , oracle
        NTEXT                   (StandardColumnType.VARCHAR                  ),  //    ,  ,     , mssql
        NVARCHAR                (StandardColumnType.VARCHAR               ),  //    ,  ,     , mssql
        NVARCHAR2               (StandardColumnType.VARCHAR               ),  //    ,  , oracle
        PATH                    (StandardColumnType.ILLEGAL               ),  //    , pg
        MEDIUMBLOB              (StandardColumnType.BINARY            ),  //mysql,
        MEDIUMINT               (StandardColumnType.SMALLINT             ),  //mysql,
        MEDIUMTEXT              (StandardColumnType.VARCHAR            ),  //mysql,
        MULTILINE               (StandardColumnType.MULTILINESTRING       ),  //mysql,
        MULTILINESTRING         (StandardColumnType.MULTILINESTRING       ),  //mysql,
        MULTIPOINT              (StandardColumnType.MULTIPOINT            ),  //mysql,
        MULTIPOLYGON            (StandardColumnType.MULTIPOLYGON          ),  //mysql,
        NUMERIC                 (StandardColumnType.DECIMAL               ),  //mysql,  ,      , mssql, sqlite
        POINT                   (StandardColumnType.POINT                 ),  //mysql, pg
        GEOGRAPHY_POINT         (StandardColumnType.POINT                 ),  //voltdb
        POLYGON                 (StandardColumnType.POLYGON               ),  //mysql, pg
        REAL                    (StandardColumnType.REAL                  ),  //mysql,  ,     , mssql, sqlite
        RAW                     (StandardColumnType.ILLEGAL               ),  //    ,  , oracle
        ROWID                   (StandardColumnType.ILLEGAL               ),  //    ,  , oracle
        SERIAL                  (StandardColumnType.TINYINT               ),  //    , pg,
        SERIAL2                 (StandardColumnType.TINYINT               ),  //    , pg,
        SERIAL4                 (StandardColumnType.INT                   ),  //    , pg,
        SERIAL8                 (StandardColumnType.BIGINT                ),  //    , pg,
        SET                     (StandardColumnType.ILLEGAL                   ),  //mysql,
        SMALLDATETIME           (StandardColumnType.TIMESTAMP              ),  //    ,  ,     , mssql
        SMALLMONEY              (StandardColumnType.DECIMAL               ),  //    ,  ,     , mssql
        SMALLINT                (StandardColumnType.TINYINT               ),  //mysql,
        SMALLSERIAL             (StandardColumnType.TINYINT               ),  //    , pg,
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ),  //    ,  ,     , mssql
        SYSNAME                 (StandardColumnType.ILLEGAL               ),  //    ,  ,     , mssql
        TEXT                    (StandardColumnType.VARCHAR                  ),  //mysql, pg,     , mssql, sqlite
        TIME                    (StandardColumnType.TIME                  ),  //mysql, pg,     , mssql
        TIMEZ                   (StandardColumnType.TIME                  ),  //    , pg
        TIMESTAMP               (StandardColumnType.TIMESTAMP             ),  //mysql, pg, oracle, mssql
        TIMESTAMP_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ),  //    , pg
        TIMESTAMP_ZONE          (StandardColumnType.TIMESTAMP             ), //    ,pg
        TSQUERY                 (StandardColumnType.ILLEGAL               ), //    ,pg
        TSVECTOR                (StandardColumnType.ILLEGAL               ), //    ,pg
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), //    ,pg
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), //       ，    ,mssql
        UUID                    (StandardColumnType.UUID               ), //    ,pg
        UROWID                  (StandardColumnType.ILLEGAL               ), //    , ,oracle
        VARBIT                  (StandardColumnType.BINARY             ), //    ,pg
        TINYBLOB                (StandardColumnType.BINARY              ), //mysql,
        TINYINT                 (StandardColumnType.TINYINT               ), //mysql, ,    ,mssql
        TINYTEXT                (StandardColumnType.VARCHAR              ), //mysql,
        VARBINARY               (StandardColumnType.BINARY             ), //mysql, ,    ,mssql
        VARCHAR                 (StandardColumnType.VARCHAR               ), //mysql,pg,oracle,mssql
        VARCHAR2                (StandardColumnType.VARCHAR               ), //    , ,oracle,
        XML                     (StandardColumnType.VARCHAR                  ), //    ,pg，     ,mssql
        YEAR                    (StandardColumnType.DATE                  ); //mysql,
        private final ColumnType standard;
        IgniteColumnTypeAlias(ColumnType standard){
                this.standard = standard;
        }

        @Override
        public ColumnType standard() {
                return standard;
        }
}
