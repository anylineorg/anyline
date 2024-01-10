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


package org.anyline.data.jdbc.mariadb;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum MariaColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL               ), 
        BINARY_DOUBLE           (StandardColumnType.DOUBLE                ), 
        BINARY_FLOAT            (StandardColumnType.FLOAT_MySQL           ), 
        BIGINT                  (StandardColumnType.BIGINT                ), 
        BIGSERIAL               (StandardColumnType.BIGINT                ),
        BINARY                  (StandardColumnType.BINARY                ), 
        BIT                     (StandardColumnType.BIT                   ), 
        BLOB                    (StandardColumnType.BLOB                  ), 
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
        ENUM                    (StandardColumnType.ENUM                  ), 
        FLOAT                   (StandardColumnType.FLOAT_MySQL           ),
        FLOAT4                  (StandardColumnType.FLOAT_MySQL           ), 
        FLOAT8                  (StandardColumnType.FLOAT_MySQL           ), 
        GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
        GEOMETRY                (StandardColumnType.GEOMETRY              ),
        GEOMETRYCOLLECTION      (StandardColumnType.GEOMETRYCOLLECTION    ),
        HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
        IMAGE                   (StandardColumnType.BLOB                  ), 
        INET                    (StandardColumnType.ILLEGAL               ), 
        INTERVAL                (StandardColumnType.ILLEGAL               ), 
        INT                     (StandardColumnType.INT                   ), 
        INT2                    (StandardColumnType.INT                   ), 
        INT4                    (StandardColumnType.INT                   ), 
        INT8                    (StandardColumnType.BIGINT                ), 
        INTEGER                 (StandardColumnType.INT                   ), 
        JSON                    (StandardColumnType.JSON                  ), 
        JSONB                   (StandardColumnType.BLOB                  ), 
        LINE                    (StandardColumnType.LINESTRING            ), 
        LINESTRING              (StandardColumnType.LINESTRING            ),
        LONG                    (StandardColumnType.BIGINT                ), 
        LONGBLOB                (StandardColumnType.VARBINARY             ),
        LONGTEXT                (StandardColumnType.LONGTEXT              ),
        LSEG                    (StandardColumnType.ILLEGAL               ), 
        MACADDR                 (StandardColumnType.ILLEGAL               ), 
        MONEY                   (StandardColumnType.DECIMAL               ), 
        NUMBER                  (StandardColumnType.NUMERIC               ), 
        NCHAR                   (StandardColumnType.VARCHAR               ), 
        NCLOB                   (StandardColumnType.TEXT                  ), 
        NTEXT                   (StandardColumnType.TEXT                  ), 
        NVARCHAR                (StandardColumnType.VARCHAR               ), 
        NVARCHAR2               (StandardColumnType.VARCHAR               ), 
        PATH                    (StandardColumnType.ILLEGAL               ), 
        MEDIUMBLOB              (StandardColumnType.MEDIUMBLOB            ), 
        MEDIUMINT               (StandardColumnType.MEDIUMINT             ), 
        MEDIUMTEXT              (StandardColumnType.MEDIUMTEXT            ), 
        MULTILINESTRING         (StandardColumnType.MULTILINESTRING       ), 
        MULTIPOINT              (StandardColumnType.MULTIPOINT            ), 
        MULTIPOLYGON            (StandardColumnType.MULTIPOLYGON          ), 
        NUMERIC                 (StandardColumnType.NUMERIC               ),  
        POINT                   (StandardColumnType.POINT                 ), 
        GEOGRAPHY_POINT         (StandardColumnType.POINT                 ),
        POLYGON                 (StandardColumnType.POLYGON               ), 
        REAL                    (StandardColumnType.REAL                  ), 
        RAW                     (StandardColumnType.ILLEGAL               ), 
        ROWID                   (StandardColumnType.ILLEGAL               ), 
        SERIAL                  (StandardColumnType.TINYINT               ),
        SERIAL2                 (StandardColumnType.TINYINT               ),
        SERIAL4                 (StandardColumnType.INT                   ),
        SERIAL8                 (StandardColumnType.BIGINT                ),
        SET                     (StandardColumnType.SET                   ), 
        SMALLDATETIME           (StandardColumnType.DATETIME              ), 
        SMALLMONEY              (StandardColumnType.DECIMAL               ), 
        SMALLINT                (StandardColumnType.TINYINT               ), 
        SMALLSERIAL             (StandardColumnType.TINYINT               ),
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ),
        STRING                  (StandardColumnType.VARCHAR                ),
        SYSNAME                 (StandardColumnType.ILLEGAL               ),
        TEXT                    (StandardColumnType.TEXT                  ),
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
        VARBIT                  (StandardColumnType.VARBINARY             ), 
        TINYBLOB                (StandardColumnType.TINYBLOB              ), 
        TINYINT                 (StandardColumnType.TINYINT               ), 
        TINYTEXT                (StandardColumnType.TINYTEXT              ), 
        VARBINARY               (StandardColumnType.VARBINARY             ), 
        VARCHAR                 (StandardColumnType.VARCHAR               ), 
        VARCHAR2                (StandardColumnType.VARCHAR               ),
        XML                     (StandardColumnType.TEXT                  ),
        YEAR                    (StandardColumnType.DATE                  ); 
        private final TypeMetadata standard;
        MariaColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
