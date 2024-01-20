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


package org.anyline.data.jdbc.tdengine;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.metadata.type.init.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum TDengineColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.ILLEGAL             ),
    BINARY_DOUBLE           (StandardColumnType.ILLEGAL             ), 
    BINARY_FLOAT            (StandardColumnType.ILLEGAL             ), 
    BIGINT                  (StandardColumnType.BIGINT              ),   
    BIGSERIAL               (StandardColumnType.BIGINT              ),
    BINARY                  (StandardColumnType.BINARY              ),
    BIT                     (StandardColumnType.BOOL                ),
    BLOB                    (StandardColumnType.ILLEGAL             ),
    BOOL                    (StandardColumnType.BOOL                ),        
    BOX                     (StandardColumnType.ILLEGAL             ), 
    BYTEA                   (StandardColumnType.ILLEGAL             ), 
    CHAR                    (StandardColumnType.NCHAR               ),
    CIDR                    (StandardColumnType.ILLEGAL             ), 
    CIRCLE                  (StandardColumnType.ILLEGAL             ), 
    CLOB                    (StandardColumnType.ILLEGAL             ), 
    DATE                    (StandardColumnType.TIMESTAMP           ), 
    DATETIME                (StandardColumnType.TIMESTAMP           ), 
    DATETIME2               (StandardColumnType.TIMESTAMP           ), 
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP           ), 
    DECIMAL                 (StandardColumnType.DOUBLE              ), 
    DOUBLE                  (StandardColumnType.DOUBLE              ),   
    ENUM                    (StandardColumnType.ILLEGAL             ), 
    FLOAT                   (StandardColumnType.FLOAT_MySQL         ), 
    FLOAT4                  (StandardColumnType.FLOAT_MySQL         ), 
    FLOAT8                  (StandardColumnType.DOUBLE              ), 
    GEOGRAPHY               (StandardColumnType.ILLEGAL             ), //    
    GEOMETRY                (StandardColumnType.ILLEGAL             ),
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL             ),
    HIERARCHYID             (StandardColumnType.ILLEGAL             ), 
    IMAGE                   (StandardColumnType.ILLEGAL             ), 
    INET                    (StandardColumnType.ILLEGAL             ), 
    INTERVAL                (StandardColumnType.ILLEGAL             ), 
    INT                     (StandardColumnType.INT                 ),
    INT2                    (StandardColumnType.INT                 ), 
    INT4                    (StandardColumnType.INT                 ), 
    INT8                    (StandardColumnType.INT                 ), 
    INTEGER                 (StandardColumnType.INT                 ),
    JSON                    (StandardColumnType.JSON                ),
    JSONB                   (StandardColumnType.BLOB                ), 
    LINE                    (StandardColumnType.ILLEGAL               ), 
    LONG                    (StandardColumnType.INT                 ), 
    LONGBLOB                (StandardColumnType.ILLEGAL             ),
    LONGTEXT                (StandardColumnType.NCHAR               ),
    LSEG                    (StandardColumnType.ILLEGAL             ), 
    MACADDR                 (StandardColumnType.ILLEGAL             ), 
    MONEY                   (StandardColumnType.DOUBLE              ), 
    NUMBER                  (StandardColumnType.DOUBLE              ), 
    NCHAR                   (StandardColumnType.NCHAR               ), 
    NCLOB                   (StandardColumnType.NCHAR               ), 
    NTEXT                   (StandardColumnType.NCHAR               ), 
    NVARCHAR                (StandardColumnType.NCHAR               ), 
    NVARCHAR2               (StandardColumnType.NCHAR               ), 
    PATH                    (StandardColumnType.ILLEGAL             ), 
    MEDIUMBLOB              (StandardColumnType.ILLEGAL             ), 
    MEDIUMINT               (StandardColumnType.ILLEGAL             ), 
    MEDIUMTEXT              (StandardColumnType.NCHAR               ), 
    MULTILINESTRING         (StandardColumnType.ILLEGAL             ), 
    MULTIPOINT              (StandardColumnType.ILLEGAL             ), 
    MULTIPOLYGON            (StandardColumnType.ILLEGAL             ), 
    NUMERIC                 (StandardColumnType.DOUBLE              ),
    POINT                   (StandardColumnType.ILLEGAL               ), 
    POLYGON                 (StandardColumnType.ILLEGAL               ), 
    REAL                    (StandardColumnType.DOUBLE              ),
    RAW                     (StandardColumnType.ILLEGAL             ), 
    ROWID                   (StandardColumnType.ILLEGAL             ), 
    SERIAL                  (StandardColumnType.INT                 ),
    SERIAL2                 (StandardColumnType.INT                 ),
    SERIAL4                 (StandardColumnType.INT                 ),
    SERIAL8                 (StandardColumnType.INT                 ),
    SET                     (StandardColumnType.ILLEGAL             ), 
    SMALLDATETIME           (StandardColumnType.TIMESTAMP           ), 
    SMALLMONEY              (StandardColumnType.DECIMAL             ), 
    SMALLINT                (StandardColumnType.INT                 ),
    SMALLSERIAL             (StandardColumnType.INT                 ),
    SQL_VARIANT             (StandardColumnType.ILLEGAL             ), 
    SYSNAME                 (StandardColumnType.ILLEGAL             ), 
    TEXT                    (StandardColumnType.NCHAR               ),
    TIME                    (StandardColumnType.TIMESTAMP           ),
    TIMEZ                   (StandardColumnType.TIMESTAMP           ), 
    TIMESTAMP               (StandardColumnType.TIMESTAMP           ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP           ), 
    TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP           ), 
    TSQUERY                 (StandardColumnType.ILLEGAL             ), 
    TSVECTOR                (StandardColumnType.ILLEGAL             ), 
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL             ), 
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL             ), 
    UUID                    (StandardColumnType.ILLEGAL             ), 
    UROWID                  (StandardColumnType.ILLEGAL             ), 
    VARBIT                  (StandardColumnType.ILLEGAL             ), 
    TINYBLOB                (StandardColumnType.ILLEGAL             ), 
    TINYINT                 (StandardColumnType.INT                 ),
    TINYTEXT                (StandardColumnType.NCHAR               ), 
    VARBINARY               (StandardColumnType.BLOB                ), 
    VARCHAR                 (StandardColumnType.NCHAR               ), 
    VARCHAR2                (StandardColumnType.NCHAR               ),
    XML                     (StandardColumnType.NCHAR               ),
    YEAR                    (StandardColumnType.INT                 ); 
    private final TypeMetadata standard;
    private TDengineColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }

}
