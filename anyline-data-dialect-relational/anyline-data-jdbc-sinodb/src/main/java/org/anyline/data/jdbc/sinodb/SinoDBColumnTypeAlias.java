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
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum SinoDBColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.ILLEGAL            ),
    BINARY_DOUBLE           (StandardColumnType.ILLEGAL               ),
    BINARY_FLOAT            (StandardColumnType.ILLEGAL            ),
    BIGINT                  (StandardColumnType.BIGINT            ),
    BIGSERIAL               (StandardColumnType.BIGSERIAL            ),
    BINARY                  (StandardColumnType.BLOB               ),
    BIT                     (StandardColumnType.BOOLEAN            ),
    BLOB                    (StandardColumnType.BLOB               ),
    BOOL                    (StandardColumnType.BOOLEAN            ),
    BOX                     (StandardColumnType.ILLEGAL            ),
    BYTEA                   (StandardColumnType.BLOB               ),
    CHAR                    (StandardColumnType.CHAR               ),
    CIDR                    (StandardColumnType.ILLEGAL            ),
    CIRCLE                  (StandardColumnType.TEXT               ),
    CLOB                    (StandardColumnType.TEXT               ),
    DATE                    (StandardColumnType.TEXT               ),
    DATETIME                (StandardColumnType.TEXT               ),
    DATETIME2               (StandardColumnType.TEXT               ),
    DATETIMEOFFSET          (StandardColumnType.INTEGER            ),
    DECIMAL                 (StandardColumnType.REAL               ),
    DOUBLE                  (StandardColumnType.REAL               ),
    ENUM                    (StandardColumnType.ILLEGAL            ),
    FLOAT                   (StandardColumnType.REAL               ),
    FLOAT4                  (StandardColumnType.REAL               ),
    FLOAT8                  (StandardColumnType.REAL               ),
    GEOGRAPHY               (StandardColumnType.ILLEGAL            ), //
    GEOMETRY                (StandardColumnType.REAL               ),
    GEOMETRYCOLLECTION       (StandardColumnType.ILLEGAL            ),
    HIERARCHYID             (StandardColumnType.ILLEGAL            ), //
    IMAGE                   (StandardColumnType.BLOB               ), //
    INET                    (StandardColumnType.ILLEGAL            ),
    INTERVAL                (StandardColumnType.ILLEGAL            ),
    INT                     (StandardColumnType.INTEGER            ),
    INT2                    (StandardColumnType.INTEGER            ),
    INT4                    (StandardColumnType.INTEGER            ),
    INT8                    (StandardColumnType.INTEGER            ),
    INTEGER                 (StandardColumnType.INTEGER            ),
    JSON                    (StandardColumnType.TEXT               ),
    JSONB                   (StandardColumnType.BLOB               ),
    LINE                    (StandardColumnType.ILLEGAL            ),
    LONG                    (StandardColumnType.INTEGER            ),
    LONGBLOB                (StandardColumnType.BLOB               ),
    LONGTEXT                (StandardColumnType.TEXT               ),
    LSEG                    (StandardColumnType.ILLEGAL            ),
    MACADDR                 (StandardColumnType.ILLEGAL            ),
    MONEY                   (StandardColumnType.REAL               ),
    NUMBER                  (StandardColumnType.REAL               ),
    NCHAR                   (StandardColumnType.TEXT               ),
    NCLOB                   (StandardColumnType.BLOB               ),
    NTEXT                   (StandardColumnType.TEXT               ),
    NVARCHAR                (StandardColumnType.TEXT               ),
    NVARCHAR2               (StandardColumnType.TEXT               ),
    PATH                    (StandardColumnType.ILLEGAL            ),
    MEDIUMBLOB              (StandardColumnType.BLOB               ),
    MEDIUMINT               (StandardColumnType.INTEGER            ),
    MEDIUMTEXT              (StandardColumnType.TEXT               ),
    MULTILINESTRING         (StandardColumnType.ILLEGAL            ),
    MULTIPOINT              (StandardColumnType.ILLEGAL            ),
    MULTIPOLYGON            (StandardColumnType.ILLEGAL            ),
    NUMERIC                 (StandardColumnType.REAL               ),
    POINT                   (StandardColumnType.ILLEGAL            ),
    POLYGON                 (StandardColumnType.ILLEGAL            ),
    REAL                    (StandardColumnType.REAL               ),
    RAW                     (StandardColumnType.ILLEGAL            ),
    ROWID                   (StandardColumnType.ILLEGAL            ),
    SERIAL                  (StandardColumnType.INTEGER            ),
    SERIAL2                 (StandardColumnType.INTEGER            ),
    SERIAL4                 (StandardColumnType.INTEGER            ),
    SERIAL8                 (StandardColumnType.INTEGER            ),
    SET                     (StandardColumnType.ILLEGAL            ),
    SMALLDATETIME           (StandardColumnType.TEXT               ),
    SMALLMONEY              (StandardColumnType.TEXT               ),
    SMALLINT                (StandardColumnType.INTEGER            ),
    SMALLSERIAL             (StandardColumnType.INTEGER            ),
    SQL_VARIANT             (StandardColumnType.ILLEGAL            ),
    SYSNAME                 (StandardColumnType.ILLEGAL            ),
    TEXT                    (StandardColumnType.TEXT               ),
    TIME                    (StandardColumnType.TEXT               ),
    TIMEZ                   (StandardColumnType.TEXT               ),
    TIMESTAMP               (StandardColumnType.INTEGER            ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.INTEGER            ),
    TIMESTAMP_WITH_ZONE          (StandardColumnType.INTEGER            ),
    TSQUERY                 (StandardColumnType.ILLEGAL            ),
    TSVECTOR                (StandardColumnType.ILLEGAL            ),
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL            ),
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL            ),
    UUID                    (StandardColumnType.ILLEGAL            ),
    UROWID                  (StandardColumnType.ILLEGAL            ),
    VARBIT                  (StandardColumnType.BLOB               ),
    TINYBLOB                (StandardColumnType.BLOB               ),
    TINYINT                 (StandardColumnType.INTEGER            ),
    TINYTEXT                (StandardColumnType.TEXT               ),
    VARBINARY               (StandardColumnType.BLOB               ),
    VARCHAR                 (StandardColumnType.TEXT               ),
    VARCHAR2                (StandardColumnType.TEXT               ),
    XML                     (StandardColumnType.TEXT               ),
    YEAR                    (StandardColumnType.INTEGER            );
    private final TypeMetadata standard;
    private SinoDBColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }

}
