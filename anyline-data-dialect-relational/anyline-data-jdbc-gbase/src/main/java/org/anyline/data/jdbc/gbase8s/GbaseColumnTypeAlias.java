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


package org.anyline.data.jdbc.gbase8s;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum GbaseColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.BFILE                 ),
    BINARY_DOUBLE           (StandardColumnType.BINARY_DOUBLE         ),
    BINARY_FLOAT            (StandardColumnType.FLOAT4                ),
    BIGINT                  (StandardColumnType.NUMBER                ),
    BIGSERIAL               (StandardColumnType.NUMBER                ),
    BINARY                  (StandardColumnType.BLOB                  ),
    BIT                     (StandardColumnType.NUMBER                ),
    BLOB                    (StandardColumnType.BLOB                  ),
    BOOL                    (StandardColumnType.NUMBER                ), 
    BOX                     (StandardColumnType.ILLEGAL               ), 
    BYTEA                   (StandardColumnType.BLOB                  ), 
    CHAR                    (StandardColumnType.CHAR                  ),
    CIDR                    (StandardColumnType.ILLEGAL               ), 
    CIRCLE                  (StandardColumnType.ILLEGAL               ), 
    CLOB                    (StandardColumnType.CLOB                  ), 
    DATE                    (StandardColumnType.DATE                  ), 
    DATETIME                (StandardColumnType.TIMESTAMP             ),      
    DATETIME2               (StandardColumnType.TIMESTAMP             ),      
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP             ),      
    DECIMAL                 (StandardColumnType.NUMBER                ), 
    DOUBLE                  (StandardColumnType.NUMBER                ), 
    ENUM                    (StandardColumnType.ILLEGAL               ), 
    FLOAT                   (StandardColumnType.FLOAT_ORACLE          ),
    FLOAT4                  (StandardColumnType.FLOAT_ORACLE          ), 
    FLOAT8                  (StandardColumnType.FLOAT_ORACLE          ), 
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
    GEOMETRY                (StandardColumnType.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ),
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
    IMAGE                   (StandardColumnType.BLOB                  ), 
    INET                    (StandardColumnType.ILLEGAL               ), 
    INTERVAL                (StandardColumnType.ILLEGAL               ), 
    INT                     (StandardColumnType.NUMBER                ),
    INT2                    (StandardColumnType.NUMBER                ), 
    INT4                    (StandardColumnType.NUMBER                ), 
    INT8                    (StandardColumnType.NUMBER                ), 
    INTEGER                 (StandardColumnType.NUMBER                ),
    JSON                    (StandardColumnType.CLOB                  ), 
    JSONB                   (StandardColumnType.BLOB                  ), 
    LINE                    (StandardColumnType.ILLEGAL               ), 
    LONG                    (StandardColumnType.LONG_TEXT                  ), 
    LONGBLOB                (StandardColumnType.BLOB                  ),
    LONGTEXT                (StandardColumnType.CLOB                  ),
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
    MEDIUMTEXT              (StandardColumnType.CLOB                  ), 
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), 
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), 
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), 
    NUMERIC                 (StandardColumnType.NUMBER                ),
    POINT                   (StandardColumnType.ILLEGAL               ), 
    POLYGON                 (StandardColumnType.ILLEGAL               ), 
    REAL                    (StandardColumnType.FLOAT_ORACLE          ),
    RAW                     (StandardColumnType.RAW                   ), 
    ROWID                   (StandardColumnType.ROWID                 ), 
    SERIAL                  (StandardColumnType.NUMBER                ),
    SERIAL2                 (StandardColumnType.NUMBER                ),
    SERIAL4                 (StandardColumnType.NUMBER                ),
    SERIAL8                 (StandardColumnType.NUMBER                ),
    SET                     (StandardColumnType.ILLEGAL               ), 
    SMALLDATETIME           (StandardColumnType.TIMESTAMP             ), 
    SMALLMONEY              (StandardColumnType.NUMBER                ), 
    SMALLINT                (StandardColumnType.NUMBER                ), 
    SMALLSERIAL             (StandardColumnType.NUMBER                ),
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), 
    SYSNAME                 (StandardColumnType.ILLEGAL               ), 
    TEXT                    (StandardColumnType.CLOB                  ),
    TIME                    (StandardColumnType.TIMESTAMP             ),
    TIMEZ                   (StandardColumnType.TIMESTAMP             ), 
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ), 
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), 
    TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP             ), 
    TSQUERY                 (StandardColumnType.ILLEGAL               ), 
    TSVECTOR                (StandardColumnType.ILLEGAL               ), 
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
    UUID                    (StandardColumnType.ILLEGAL               ), 
    UROWID                  (StandardColumnType.UROWID                ), 
    VARBIT                  (StandardColumnType.BLOB                  ), 
    TINYBLOB                (StandardColumnType.BLOB                  ), 
    TINYINT                 (StandardColumnType.NUMBER                ),      
    TINYTEXT                (StandardColumnType.CLOB                  ), 
    VARBINARY               (StandardColumnType.BLOB                  ),      
    VARCHAR                 (StandardColumnType.VARCHAR               ), 
    VARCHAR2                (StandardColumnType.VARCHAR               ),
    XML                     (StandardColumnType.ILLEGAL               ),
    YEAR                    (StandardColumnType.DATE                  ); 
    private final TypeMetadata standard;
    private GbaseColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}
