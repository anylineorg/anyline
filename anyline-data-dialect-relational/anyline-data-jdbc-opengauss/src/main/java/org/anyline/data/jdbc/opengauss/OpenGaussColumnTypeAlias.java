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


package org.anyline.data.jdbc.opengauss;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum OpenGaussColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.ILLEGAL               ), 
    BINARY_DOUBLE           (StandardColumnType.BINARY_DOUBLE         ), 
    BINARY_FLOAT            (StandardColumnType.BINARY_FLOAT          ), 
    BIGINT                  (StandardColumnType.INT8                  ), 
    BIGSERIAL               (StandardColumnType.BIGSERIAL             ),
    BINARY                  (StandardColumnType.BIT                   ), 
    BIT                     (StandardColumnType.BIT                   ), 
    BLOB                    (StandardColumnType.BYTEA                 ), 
    BOOL                    (StandardColumnType.BOOL                   ), 
    BOX                     (StandardColumnType.ILLEGAL               ), 
    BYTEA                   (StandardColumnType.BYTEA                 ), 
    CHAR                    (StandardColumnType.CHAR                  ), 
    CIDR                    (StandardColumnType.ILLEGAL               ), 
    CIRCLE                  (StandardColumnType.ILLEGAL               ), 
    CLOB                    (StandardColumnType.TEXT                  ), 
    DATE                    (StandardColumnType.DATE                  ), 
    DATETIME                (StandardColumnType.TIMESTAMP             ), 
    DATETIME2               (StandardColumnType.TIMESTAMP             ), 
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP             ), 
    DECIMAL                 (StandardColumnType.DECIMAL               ), 
    DOUBLE                  (StandardColumnType.DECIMAL               ), 
    ENUM                    (StandardColumnType.ILLEGAL               ), 
    FLOAT                   (StandardColumnType.FLOAT4                ),
    FLOAT4                  (StandardColumnType.FLOAT4                ), 
    FLOAT8                  (StandardColumnType.FLOAT8                ), 
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
    GEOMETRY                (StandardColumnType.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ),
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
    IMAGE                   (StandardColumnType.BYTEA                 ), 
    INET                    (StandardColumnType.INET                  ), 
    INTERVAL                (StandardColumnType.INTERVAL              ), 
    INT                     (StandardColumnType.INT4                  ), 
    INT2                    (StandardColumnType.INT2                  ), 
    INT4                    (StandardColumnType.INT4                  ), //
    INT8                    (StandardColumnType.INT8                  ), //
    INTEGER                 (StandardColumnType.INT4                  ), 
    JSON                    (StandardColumnType.JSON                  ), 
    JSONB                   (StandardColumnType.JSONB                 ), 
    LINE                    (StandardColumnType.LINE                  ), 
    LINESTRING              (StandardColumnType.LINE                  ),
    LONG                    (StandardColumnType.INT8                  ), 
    LONGBLOB                (StandardColumnType.BYTEA                 ),
    LONGTEXT                (StandardColumnType.TEXT                  ),
    LSEG                    (StandardColumnType.LSEG                  ), 
    MACADDR                 (StandardColumnType.MACADDR               ), 
    MONEY                   (StandardColumnType.MONEY                 ), 
    NUMBER                  (StandardColumnType.DECIMAL               ), 
    NCHAR                   (StandardColumnType.VARCHAR               ), 
    NCLOB                   (StandardColumnType.BYTEA                 ), 
    NTEXT                   (StandardColumnType.TEXT                  ), 
    NVARCHAR                (StandardColumnType.VARCHAR               ), 
    NVARCHAR2               (StandardColumnType.VARCHAR               ), 
    PATH                    (StandardColumnType.PATH                  ), 
    MEDIUMBLOB              (StandardColumnType.BYTEA                 ), 
    MEDIUMINT               (StandardColumnType.INT8                  ), 
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), 
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), 
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), 
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), 
    NUMERIC                 (StandardColumnType.DECIMAL               ),
    POINT                   (StandardColumnType.POINT                 ), 
    GEOGRAPHY_POINT         (StandardColumnType.POINT                 ),
    POLYGON                 (StandardColumnType.POLYGON               ), 
    REAL                    (StandardColumnType.FLOAT4                ), 
    RAW                     (StandardColumnType.ILLEGAL               ), 
    ROWID                   (StandardColumnType.ILLEGAL               ), 
    SERIAL                  (StandardColumnType.SERIAL                ),
    SERIAL2                 (StandardColumnType.SERIAL2               ),
    SERIAL4                 (StandardColumnType.SERIAL4               ),
    SERIAL8                 (StandardColumnType.SERIAL8               ),
    SET                     (StandardColumnType.ILLEGAL               ), 
    SMALLDATETIME           (StandardColumnType.TIMESTAMP             ), 
    SMALLMONEY              (StandardColumnType.DECIMAL               ), 
    SMALLINT                (StandardColumnType.INT2                  ), 
    SMALLSERIAL             (StandardColumnType.SMALLSERIAL           ),
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), 
    SYSNAME                 (StandardColumnType.ILLEGAL               ), 
    TEXT                    (StandardColumnType.TEXT                  ),  
    TIME                    (StandardColumnType.TIME                  ),  
    TIMEZ                   (StandardColumnType.TIMEZ                 ), 
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ), 
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP_WITH_LOCAL_ZONE  ), 
    TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP_WITH_ZONE        ), 
    TSQUERY                 (StandardColumnType.TSQUERY               ), 
    TSVECTOR                (StandardColumnType.TSVECTOR              ), 
    TXID_SNAPSHOT           (StandardColumnType.TXID_SNAPSHOT         ), 
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
    UUID                    (StandardColumnType.UUID                  ), 
    UROWID                  (StandardColumnType.ILLEGAL               ), 
    VARBIT                  (StandardColumnType.VARBIT                ), 
    TINYBLOB                (StandardColumnType.BYTEA                 ), 
    TINYINT                 (StandardColumnType.INT2                  ), 
    TINYTEXT                (StandardColumnType.TEXT                  ), 
    VARBINARY               (StandardColumnType.VARBIT                ), 
    VARCHAR                 (StandardColumnType.VARCHAR               ), 
    VARCHAR2                (StandardColumnType.VARCHAR               ), 
    XML                     (StandardColumnType.XML                   ),
    YEAR                    (StandardColumnType.DATE                  ); 
    private final TypeMetadata standard;
    private OpenGaussColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}
