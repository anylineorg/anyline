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


package org.anyline.data.jdbc.ignite;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum IgniteColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL               ), 
        BINARY_DOUBLE           (StandardColumnType.ILLEGAL                ), 
        BINARY_FLOAT            (StandardColumnType.ILLEGAL           ), 
        BIGINT                  (StandardColumnType.BIGINT                ), 
        BIGSERIAL               (StandardColumnType.BIGINT                ),
        BINARY                  (StandardColumnType.BINARY                ), 
        BIT                     (StandardColumnType.ILLEGAL                   ), 
        BLOB                    (StandardColumnType.BINARY                  ), 
        BOOL                    (StandardColumnType.BOOLEAN                   ), 
        BOX                     (StandardColumnType.ILLEGAL               ), 
        BYTEA                   (StandardColumnType.ILLEGAL             ), 
        CHAR                    (StandardColumnType.CHAR                  ), 
        CIDR                    (StandardColumnType.ILLEGAL               ), 
        CIRCLE                  (StandardColumnType.ILLEGAL               ), 
        CLOB                    (StandardColumnType.VARCHAR                  ), 
        DATE                    (StandardColumnType.DATE                  ), 
        DATETIME                (StandardColumnType.TIMESTAMP              ), 
        DATETIME2               (StandardColumnType.TIMESTAMP             ), 
        DATETIMEOFFSET          (StandardColumnType.TIMESTAMP        ), 
        DECIMAL                 (StandardColumnType.DECIMAL               ), 
        DOUBLE                  (StandardColumnType.DOUBLE                ), 
        ENUM                    (StandardColumnType.ILLEGAL                  ), 
        FLOAT                   (StandardColumnType.DOUBLE           ),
        FLOAT4                  (StandardColumnType.DOUBLE           ), 
        FLOAT8                  (StandardColumnType.DOUBLE           ), 
        GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
        GEOMETRY                (StandardColumnType.GEOMETRY              ),
        GEOMETRYCOLLECTION      (StandardColumnType.GEOMETRYCOLLECTION    ),
        HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
        IMAGE                   (StandardColumnType.BINARY                  ), 
        INET                    (StandardColumnType.ILLEGAL               ), 
        INTERVAL                (StandardColumnType.ILLEGAL               ), 
        INT                     (StandardColumnType.INT                   ), 
        INT2                    (StandardColumnType.INT                   ), 
        INT4                    (StandardColumnType.INT                   ), 
        INT8                    (StandardColumnType.BIGINT                ), 
        INTEGER                 (StandardColumnType.INT                   ), 
        JSON                    (StandardColumnType.VARCHAR                  ), 
        JSONB                   (StandardColumnType.BINARY                  ), 
        LINE                    (StandardColumnType.LINESTRING            ), 
        LINESTRING              (StandardColumnType.LINESTRING            ),
        LONG                    (StandardColumnType.BIGINT                ), 
        LONGBLOB                (StandardColumnType.BINARY             ),
        LONGTEXT                (StandardColumnType.VARCHAR              ),
        LSEG                    (StandardColumnType.ILLEGAL               ), 
        MACADDR                 (StandardColumnType.ILLEGAL               ), 
        MONEY                   (StandardColumnType.DECIMAL               ), 
        NUMBER                  (StandardColumnType.DECIMAL               ), 
        NCHAR                   (StandardColumnType.VARCHAR               ), 
        NCLOB                   (StandardColumnType.VARCHAR                  ), 
        NTEXT                   (StandardColumnType.VARCHAR                  ), 
        NVARCHAR                (StandardColumnType.VARCHAR               ), 
        NVARCHAR2               (StandardColumnType.VARCHAR               ), 
        PATH                    (StandardColumnType.ILLEGAL               ), 
        MEDIUMBLOB              (StandardColumnType.BINARY            ), 
        MEDIUMINT               (StandardColumnType.SMALLINT             ), 
        MEDIUMTEXT              (StandardColumnType.VARCHAR            ), 
        MULTILINE               (StandardColumnType.MULTILINESTRING       ), 
        MULTILINESTRING         (StandardColumnType.MULTILINESTRING       ), 
        MULTIPOINT              (StandardColumnType.MULTIPOINT            ), 
        MULTIPOLYGON            (StandardColumnType.MULTIPOLYGON          ), 
        NUMERIC                 (StandardColumnType.DECIMAL               ), 
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
        SET                     (StandardColumnType.ILLEGAL                   ), 
        SMALLDATETIME           (StandardColumnType.TIMESTAMP              ), 
        SMALLMONEY              (StandardColumnType.DECIMAL               ), 
        SMALLINT                (StandardColumnType.TINYINT               ), 
        SMALLSERIAL             (StandardColumnType.TINYINT               ),
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ), 
        SYSNAME                 (StandardColumnType.ILLEGAL               ), 
        TEXT                    (StandardColumnType.VARCHAR                  ), 
        TIME                    (StandardColumnType.TIME                  ),  
        TIMEZ                   (StandardColumnType.TIME                  ), 
        TIMESTAMP               (StandardColumnType.TIMESTAMP             ), 
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), 
        TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP             ), 
        TSQUERY                 (StandardColumnType.ILLEGAL               ), 
        TSVECTOR                (StandardColumnType.ILLEGAL               ), 
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
        UUID                    (StandardColumnType.UUID               ), 
        UROWID                  (StandardColumnType.ILLEGAL               ),
        VARBIT                  (StandardColumnType.BINARY             ), 
        TINYBLOB                (StandardColumnType.BINARY              ), 
        TINYINT                 (StandardColumnType.TINYINT               ), 
        TINYTEXT                (StandardColumnType.VARCHAR              ), 
        VARBINARY               (StandardColumnType.BINARY             ), 
        VARCHAR                 (StandardColumnType.VARCHAR               ), 
        VARCHAR2                (StandardColumnType.VARCHAR               ),
        XML                     (StandardColumnType.VARCHAR                  ),
        YEAR                    (StandardColumnType.DATE                  ); 
        private final TypeMetadata standard;
        IgniteColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
