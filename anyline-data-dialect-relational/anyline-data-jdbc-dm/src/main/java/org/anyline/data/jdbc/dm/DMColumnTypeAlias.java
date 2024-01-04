/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:
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
    
    BFILE                   (StandardColumnType.BFILE                 ), 
    BINARY_DOUBLE           (StandardColumnType.BINARY_DOUBLE         ), 
    BINARY_FLOAT            (StandardColumnType.FLOAT_ORACLE          ), 
    BIGINT                  (StandardColumnType.BIGINT                ), 
    BIGSERIAL               (StandardColumnType.BIGINT                ), 
    BINARY                  (StandardColumnType.BINARY                  ), 
    BIT                     (StandardColumnType.BIT                ), 
    BLOB                    (StandardColumnType.BLOB                  ), 
    BOOL                    (StandardColumnType.BOOL                ), 
    BOOLEAN                 (StandardColumnType.BOOLEAN                ), 
    BOX                     (StandardColumnType.ILLEGAL               ), 
    BYTEA                   (StandardColumnType.BLOB                  ), 
    BYTE                    (StandardColumnType.BYTE                  ),
    CHAR                    (StandardColumnType.CHAR                  ), 
    CIDR                    (StandardColumnType.ILLEGAL               ), 
    CIRCLE                  (StandardColumnType.ILLEGAL               ), 
    CLOB                    (StandardColumnType.CLOB                  ), 
    DATE                    (StandardColumnType.DATE                  ), 
    TIME_WITH_ZONE          (StandardColumnType.TIME_WITH_ZONE        ),
    DATETIME                (StandardColumnType.DATETIME             ), 
    DATETIME2               (StandardColumnType.DATETIME             ), 
    DATETIMEOFFSET          (StandardColumnType.DATETIME             ), 
    DECIMAL                 (StandardColumnType.DECIMAL               ), 
    DOUBLE                  (StandardColumnType.DOUBLE                ), 
    ENUM                    (StandardColumnType.ILLEGAL               ), 
    FLOAT                   (StandardColumnType.FLOAT_ORACLE          ), 
    FLOAT4                  (StandardColumnType.FLOAT_ORACLE          ), 
    FLOAT8                  (StandardColumnType.FLOAT_ORACLE          ), 
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
    GEOMETRY                (StandardColumnType.ILLEGAL               ), 
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ), 
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
    IMAGE                   (StandardColumnType.IMAGE                  ), 
    INET                    (StandardColumnType.ILLEGAL               ), 
    INTERVAL                (StandardColumnType.ILLEGAL               ), 
    INT                     (StandardColumnType.INT                   ), 
    INT2                    (StandardColumnType.INT                   ), 
    INT4                    (StandardColumnType.INT                   ), 
    INT8                    (StandardColumnType.BIGINT                ), 
    INTEGER                 (StandardColumnType.INTEGER               ), 
    JSON                    (StandardColumnType.CLOB                  ), 
    JSONB                   (StandardColumnType.BLOB                  ), 
    LINE                    (StandardColumnType.ILLEGAL               ), 
    LONG                    (StandardColumnType.LONG                  ), 
    LONGBLOB                (StandardColumnType.BLOB                  ), 
    LONGTEXT                (StandardColumnType.TEXT                  ), 
    LSEG                    (StandardColumnType.ILLEGAL               ), 
    MACADDR                 (StandardColumnType.ILLEGAL               ), 
    MONEY                   (StandardColumnType.NUMBER                ), 
    NUMBER                  (StandardColumnType.NUMBER                ), 
    NCHAR                   (StandardColumnType.NCHAR                 ), 
    NCLOB                   (StandardColumnType.NCLOB                 ), 
    NTEXT                   (StandardColumnType.NCLOB                 ), 
    NVARCHAR                (StandardColumnType.NVARCHAR2             ), 
    NVARCHAR2               (StandardColumnType.NVARCHAR2             ), 
    PATH                    (StandardColumnType.ILLEGAL               ), 
    MEDIUMBLOB              (StandardColumnType.BLOB                  ), 
    MEDIUMINT               (StandardColumnType.NUMBER                ), 
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), 
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), 
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), 
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), 
    NUMERIC                 (StandardColumnType.NUMBER                ), 
    POINT                   (StandardColumnType.ILLEGAL               ), 
    POLYGON                 (StandardColumnType.ILLEGAL               ), 
    REAL                    (StandardColumnType.REAL          ), 
    RAW                     (StandardColumnType.RAW                   ), 
    ROWID                   (StandardColumnType.ROWID                 ), 
    SERIAL                  (StandardColumnType.INT                   ), 
    SERIAL2                 (StandardColumnType.INT                   ), 
    SERIAL4                 (StandardColumnType.INT                   ), 
    SERIAL8                 (StandardColumnType.BIGINT                ), 
    SET                     (StandardColumnType.ILLEGAL               ), 
    SMALLDATETIME           (StandardColumnType.TIMESTAMP             ), 
    SMALLMONEY              (StandardColumnType.NUMBER                ), 
    SMALLINT                (StandardColumnType.SMALLINT                ), 
    SMALLSERIAL             (StandardColumnType.NUMBER                ), 
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), 
    SYSNAME                 (StandardColumnType.ILLEGAL               ), 
    TEXT                    (StandardColumnType.TEXT                  ), 
    TIME                    (StandardColumnType.TIME             ), 
    TIMEZ                   (StandardColumnType.TIME             ), 
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ), 
    TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP_WITH_ZONE),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP_WITH_LOCAL_ZONE             ), 
    TSQUERY                 (StandardColumnType.ILLEGAL               ), 
    TSVECTOR                (StandardColumnType.ILLEGAL               ), 
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
    UUID                    (StandardColumnType.ILLEGAL               ), 
    UROWID                  (StandardColumnType.UROWID                ), 
    VARBIT                  (StandardColumnType.BLOB                  ), 
    TINYBLOB                (StandardColumnType.BLOB                  ), 
    TINYINT                 (StandardColumnType.TINYINT                ), 
    TINYTEXT                (StandardColumnType.TEXT                  ), 
    VARBINARY               (StandardColumnType.VARBINARY                  ), 
    VARCHAR                 (StandardColumnType.VARCHAR               ), 
    VARCHAR2                (StandardColumnType.VARCHAR               ), 
    XML                     (StandardColumnType.ILLEGAL               ), 
    YEAR                    (StandardColumnType.DATE                  ); 
    private final ColumnType standard;
    DMColumnTypeAlias(ColumnType standard){
        this.standard = standard;
    }

    @Override
    public ColumnType standard() {
        return standard;
    }
}