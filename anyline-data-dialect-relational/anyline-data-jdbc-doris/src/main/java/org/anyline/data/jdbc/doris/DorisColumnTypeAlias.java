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


package org.anyline.data.jdbc.doris;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum DorisColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL               ), 
        BINARY_DOUBLE           (StandardColumnType.DOUBLE                ), 
        BINARY_FLOAT            (StandardColumnType.FLOAT                 ), 
        BIGINT                  (StandardColumnType.BIGINT                ), 
        BIGSERIAL               (StandardColumnType.BIGINT                ),
        BINARY                  (StandardColumnType.BITMAP                ),
        BIT                     (StandardColumnType.TINYINT                   ),
        BLOB                    (StandardColumnType.BITMAP                  ),
        BOOL                    (StandardColumnType.TINYINT                   ),
        BOX                     (StandardColumnType.ILLEGAL               ), 
        BYTEA                   (StandardColumnType.BITMAP             ),
        CHAR                    (StandardColumnType.CHAR                  ), 
        CIDR                    (StandardColumnType.ILLEGAL               ), 
        CIRCLE                  (StandardColumnType.ILLEGAL               ), 
        CLOB                    (StandardColumnType.VARCHAR                  ),
        DATE                    (StandardColumnType.DATE                  ), 
        DATETIME                (StandardColumnType.DATETIME              ), 
        DATETIME2               (StandardColumnType.DATETIME             ),
        DATETIMEOFFSET          (StandardColumnType.DATETIME        ),
        DECIMAL                 (StandardColumnType.DECIMAL               ), 
        DOUBLE                  (StandardColumnType.DOUBLE                ), 
        ENUM                    (StandardColumnType.ILLEGAL                  ),
        FLOAT                   (StandardColumnType.FLOAT           ),
        FLOAT4                  (StandardColumnType.FLOAT           ),
        FLOAT8                  (StandardColumnType.FLOAT           ),
        GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
        GEOMETRY                (StandardColumnType.ILLEGAL              ),
        GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL    ),
        HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
        IMAGE                   (StandardColumnType.BITMAP                  ),
        INET                    (StandardColumnType.ILLEGAL               ), 
        INTERVAL                (StandardColumnType.ILLEGAL               ), 
        INT                     (StandardColumnType.INT                   ), 
        INT2                    (StandardColumnType.INT                   ), 
        INT4                    (StandardColumnType.INT                   ), 
        INT8                    (StandardColumnType.BIGINT                ), 
        INTEGER                 (StandardColumnType.INT                   ), 
        JSON                    (StandardColumnType.VARCHAR                  ),
        JSONB                   (StandardColumnType.BITMAP                  ),
        LINE                    (StandardColumnType.ILLEGAL            ),
        LINESTRING              (StandardColumnType.ILLEGAL            ),
        LONG                    (StandardColumnType.BIGINT                ), 
        LONGBLOB                (StandardColumnType.BITMAP             ),
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
        MEDIUMBLOB              (StandardColumnType.BITMAP            ),
        MEDIUMINT               (StandardColumnType.INT             ),
        MEDIUMTEXT              (StandardColumnType.INT            ),
        MULTILINE               (StandardColumnType.ILLEGAL       ),
        MULTILINESTRING         (StandardColumnType.ILLEGAL       ),
        MULTIPOINT              (StandardColumnType.ILLEGAL            ),
        MULTIPOLYGON            (StandardColumnType.ILLEGAL          ),
        NUMERIC                 (StandardColumnType.DECIMAL               ),
        POINT                   (StandardColumnType.ILLEGAL                 ),
        GEOGRAPHY_POINT         (StandardColumnType.ILLEGAL                 ),
        POLYGON                 (StandardColumnType.ILLEGAL               ),
        REAL                    (StandardColumnType.ILLEGAL                  ),
        RAW                     (StandardColumnType.ILLEGAL               ), 
        ROWID                   (StandardColumnType.ILLEGAL               ), 
        SERIAL                  (StandardColumnType.INT               ),
        SERIAL2                 (StandardColumnType.INT               ),
        SERIAL4                 (StandardColumnType.INT                   ),
        SERIAL8                 (StandardColumnType.BIGINT                ),
        SET                     (StandardColumnType.ILLEGAL                   ),
        SMALLDATETIME           (StandardColumnType.DATETIME              ), 
        SMALLMONEY              (StandardColumnType.DECIMAL               ), 
        SMALLINT                (StandardColumnType.TINYINT               ), 
        SMALLSERIAL             (StandardColumnType.TINYINT               ),
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ), 
        SYSNAME                 (StandardColumnType.ILLEGAL               ), 
        TEXT                    (StandardColumnType.VARCHAR                  ),
        TIME                    (StandardColumnType.DATETIME                  ),
        TIMEZ                   (StandardColumnType.DATETIME                  ),
        TIMESTAMP               (StandardColumnType.DATETIME             ),
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.DATETIME             ),
        TIMESTAMP_WITH_ZONE          (StandardColumnType.DATETIME             ),
        TSQUERY                 (StandardColumnType.ILLEGAL               ), 
        TSVECTOR                (StandardColumnType.ILLEGAL               ), 
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
        UUID                    (StandardColumnType.ILLEGAL               ), 
        UROWID                  (StandardColumnType.ILLEGAL               ), 
        VARBIT                  (StandardColumnType.BITMAP             ),
        TINYBLOB                (StandardColumnType.BITMAP              ),
        TINYINT                 (StandardColumnType.TINYINT               ), 
        TINYTEXT                (StandardColumnType.TINYTEXT              ), 
        VARBINARY               (StandardColumnType.BITMAP             ),
        VARCHAR                 (StandardColumnType.VARCHAR               ), 
        VARCHAR2                (StandardColumnType.VARCHAR               ), 
        XML                     (StandardColumnType.VARCHAR                  ),
        YEAR                    (StandardColumnType.DATE                  );
        private final TypeMetadata standard;
        DorisColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
