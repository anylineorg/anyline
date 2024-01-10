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


package org.anyline.data.jdbc.mssql;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum MSSQLColumnTypeAlias implements ColumnTypeAlias {
    BFILE                   (TypeMetadata.ILLEGAL                       ), 
    BINARY_DOUBLE           (StandardColumnType.NUMERIC               ), 
    BINARY_FLOAT            (StandardColumnType.FLOAT_MSSQL              ), 
    BIGINT                  (StandardColumnType.BIGINT                ), 
    BIGSERIAL               (StandardColumnType.BIGINT                ),
    BINARY                  (StandardColumnType.BINARY                ), 
    BIT                     (StandardColumnType.BIT                   ), 
    BLOB                    (StandardColumnType.VARBINARY             ), 
    BOOL                    (StandardColumnType.BIT                   ), 
    BOX                     (StandardColumnType.ILLEGAL               ), 
    BYTEA                   (StandardColumnType.VARBINARY             ), 
    CHAR                    (StandardColumnType.CHAR                  ), 
    CIDR                    (StandardColumnType.ILLEGAL               ), 
    CIRCLE                  (StandardColumnType.ILLEGAL               ), 
    CLOB                    (StandardColumnType.TEXT                  ), 
    DATE                    (StandardColumnType.DATE                  ), 
    DATETIME                (StandardColumnType.DATETIME              ), 
    DATETIME2               (StandardColumnType.DATETIME2             ), 
    DATETIMEOFFSET          (StandardColumnType.DATETIMEOFFSET        ), 
    DECIMAL                 (StandardColumnType.DECIMAL               ), 
    DOUBLE                  (StandardColumnType.DECIMAL               ), 
    ENUM                    (StandardColumnType.ILLEGAL               ), 
    FLOAT                   (StandardColumnType.FLOAT_MSSQL                 ),
    FLOAT4                  (StandardColumnType.FLOAT_MSSQL                 ), 
    FLOAT8                  (StandardColumnType.FLOAT_MSSQL                 ), 
    GEOGRAPHY               (StandardColumnType.GEOGRAPHY             ), 
    GEOMETRY                (StandardColumnType.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ),
    HIERARCHYID             (StandardColumnType.HIERARCHYID           ), 
    IMAGE                   (StandardColumnType.IMAGE                 ), 
    INET                    (StandardColumnType.ILLEGAL               ), 
    INTERVAL                (StandardColumnType.ILLEGAL               ), 
    INT                     (StandardColumnType.INT                   ), 
    INT2                    (StandardColumnType.INT                   ), 
    INT4                    (StandardColumnType.INT                   ), 
    INT8                    (StandardColumnType.BIGINT                ), 
    INTEGER                 (StandardColumnType.INT                   ), 
    JSON                    (StandardColumnType.ILLEGAL               ), 
    JSONB                   (StandardColumnType.ILLEGAL               ), 
    LINE                    (StandardColumnType.ILLEGAL               ), 
    LONG                    (StandardColumnType.BIGINT                ), 
    LONGBLOB                (StandardColumnType.VARBINARY             ),
    LONGTEXT                (StandardColumnType.TEXT                  ),
    LSEG                    (StandardColumnType.ILLEGAL               ), 
    MACADDR                 (StandardColumnType.ILLEGAL               ), 
    MONEY                   (StandardColumnType.MONEY                 ), 
    NUMBER                  (StandardColumnType.NUMERIC               ), 
    NCHAR                   (StandardColumnType.NCHAR                 ), 
    NCLOB                   (StandardColumnType.VARBINARY             ), 
    NTEXT                   (StandardColumnType.NTEXT                 ), 
    NVARCHAR                (StandardColumnType.NVARCHAR              ), 
    NVARCHAR2               (StandardColumnType.NVARCHAR              ), 
    PATH                    (StandardColumnType.ILLEGAL               ), 
    MEDIUMBLOB              (StandardColumnType.VARBINARY             ), 
    MEDIUMINT               (StandardColumnType.INT                   ), 
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), 
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), 
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), 
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), 
    NUMERIC                 (StandardColumnType.NUMERIC               ),
    POINT                   (StandardColumnType.ILLEGAL               ), 
    POLYGON                 (StandardColumnType.ILLEGAL               ), 
    REAL                    (StandardColumnType.REAL                  ), 
    RAW                     (StandardColumnType.ILLEGAL               ), 
    ROWID                   (StandardColumnType.ILLEGAL               ), 
    SERIAL                  (StandardColumnType.INT                   ),
    SERIAL2                 (StandardColumnType.TINYINT               ),
    SERIAL4                 (StandardColumnType.INT                   ),
    SERIAL8                 (StandardColumnType.BIGINT                ),
    SET                     (StandardColumnType.ILLEGAL               ), 
    SMALLDATETIME           (StandardColumnType.SMALLDATETIME         ), 
    SMALLMONEY              (StandardColumnType.SMALLMONEY            ), 
    SMALLINT                (StandardColumnType.INT                   ), 
    SMALLSERIAL             (StandardColumnType.INT                   ),
    SQL_VARIANT             (StandardColumnType.SQL_VARIANT           ), 
    SYSNAME                 (StandardColumnType.SYSNAME               ),
    TEXT                    (StandardColumnType.TEXT                  ),
    TIME                    (StandardColumnType.TIME                  ),
    TIMEZ                   (StandardColumnType.TIME                  ), 
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ), 
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), 
    TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP             ), 
    TSQUERY                 (StandardColumnType.ILLEGAL               ), 
    TSVECTOR                (StandardColumnType.ILLEGAL               ), 
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
    UNIQUEIDENTIFIER        (StandardColumnType.UNIQUEIDENTIFIER      ),
    UUID                    (StandardColumnType.ILLEGAL               ), 
    UROWID                  (StandardColumnType.ILLEGAL               ),
    VARBIT                  (StandardColumnType.VARBINARY             ), 
    TINYBLOB                (StandardColumnType.VARBINARY             ), 
    TINYINT                 (StandardColumnType.TINYINT               ), 
    TINYTEXT                (StandardColumnType.TEXT                  ), 
    VARBINARY               (StandardColumnType.VARBINARY             ), 
    VARCHAR                 (StandardColumnType.VARCHAR               ), 
    VARCHAR2                (StandardColumnType.VARCHAR               ),
    XML                     (StandardColumnType.XML                   ),
    YEAR                    (StandardColumnType.DATE                  ); 
    private final TypeMetadata standard;
    private MSSQLColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}
