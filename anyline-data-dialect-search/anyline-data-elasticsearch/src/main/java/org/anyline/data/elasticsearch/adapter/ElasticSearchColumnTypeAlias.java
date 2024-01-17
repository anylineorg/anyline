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


package org.anyline.data.elasticsearch.adapter;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.data.metadata.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum ElasticSearchColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.BINARY                 ), 
    BINARY_DOUBLE           (StandardColumnType.BINARY         ), 
    BINARY_FLOAT            (StandardColumnType.FLOAT          ), 
    BIGINT                  (StandardColumnType.LONG_TEXT                ), 
    BIGSERIAL               (StandardColumnType.LONG_TEXT                ),
    BINARY                  (StandardColumnType.BINARY                  ), 
    BIT                     (StandardColumnType.BYTE                ), 
    BLOB                    (StandardColumnType.BINARY                  ), 
    BOOL                    (StandardColumnType.BOOLEAN                ), 
    BOX                     (StandardColumnType.ILLEGAL               ), 
    BYTEA                   (StandardColumnType.BINARY                  ), 
    CHAR                    (StandardColumnType.TEXT                  ), 
    CIDR                    (StandardColumnType.ILLEGAL               ), 
    CIRCLE                  (StandardColumnType.ILLEGAL               ), 
    CLOB                    (StandardColumnType.TEXT                  ), 
    DATE                    (StandardColumnType.DATE                  ), 
    DATETIME                (StandardColumnType.DATE             ), 
    DATETIME2               (StandardColumnType.DATE             ), 
    DATETIMEOFFSET          (StandardColumnType.DATE             ), 
    DECIMAL                 (StandardColumnType.DOUBLE                ), 
    DOUBLE                  (StandardColumnType.DOUBLE                ), 
    ENUM                    (StandardColumnType.ILLEGAL               ), 
    FLOAT                   (StandardColumnType.FLOAT          ),
    FLOAT4                  (StandardColumnType.FLOAT          ), 
    FLOAT8                  (StandardColumnType.FLOAT          ), 
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ), 
    GEOMETRY                (StandardColumnType.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ),
    HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
    IMAGE                   (StandardColumnType.BINARY                  ), 
    INET                    (StandardColumnType.ILLEGAL               ), 
    INTERVAL                (StandardColumnType.ILLEGAL               ), 
    INT                     (StandardColumnType.INTEGER                   ), 
    INT2                    (StandardColumnType.INTEGER                   ), 
    INT4                    (StandardColumnType.INTEGER                   ), 
    INT8                    (StandardColumnType.LONG_TEXT                ), 
    INTEGER                 (StandardColumnType.INTEGER                   ), 
    JSON                    (StandardColumnType.OBJECT                  ), 
    JSONB                   (StandardColumnType.BINARY                  ), 
    LINE                    (StandardColumnType.ILLEGAL               ), 
    LONG                    (StandardColumnType.LONG_TEXT                  ), 
    LONGBLOB                (StandardColumnType.BINARY                  ),
    LONGTEXT                (StandardColumnType.TEXT                  ),
    LSEG                    (StandardColumnType.ILLEGAL               ), 
    MACADDR                 (StandardColumnType.ILLEGAL               ), 
    MONEY                   (StandardColumnType.DOUBLE                ),
    NUMBER                  (StandardColumnType.DOUBLE                ), 
    NCHAR                   (StandardColumnType.TEXT                 ),
    NCLOB                   (StandardColumnType.TEXT                 ), 
    NTEXT                   (StandardColumnType.TEXT                 ), 
    NVARCHAR                (StandardColumnType.TEXT             ), 
    NVARCHAR2               (StandardColumnType.TEXT             ), 
    PATH                    (StandardColumnType.ILLEGAL               ), 
    MEDIUMBLOB              (StandardColumnType.BINARY                  ), 
    MEDIUMINT               (StandardColumnType.DOUBLE                ), 
    MEDIUMTEXT              (StandardColumnType.TEXT                  ), 
    MULTILINESTRING         (StandardColumnType.ILLEGAL               ), 
    MULTIPOINT              (StandardColumnType.ILLEGAL               ), 
    MULTIPOLYGON            (StandardColumnType.ILLEGAL               ), 
    NUMERIC                 (StandardColumnType.DOUBLE                ),
    POINT                   (StandardColumnType.ILLEGAL               ), 
    POLYGON                 (StandardColumnType.ILLEGAL               ), 
    REAL                    (StandardColumnType.FLOAT          ),
    RAW                     (StandardColumnType.ILLEGAL                   ), 
    ROWID                   (StandardColumnType.ILLEGAL                 ), 
    SERIAL                  (StandardColumnType.INTEGER),
    SERIAL2                 (StandardColumnType.INTEGER                   ),
    SERIAL4                 (StandardColumnType.INTEGER                   ),
    SERIAL8                 (StandardColumnType.LONG_TEXT                ),
    SET                     (StandardColumnType.DATE               ), 
    SMALLDATETIME           (StandardColumnType.DATE             ), 
    SMALLMONEY              (StandardColumnType.DOUBLE                ), 
    SMALLINT                (StandardColumnType.DOUBLE                ), 
    SMALLSERIAL             (StandardColumnType.DOUBLE                ),
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ), 
    SYSNAME                 (StandardColumnType.ILLEGAL               ), 
    TEXT                    (StandardColumnType.TEXT                  ),  
    TIME                    (StandardColumnType.DATE             ),  
    TIMEZ                   (StandardColumnType.DATE             ), 
    TIMESTAMP               (StandardColumnType.DATE             ), 
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.DATE             ), 
    TIMESTAMP_WITH_ZONE          (StandardColumnType.DATE             ), 
    TSQUERY                 (StandardColumnType.ILLEGAL               ), 
    TSVECTOR                (StandardColumnType.ILLEGAL               ), 
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
    UUID                    (StandardColumnType.ILLEGAL               ), 
    UROWID                  (StandardColumnType.ILLEGAL                ), 
    VARBIT                  (StandardColumnType.BINARY                  ), 
    TINYBLOB                (StandardColumnType.BINARY                  ), 
    TINYINT                 (StandardColumnType.DOUBLE                ), 
    TINYTEXT                (StandardColumnType.TEXT                  ), 
    VARBINARY               (StandardColumnType.BINARY                  ), 
    VARCHAR                 (StandardColumnType.TEXT               ), 
    VARCHAR2                (StandardColumnType.TEXT               ), 
    XML                     (StandardColumnType.ILLEGAL               ), 
    YEAR                    (StandardColumnType.DATE                  ), 
    OBJECT                  (StandardColumnType.OBJECT                  ), 
    KEYWORD                 (StandardColumnType.KEYWORD                  );  
    private final TypeMetadata standard;
    private ElasticSearchColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}
