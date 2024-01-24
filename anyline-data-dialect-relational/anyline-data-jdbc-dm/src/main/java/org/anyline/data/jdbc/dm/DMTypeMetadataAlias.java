/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.data.jdbc.dm;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum DMTypeMetadataAlias implements TypeMetadataAlias {
    
    BFILE                   (StandardTypeMetadata.BFILE                 ),
    BINARY_DOUBLE           (StandardTypeMetadata.BINARY_DOUBLE         ),
    BINARY_FLOAT            (StandardTypeMetadata.FLOAT_ORACLE          ),
    BIGINT                  (StandardTypeMetadata.BIGINT                ),
    BIGSERIAL               (StandardTypeMetadata.BIGINT                ),
    BINARY                  (StandardTypeMetadata.BINARY                  ),
    BIT                     (StandardTypeMetadata.BIT                ),
    BLOB                    (StandardTypeMetadata.BLOB                  ),
    BOOL                    (StandardTypeMetadata.BOOL                ),
    BOOLEAN                 (StandardTypeMetadata.BOOLEAN                ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BYTEA                   (StandardTypeMetadata.BLOB                  ),
    BYTE                    (StandardTypeMetadata.BYTE                  ),
    CHAR                    (StandardTypeMetadata.CHAR                  ),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.CLOB                  ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    TIME_WITH_ZONE          (StandardTypeMetadata.TIME_WITH_ZONE        ),
    DATETIME                (StandardTypeMetadata.DATETIME             ),
    DATETIME2               (StandardTypeMetadata.DATETIME             ),
    DATETIMEOFFSET          (StandardTypeMetadata.DATETIME             ),
    DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
    DOUBLE                  (StandardTypeMetadata.DOUBLE                ),
    ENUM                    (StandardTypeMetadata.ILLEGAL               ),
    FLOAT                   (StandardTypeMetadata.FLOAT_ORACLE          ),
    FLOAT4                  (StandardTypeMetadata.FLOAT_ORACLE          ),
    FLOAT8                  (StandardTypeMetadata.FLOAT_ORACLE          ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
    IMAGE                   (StandardTypeMetadata.IMAGE                  ),
    INET                    (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
    INT                     (StandardTypeMetadata.INT                   ),
    INT2                    (StandardTypeMetadata.INT                   ),
    INT4                    (StandardTypeMetadata.INT                   ),
    INT8                    (StandardTypeMetadata.BIGINT                ),
    INTEGER                 (StandardTypeMetadata.INTEGER               ),
    JSON                    (StandardTypeMetadata.CLOB                  ),
    JSONB                   (StandardTypeMetadata.BLOB                  ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.LONG_TEXT                  ),
    LONGBLOB                (StandardTypeMetadata.BLOB                  ),
    LONGTEXT                (StandardTypeMetadata.TEXT                  ),
    LSEG                    (StandardTypeMetadata.ILLEGAL               ),
    MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
    MONEY                   (StandardTypeMetadata.NUMBER                ),
    NUMBER                  (StandardTypeMetadata.NUMBER                ),
    NCHAR                   (StandardTypeMetadata.NCHAR                 ),
    NCLOB                   (StandardTypeMetadata.NCLOB                 ),
    NTEXT                   (StandardTypeMetadata.NCLOB                 ),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR2             ),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR2             ),
    PATH                    (StandardTypeMetadata.ILLEGAL               ),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB                  ),
    MEDIUMINT               (StandardTypeMetadata.NUMBER                ),
    MEDIUMTEXT              (StandardTypeMetadata.TEXT                  ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL               ),
    NUMERIC                 (StandardTypeMetadata.NUMBER                ),
    POINT                   (StandardTypeMetadata.ILLEGAL               ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
    REAL                    (StandardTypeMetadata.REAL          ),
    RAW                     (StandardTypeMetadata.RAW                   ),
    ROWID                   (StandardTypeMetadata.ROWID                 ),
    SERIAL                  (StandardTypeMetadata.INT                   ),
    SERIAL2                 (StandardTypeMetadata.INT                   ),
    SERIAL4                 (StandardTypeMetadata.INT                   ),
    SERIAL8                 (StandardTypeMetadata.BIGINT                ),
    SET                     (StandardTypeMetadata.ILLEGAL               ),
    SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP             ),
    SMALLMONEY              (StandardTypeMetadata.NUMBER                ),
    SMALLINT                (StandardTypeMetadata.SMALLINT                ),
    SMALLSERIAL             (StandardTypeMetadata.NUMBER                ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
    TEXT                    (StandardTypeMetadata.TEXT                  ),
    TIME                    (StandardTypeMetadata.TIME             ),
    TIMEZ                   (StandardTypeMetadata.TIME             ),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP_WITH_ZONE),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP_WITH_LOCAL_ZONE             ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
    UUID                    (StandardTypeMetadata.ILLEGAL               ),
    UROWID                  (StandardTypeMetadata.UROWID                ),
    VARBIT                  (StandardTypeMetadata.BLOB                  ),
    TINYBLOB                (StandardTypeMetadata.BLOB                  ),
    TINYINT                 (StandardTypeMetadata.TINYINT                ),
    TINYTEXT                (StandardTypeMetadata.TEXT                  ),
    VARBINARY               (StandardTypeMetadata.VARBINARY                  ),
    VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
    VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
    XML                     (StandardTypeMetadata.ILLEGAL               ),
    YEAR                    (StandardTypeMetadata.DATE                  );
    private final TypeMetadata standard;
    DMTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}