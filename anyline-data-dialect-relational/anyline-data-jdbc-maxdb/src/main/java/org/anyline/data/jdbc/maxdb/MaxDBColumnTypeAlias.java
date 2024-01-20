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
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum MaxDBColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardTypeMetadata.ILLEGAL               ),
        BINARY_DOUBLE           (StandardTypeMetadata.DOUBLE                ),
        BINARY_FLOAT            (StandardTypeMetadata.FLOAT                ),
        BIGINT                  (StandardTypeMetadata.FLOAT                ),
        BIGSERIAL               (StandardTypeMetadata.FLOAT                ),
        BINARY                  (StandardTypeMetadata.BLOB                ),
        BIT                     (StandardTypeMetadata.BOOLEAN                   ),
        BLOB                    (StandardTypeMetadata.BLOB                  ),
        BOOL                    (StandardTypeMetadata.BOOLEAN                   ),
        BOX                     (StandardTypeMetadata.ILLEGAL               ),
        BYTEA                   (StandardTypeMetadata.BLOB             ),
        CHAR                    (StandardTypeMetadata.CHAR                  ),
        CIDR                    (StandardTypeMetadata.ILLEGAL               ),
        CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
        CLOB                    (StandardTypeMetadata.CLOB                  ),
        DATE                    (StandardTypeMetadata.DATE                  ),
        DATETIME                (StandardTypeMetadata.TIMESTAMP              ),
        DATETIME2               (StandardTypeMetadata.TIMESTAMP             ),
        DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP        ),
        DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
        DOUBLE                  (StandardTypeMetadata.FLOAT                ),
        ENUM                    (StandardTypeMetadata.ILLEGAL                  ),
        FLOAT                   (StandardTypeMetadata.FLOAT           ),
        FLOAT4                  (StandardTypeMetadata.FLOAT           ),
        FLOAT8                  (StandardTypeMetadata.FLOAT           ),
        GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
        GEOMETRY                (StandardTypeMetadata.ILLEGAL              ),
        GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL    ),
        HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
        IMAGE                   (StandardTypeMetadata.BLOB                  ),
        INET                    (StandardTypeMetadata.ILLEGAL               ),
        INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
        INT                     (StandardTypeMetadata.INT                   ),
        INT2                    (StandardTypeMetadata.INT                   ),
        INT4                    (StandardTypeMetadata.INT                   ),
        INT8                    (StandardTypeMetadata.FLOAT                ),
        INTEGER                 (StandardTypeMetadata.INT                   ),
        JSON                    (StandardTypeMetadata.CLOB                  ),
        JSONB                   (StandardTypeMetadata.BLOB                  ),
        LINE                    (StandardTypeMetadata.ILLEGAL            ),
        LINESTRING              (StandardTypeMetadata.ILLEGAL            ),
        LONG                    (StandardTypeMetadata.LONG_TEXT                ),
        LONGBLOB                (StandardTypeMetadata.BLOB             ),
        LONGTEXT                (StandardTypeMetadata.CLOB              ),
        LSEG                    (StandardTypeMetadata.ILLEGAL               ),
        MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
        MONEY                   (StandardTypeMetadata.DECIMAL               ),
        NUMBER                  (StandardTypeMetadata.DECIMAL               ),
        NCHAR                   (StandardTypeMetadata.VARCHAR               ),
        NCLOB                   (StandardTypeMetadata.CLOB                  ),
        NTEXT                   (StandardTypeMetadata.CLOB                  ),
        NVARCHAR                (StandardTypeMetadata.VARCHAR               ),
        NVARCHAR2               (StandardTypeMetadata.VARCHAR               ),
        PATH                    (StandardTypeMetadata.ILLEGAL               ),
        MEDIUMBLOB              (StandardTypeMetadata.BLOB            ),
        MEDIUMINT               (StandardTypeMetadata.INT             ),
        MEDIUMTEXT              (StandardTypeMetadata.CLOB            ),
        MULTILINE               (StandardTypeMetadata.ILLEGAL       ),
        MULTILINESTRING         (StandardTypeMetadata.ILLEGAL       ),
        MULTIPOINT              (StandardTypeMetadata.ILLEGAL            ),
        MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL          ),
        NUMERIC                 (StandardTypeMetadata.NUMERIC               ),
        POINT                   (StandardTypeMetadata.ILLEGAL                 ),
        GEOGRAPHY_POINT         (StandardTypeMetadata.ILLEGAL                 ),
        POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
        REAL                    (StandardTypeMetadata.REAL                  ),
        RAW                     (StandardTypeMetadata.ILLEGAL               ),
        ROWID                   (StandardTypeMetadata.ILLEGAL               ),
        SERIAL                  (StandardTypeMetadata.INT               ),
        SERIAL2                 (StandardTypeMetadata.INT               ),
        SERIAL4                 (StandardTypeMetadata.INT                   ),
        SERIAL8                 (StandardTypeMetadata.FLOAT                ),
        SET                     (StandardTypeMetadata.INT                   ),
        SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP             ),
        SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
        SMALLINT                (StandardTypeMetadata.SMALLINT               ),
        SMALLSERIAL             (StandardTypeMetadata.SMALLINT               ),
        SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
        STRING                  (StandardTypeMetadata.VARCHAR                ),
        SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
        TEXT                    (StandardTypeMetadata.CLOB                  ),
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
        VARBIT                  (StandardTypeMetadata.BINARY             ),
        TINYBLOB                (StandardTypeMetadata.BLOB              ),
        TINYINT                 (StandardTypeMetadata.SMALLINT               ),
        TINYTEXT                (StandardTypeMetadata.CLOB              ),
        VARBINARY               (StandardTypeMetadata.BINARY             ),
        VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
        VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
        XML                     (StandardTypeMetadata.CLOB                  ),
        YEAR                    (StandardTypeMetadata.DATE                  );
        private final TypeMetadata standard;
        MaxDBColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
