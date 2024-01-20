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
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum InformixColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardTypeMetadata.ILLEGAL               ),
    BINARY_DOUBLE           (StandardTypeMetadata.BINARY_DOUBLE         ),
    BINARY_FLOAT            (StandardTypeMetadata.BINARY_FLOAT          ),
    BIGINT                  (StandardTypeMetadata.BIGINT                ),
    BIGSERIAL               (StandardTypeMetadata.BIGSERIAL             ),
    BINARY                  (StandardTypeMetadata.BYTE                  ),
    BIT                     (StandardTypeMetadata.BYTE                  ),
    BLOB                    (StandardTypeMetadata.BLOB                  ),
    BOOL                    (StandardTypeMetadata.BOOLEAN               ),
    BOOLEAN                 (StandardTypeMetadata.BOOLEAN               ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BYTE                    (StandardTypeMetadata.BYTE                  ),
    BYTEA                   (StandardTypeMetadata.BYTE                  ),
    CHAR                    (StandardTypeMetadata.CHAR                  ),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.CLOB                  ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    DATETIME                (StandardTypeMetadata.DATETIME              ),
    DATETIME2               (StandardTypeMetadata.DATETIME              ),
    DATETIMEOFFSET          (StandardTypeMetadata.DATETIME              ),
    DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
    DOUBLE                  (StandardTypeMetadata.DOUBLE                ),
    ENUM                    (StandardTypeMetadata.ILLEGAL               ),
    FLOAT                   (StandardTypeMetadata.FLOAT_INFORMIX        ),
    FLOAT4                  (StandardTypeMetadata.FLOAT_INFORMIX        ),
    FLOAT8                  (StandardTypeMetadata.FLOAT_INFORMIX        ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
    IMAGE                   (StandardTypeMetadata.BYTE                  ),
    INET                    (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL                (StandardTypeMetadata.INTERVAL              ),
    INT                     (StandardTypeMetadata.INT                   ),
    INT2                    (StandardTypeMetadata.INT                   ),
    INT4                    (StandardTypeMetadata.INT                   ), //
    INT8                    (StandardTypeMetadata.INT8                  ),
    INTEGER                 (StandardTypeMetadata.INFORMIX_INTEGER      ),
    JSON                    (StandardTypeMetadata.TEXT                  ),
    JSONB                   (StandardTypeMetadata.TEXT                  ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.BIGINT                ),
    LONGBLOB                (StandardTypeMetadata.BLOB                  ),
    LONGTEXT                (StandardTypeMetadata.TEXT                  ),
    LSEG                    (StandardTypeMetadata.ILLEGAL               ),
    MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
    MONEY                   (StandardTypeMetadata.MONEY                 ),
    NUMBER                  (StandardTypeMetadata.DECIMAL               ),
    NCHAR                   (StandardTypeMetadata.NCHAR                 ),
    NCLOB                   (StandardTypeMetadata.CLOB                  ),
    NTEXT                   (StandardTypeMetadata.TEXT                  ),
    NVARCHAR                (StandardTypeMetadata.VARCHAR               ),
    NVARCHAR2               (StandardTypeMetadata.VARCHAR               ),
    PATH                    (StandardTypeMetadata.ILLEGAL               ),
    MEDIUMBLOB              (StandardTypeMetadata.ILLEGAL               ),
    MEDIUMINT               (StandardTypeMetadata.INT                   ),
    MEDIUMTEXT              (StandardTypeMetadata.TEXT                  ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL               ),
    NUMERIC                 (StandardTypeMetadata.DECIMAL               ),
    POINT                   (StandardTypeMetadata.ILLEGAL               ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
    REAL                    (StandardTypeMetadata.FLOAT_INFORMIX        ),
    RAW                     (StandardTypeMetadata.ILLEGAL               ),
    ROWID                   (StandardTypeMetadata.ILLEGAL               ),
    SERIAL                  (StandardTypeMetadata.SERIAL                ),
    SERIAL2                 (StandardTypeMetadata.SERIAL                ),
    SERIAL4                 (StandardTypeMetadata.SERIAL                ),
    SERIAL8                 (StandardTypeMetadata.SERIAL8               ),
    SET                     (StandardTypeMetadata.ILLEGAL               ),
    SMALLDATETIME           (StandardTypeMetadata.DATETIME              ),
    SMALLFLOAT              (StandardTypeMetadata.FLOAT_INFORMIX        ),
    SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
    SMALLINT                (StandardTypeMetadata.INT                   ),
    SMALLSERIAL             (StandardTypeMetadata.SERIAL                ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
    TEXT                    (StandardTypeMetadata.TEXT                  ),
    TIME                    (StandardTypeMetadata.DATETIME              ),
    TIMEZ                   (StandardTypeMetadata.DATETIME              ),
    TIMESTAMP               (StandardTypeMetadata.DATETIME              ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.DATETIME              ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.DATETIME              ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
    UUID                    (StandardTypeMetadata.ILLEGAL               ),
    UROWID                  (StandardTypeMetadata.ILLEGAL               ),
    VARBIT                  (StandardTypeMetadata.BYTEA                 ),
    TINYBLOB                (StandardTypeMetadata.BYTE                  ),
    TINYINT                 (StandardTypeMetadata.INT                   ),
    TINYTEXT                (StandardTypeMetadata.TEXT                  ),
    VARBINARY               (StandardTypeMetadata.BYTE                  ),
    VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
    LVARCHAR                (StandardTypeMetadata.LVARCHAR              ),
    VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
    XML                     (StandardTypeMetadata.TEXT                  ),
    YEAR                    (StandardTypeMetadata.DATETIME              );
    private final TypeMetadata standard;
    private InformixColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}
