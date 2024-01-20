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


package org.anyline.data.jdbc.hana;


import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.metadata.type.init.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum HanaColumnTypeAlias implements ColumnTypeAlias {
    BFILE                   (StandardColumnType.ILLEGAL               ),
    BINARY_DOUBLE           (StandardColumnType.ILLEGAL               ),
    BINARY_FLOAT            (StandardColumnType.ILLEGAL               ),
    BIGINT                  (StandardColumnType.BIGINT                ),
    BIGSERIAL               (StandardColumnType.BIGINT                ),
    BINARY                  (StandardColumnType.BINARY                ),
    BIT                     (StandardColumnType.BOOLEAN               ),
    BLOB                    (StandardColumnType.BLOB                  ),
    BOOL                    (StandardColumnType.BOOLEAN               ),
    BOOLEAN                 (StandardColumnType.BOOLEAN               ),
    BOX                     (StandardColumnType.ILLEGAL               ),
    BYTEA                   (StandardColumnType.ILLEGAL               ),
    CHAR                    (StandardColumnType.CHAR                  ),
    CIDR                    (StandardColumnType.ILLEGAL               ),
    CIRCLE                  (StandardColumnType.ILLEGAL               ),
    CLOB                    (StandardColumnType.NCLOB                 ),
    DATE                    (StandardColumnType.DATE                  ),
    DATETIME                (StandardColumnType.TIMESTAMP             ),
    DATETIME2               (StandardColumnType.TIMESTAMP             ),
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP             ),
    DECIMAL                 (StandardColumnType.DECIMAL               ),
    DOUBLE                  (StandardColumnType.DOUBLE                ),
    ENUM                    (StandardColumnType.ILLEGAL               ),
    FLOAT                   (StandardColumnType.DOUBLE                ),
    FLOAT4                  (StandardColumnType.DOUBLE                ),
    FLOAT8                  (StandardColumnType.DOUBLE                ),
    GEOGRAPHY               (StandardColumnType.ILLEGAL               ),
    GEOMETRY                (StandardColumnType.ST_GEOMETRY           ),
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL               ),
    HIERARCHYID             (StandardColumnType.ILLEGAL               ),
    IMAGE                   (StandardColumnType.BLOB                  ),
    INET                    (StandardColumnType.ILLEGAL               ),
    INTERVAL                (StandardColumnType.ILLEGAL               ),
    INT                     (StandardColumnType.INTEGER               ),       
    INT2                    (StandardColumnType.INTEGER               ),
    INT4                    (StandardColumnType.INTEGER               ),
    INT8                    (StandardColumnType.INTEGER               ),
    INTEGER                 (StandardColumnType.INTEGER               ), 
    JSON                    (StandardColumnType.NCLOB                 ), 
    JSONB                   (StandardColumnType.BLOB                  ),
    LINE                    (StandardColumnType.ILLEGAL               ), 
    LONG                    (StandardColumnType.LONG_TEXT                  ),
    LONGBLOB                (StandardColumnType.BLOB                  ),
    LONGTEXT                (StandardColumnType.NCLOB                 ),
    LSEG                    (StandardColumnType.ST_GEOMETRY           ),
    MACADDR                 (StandardColumnType.ST_GEOMETRY           ),
    MONEY                   (StandardColumnType.DECIMAL               ), 
    NUMBER                  (StandardColumnType.DECIMAL               ),
    NCHAR                   (StandardColumnType.NVARCHAR              ),
    NCLOB                   (StandardColumnType.NCLOB                 ),
    NTEXT                   (StandardColumnType.NCLOB                 ),
    NVARCHAR                (StandardColumnType.NVARCHAR              ),
    NVARCHAR2               (StandardColumnType.NVARCHAR              ),
    PATH                    (StandardColumnType.ST_GEOMETRY           ),
    MEDIUMBLOB              (StandardColumnType.BLOB                  ),
    MEDIUMINT               (StandardColumnType.INTEGER               ),
    MEDIUMTEXT              (StandardColumnType.NCLOB                 ),
    MULTILINESTRING         (StandardColumnType.ST_GEOMETRY           ),
    MULTIPOINT              (StandardColumnType.ST_GEOMETRY           ),
    MULTIPOLYGON            (StandardColumnType.ST_GEOMETRY           ),
    NUMERIC                 (StandardColumnType.DECIMAL               ),
    POINT                   (StandardColumnType.ST_POINT              ), 
    POLYGON                 (StandardColumnType.ST_GEOMETRY           ), 
    REAL                    (StandardColumnType.REAL                  ),
    RAW                     (StandardColumnType.ILLEGAL               ),
    ROWID                   (StandardColumnType.ILLEGAL               ),
    SECONDDATE              (StandardColumnType.SECONDDATE            ),
    SERIAL                  (StandardColumnType.INTEGER               ),
    SERIAL2                 (StandardColumnType.INTEGER               ),
    SERIAL4                 (StandardColumnType.INTEGER               ),
    SERIAL8                 (StandardColumnType.BIGINT                ),
    SET                     (StandardColumnType.ILLEGAL               ),
    SMALLDATETIME           (StandardColumnType.TIMESTAMP             ),
    SMALLDECIMAL            (StandardColumnType.SMALLDECIMAL          ),
    SMALLMONEY              (StandardColumnType.DECIMAL               ),
    SMALLINT                (StandardColumnType.SMALLINT              ),
    SMALLSERIAL             (StandardColumnType.INTEGER               ),
    SQL_VARIANT             (StandardColumnType.ILLEGAL               ),
    ST_GEOMETRY             (StandardColumnType.ST_GEOMETRY           ),
    ST_POINT                (StandardColumnType.ST_POINT              ),
    SYSNAME                 (StandardColumnType.ILLEGAL               ),
    TEXT                    (StandardColumnType.NCLOB                 ),
    TIME                    (StandardColumnType.TIME                  ), 
    TIMEZ                   (StandardColumnType.TIMESTAMP             ),
    TIMESTAMP               (StandardColumnType.TIMESTAMP             ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ),
    TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP             ),
    TSQUERY                 (StandardColumnType.ILLEGAL               ),
    TSVECTOR                (StandardColumnType.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ),
    UUID                    (StandardColumnType.ILLEGAL               ),
    UROWID                  (StandardColumnType.VARCHAR               ),
    VARBIT                  (StandardColumnType.BLOB                  ),
    TINYBLOB                (StandardColumnType.BLOB                  ),
    TINYINT                 (StandardColumnType.TINYINT               ),
    TINYTEXT                (StandardColumnType.NCLOB                 ),
    VARBINARY               (StandardColumnType.VARBINARY             ),
    VARCHAR                 (StandardColumnType.VARCHAR               ),
    VARCHAR2                (StandardColumnType.VARCHAR               ),
    XML                     (StandardColumnType.NVARCHAR              ),
    YEAR                    (StandardColumnType.INTEGER               );
    private final TypeMetadata standard;
    private HanaColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }
}
