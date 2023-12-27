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


package org.anyline.data.jdbc.hana;


import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.ColumnType;

public enum HanaColumnTypeAlias implements ColumnTypeAlias {
    BFILE                   (StandardColumnType.ILLEGAL               ),  //     ,       , oracle,
    BINARY_DOUBLE           (StandardColumnType.ILLEGAL               ),  //     ,       , oracle,
    BINARY_FLOAT            (StandardColumnType.ILLEGAL               ),  //     ,       , oracle,
    BIGINT                  (StandardColumnType.BIGINT                ),  // HANA, mysql        , mssql,
    BIGSERIAL               (StandardColumnType.BIGINT                ),  //     ,     , pg,
    BINARY                  (StandardColumnType.BINARY                ),  // HANA, mysql        , mssql,
    BIT                     (StandardColumnType.BOOLEAN               ),  //     , mysql, pg,     , mssql,
    BLOB                    (StandardColumnType.BLOB                  ),  // HANA, mysql , oracle,   , sqlite
    BOOL                    (StandardColumnType.BOOLEAN               ),  //     ,     , pg
    BOOLEAN                 (StandardColumnType.BOOLEAN               ),  // HANA,     , pg
    BOX                     (StandardColumnType.ILLEGAL               ),  //     ,     , pg
    BYTEA                   (StandardColumnType.ILLEGAL               ),  //     ,     , pg
    CHAR                    (StandardColumnType.CHAR                  ),  // HANA, mysql, pg, oracle, mssql,
    CIDR                    (StandardColumnType.ILLEGAL               ),  //     ,       pg
    CIRCLE                  (StandardColumnType.ILLEGAL               ),  //     ,       pg
    CLOB                    (StandardColumnType.NCLOB                 ),  //     ,       , oracle
    DATE                    (StandardColumnType.DATE                  ),  // HANA, mysql, pg, oracle, mssql
    DATETIME                (StandardColumnType.TIMESTAMP             ),  //     , mysql        , mssql
    DATETIME2               (StandardColumnType.TIMESTAMP             ),  //     , mysql        , mssql
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP             ),  //     , mysql        , mssql
    DECIMAL                 (StandardColumnType.DECIMAL               ),  // HANA, mysql, pg, oracle, mssql
    DOUBLE                  (StandardColumnType.DOUBLE                ),  // HANA, mysql,
    ENUM                    (StandardColumnType.ILLEGAL               ),  //     , mysql,
    FLOAT                   (StandardColumnType.DOUBLE                ),  //     , mysql , oracle, mssql
    FLOAT4                  (StandardColumnType.DOUBLE                ),  //     ,     , pg
    FLOAT8                  (StandardColumnType.DOUBLE                ),  //     ,     , pg
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ),  //     ,              , mssql
    GEOMETRY                (StandardColumnType.ST_GEOMETRY           ),  //     , mysql
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ),  //     , mysql
    HIERARCHYID             (StandardColumnType.ILLEGAL               ),  //     ,              , mssql
    IMAGE                   (StandardColumnType.BLOB                  ),  //     ,              , mssql
    INET                    (StandardColumnType.ILLEGAL               ),  //     ,     , pg
    INTERVAL                (StandardColumnType.ILLEGAL               ),  //     ,     , pg
    INT                     (StandardColumnType.INTEGER               ),  //     , mysql        , mssql,
    INT2                    (StandardColumnType.INTEGER               ),  //     ,     , pg
    INT4                    (StandardColumnType.INTEGER               ),  //     ,     , pg
    INT8                    (StandardColumnType.INTEGER               ),  //     ,     , pg
    INTEGER                 (StandardColumnType.INTEGER               ),  // HANA, mysql                , sqlite
    JSON                    (StandardColumnType.NCLOB                 ),  //     , mysql, pg
    JSONB                   (StandardColumnType.BLOB                  ),  //     ,     , pg
    LINE                    (StandardColumnType.ILLEGAL               ),  //     , mysql, pg
    LONG                    (StandardColumnType.LONG                  ),  //     ,       , oracle
    LONGBLOB                (StandardColumnType.BLOB                  ),  //     , mysql
    LONGTEXT                (StandardColumnType.NCLOB                 ),  //     , mysql
    LSEG                    (StandardColumnType.ST_GEOMETRY           ),  //     ,     , pg
    MACADDR                 (StandardColumnType.ST_GEOMETRY           ),  //     ,     , pg
    MONEY                   (StandardColumnType.DECIMAL               ),  //     ,     , pg,     , mssql
    NUMBER                  (StandardColumnType.DECIMAL               ),  //     ,       , oracle
    NCHAR                   (StandardColumnType.NVARCHAR              ),  //     ,       , oracle, mssql
    NCLOB                   (StandardColumnType.NCLOB                 ),  // HANA,       , oracle
    NTEXT                   (StandardColumnType.NCLOB                 ),  //     ,              , mssql
    NVARCHAR                (StandardColumnType.NVARCHAR              ),  // HANA,              , mssql
    NVARCHAR2               (StandardColumnType.NVARCHAR              ),  //     ,       , oracle
    PATH                    (StandardColumnType.ST_GEOMETRY           ),  //     ,     , pg
    MEDIUMBLOB              (StandardColumnType.BLOB                  ),  //     , mysql,
    MEDIUMINT               (StandardColumnType.INTEGER               ),  //     , mysql,
    MEDIUMTEXT              (StandardColumnType.NCLOB                 ),  //     , mysql,
    MULTILINESTRING         (StandardColumnType.ST_GEOMETRY           ),  //     , mysql,
    MULTIPOINT              (StandardColumnType.ST_GEOMETRY           ),  //     , mysql,
    MULTIPOLYGON            (StandardColumnType.ST_GEOMETRY           ),  //     , mysql,
    NUMERIC                 (StandardColumnType.DECIMAL               ),  //     , mysql         , mssql, sqlite
    POINT                   (StandardColumnType.ST_POINT              ),  //     , mysql, pg
    POLYGON                 (StandardColumnType.ST_GEOMETRY           ),  //     , mysql, pg
    REAL                    (StandardColumnType.REAL                  ),  // HANA, mysql        , mssql, sqlite
    RAW                     (StandardColumnType.ILLEGAL               ),  //     ,       , oracle
    ROWID                   (StandardColumnType.ILLEGAL               ),  //     ,       , oracle
    SECONDDATE              (StandardColumnType.SECONDDATE            ),  // HANA
    SERIAL                  (StandardColumnType.INTEGER               ),  //     ,     , pg,
    SERIAL2                 (StandardColumnType.INTEGER               ),  //     ,     , pg,
    SERIAL4                 (StandardColumnType.INTEGER               ),  //     ,     , pg,
    SERIAL8                 (StandardColumnType.BIGINT                ),  //     ,     , pg,
    SET                     (StandardColumnType.ILLEGAL               ),  //     , mysql,
    SMALLDATETIME           (StandardColumnType.TIMESTAMP             ),  //     ,              , mssql
    SMALLDECIMAL            (StandardColumnType.SMALLDECIMAL          ),  // HANA
    SMALLMONEY              (StandardColumnType.DECIMAL               ),  //     ,              , mssql
    SMALLINT                (StandardColumnType.SMALLINT              ),  // HANA, mysql,
    SMALLSERIAL             (StandardColumnType.INTEGER               ),  //     ,     , pg,
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ),  //     ,              , mssql
    ST_GEOMETRY             (StandardColumnType.ST_GEOMETRY           ),  // HANA
    ST_POINT                (StandardColumnType.ST_POINT              ),  // HANA
    SYSNAME                 (StandardColumnType.ILLEGAL               ),  //     ,              , mssql
    TEXT                    (StandardColumnType.NCLOB                 ),  //     , mysql, pg,     , mssql, sqlite
    TIME                    (StandardColumnType.TIME                  ),  // HANA, mysql, pg,     , mssql
    TIMEZ                   (StandardColumnType.TIMESTAMP             ),  //     ,     , pg
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ),  // HANA, mysql, pg, oracle, mssql
    TIMESTAMP_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ),  //     ,     , pg
    TIMESTAMP_ZONE          (StandardColumnType.TIMESTAMP             ),  //     ,     , pg
    TSQUERY                 (StandardColumnType.ILLEGAL               ),  //     ,     , pg
    TSVECTOR                (StandardColumnType.ILLEGAL               ),  //     ,     , pg
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ),  //     ,     , pg
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ),  //     ,        ，    , mssql
    UUID                    (StandardColumnType.ILLEGAL               ),  //     ,     , pg
    UROWID                  (StandardColumnType.VARCHAR               ),  //     ,       , oracle
    VARBIT                  (StandardColumnType.BLOB                  ),  //     ,     , pg
    TINYBLOB                (StandardColumnType.BLOB                  ),  //     , mysql,
    TINYINT                 (StandardColumnType.TINYINT               ),  // HANA, mysql        , mssql
    TINYTEXT                (StandardColumnType.NCLOB                 ),  //     , mysql,
    VARBINARY               (StandardColumnType.VARBINARY             ),  // HANA, mysql        , mssql
    VARCHAR                 (StandardColumnType.VARCHAR               ),  // HANA, mysql, pg, oracle, mssql
    VARCHAR2                (StandardColumnType.VARCHAR               ),  //     ,        , oracle,
    XML                     (StandardColumnType.NVARCHAR              ),  //     ,     , pg，     , mssql
    YEAR                    (StandardColumnType.INTEGER               ); //     , mysql,
    private final ColumnType standard;
    private HanaColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }
}
