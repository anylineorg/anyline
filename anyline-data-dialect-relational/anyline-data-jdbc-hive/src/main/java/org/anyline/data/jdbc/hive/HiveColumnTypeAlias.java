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


package org.anyline.data.jdbc.hive;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum HiveColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardTypeMetadata.ILLEGAL               ),
        BINARY_DOUBLE           (StandardTypeMetadata.DOUBLE                ),
        BINARY_FLOAT            (StandardTypeMetadata.FLOAT_MySQL           ),
        BIGINT                  (StandardTypeMetadata.BIGINT                ),
        BIGSERIAL               (StandardTypeMetadata.BIGINT                ),
        BINARY                  (StandardTypeMetadata.BINARY                ),
        BIT                     (StandardTypeMetadata.BIT                   ),
        BLOB                    (StandardTypeMetadata.BLOB                  ),
        BOOL                    (StandardTypeMetadata.BIT                   ),
        BOX                     (StandardTypeMetadata.ILLEGAL               ),
        BYTEA                   (StandardTypeMetadata.VARBINARY             ),
        CHAR                    (StandardTypeMetadata.CHAR                  ),
        CIDR                    (StandardTypeMetadata.ILLEGAL               ),
        CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
        CLOB                    (StandardTypeMetadata.TEXT                  ),
        DATE                    (StandardTypeMetadata.DATE                  ),
        DATETIME                (StandardTypeMetadata.DATETIME              ),
        DATETIME2               (StandardTypeMetadata.DATETIME2             ),
        DATETIMEOFFSET          (StandardTypeMetadata.DATETIMEOFFSET        ),
        DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
        DOUBLE                  (StandardTypeMetadata.DOUBLE                ),
        ENUM                    (StandardTypeMetadata.ENUM                  ),
        FLOAT                   (StandardTypeMetadata.FLOAT_MySQL           ),
        FLOAT4                  (StandardTypeMetadata.FLOAT_MySQL           ),
        FLOAT8                  (StandardTypeMetadata.FLOAT_MySQL           ),
        GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
        GEOMETRY                (StandardTypeMetadata.GEOMETRY              ),
        GEOMETRYCOLLECTION      (StandardTypeMetadata.GEOMETRYCOLLECTION    ),
        HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
        IMAGE                   (StandardTypeMetadata.BLOB                  ),
        INET                    (StandardTypeMetadata.ILLEGAL               ),
        INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
        INT                     (StandardTypeMetadata.INT                   ),
        INT2                    (StandardTypeMetadata.INT                   ),
        INT4                    (StandardTypeMetadata.INT                   ),
        INT8                    (StandardTypeMetadata.BIGINT                ),
        INTEGER                 (StandardTypeMetadata.INT                   ),
        JSON                    (StandardTypeMetadata.JSON                  ),
        JSONB                   (StandardTypeMetadata.BLOB                  ),
        LINE                    (StandardTypeMetadata.LINESTRING            ),
        LINESTRING              (StandardTypeMetadata.LINESTRING            ),
        LONG                    (StandardTypeMetadata.BIGINT                ),
        LONGBLOB                (StandardTypeMetadata.VARBINARY             ),
        LONGTEXT                (StandardTypeMetadata.LONGTEXT              ),
        LSEG                    (StandardTypeMetadata.ILLEGAL               ),
        MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
        MONEY                   (StandardTypeMetadata.DECIMAL               ),
        NUMBER                  (StandardTypeMetadata.NUMERIC               ),
        NCHAR                   (StandardTypeMetadata.VARCHAR               ),
        NCLOB                   (StandardTypeMetadata.TEXT                  ),
        NTEXT                   (StandardTypeMetadata.TEXT                  ),
        NVARCHAR                (StandardTypeMetadata.VARCHAR               ),
        NVARCHAR2               (StandardTypeMetadata.VARCHAR               ),
        PATH                    (StandardTypeMetadata.ILLEGAL               ),
        MEDIUMBLOB              (StandardTypeMetadata.MEDIUMBLOB            ),
        MEDIUMINT               (StandardTypeMetadata.MEDIUMINT             ),
        MEDIUMTEXT              (StandardTypeMetadata.MEDIUMTEXT            ),
        MULTILINESTRING         (StandardTypeMetadata.MULTILINESTRING       ),
        MULTIPOINT              (StandardTypeMetadata.MULTIPOINT            ),
        MULTIPOLYGON            (StandardTypeMetadata.MULTIPOLYGON          ),
        NUMERIC                 (StandardTypeMetadata.NUMERIC               ),
        POINT                   (StandardTypeMetadata.POINT                 ),
        GEOGRAPHY_POINT         (StandardTypeMetadata.POINT                 ),
        POLYGON                 (StandardTypeMetadata.POLYGON               ),
        REAL                    (StandardTypeMetadata.REAL                  ),
        RAW                     (StandardTypeMetadata.ILLEGAL               ),
        ROWID                   (StandardTypeMetadata.ILLEGAL               ),
        SERIAL                  (StandardTypeMetadata.TINYINT               ),
        SERIAL2                 (StandardTypeMetadata.TINYINT               ),
        SERIAL4                 (StandardTypeMetadata.INT                   ),
        SERIAL8                 (StandardTypeMetadata.BIGINT                ),
        SET                     (StandardTypeMetadata.SET                   ),
        SMALLDATETIME           (StandardTypeMetadata.DATETIME              ),
        SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
        SMALLINT                (StandardTypeMetadata.TINYINT               ),
        SMALLSERIAL             (StandardTypeMetadata.TINYINT               ),
        SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
        STRING                  (StandardTypeMetadata.VARCHAR                ),
        SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
        TEXT                    (StandardTypeMetadata.TEXT                  ),
        TIME                    (StandardTypeMetadata.TIME                  ),
        TIMEZ                   (StandardTypeMetadata.TIME                  ),
        TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP             ),
        TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP             ),
        TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
        TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
        TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
        UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
        UUID                    (StandardTypeMetadata.ILLEGAL               ),
        UROWID                  (StandardTypeMetadata.ILLEGAL               ),
        VARBIT                  (StandardTypeMetadata.VARBINARY             ),
        TINYBLOB                (StandardTypeMetadata.TINYBLOB              ),
        TINYINT                 (StandardTypeMetadata.TINYINT               ),
        TINYTEXT                (StandardTypeMetadata.TINYTEXT              ),
        VARBINARY               (StandardTypeMetadata.VARBINARY             ),
        VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
        VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
        XML                     (StandardTypeMetadata.TEXT                  ),
        YEAR                    (StandardTypeMetadata.DATE                  );
        private final TypeMetadata standard;
        HiveColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
