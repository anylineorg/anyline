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
import org.anyline.metadata.type.TypeMetadata;

public enum MaxDBColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL               ), 
        BINARY_DOUBLE           (StandardColumnType.DOUBLE                ), 
        BINARY_FLOAT            (StandardColumnType.FLOAT                ), 
        BIGINT                  (StandardColumnType.FLOAT                ), 
        BIGSERIAL               (StandardColumnType.FLOAT                ),
        BINARY                  (StandardColumnType.BLOB                ), 
        BIT                     (StandardColumnType.BOOLEAN                   ), 
        BLOB                    (StandardColumnType.BLOB                  ), 
        BOOL                    (StandardColumnType.BOOLEAN                   ), 
        BOX                     (StandardColumnType.ILLEGAL               ), 
        BYTEA                   (StandardColumnType.BLOB             ), 
        CHAR                    (StandardColumnType.CHAR                  ), 
        CIDR                    (StandardColumnType.ILLEGAL               ), 
        CIRCLE                  (StandardColumnType.ILLEGAL               ), 
        CLOB                    (StandardColumnType.CLOB                  ), 
        DATE                    (StandardColumnType.DATE                  ), 
        DATETIME                (StandardColumnType.TIMESTAMP              ), 
        DATETIME2               (StandardColumnType.TIMESTAMP             ), 
        DATETIMEOFFSET          (StandardColumnType.TIMESTAMP        ), 
        DECIMAL                 (StandardColumnType.DECIMAL               ), 
        DOUBLE                  (StandardColumnType.FLOAT                ), 
        ENUM                    (StandardColumnType.ILLEGAL                  ), 
        FLOAT                   (StandardColumnType.FLOAT           ),
        FLOAT4                  (StandardColumnType.FLOAT           ), 
        FLOAT8                  (StandardColumnType.FLOAT           ), 
        GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
        GEOMETRY                (StandardColumnType.ILLEGAL              ),
        GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL    ),
        HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
        IMAGE                   (StandardColumnType.BLOB                  ), 
        INET                    (StandardColumnType.ILLEGAL               ), 
        INTERVAL                (StandardColumnType.ILLEGAL               ), 
        INT                     (StandardColumnType.INT                   ), 
        INT2                    (StandardColumnType.INT                   ), 
        INT4                    (StandardColumnType.INT                   ), 
        INT8                    (StandardColumnType.FLOAT                ), 
        INTEGER                 (StandardColumnType.INT                   ), 
        JSON                    (StandardColumnType.CLOB                  ),  
        JSONB                   (StandardColumnType.BLOB                  ), 
        LINE                    (StandardColumnType.ILLEGAL            ), 
        LINESTRING              (StandardColumnType.ILLEGAL            ),
        LONG                    (StandardColumnType.LONG_TEXT                ), 
        LONGBLOB                (StandardColumnType.BLOB             ),
        LONGTEXT                (StandardColumnType.CLOB              ),
        LSEG                    (StandardColumnType.ILLEGAL               ), 
        MACADDR                 (StandardColumnType.ILLEGAL               ), 
        MONEY                   (StandardColumnType.DECIMAL               ), 
        NUMBER                  (StandardColumnType.DECIMAL               ), 
        NCHAR                   (StandardColumnType.VARCHAR               ), 
        NCLOB                   (StandardColumnType.CLOB                  ), 
        NTEXT                   (StandardColumnType.CLOB                  ), 
        NVARCHAR                (StandardColumnType.VARCHAR               ), 
        NVARCHAR2               (StandardColumnType.VARCHAR               ), 
        PATH                    (StandardColumnType.ILLEGAL               ), 
        MEDIUMBLOB              (StandardColumnType.BLOB            ), 
        MEDIUMINT               (StandardColumnType.INT             ), 
        MEDIUMTEXT              (StandardColumnType.CLOB            ), 
        MULTILINE               (StandardColumnType.ILLEGAL       ), 
        MULTILINESTRING         (StandardColumnType.ILLEGAL       ), 
        MULTIPOINT              (StandardColumnType.ILLEGAL            ), 
        MULTIPOLYGON            (StandardColumnType.ILLEGAL          ), 
        NUMERIC                 (StandardColumnType.NUMERIC               ),  
        POINT                   (StandardColumnType.ILLEGAL                 ), 
        GEOGRAPHY_POINT         (StandardColumnType.ILLEGAL                 ),
        POLYGON                 (StandardColumnType.ILLEGAL               ), 
        REAL                    (StandardColumnType.REAL                  ), 
        RAW                     (StandardColumnType.ILLEGAL               ), 
        ROWID                   (StandardColumnType.ILLEGAL               ), 
        SERIAL                  (StandardColumnType.INT               ),
        SERIAL2                 (StandardColumnType.INT               ),
        SERIAL4                 (StandardColumnType.INT                   ),
        SERIAL8                 (StandardColumnType.FLOAT                ),
        SET                     (StandardColumnType.INT                   ), 
        SMALLDATETIME           (StandardColumnType.TIMESTAMP             ), 
        SMALLMONEY              (StandardColumnType.DECIMAL               ), 
        SMALLINT                (StandardColumnType.SMALLINT               ), 
        SMALLSERIAL             (StandardColumnType.SMALLINT               ),
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ),
        STRING                  (StandardColumnType.VARCHAR                ), 
        SYSNAME                 (StandardColumnType.ILLEGAL               ), 
        TEXT                    (StandardColumnType.CLOB                  ),  
        TIME                    (StandardColumnType.TIME                  ),  
        TIMEZ                   (StandardColumnType.TIME                  ), 
        TIMESTAMP               (StandardColumnType.TIMESTAMP             ), 
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), 
        TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP             ), 
        TSQUERY                 (StandardColumnType.ILLEGAL               ), 
        TSVECTOR                (StandardColumnType.ILLEGAL               ), 
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
        UUID                    (StandardColumnType.ILLEGAL               ), 
        UROWID                  (StandardColumnType.ILLEGAL               ), 
        VARBIT                  (StandardColumnType.BINARY             ), 
        TINYBLOB                (StandardColumnType.BLOB              ), 
        TINYINT                 (StandardColumnType.SMALLINT               ), 
        TINYTEXT                (StandardColumnType.CLOB              ), 
        VARBINARY               (StandardColumnType.BINARY             ), 
        VARCHAR                 (StandardColumnType.VARCHAR               ), 
        VARCHAR2                (StandardColumnType.VARCHAR               ), 
        XML                     (StandardColumnType.CLOB                  ),
        YEAR                    (StandardColumnType.DATE                  );  
        private final TypeMetadata standard;
        MaxDBColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
