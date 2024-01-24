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


package org.anyline.data.jdbc.db2;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;

public enum DB2TypeMetadataAlias implements TypeMetadataAlias {
        ARRAY                   (StandardTypeMetadata.ILLEGAL               ),
        AGG_STATE               (StandardTypeMetadata.ILLEGAL               ),
        BFILE                   (StandardTypeMetadata.ILLEGAL               ),
        BINARY_DOUBLE           (StandardTypeMetadata.DOUBLE                ),
        BINARY_FLOAT            (StandardTypeMetadata.DOUBLE                 ),
        BIGINT                  (StandardTypeMetadata.BIGINT                ),
        BOOLEAN                  (StandardTypeMetadata.BOOLEAN                ),
        BIGSERIAL               (StandardTypeMetadata.BIGINT                ),
        BINARY                  (StandardTypeMetadata.BINARY                ),
        BIT                     (StandardTypeMetadata.BOOLEAN                   ),
        BLOB                    (StandardTypeMetadata.BLOB                  ),
        BOOL                    (StandardTypeMetadata.BOOLEAN                   ),
        BOX                     (StandardTypeMetadata.ILLEGAL               ),
        BYTEA                   (StandardTypeMetadata.ILLEGAL             ),
        CHAR                    (StandardTypeMetadata.CHAR                  ),
        CIDR                    (StandardTypeMetadata.ILLEGAL               ),
        CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
        CLOB                    (StandardTypeMetadata.CLOB                  ),
        DATE                    (StandardTypeMetadata.DATE                  ),
        DATETIME                (StandardTypeMetadata.TIMESTAMP              ),
        DATETIME2               (StandardTypeMetadata.TIMESTAMP             ),
        DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP        ),
        DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
        DOUBLE                  (StandardTypeMetadata.DOUBLE                ),
        ENUM                    (StandardTypeMetadata.ILLEGAL                  ),
        FLOAT                   (StandardTypeMetadata.DOUBLE           ),
        FLOAT4                  (StandardTypeMetadata.DOUBLE           ),
        FLOAT8                  (StandardTypeMetadata.DOUBLE           ),
        GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
        GEOMETRY                (StandardTypeMetadata.ILLEGAL              ),
        GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL    ),
        HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
        HLL                     (StandardTypeMetadata.ILLEGAL               ),
        HYPERLOGLOG             (StandardTypeMetadata.ILLEGAL               ),
        IMAGE                   (StandardTypeMetadata.BLOB                  ),
        INET                    (StandardTypeMetadata.ILLEGAL               ),
        INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
        INT                     (StandardTypeMetadata.INT                   ),
        INT2                    (StandardTypeMetadata.INT                   ),
        INT4                    (StandardTypeMetadata.INT                   ),
        INT8                    (StandardTypeMetadata.BIGINT                ),
        INTEGER                 (StandardTypeMetadata.INTEGER                   ),
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
        NCLOB                   (StandardTypeMetadata.CLOB                  ),
        NTEXT                   (StandardTypeMetadata.CLOB                  ),
        NVARCHAR                (StandardTypeMetadata.VARCHAR               ),
        NVARCHAR2               (StandardTypeMetadata.VARCHAR               ),
        PATH                    (StandardTypeMetadata.ILLEGAL               ),
        MAP                     (StandardTypeMetadata.ILLEGAL            ),
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
        REAL                    (StandardTypeMetadata.REAL                  ),
        RAW                     (StandardTypeMetadata.ILLEGAL               ),
        ROWID                   (StandardTypeMetadata.ILLEGAL               ),
        SERIAL                  (StandardTypeMetadata.INT               ),
        SERIAL2                 (StandardTypeMetadata.INT               ),
        SERIAL4                 (StandardTypeMetadata.INT                   ),
        SERIAL8                 (StandardTypeMetadata.BIGINT                ),
        SET                     (StandardTypeMetadata.ILLEGAL                   ),
        SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP              ),
        SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
        SMALLINT                (StandardTypeMetadata.SMALLINT               ),
        SMALLSERIAL             (StandardTypeMetadata.TINYINT               ),
        SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
        STRING                  (StandardTypeMetadata.CLOB                ),
        STRUCT                 (StandardTypeMetadata.ILLEGAL                ),
        SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
        TEXT                    (StandardTypeMetadata.CLOB                  ),
        TIME                    (StandardTypeMetadata.TIME                  ),
        TIMEZ                   (StandardTypeMetadata.TIMESTAMP                  ),
        TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP             ),
        TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP             ),
        TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
        TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
        TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
        UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
        UUID                    (StandardTypeMetadata.ILLEGAL               ),
        UROWID                  (StandardTypeMetadata.ILLEGAL               ),
        VARBIT                  (StandardTypeMetadata.ILLEGAL             ),
        TINYBLOB                (StandardTypeMetadata.BLOB              ),
        TINYINT                 (StandardTypeMetadata.SMALLINT               ),
        TINYTEXT                (StandardTypeMetadata.CLOB              ),
        VARBINARY               (StandardTypeMetadata.VARBINARY             ),
        VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
        VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
        XML                     (StandardTypeMetadata.XML                  ),
        YEAR                    (StandardTypeMetadata.INT                  );
        private final TypeMetadata standard;
        DB2TypeMetadataAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
