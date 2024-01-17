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
    //https://forum.sinoregal.cn/t/topic/149
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
    BYTE                   (StandardColumnType.BYTE               ),
    BYTEA                   (StandardColumnType.BYTE               ),
    CHAR                    (StandardColumnType.CHAR               ),
    CIDR                    (StandardColumnType.ILLEGAL            ),
    CIRCLE                  (StandardColumnType.ILLEGAL               ),
    CLOB                    (StandardColumnType.CLOB               ),
    DATE                    (StandardColumnType.DATE               ),
    DATETIME                (StandardColumnType.DATETIME               ),
    DATETIME2               (StandardColumnType.DATETIME               ),
    DATETIMEOFFSET          (StandardColumnType.DATETIME            ),
    DECIMAL                 (StandardColumnType.DECIMAL               ),
    DOUBLE                  (StandardColumnType.FLOAT               ),
    ENUM                    (StandardColumnType.ILLEGAL            ),
    FLOAT                   (StandardColumnType.FLOAT               ),
    FLOAT4                  (StandardColumnType.FLOAT               ),
    FLOAT8                  (StandardColumnType.FLOAT               ),
    GEOGRAPHY               (StandardColumnType.ILLEGAL            ), //
    GEOMETRY                (StandardColumnType.ILLEGAL               ),
    GEOMETRYCOLLECTION       (StandardColumnType.ILLEGAL            ),
    HIERARCHYID             (StandardColumnType.ILLEGAL            ), //
    IMAGE                   (StandardColumnType.BLOB               ), //
    INET                    (StandardColumnType.ILLEGAL            ),
    INTERVAL                (StandardColumnType.ILLEGAL            ),
    INT                     (StandardColumnType.INT8            ),
    INT2                    (StandardColumnType.SMALLINT            ),
    INT4                    (StandardColumnType.INT8            ),
    INT8                    (StandardColumnType.INT8            ),
    INTEGER                 (StandardColumnType.INTEGER            ),
    JSON                    (StandardColumnType.TEXT               ),
    JSONB                   (StandardColumnType.BLOB               ),
    LINE                    (StandardColumnType.ILLEGAL            ),
    LIST                    (StandardColumnType.LIST            ),
    LONG                    (StandardColumnType.INT8            ),
    LONGBLOB                (StandardColumnType.BLOB               ),
    LONGTEXT                (StandardColumnType.TEXT               ),
    LSEG                    (StandardColumnType.ILLEGAL            ),
    MACADDR                 (StandardColumnType.ILLEGAL            ),
    MONEY                   (StandardColumnType.MONEY               ),
    NUMERIC                 (StandardColumnType.NUMERIC               ),
    NUMBER                  (StandardColumnType.DECIMAL               ),
    NCHAR                   (StandardColumnType.NCHAR               ),
    NCLOB                   (StandardColumnType.CLOB               ),
    NTEXT                   (StandardColumnType.TEXT               ),
    NVARCHAR                (StandardColumnType.NVARCHAR               ),
    NVARCHAR2               (StandardColumnType.NVARCHAR               ),
    PATH                    (StandardColumnType.ILLEGAL            ),
    MEDIUMBLOB              (StandardColumnType.BLOB               ),
    MEDIUMINT               (StandardColumnType.INTEGER            ),
    MEDIUMTEXT              (StandardColumnType.TEXT               ),
    MULTILINESTRING         (StandardColumnType.ILLEGAL            ),
    MULTIPOINT              (StandardColumnType.ILLEGAL            ),
    MULTIPOLYGON            (StandardColumnType.ILLEGAL            ),
    POINT                   (StandardColumnType.ILLEGAL            ),
    POLYGON                 (StandardColumnType.ILLEGAL            ),
    REAL                    (StandardColumnType.REAL               ),
    RAW                     (StandardColumnType.ILLEGAL            ),
    ROWID                   (StandardColumnType.ILLEGAL            ),
    SERIAL                  (StandardColumnType.INTEGER            ),
    SERIAL2                 (StandardColumnType.INTEGER            ),
    SERIAL4                 (StandardColumnType.INTEGER            ),
    SERIAL8                 (StandardColumnType.INTEGER            ),
    SET                     (StandardColumnType.SET            ),
    MULTISET                (StandardColumnType.MULTISET            ),
    SMALLDATETIME           (StandardColumnType.DATETIME               ),
    SMALLMONEY              (StandardColumnType.MONEY               ),
    SMALLINT                (StandardColumnType.SMALLINT            ),
    SMALLSERIAL             (StandardColumnType.SERIAL            ),
    SQL_VARIANT             (StandardColumnType.ILLEGAL            ),
    SYSNAME                 (StandardColumnType.ILLEGAL            ),
    TEXT                    (StandardColumnType.TEXT               ),
    TIME                    (StandardColumnType.DATETIME               ),
    TIMEZ                   (StandardColumnType.DATETIME               ),
    TIMESTAMP               (StandardColumnType.DATETIME            ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.DATETIME            ),
    TIMESTAMP_WITH_ZONE          (StandardColumnType.DATETIME            ),
    TSQUERY                 (StandardColumnType.ILLEGAL            ),
    TSVECTOR                (StandardColumnType.ILLEGAL            ),
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL            ),
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL            ),
    UUID                    (StandardColumnType.ILLEGAL            ),
    UROWID                  (StandardColumnType.ILLEGAL            ),
    VARBIT                  (StandardColumnType.BLOB               ),
    TINYBLOB                (StandardColumnType.BLOB               ),
    TINYINT                 (StandardColumnType.SMALLINT            ),
    TINYTEXT                (StandardColumnType.TEXT               ),
    VARBINARY               (StandardColumnType.BLOB               ),
    VARCHAR                 (StandardColumnType.VARCHAR               ),
    VARCHAR2                (StandardColumnType.VARCHAR               ),
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
