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


package org.anyline.data.jdbc.sinodb;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum SinoDBColumnTypeAlias implements ColumnTypeAlias {
    //https://forum.sinoregal.cn/t/topic/149
    BFILE                   (StandardTypeMetadata.ILLEGAL            ),
    BINARY_DOUBLE           (StandardTypeMetadata.ILLEGAL               ),
    BINARY_FLOAT            (StandardTypeMetadata.ILLEGAL            ),
    BIGINT                  (StandardTypeMetadata.BIGINT            ),
    BIGSERIAL               (StandardTypeMetadata.BIGSERIAL            ),
    BINARY                  (StandardTypeMetadata.BLOB               ),
    BIT                     (StandardTypeMetadata.BOOLEAN            ),
    BLOB                    (StandardTypeMetadata.BLOB               ),
    BOOL                    (StandardTypeMetadata.BOOLEAN            ),
    BOX                     (StandardTypeMetadata.ILLEGAL            ),
    BYTE                   (StandardTypeMetadata.BYTE               ),
    BYTEA                   (StandardTypeMetadata.BYTE               ),
    CHAR                    (StandardTypeMetadata.CHAR               ),
    CIDR                    (StandardTypeMetadata.ILLEGAL            ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.CLOB               ),
    DATE                    (StandardTypeMetadata.DATE               ),
    DATETIME                (StandardTypeMetadata.DATETIME               ),
    DATETIME2               (StandardTypeMetadata.DATETIME               ),
    DATETIMEOFFSET          (StandardTypeMetadata.DATETIME            ),
    DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
    DOUBLE                  (StandardTypeMetadata.FLOAT               ),
    ENUM                    (StandardTypeMetadata.ILLEGAL            ),
    FLOAT                   (StandardTypeMetadata.FLOAT               ),
    FLOAT4                  (StandardTypeMetadata.FLOAT               ),
    FLOAT8                  (StandardTypeMetadata.FLOAT               ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL            ), //
    GEOMETRY                (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRYCOLLECTION       (StandardTypeMetadata.ILLEGAL            ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL            ), //
    IMAGE                   (StandardTypeMetadata.BLOB               ), //
    INET                    (StandardTypeMetadata.ILLEGAL            ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL            ),
    INT                     (StandardTypeMetadata.INT8            ),
    INT2                    (StandardTypeMetadata.SMALLINT            ),
    INT4                    (StandardTypeMetadata.INT8            ),
    INT8                    (StandardTypeMetadata.INT8            ),
    INTEGER                 (StandardTypeMetadata.INTEGER            ),
    JSON                    (StandardTypeMetadata.TEXT               ),
    JSONB                   (StandardTypeMetadata.BLOB               ),
    LINE                    (StandardTypeMetadata.ILLEGAL            ),
    LIST                    (StandardTypeMetadata.LIST            ),
    LONG                    (StandardTypeMetadata.INT8            ),
    LONGBLOB                (StandardTypeMetadata.BLOB               ),
    LONGTEXT                (StandardTypeMetadata.TEXT               ),
    LSEG                    (StandardTypeMetadata.ILLEGAL            ),
    MACADDR                 (StandardTypeMetadata.ILLEGAL            ),
    MONEY                   (StandardTypeMetadata.MONEY               ),
    NUMERIC                 (StandardTypeMetadata.NUMERIC               ),
    NUMBER                  (StandardTypeMetadata.DECIMAL               ),
    NCHAR                   (StandardTypeMetadata.NCHAR               ),
    NCLOB                   (StandardTypeMetadata.CLOB               ),
    NTEXT                   (StandardTypeMetadata.TEXT               ),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR               ),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR               ),
    PATH                    (StandardTypeMetadata.ILLEGAL            ),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB               ),
    MEDIUMINT               (StandardTypeMetadata.INTEGER            ),
    MEDIUMTEXT              (StandardTypeMetadata.TEXT               ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL            ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL            ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL            ),
    POINT                   (StandardTypeMetadata.ILLEGAL            ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL            ),
    REAL                    (StandardTypeMetadata.REAL               ),
    RAW                     (StandardTypeMetadata.ILLEGAL            ),
    ROWID                   (StandardTypeMetadata.ILLEGAL            ),
    SERIAL                  (StandardTypeMetadata.SERIAL            ),
    SERIAL2                 (StandardTypeMetadata.SERIAL            ),
    SERIAL4                 (StandardTypeMetadata.SERIAL            ),
    SERIAL8                 (StandardTypeMetadata.SERIAL8            ),
    SET                     (StandardTypeMetadata.SET            ),
    MULTISET                (StandardTypeMetadata.MULTISET            ),
    SMALLDATETIME           (StandardTypeMetadata.DATETIME               ),
    SMALLMONEY              (StandardTypeMetadata.MONEY               ),
    SMALLINT                (StandardTypeMetadata.SMALLINT            ),
    SMALLSERIAL             (StandardTypeMetadata.SERIAL            ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL            ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL            ),
    TEXT                    (StandardTypeMetadata.TEXT               ),
    TIME                    (StandardTypeMetadata.DATETIME               ),
    TIMEZ                   (StandardTypeMetadata.DATETIME               ),
    TIMESTAMP               (StandardTypeMetadata.DATETIME            ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.DATETIME            ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.DATETIME            ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL            ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL            ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL            ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL            ),
    UUID                    (StandardTypeMetadata.ILLEGAL            ),
    UROWID                  (StandardTypeMetadata.ILLEGAL            ),
    VARBIT                  (StandardTypeMetadata.BLOB               ),
    TINYBLOB                (StandardTypeMetadata.BLOB               ),
    TINYINT                 (StandardTypeMetadata.SMALLINT            ),
    TINYTEXT                (StandardTypeMetadata.TEXT               ),
    VARBINARY               (StandardTypeMetadata.BLOB               ),
    VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
    VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
    XML                     (StandardTypeMetadata.TEXT               ),
    YEAR                    (StandardTypeMetadata.INTEGER            );
    private final TypeMetadata standard;
    private SinoDBColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }

}
