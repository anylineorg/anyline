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


package org.anyline.data.jdbc.voltdb;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.metadata.type.init.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum VoltDBColumnTypeAlias implements ColumnTypeAlias {
        /*
        * 注意日期时间格式
        * For String variables, the text must be formatted as either YYYY-MM-DD hh.mm.ss.nnnnnn or just the date portion YYYY-MM-DD.
        * */
        BFILE                   (StandardColumnType.ILLEGAL               ),
        BINARY_DOUBLE           (StandardColumnType.DECIMAL                ),
        BINARY_FLOAT            (StandardColumnType.FLOAT           ),
        BIGINT                  (StandardColumnType.BIGINT                ),
        BIGSERIAL               (StandardColumnType.BIGINT                ),
        BINARY                  (StandardColumnType.VARBINARY                ),
        BIT                     (StandardColumnType.TINYINT                   ),
        BLOB                    (StandardColumnType.VARBINARY                  ),
        BOOL                    (StandardColumnType.TINYINT                   ), 
        BOX                     (StandardColumnType.ILLEGAL               ), 
        BYTEA                   (StandardColumnType.ILLEGAL             ), 
        CHAR                    (StandardColumnType.VARCHAR                  ),
        CIDR                    (StandardColumnType.ILLEGAL               ), 
        CIRCLE                  (StandardColumnType.ILLEGAL               ), 
        CLOB                    (StandardColumnType.VARCHAR                  ),
        DATE                    (StandardColumnType.TIMESTAMP                  ), 
        DATETIME                (StandardColumnType.TIMESTAMP              ),
        DATETIME2               (StandardColumnType.TIMESTAMP             ),
        DATETIMEOFFSET          (StandardColumnType.TIMESTAMP        ),
        DECIMAL                 (StandardColumnType.DECIMAL               ), 
        DOUBLE                  (StandardColumnType.DECIMAL                ), 
        ENUM                    (StandardColumnType.ILLEGAL                  ), 
        FLOAT                   (StandardColumnType.FLOAT           ),
        FLOAT4                  (StandardColumnType.FLOAT           ), 
        FLOAT8                  (StandardColumnType.FLOAT           ), 
        GEOGRAPHY               (StandardColumnType.GEOGRAPHY               ),
        GEOMETRY                (StandardColumnType.GEOGRAPHY              ),
        GEOMETRYCOLLECTION      (StandardColumnType.GEOGRAPHY    ),
        HIERARCHYID             (StandardColumnType.ILLEGAL               ), 
        IMAGE                   (StandardColumnType.VARBINARY                  ), 
        INET                    (StandardColumnType.ILLEGAL               ), 
        INTERVAL                (StandardColumnType.ILLEGAL               ), 
        INT                     (StandardColumnType.INTEGER                   ),
        INT2                    (StandardColumnType.INTEGER                   ), 
        INT4                    (StandardColumnType.INTEGER                   ), 
        INT8                    (StandardColumnType.BIGINT                ), 
        INTEGER                 (StandardColumnType.INTEGER                   ),
        JSON                    (StandardColumnType.VARCHAR                  ),
        JSONB                   (StandardColumnType.VARBINARY                  ), 
        LINE                    (StandardColumnType.GEOGRAPHY            ), 
        LINESTRING              (StandardColumnType.GEOGRAPHY            ),
        LONG                    (StandardColumnType.BIGINT                ),
        LONGBLOB                (StandardColumnType.VARBINARY             ),
        LONGTEXT                (StandardColumnType.VARCHAR              ),
        LSEG                    (StandardColumnType.ILLEGAL               ), 
        MACADDR                 (StandardColumnType.ILLEGAL               ), 
        MONEY                   (StandardColumnType.DECIMAL               ), 
        NUMBER                  (StandardColumnType.DECIMAL               ),
        NCHAR                   (StandardColumnType.VARCHAR               ),
        NCLOB                   (StandardColumnType.VARCHAR                  ),
        NTEXT                   (StandardColumnType.VARCHAR                  ), 
        NVARCHAR                (StandardColumnType.VARCHAR               ), 
        NVARCHAR2               (StandardColumnType.VARCHAR               ),
        PATH                    (StandardColumnType.ILLEGAL               ), 
        MEDIUMBLOB              (StandardColumnType.GEOGRAPHY            ), 
        MEDIUMINT               (StandardColumnType.GEOGRAPHY             ), 
        MEDIUMTEXT              (StandardColumnType.GEOGRAPHY            ), 
        MULTILINE               (StandardColumnType.GEOGRAPHY       ), 
        MULTILINESTRING         (StandardColumnType.GEOGRAPHY       ), 
        MULTIPOINT              (StandardColumnType.GEOGRAPHY            ), 
        MULTIPOLYGON            (StandardColumnType.GEOGRAPHY          ), 
        NUMERIC                 (StandardColumnType.DECIMAL               ),
        POINT                   (StandardColumnType.GEOGRAPHY_POINT                 ),
        GEOGRAPHY_POINT         (StandardColumnType.GEOGRAPHY_POINT                 ),
        POLYGON                 (StandardColumnType.POLYGON               ),
        REAL                    (StandardColumnType.FLOAT                  ),
        RAW                     (StandardColumnType.ILLEGAL               ),
        ROWID                   (StandardColumnType.ILLEGAL               ),
        SERIAL                  (StandardColumnType.TINYINT               ),
        SERIAL2                 (StandardColumnType.TINYINT               ),
        SERIAL4                 (StandardColumnType.INTEGER                   ),
        SERIAL8                 (StandardColumnType.BIGINT                ),
        SET                     (StandardColumnType.ILLEGAL                   ), 
        SMALLDATETIME           (StandardColumnType.TIMESTAMP              ), 
        SMALLMONEY              (StandardColumnType.DECIMAL               ), 
        SMALLINT                (StandardColumnType.TINYINT               ), 
        SMALLSERIAL             (StandardColumnType.TINYINT               ),
        SQL_VARIANT             (StandardColumnType.ILLEGAL               ),
        STRING                  (StandardColumnType.VARCHAR                ), 
        SYSNAME                 (StandardColumnType.ILLEGAL               ), 
        TEXT                    (StandardColumnType.VARCHAR                  ),
        TIME                    (StandardColumnType.TIMESTAMP                  ),
        TIMEZ                   (StandardColumnType.TIMESTAMP                  ), 
        TIMESTAMP               (StandardColumnType.TIMESTAMP             ), 
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP             ), 
        TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP             ), 
        TSQUERY                 (StandardColumnType.ILLEGAL               ), 
        TSVECTOR                (StandardColumnType.ILLEGAL               ), 
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL               ), 
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL               ), 
        UUID                    (StandardColumnType.ILLEGAL               ), 
        UROWID                  (StandardColumnType.ILLEGAL               ),
        VARBIT                  (StandardColumnType.VARBINARY             ), 
        TINYBLOB                (StandardColumnType.VARBINARY              ), 
        TINYINT                 (StandardColumnType.TINYINT               ),
        TINYTEXT                (StandardColumnType.VARCHAR              ), 
        VARBINARY               (StandardColumnType.VARBINARY             ),
        VARCHAR                 (StandardColumnType.VARCHAR               ), 
        VARCHAR2                (StandardColumnType.VARCHAR               ),
        XML                     (StandardColumnType.VARCHAR                  ),
        YEAR                    (StandardColumnType.TIMESTAMP                  ); 
        private final TypeMetadata standard;
        VoltDBColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
