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
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum DorisColumnTypeAlias implements ColumnTypeAlias {
        ARRAY                   (StandardTypeMetadata.ARRAY               ),
        AGG_STATE               (StandardTypeMetadata.AGG_STATE               ),
        BFILE                   (StandardTypeMetadata.ILLEGAL               ),
        BINARY_DOUBLE           (StandardTypeMetadata.DOUBLE                ),
        BINARY_FLOAT            (StandardTypeMetadata.FLOAT                 ),
        BIGINT                  (StandardTypeMetadata.BIGINT                ),
        BIGSERIAL               (StandardTypeMetadata.BIGINT                ),
        BINARY                  (StandardTypeMetadata.ILLEGAL                ),
        BIT                     (StandardTypeMetadata.TINYINT                   ),
        BLOB                    (StandardTypeMetadata.ILLEGAL                  ),
        BOOL                    (StandardTypeMetadata.BOOL                   ),
        BOX                     (StandardTypeMetadata.ILLEGAL               ),
        BYTEA                   (StandardTypeMetadata.ILLEGAL             ),
        CHAR                    (StandardTypeMetadata.CHAR                  ),
        CIDR                    (StandardTypeMetadata.ILLEGAL               ),
        CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
        CLOB                    (StandardTypeMetadata.VARCHAR                  ),
        DATE                    (StandardTypeMetadata.DATE                  ),
        DATETIME                (StandardTypeMetadata.DATETIME              ),
        DATETIME2               (StandardTypeMetadata.DATETIME             ),
        DATETIMEOFFSET          (StandardTypeMetadata.DATETIME        ),
        DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
        DOUBLE                  (StandardTypeMetadata.DOUBLE                ),
        ENUM                    (StandardTypeMetadata.ILLEGAL                  ),
        FLOAT                   (StandardTypeMetadata.FLOAT           ),
        FLOAT4                  (StandardTypeMetadata.FLOAT           ),
        FLOAT8                  (StandardTypeMetadata.FLOAT           ),
        GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
        GEOMETRY                (StandardTypeMetadata.ILLEGAL              ),
        GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL    ),
        HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
        HLL                     (StandardTypeMetadata.HLL               ),
        HYPERLOGLOG             (StandardTypeMetadata.HLL               ),
        IMAGE                   (StandardTypeMetadata.ILLEGAL                  ),
        INET                    (StandardTypeMetadata.ILLEGAL               ),
        INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
        INT                     (StandardTypeMetadata.INT                   ),
        INT2                    (StandardTypeMetadata.INT                   ),
        INT4                    (StandardTypeMetadata.INT                   ),
        INT8                    (StandardTypeMetadata.BIGINT                ),
        INTEGER                 (StandardTypeMetadata.INT                   ),
        JSON                    (StandardTypeMetadata.JSON                  ),
        JSONB                   (StandardTypeMetadata.ILLEGAL                  ),
        LINE                    (StandardTypeMetadata.ILLEGAL            ),
        LINESTRING              (StandardTypeMetadata.ILLEGAL            ),
        LONG                    (StandardTypeMetadata.BIGINT                ),
        LONGBLOB                (StandardTypeMetadata.ILLEGAL             ),
        LONGTEXT                (StandardTypeMetadata.VARCHAR              ),
        LSEG                    (StandardTypeMetadata.ILLEGAL               ),
        MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
        MONEY                   (StandardTypeMetadata.DECIMAL               ),
        NUMBER                  (StandardTypeMetadata.DECIMAL               ),
        NCHAR                   (StandardTypeMetadata.VARCHAR               ),
        NCLOB                   (StandardTypeMetadata.VARCHAR                  ),
        NTEXT                   (StandardTypeMetadata.VARCHAR                  ),
        NVARCHAR                (StandardTypeMetadata.VARCHAR               ),
        NVARCHAR2               (StandardTypeMetadata.VARCHAR               ),
        PATH                    (StandardTypeMetadata.ILLEGAL               ),
        MAP              (StandardTypeMetadata.MAP            ),
        MEDIUMBLOB              (StandardTypeMetadata.ILLEGAL            ),
        MEDIUMINT               (StandardTypeMetadata.INT             ),
        MEDIUMTEXT              (StandardTypeMetadata.INT            ),
        MULTILINE               (StandardTypeMetadata.ILLEGAL       ),
        MULTILINESTRING         (StandardTypeMetadata.ILLEGAL       ),
        MULTIPOINT              (StandardTypeMetadata.ILLEGAL            ),
        MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL          ),
        NUMERIC                 (StandardTypeMetadata.DECIMAL               ),
        POINT                   (StandardTypeMetadata.ILLEGAL                 ),
        GEOGRAPHY_POINT         (StandardTypeMetadata.ILLEGAL                 ),
        POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
        REAL                    (StandardTypeMetadata.ILLEGAL                  ),
        RAW                     (StandardTypeMetadata.ILLEGAL               ),
        ROWID                   (StandardTypeMetadata.ILLEGAL               ),
        SERIAL                  (StandardTypeMetadata.INT               ),
        SERIAL2                 (StandardTypeMetadata.INT               ),
        SERIAL4                 (StandardTypeMetadata.INT                   ),
        SERIAL8                 (StandardTypeMetadata.BIGINT                ),
        SET                     (StandardTypeMetadata.ILLEGAL                   ),
        SMALLDATETIME           (StandardTypeMetadata.DATETIME              ),
        SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
        SMALLINT                (StandardTypeMetadata.SMALLINT               ),
        SMALLSERIAL             (StandardTypeMetadata.TINYINT               ),
        SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
        STRING                  (StandardTypeMetadata.STRING                ),
        STRUCT                 (StandardTypeMetadata.STRUCT                ),
        SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
        TEXT                    (StandardTypeMetadata.VARCHAR                  ),
        TIME                    (StandardTypeMetadata.DATETIME                  ),
        TIMEZ                   (StandardTypeMetadata.DATETIME                  ),
        TIMESTAMP               (StandardTypeMetadata.DATETIME             ),
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.DATETIME             ),
        TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.DATETIME             ),
        TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
        TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
        TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
        UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
        UUID                    (StandardTypeMetadata.ILLEGAL               ),
        UROWID                  (StandardTypeMetadata.ILLEGAL               ),
        VARBIT                  (StandardTypeMetadata.BITMAP             ),
        TINYBLOB                (StandardTypeMetadata.BITMAP              ),
        TINYINT                 (StandardTypeMetadata.TINYINT               ),
        TINYTEXT                (StandardTypeMetadata.TINYTEXT              ),
        VARBINARY               (StandardTypeMetadata.BITMAP             ),
        VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
        VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
        XML                     (StandardTypeMetadata.VARCHAR                  ),
        YEAR                    (StandardTypeMetadata.DATE                  );
        private final TypeMetadata standard;
        DorisColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
