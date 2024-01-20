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


package org.anyline.data.jdbc.highgo;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum HighgoColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardTypeMetadata.ILLEGAL               ),
    BINARY_DOUBLE           (StandardTypeMetadata.BINARY_DOUBLE         ),
    BINARY_FLOAT            (StandardTypeMetadata.BINARY_FLOAT          ),
    BIGINT                  (StandardTypeMetadata.INT8                  ),
    BIGSERIAL               (StandardTypeMetadata.BIGSERIAL             ),
    BINARY                  (StandardTypeMetadata.BIT                   ),
    BIT                     (StandardTypeMetadata.BIT                   ),
    BLOB                    (StandardTypeMetadata.BYTEA                 ),
    BOOL                    (StandardTypeMetadata.BOOL                  ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BYTEA                   (StandardTypeMetadata.BYTEA                 ),
    CHAR                    (StandardTypeMetadata.CHAR                  ),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.TEXT                  ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    DATETIME                (StandardTypeMetadata.TIMESTAMP             ),
    DATETIME2               (StandardTypeMetadata.TIMESTAMP             ),
    DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP             ),
    DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
    DOUBLE                  (StandardTypeMetadata.DECIMAL               ),
    ENUM                    (StandardTypeMetadata.ILLEGAL               ),
    FLOAT                   (StandardTypeMetadata.FLOAT4                ),
    FLOAT4                  (StandardTypeMetadata.FLOAT4                ),
    FLOAT8                  (StandardTypeMetadata.FLOAT8                ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
    IMAGE                   (StandardTypeMetadata.BYTEA                 ),
    INET                    (StandardTypeMetadata.INET                  ),
    INTERVAL                (StandardTypeMetadata.INTERVAL              ),
    INT                     (StandardTypeMetadata.INT4                  ),
    INT2                    (StandardTypeMetadata.INT2                  ),
    INT4                    (StandardTypeMetadata.INT4                  ), //
    INT8                    (StandardTypeMetadata.INT8                  ), //
    INTEGER                 (StandardTypeMetadata.INT4                  ),
    JSON                    (StandardTypeMetadata.JSON                  ),
    JSONB                   (StandardTypeMetadata.JSONB                 ),
    LINE                    (StandardTypeMetadata.LINE                  ),
    LINESTRING              (StandardTypeMetadata.LINE                  ),
    LONG                    (StandardTypeMetadata.INT8                  ),
    LONGBLOB                (StandardTypeMetadata.BYTEA                 ),
    LONGTEXT                (StandardTypeMetadata.TEXT                  ),
    LSEG                    (StandardTypeMetadata.LSEG                  ),
    MACADDR                 (StandardTypeMetadata.MACADDR               ),
    MONEY                   (StandardTypeMetadata.MONEY                 ),
    NUMBER                  (StandardTypeMetadata.DECIMAL               ),
    NCHAR                   (StandardTypeMetadata.VARCHAR               ),
    NCLOB                   (StandardTypeMetadata.BYTEA                 ),
    NTEXT                   (StandardTypeMetadata.TEXT                  ),
    NVARCHAR                (StandardTypeMetadata.VARCHAR               ),
    NVARCHAR2               (StandardTypeMetadata.VARCHAR               ),
    PATH                    (StandardTypeMetadata.PATH                  ),
    MEDIUMBLOB              (StandardTypeMetadata.BYTEA                 ),
    MEDIUMINT               (StandardTypeMetadata.INT8                  ),
    MEDIUMTEXT              (StandardTypeMetadata.TEXT                  ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL               ),
    NUMERIC                 (StandardTypeMetadata.DECIMAL               ),
    POINT                   (StandardTypeMetadata.POINT                 ),
    GEOGRAPHY_POINT         (StandardTypeMetadata.POINT                 ),
    POLYGON                 (StandardTypeMetadata.POLYGON               ),
    REAL                    (StandardTypeMetadata.FLOAT4                ),
    RAW                     (StandardTypeMetadata.ILLEGAL               ),
    ROWID                   (StandardTypeMetadata.ILLEGAL               ),
    SERIAL                  (StandardTypeMetadata.SERIAL                ),
    SERIAL2                 (StandardTypeMetadata.SERIAL2               ),
    SERIAL4                 (StandardTypeMetadata.SERIAL4               ),
    SERIAL8                 (StandardTypeMetadata.SERIAL8               ),
    SET                     (StandardTypeMetadata.ILLEGAL               ),
    SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP             ),
    SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
    SMALLINT                (StandardTypeMetadata.INT2                  ),
    SMALLSERIAL             (StandardTypeMetadata.SMALLSERIAL           ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
    TEXT                    (StandardTypeMetadata.TEXT                  ),
    TIME                    (StandardTypeMetadata.TIME                  ),
    TIMEZ                   (StandardTypeMetadata.TIMEZ                 ),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP_WITH_LOCAL_ZONE  ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP_WITH_ZONE        ),
    TSQUERY                 (StandardTypeMetadata.TSQUERY               ),
    TSVECTOR                (StandardTypeMetadata.TSVECTOR              ),
    TXID_SNAPSHOT           (StandardTypeMetadata.TXID_SNAPSHOT         ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
    UUID                    (StandardTypeMetadata.UUID                  ),
    UROWID                  (StandardTypeMetadata.ILLEGAL               ),
    VARBIT                  (StandardTypeMetadata.VARBIT                ),
    TINYBLOB                (StandardTypeMetadata.BYTEA                 ),
    TINYINT                 (StandardTypeMetadata.INT2                  ),
    TINYTEXT                (StandardTypeMetadata.TEXT                  ),
    VARBINARY               (StandardTypeMetadata.VARBIT                ),
    VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
    VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
    XML                     (StandardTypeMetadata.XML                   ),
    YEAR                    (StandardTypeMetadata.DATE                  );
    private final TypeMetadata standard;
    private HighgoColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}
