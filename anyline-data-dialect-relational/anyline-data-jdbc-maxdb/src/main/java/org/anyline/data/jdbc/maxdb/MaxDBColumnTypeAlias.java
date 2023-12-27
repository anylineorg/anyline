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


package org.anyline.data.jdbc.maxdb;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum MaxDBColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL               ), //    ,  , oracle,
        BINARY_DOUBLE           (StandardColumnType.DOUBLE                ),  //    ,  , oracle, 
        BINARY_FLOAT            (StandardColumnType.FLOAT                ),  //    ,  , oracle, 
        BIGINT                  (StandardColumnType.FLOAT                ),  //mysql,  ,     , mssql, 
        BIGSERIAL               (StandardColumnType.FLOAT                ),  //    , pg, 
        BINARY                  (StandardColumnType.BLOB                ),  //mysql,  ,     , mssql, 
        BIT                     (StandardColumnType.BOOLEAN                   ),  //mysql, pg,     , mssql, 
        BLOB                    (StandardColumnType.BLOB                  ),  //mysql,  , oracle,   , sqlite
        BOOL                    (StandardColumnType.BOOLEAN                   ),  //    , pg
        BOX                     (StandardColumnType.ILLEGAL               ),  //    , pg
        BYTEA                   (StandardColumnType.BLOB             ),  //    , pg
        CHAR                    (StandardColumnType.CHAR                  ),  //mysql, pg, oracle, mssql, 
        CIDR                    (StandardColumnType.ILLEGAL               ),  //      pg
        CIRCLE                  (StandardColumnType.ILLEGAL               ),  //      pg
        CLOB                    (StandardColumnType.CLOB                  ),  //    ,  , oracle
        DATE                    (StandardColumnType.DATE                  ),  //mysql, pg, oracle, mssql
        DATETIME                (StandardColumnType.TIMESTAMP              ),  //mysql,  ,     , mssql
        DATETIME2               (StandardColumnType.TIMESTAMP             ),  //mysql,  ,     , mssql
        DATETIMEOFFSET          (StandardColumnType.TIMESTAMP        ),  //mysql,  ,     , mssql
        DECIMAL                 (StandardColumnType.DECIMAL               ),  //mysql, pg, oracle, mssql
        DOUBLE                  (StandardColumnType.FLOAT                ),  //mysql, 
        ENUM                    (StandardColumnType.ILLEGAL                  ),  //mysql, 
        FLOAT                   (StandardColumnType.FLOAT           ),  //mysql,  , oracle, mssql
        FLOAT4                  (StandardColumnType.FLOAT           ),  //    , pg
        FLOAT8                  (StandardColumnType.FLOAT           ),  //    , pg
        GEOGRAPHY               (StandardColumnType.ILLEGAL               ),  //    ,  ,     , mssql
        GEOMETRY                (StandardColumnType.ILLEGAL              ),  //mysql
        GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL    ),  //mysql
        HIERARCHYID             (StandardColumnType.ILLEGAL               ),  //    ,  ,     , mssql
        IMAGE                   (StandardColumnType.BLOB                  ),  //    ,  ,     , mssql
        INET                    (StandardColumnType.ILLEGAL               ),  //    , pg
        INTERVAL                (StandardColumnType.ILLEGAL               ),  //    , pg
        INT                     (StandardColumnType.INT                   ),  //mysql,  ,     , mssql, 
        INT2                    (StandardColumnType.INT                   ),  //    , pg
        INT4                    (StandardColumnType.INT                   ),  //    , pg
        INT8                    (StandardColumnType.FLOAT                ),  //    , pg
        INTEGER                 (StandardColumnType.INT                   ),  //mysql                , sqlite
        JSON                    (StandardColumnType.CLOB                  ),  //mysql, pg
        JSONB                   (StandardColumnType.BLOB                  ),  //    , pg
        LINE                    (StandardColumnType.ILLEGAL            ),  //    , pg
        LINESTRING              (StandardColumnType.ILLEGAL            ),  //mysql
        LONG                    (StandardColumnType.LONG                ),  //    ,  , oracle
        LONGBLOB                (StandardColumnType.BLOB             ),  //mysql
        LONGTEXT                (StandardColumnType.CLOB              ),  //mysql
        LSEG                    (StandardColumnType.ILLEGAL               ),  //    , pg
        MACADDR                 (StandardColumnType.ILLEGAL               ),  //    , pg
        MONEY                   (StandardColumnType.DECIMAL               ),  //    , pg,     , mssql
        NUMBER                  (StandardColumnType.DECIMAL               ),  //    ,  , oracle
        NCHAR                   (StandardColumnType.VARCHAR               ),  //    ,  , oracle, mssql
        NCLOB                   (StandardColumnType.CLOB                  ),  //    ,  , oracle
        NTEXT                   (StandardColumnType.CLOB                  ),  //    ,  ,     , mssql
        NVARCHAR                (StandardColumnType.VARCHAR               ),  //    ,  ,     , mssql
        NVARCHAR2               (StandardColumnType.VARCHAR               ),  //    ,  , oracle
        PATH                    (StandardColumnType.ILLEGAL               ),  //    , pg
        MEDIUMBLOB              (StandardColumnType.BLOB            ),  //mysql, 
        MEDIUMINT               (StandardColumnType.INT             ),  //mysql, 
        MEDIUMTEXT              (StandardColumnType.CLOB            ),  //mysql, 
        MULTILINE               (StandardColumnType.ILLEGAL       ),  //mysql, 
        MULTILINESTRING         (StandardColumnType.ILLEGAL       ),  //mysql, 
        MULTIPOINT              (StandardColumnType.ILLEGAL            ),  //mysql, 
        MULTIPOLYGON            (StandardColumnType.ILLEGAL          ),  //mysql, 
        NUMERIC                 (StandardColumnType.NUMERIC               ),  //mysql,  ,      , mssql, sqlite
        POINT                   (StandardColumnType.ILLEGAL                 ),  //mysql, pg
        GEOGRAPHY_POINT         (StandardColumnType.ILLEGAL                 ),  //voltdb
        POLYGON                 (StandardColumnType.ILLEGAL               ),  //mysql, pg
        REAL                    (StandardColumnType.REAL                  ),  //mysql,  ,     , mssql, sqlite
        RAW                     (StandardColumnType.ILLEGAL               ),  //    ,  , oracle
        ROWID                   (StandardColumnType.ILLEGAL               ),  //    ,  , oracle
        SERIAL                  (StandardColumnType.INT               ),  //    , pg, 
        SERIAL2                 (StandardColumnType.INT               ),  //    , pg, 
        SERIAL4                 (StandardColumnType.INT                   ),  //    , pg, 
        SERIAL8                 (StandardColumnType.FLOAT                ),  //    , pg, 
        SET                     (StandardColumnType.INT                   ),  //mysql, 
        SMALLDATETIME           (StandardColumnType.TIMESTAMP             ),  //    ,  ,     , mssql
        SMALLMONEY              (StandardColumnType.DECIMAL               ),  //    ,  ,     , mssql
        SMALLINT                (StandardColumnType.SMALLINT               ),  //mysql, 
        SMALLSERIAL             (StandardColumnType.SMALLINT               ),  //    , pg, 
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ),  //    ,  ,     , mssql
        SYSNAME                 (StandardColumnType.ILLEGAL               ),  //    ,  ,     , mssql
        TEXT                    (StandardColumnType.CLOB                  ),  //mysql, pg,     , mssql, sqlite
        TIME                    (StandardColumnType.TIME                  ),  //mysql, pg,     , mssql
        TIMEZ                   (StandardColumnType.TIME                  ),  //    , pg
        TIMESTAMP               (StandardColumnType.TIMESTAMP             ),  //mysql, pg, oracle, mssql
        TIMESTAMP_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ),  //    , pg
        TIMESTAMP_ZONE          (StandardColumnType.TIMESTAMP             ),  //    , pg
        TSQUERY                 (StandardColumnType.ILLEGAL               ),  //    , pg
        TSVECTOR                (StandardColumnType.ILLEGAL               ),  //    , pg
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ),  //    , pg
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ),  //       ，    , mssql
        UUID                    (StandardColumnType.ILLEGAL               ),  //    , pg
        UROWID                  (StandardColumnType.ILLEGAL               ),  //    ,  , oracle
        VARBIT                  (StandardColumnType.BINARY             ),  //    , pg
        TINYBLOB                (StandardColumnType.BLOB              ),  //mysql, 
        TINYINT                 (StandardColumnType.SMALLINT               ),  //mysql,  ,     , mssql
        TINYTEXT                (StandardColumnType.CLOB              ),  //mysql, 
        VARBINARY               (StandardColumnType.BINARY             ),  //mysql,  ,     , mssql
        VARCHAR                 (StandardColumnType.VARCHAR               ),  //mysql, pg, oracle, mssql
        VARCHAR2                (StandardColumnType.VARCHAR               ),  //    ,  , oracle, 
        XML                     (StandardColumnType.CLOB                  ),  //    , pg，     , mssql
        YEAR                    (StandardColumnType.DATE                  ); //mysql, 
        private final ColumnType standard;
        MaxDBColumnTypeAlias(ColumnType standard){
                this.standard = standard;
        }

        @Override
        public ColumnType standard() {
                return standard;
        }
}
