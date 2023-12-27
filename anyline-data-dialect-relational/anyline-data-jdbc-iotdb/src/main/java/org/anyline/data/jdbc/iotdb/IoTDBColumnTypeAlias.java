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


package org.anyline.data.jdbc.iotdb;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;
public enum IoTDBColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL      ),  //    ,  , oracle, 
        BINARY_DOUBLE           (StandardColumnType.DOUBLE       ),  //    ,  , oracle, 
        BINARY_FLOAT            (StandardColumnType.ILLEGAL      ),  //    ,  , oracle, 
        BIGINT                  (StandardColumnType.INT64        ),  //mysql,  ,     , mssql, 
        BIGSERIAL               (StandardColumnType.INT64        ),  //    , pg, 
        BINARY                  (StandardColumnType.ILLEGAL      ),  //mysql,  ,     , mssql, 
        BIT                     (StandardColumnType.ILLEGAL      ),  //mysql, pg,     , mssql, 
        BLOB                    (StandardColumnType.ILLEGAL      ),  //mysql,  , oracle,   , sqlite
        BOOL                    (StandardColumnType.ILLEGAL      ),  //    , pg
        BOX                     (StandardColumnType.ILLEGAL      ),  //    , pg
        BYTEA                   (StandardColumnType.ILLEGAL      ),  //    , pg
        CHAR                    (StandardColumnType.TEXT         ),  //mysql, pg, oracle, mssql, 
        CIDR                    (StandardColumnType.ILLEGAL      ),  //      pg
        CIRCLE                  (StandardColumnType.ILLEGAL      ),  //      pg
        CLOB                    (StandardColumnType.TEXT         ),  //    ,  , oracle
        DATE                    (StandardColumnType.INT64        ),  //mysql, pg, oracle, mssql
        DATETIME                (StandardColumnType.INT64        ),  //mysql,  ,     , mssql
        DATETIME2               (StandardColumnType.INT64        ),  //mysql,  ,     , mssql
        DATETIMEOFFSET          (StandardColumnType.INT64        ),  //mysql,  ,     , mssql
        DECIMAL                 (StandardColumnType.DOUBLE       ),  //mysql, pg, oracle, mssql
        DOUBLE                  (StandardColumnType.DOUBLE       ),  //mysql, 
        ENUM                    (StandardColumnType.ILLEGAL      ),  //mysql, 
        FLOAT                   (StandardColumnType.FLOAT        ),  //mysql,  , oracle, mssql
        FLOAT4                  (StandardColumnType.FLOAT        ),  //    , pg
        FLOAT8                  (StandardColumnType.FLOAT        ),  //    , pg
        GEOGRAPHY               (StandardColumnType.ILLEGAL      ),  //    ,  ,     , mssql
        GEOMETRY                (StandardColumnType.ILLEGAL      ),  //mysql
        GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL      ),  //mysql
        HIERARCHYID             (StandardColumnType.ILLEGAL      ),  //    ,  ,     , mssql
        IMAGE                   (StandardColumnType.ILLEGAL      ),  //    ,  ,     , mssql
        INET                    (StandardColumnType.ILLEGAL      ),  //    , pg
        INTERVAL                (StandardColumnType.ILLEGAL      ),  //    , pg
        INT                     (StandardColumnType.INT32        ),  //mysql,  ,     , mssql, 
        INT2                    (StandardColumnType.INT32        ),  //    , pg
        INT4                    (StandardColumnType.INT32        ),  //    , pg
        INT8                    (StandardColumnType.INT64        ),  //    , pg
        INT32                    (StandardColumnType.INT32       ),  //    , iotdb
        INT64                    (StandardColumnType.INT64       ),  //    , iotdb
        INTEGER                 (StandardColumnType.INT32        ),  //mysql                , sqlite
        JSON                    (StandardColumnType.TEXT         ),  //mysql, pg
        JSONB                   (StandardColumnType.ILLEGAL      ),  //    , pg
        LINE                    (StandardColumnType.ILLEGAL      ),  //    , pg
        LINESTRING              (StandardColumnType.ILLEGAL      ),  //mysql
        LONG                    (StandardColumnType.INT64        ),  //    ,  , oracle
        LONGBLOB                (StandardColumnType.ILLEGAL      ),  //mysql
        LONGTEXT                (StandardColumnType.TEXT         ),  //mysql
        LSEG                    (StandardColumnType.ILLEGAL      ),  //    , pg
        MACADDR                 (StandardColumnType.ILLEGAL      ),  //    , pg
        MONEY                   (StandardColumnType.DOUBLE       ),  //    , pg,     , mssql
        NUMBER                  (StandardColumnType.DOUBLE       ),  //    ,  , oracle
        NCHAR                   (StandardColumnType.TEXT         ),  //    ,  , oracle, mssql
        NCLOB                   (StandardColumnType.TEXT         ),  //    ,  , oracle
        NTEXT                   (StandardColumnType.TEXT         ),  //    ,  ,     , mssql
        NVARCHAR                (StandardColumnType.TEXT         ),  //    ,  ,     , mssql
        NVARCHAR2               (StandardColumnType.TEXT         ),  //    ,  , oracle
        PATH                    (StandardColumnType.ILLEGAL      ),  //    , pg
        MEDIUMBLOB              (StandardColumnType.ILLEGAL      ),  //mysql, 
        MEDIUMINT               (StandardColumnType.INT32        ),  //mysql, 
        MEDIUMTEXT              (StandardColumnType.TEXT         ),  //mysql, 
        MULTILINE               (StandardColumnType.ILLEGAL      ),  //mysql, 
        MULTILINESTRING         (StandardColumnType.ILLEGAL      ),  //mysql, 
        MULTIPOINT              (StandardColumnType.ILLEGAL      ),  //mysql, 
        MULTIPOLYGON            (StandardColumnType.ILLEGAL      ),  //mysql, 
        NUMERIC                 (StandardColumnType.DOUBLE       ),  //mysql,  ,      , mssql, sqlite
        POINT                   (StandardColumnType.ILLEGAL      ),  //mysql, pg
        POLYGON                 (StandardColumnType.ILLEGAL      ),  //mysql, pg
        REAL                    (StandardColumnType.DOUBLE       ),  //mysql,  ,     , mssql, sqlite
        RAW                     (StandardColumnType.ILLEGAL      ),  //    ,  , oracle
        ROWID                   (StandardColumnType.ILLEGAL      ),  //    ,  , oracle
        SERIAL                  (StandardColumnType.INT32        ),  //    , pg, 
        SERIAL2                 (StandardColumnType.INT32        ),  //    , pg, 
        SERIAL4                 (StandardColumnType.INT32        ),  //    , pg, 
        SERIAL8                 (StandardColumnType.INT64        ),  //    , pg, 
        SET                     (StandardColumnType.ILLEGAL      ),  //mysql, 
        SMALLDATETIME           (StandardColumnType.INT64        ),  //    ,  ,     , mssql
        SMALLMONEY              (StandardColumnType.DOUBLE       ),  //    ,  ,     , mssql
        SMALLINT                (StandardColumnType.INT32        ),  //mysql, 
        SMALLSERIAL             (StandardColumnType.INT32        ),  //    , pg, 
        SQL_VARIANT             (StandardColumnType.ILLEGAL      ),  //    ,  ,     , mssql
        SYSNAME                 (StandardColumnType.ILLEGAL      ),  //    ,  ,     , mssql
        TEXT                    (StandardColumnType.TEXT         ),  //mysql, pg,     , mssql, sqlite
        TIME                    (StandardColumnType.INT64        ),  //mysql, pg,     , mssql
        TIMEZ                   (StandardColumnType.INT64        ),  //    , pg
        TIMESTAMP               (StandardColumnType.INT64        ),  //mysql, pg, oracle, mssql
        TIMESTAMP_LOCAL_ZONE    (StandardColumnType.INT64        ),  //    , pg
        TIMESTAMP_ZONE          (StandardColumnType.INT64        ),  //    , pg
        TSQUERY                 (StandardColumnType.ILLEGAL      ),  //    , pg
        TSVECTOR                (StandardColumnType.ILLEGAL      ),  //    , pg
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL      ),  //    , pg
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL      ),  //       ，    , mssql
        UUID                    (StandardColumnType.ILLEGAL      ),  //    , pg
        UROWID                  (StandardColumnType.ILLEGAL      ),  //    ,  , oracle
        VARBIT                  (StandardColumnType.ILLEGAL      ),  //    , pg
        TINYBLOB                (StandardColumnType.ILLEGAL      ),  //mysql, 
        TINYINT                 (StandardColumnType.INT32        ),  //mysql,  ,     , mssql
        TINYTEXT                (StandardColumnType.TEXT         ),  //mysql, 
        VARBINARY               (StandardColumnType.TEXT         ),  //mysql,  ,     , mssql
        VARCHAR                 (StandardColumnType.TEXT         ),  //mysql, pg, oracle, mssql
        VARCHAR2                (StandardColumnType.TEXT         ),  //    ,  , oracle, 
        XML                     (StandardColumnType.TEXT         ),  //    , pg，     , mssql
        YEAR                    (StandardColumnType.INT32        ); //mysql, 
        private final ColumnType standard;
        IoTDBColumnTypeAlias(ColumnType standard){
                this.standard = standard;
        }

        @Override
        public ColumnType standard() {
                return standard;
        }
}
