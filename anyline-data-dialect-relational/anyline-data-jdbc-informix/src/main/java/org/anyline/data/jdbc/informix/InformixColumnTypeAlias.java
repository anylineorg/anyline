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


package org.anyline.data.jdbc.informix;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum InformixColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.ILLEGAL               ), 
    BINARY_DOUBLE           (StandardColumnType.BINARY_DOUBLE         ), 
    BINARY_FLOAT            (StandardColumnType.BINARY_FLOAT          ), 
    BIGINT                  (StandardColumnType.BIGINT                ),
    BIGSERIAL               (StandardColumnType.BIGSERIAL             ),
    BINARY                  (StandardColumnType.BYTE                  ), 
    BIT                     (StandardColumnType.BYTE                  ), 
    BLOB                    (StandardColumnType.BLOB                  ),
    BOOL                    (StandardColumnType.BOOLEAN               ), 
    BOOLEAN                 (StandardColumnType.BOOLEAN               ),  
    BOX                     (StandardColumnType.ILLEGAL               ), 
    BYTE                    (StandardColumnType.BYTE                  ),  
    BYTEA                   (StandardColumnType.BYTE                  ), 
    CHAR                    (StandardColumnType.CHAR                  ),
    CIDR                    (StandardColumnType.ILLEGAL               ), 
    CIRCLE                  (StandardColumnType.ILLEGAL               ), 
    CLOB                    (StandardColumnType.CLOB                  ),
    DATE                    (StandardColumnType.DATE                  ),
    DATETIME                (StandardColumnType.DATETIME              ),
    DATETIME2               (StandardColumnType.DATETIME              ), 
    DATETIMEOFFSET          (StandardColumnType.DATETIME              ), 
    DECIMAL                 (StandardColumnType.DECIMAL               ),
    DOUBLE                  (StandardColumnType.DOUBLE                ),      
    ENUM                    (StandardColumnType.ILLEGAL               ), 
    FLOAT                   (StandardColumnType.FLOAT_INFORMIX        ),
    FLOAT4                  (StandardColumnType.FLOAT_INFORMIX        ), 
    FLOAT8                  (StandardColumnType.FLOAT_INFORMIX        ), 
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
    GEOMETRY                (StandardColumnType.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ),
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
    IMAGE                   (StandardColumnType.BYTE                  ), 
    INET                    (StandardColumnType.ILLEGAL               ), 
    INTERVAL                (StandardColumnType.INTERVAL              ),         
    INT                     (StandardColumnType.INT                   ),
    INT2                    (StandardColumnType.INT                   ), 
    INT4                    (StandardColumnType.INT                   ), //
    INT8                    (StandardColumnType.INT8                  ),     
    INTEGER                 (StandardColumnType.INFORMIX_INTEGER      ),
    JSON                    (StandardColumnType.TEXT                  ), 
    JSONB                   (StandardColumnType.TEXT                  ), 
    LINE                    (StandardColumnType.ILLEGAL               ), 
    LONG                    (StandardColumnType.BIGINT                ), 
    LONGBLOB                (StandardColumnType.BLOB                  ),
    LONGTEXT                (StandardColumnType.TEXT                  ),
    LSEG                    (StandardColumnType.ILLEGAL               ), 
    MACADDR                 (StandardColumnType.ILLEGAL               ), 
    MONEY                   (StandardColumnType.MONEY                 ),  
    NUMBER                  (StandardColumnType.DECIMAL               ), 
    NCHAR                   (StandardColumnType.NCHAR                 ), 
    NCLOB                   (StandardColumnType.CLOB                  ), 
    NTEXT                   (StandardColumnType.TEXT                  ), 
    NVARCHAR                (StandardColumnType.VARCHAR               ), 
    NVARCHAR2               (StandardColumnType.VARCHAR               ), 
    PATH                    (StandardColumnType.ILLEGAL               ), 
    MEDIUMBLOB              (StandardColumnType.ILLEGAL               ), 
    MEDIUMINT               (StandardColumnType.INT                   ), 
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), 
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), 
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), 
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), 
    NUMERIC                 (StandardColumnType.DECIMAL               ),
    POINT                   (StandardColumnType.ILLEGAL               ), 
    POLYGON                 (StandardColumnType.ILLEGAL               ), 
    REAL                    (StandardColumnType.FLOAT_INFORMIX        ),
    RAW                     (StandardColumnType.ILLEGAL               ), 
    ROWID                   (StandardColumnType.ILLEGAL               ), 
    SERIAL                  (StandardColumnType.SERIAL                ),   
    SERIAL2                 (StandardColumnType.SERIAL                ),
    SERIAL4                 (StandardColumnType.SERIAL                ),
    SERIAL8                 (StandardColumnType.SERIAL8               ),   
    SET                     (StandardColumnType.ILLEGAL               ), 
    SMALLDATETIME           (StandardColumnType.DATETIME              ), 
    SMALLFLOAT              (StandardColumnType.FLOAT_INFORMIX        ),
    SMALLMONEY              (StandardColumnType.DECIMAL               ), 
    SMALLINT                (StandardColumnType.INT                   ),       
    SMALLSERIAL             (StandardColumnType.SERIAL                ),
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), 
    SYSNAME                 (StandardColumnType.ILLEGAL               ), 
    TEXT                    (StandardColumnType.TEXT                  ),
    TIME                    (StandardColumnType.DATETIME              ),  
    TIMEZ                   (StandardColumnType.DATETIME              ), 
    TIMESTAMP               (StandardColumnType.DATETIME              ), 
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.DATETIME              ), 
    TIMESTAMP_WITH_ZONE          (StandardColumnType.DATETIME              ), 
    TSQUERY                 (StandardColumnType.ILLEGAL               ), 
    TSVECTOR                (StandardColumnType.ILLEGAL               ), 
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
    UUID                    (StandardColumnType.ILLEGAL               ), 
    UROWID                  (StandardColumnType.ILLEGAL               ), 
    VARBIT                  (StandardColumnType.BYTEA                 ), 
    TINYBLOB                (StandardColumnType.BYTE                  ), 
    TINYINT                 (StandardColumnType.INT                   ),      
    TINYTEXT                (StandardColumnType.TEXT                  ), 
    VARBINARY               (StandardColumnType.BYTE                  ),      
    VARCHAR                 (StandardColumnType.VARCHAR               ),
    LVARCHAR                (StandardColumnType.LVARCHAR              ),          
    VARCHAR2                (StandardColumnType.VARCHAR               ),
    XML                     (StandardColumnType.TEXT                  ),
    YEAR                    (StandardColumnType.DATETIME              ); 
    private final TypeMetadata standard;
    private InformixColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}
