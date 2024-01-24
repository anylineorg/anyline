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


package org.anyline.data.jdbc.sqlite;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum SQLiteTypeMetadataAlias implements TypeMetadataAlias {

    BFILE                   (StandardTypeMetadata.ILLEGAL            ),
    BINARY_DOUBLE           (StandardTypeMetadata.REAL               ),
    BINARY_FLOAT            (StandardTypeMetadata.ILLEGAL            ),
    BIGINT                  (StandardTypeMetadata.INTEGER            ),
    BIGSERIAL               (StandardTypeMetadata.INTEGER            ),
    BINARY                  (StandardTypeMetadata.BLOB               ),
    BIT                     (StandardTypeMetadata.INTEGER            ),
    BLOB                    (StandardTypeMetadata.BLOB               ),
    BOOL                    (StandardTypeMetadata.INTEGER            ),
    BOX                     (StandardTypeMetadata.ILLEGAL            ),
    BYTEA                   (StandardTypeMetadata.BLOB               ),
    CHAR                    (StandardTypeMetadata.TEXT               ),
    CIDR                    (StandardTypeMetadata.ILLEGAL            ),
    CIRCLE                  (StandardTypeMetadata.TEXT               ),
    CLOB                    (StandardTypeMetadata.TEXT               ),
    DATE                    (StandardTypeMetadata.TEXT               ),
    DATETIME                (StandardTypeMetadata.TEXT               ),
    DATETIME2               (StandardTypeMetadata.TEXT               ),
    DATETIMEOFFSET          (StandardTypeMetadata.INTEGER            ),
    DECIMAL                 (StandardTypeMetadata.REAL               ),
    DOUBLE                  (StandardTypeMetadata.REAL               ),
    ENUM                    (StandardTypeMetadata.ILLEGAL            ),
    FLOAT                   (StandardTypeMetadata.REAL               ),
    FLOAT4                  (StandardTypeMetadata.REAL               ),
    FLOAT8                  (StandardTypeMetadata.REAL               ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL            ), //
    GEOMETRY                (StandardTypeMetadata.REAL               ),
    GEOMETRYCOLLECTION       (StandardTypeMetadata.ILLEGAL            ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL            ), //
    IMAGE                   (StandardTypeMetadata.BLOB               ), //
    INET                    (StandardTypeMetadata.ILLEGAL            ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL            ),
    INT                     (StandardTypeMetadata.INTEGER            ),
    INT2                    (StandardTypeMetadata.INTEGER            ),
    INT4                    (StandardTypeMetadata.INTEGER            ),
    INT8                    (StandardTypeMetadata.INTEGER            ),
    INTEGER                 (StandardTypeMetadata.INTEGER            ),
    JSON                    (StandardTypeMetadata.TEXT               ),
    JSONB                   (StandardTypeMetadata.BLOB               ),
    LINE                    (StandardTypeMetadata.ILLEGAL            ),
    LONG                    (StandardTypeMetadata.INTEGER            ),
    LONGBLOB                (StandardTypeMetadata.BLOB               ),
    LONGTEXT                (StandardTypeMetadata.TEXT               ),
    LSEG                    (StandardTypeMetadata.ILLEGAL            ),
    MACADDR                 (StandardTypeMetadata.ILLEGAL            ),
    MONEY                   (StandardTypeMetadata.REAL               ),
    NUMBER                  (StandardTypeMetadata.REAL               ),
    NCHAR                   (StandardTypeMetadata.TEXT               ),
    NCLOB                   (StandardTypeMetadata.BLOB               ),
    NTEXT                   (StandardTypeMetadata.TEXT               ),
    NVARCHAR                (StandardTypeMetadata.TEXT               ),
    NVARCHAR2               (StandardTypeMetadata.TEXT               ),
    PATH                    (StandardTypeMetadata.ILLEGAL            ),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB               ),
    MEDIUMINT               (StandardTypeMetadata.INTEGER            ),
    MEDIUMTEXT              (StandardTypeMetadata.TEXT               ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL            ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL            ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL            ),
    NUMERIC                 (StandardTypeMetadata.REAL               ),
    POINT                   (StandardTypeMetadata.ILLEGAL            ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL            ),
    REAL                    (StandardTypeMetadata.REAL               ),
    RAW                     (StandardTypeMetadata.ILLEGAL            ),
    ROWID                   (StandardTypeMetadata.ILLEGAL            ),
    SERIAL                  (StandardTypeMetadata.INTEGER            ),
    SERIAL2                 (StandardTypeMetadata.INTEGER            ),
    SERIAL4                 (StandardTypeMetadata.INTEGER            ),
    SERIAL8                 (StandardTypeMetadata.INTEGER            ),
    SET                     (StandardTypeMetadata.ILLEGAL            ),
    SMALLDATETIME           (StandardTypeMetadata.TEXT               ),
    SMALLMONEY              (StandardTypeMetadata.TEXT               ),
    SMALLINT                (StandardTypeMetadata.INTEGER            ),
    SMALLSERIAL             (StandardTypeMetadata.INTEGER            ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL            ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL            ),
    TEXT                    (StandardTypeMetadata.TEXT               ),
    TIME                    (StandardTypeMetadata.TEXT               ),
    TIMEZ                   (StandardTypeMetadata.TEXT               ),
    TIMESTAMP               (StandardTypeMetadata.INTEGER            ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.INTEGER            ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.INTEGER            ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL            ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL            ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL            ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL            ),
    UUID                    (StandardTypeMetadata.ILLEGAL            ),
    UROWID                  (StandardTypeMetadata.ILLEGAL            ),
    VARBIT                  (StandardTypeMetadata.BLOB               ),
    TINYBLOB                (StandardTypeMetadata.BLOB               ),
    TINYINT                 (StandardTypeMetadata.INTEGER            ),
    TINYTEXT                (StandardTypeMetadata.TEXT               ),
    VARBINARY               (StandardTypeMetadata.BLOB               ),
    VARCHAR                 (StandardTypeMetadata.TEXT               ),
    VARCHAR2                (StandardTypeMetadata.TEXT               ),
    XML                     (StandardTypeMetadata.TEXT               ),
    YEAR                    (StandardTypeMetadata.INTEGER            );
    private final TypeMetadata standard;
    private SQLiteTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }

}
