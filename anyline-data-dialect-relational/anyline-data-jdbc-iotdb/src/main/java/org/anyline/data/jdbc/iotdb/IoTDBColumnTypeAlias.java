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


package org.anyline.data.jdbc.iotdb;

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.metadata.type.init.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;
public enum IoTDBColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardColumnType.ILLEGAL      ),  
        BINARY_DOUBLE           (StandardColumnType.DOUBLE       ),  
        BINARY_FLOAT            (StandardColumnType.ILLEGAL      ),  
        BIGINT                  (StandardColumnType.INT64        ),  
        BIGSERIAL               (StandardColumnType.INT64        ), 
        BINARY                  (StandardColumnType.ILLEGAL      ),  
        BIT                     (StandardColumnType.ILLEGAL      ),  
        BLOB                    (StandardColumnType.ILLEGAL      ), 
        BOOL                    (StandardColumnType.ILLEGAL      ), 
        BOX                     (StandardColumnType.ILLEGAL      ), 
        BYTEA                   (StandardColumnType.ILLEGAL      ), 
        CHAR                    (StandardColumnType.TEXT         ),  
        CIDR                    (StandardColumnType.ILLEGAL      ), 
        CIRCLE                  (StandardColumnType.ILLEGAL      ), 
        CLOB                    (StandardColumnType.TEXT         ), 
        DATE                    (StandardColumnType.INT64        ), 
        DATETIME                (StandardColumnType.INT64        ), 
        DATETIME2               (StandardColumnType.INT64        ), 
        DATETIMEOFFSET          (StandardColumnType.INT64        ), 
        DECIMAL                 (StandardColumnType.DOUBLE       ), 
        DOUBLE                  (StandardColumnType.DOUBLE       ),  
        ENUM                    (StandardColumnType.ILLEGAL      ),  
        FLOAT                   (StandardColumnType.FLOAT        ),
        FLOAT4                  (StandardColumnType.FLOAT        ), 
        FLOAT8                  (StandardColumnType.FLOAT        ), 
        GEOGRAPHY               (StandardColumnType.ILLEGAL      ), 
        GEOMETRY                (StandardColumnType.ILLEGAL      ),
        GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL      ),
        HIERARCHYID             (StandardColumnType.ILLEGAL      ), 
        IMAGE                   (StandardColumnType.ILLEGAL      ), 
        INET                    (StandardColumnType.ILLEGAL      ), 
        INTERVAL                (StandardColumnType.ILLEGAL      ), 
        INT                     (StandardColumnType.INT32        ),  
        INT2                    (StandardColumnType.INT32        ), 
        INT4                    (StandardColumnType.INT32        ), 
        INT8                    (StandardColumnType.INT64        ), 
        INT32                    (StandardColumnType.INT32       ), //    , iotdb
        INT64                    (StandardColumnType.INT64       ), //    , iotdb
        INTEGER                 (StandardColumnType.INT32        ), 
        JSON                    (StandardColumnType.TEXT         ), 
        JSONB                   (StandardColumnType.ILLEGAL      ), 
        LINE                    (StandardColumnType.ILLEGAL      ), 
        LINESTRING              (StandardColumnType.ILLEGAL      ),
        LONG                    (StandardColumnType.INT64        ), 
        LONGBLOB                (StandardColumnType.ILLEGAL      ),
        LONGTEXT                (StandardColumnType.TEXT         ),
        LSEG                    (StandardColumnType.ILLEGAL      ), 
        MACADDR                 (StandardColumnType.ILLEGAL      ), 
        MONEY                   (StandardColumnType.DOUBLE       ),
        NUMBER                  (StandardColumnType.DOUBLE       ), 
        NCHAR                   (StandardColumnType.TEXT         ),
        NCLOB                   (StandardColumnType.TEXT         ), 
        NTEXT                   (StandardColumnType.TEXT         ), 
        NVARCHAR                (StandardColumnType.TEXT         ), 
        NVARCHAR2               (StandardColumnType.TEXT         ), 
        PATH                    (StandardColumnType.ILLEGAL      ), 
        MEDIUMBLOB              (StandardColumnType.ILLEGAL      ),  
        MEDIUMINT               (StandardColumnType.INT32        ),  
        MEDIUMTEXT              (StandardColumnType.TEXT         ),  
        MULTILINE               (StandardColumnType.ILLEGAL      ),  
        MULTILINESTRING         (StandardColumnType.ILLEGAL      ),  
        MULTIPOINT              (StandardColumnType.ILLEGAL      ),  
        MULTIPOLYGON            (StandardColumnType.ILLEGAL      ),  
        NUMERIC                 (StandardColumnType.DOUBLE       ),
        POINT                   (StandardColumnType.ILLEGAL      ), 
        POLYGON                 (StandardColumnType.ILLEGAL      ), 
        REAL                    (StandardColumnType.DOUBLE       ),
        RAW                     (StandardColumnType.ILLEGAL      ), 
        ROWID                   (StandardColumnType.ILLEGAL      ), 
        SERIAL                  (StandardColumnType.INT32        ), 
        SERIAL2                 (StandardColumnType.INT32        ), 
        SERIAL4                 (StandardColumnType.INT32        ), 
        SERIAL8                 (StandardColumnType.INT64        ), 
        SET                     (StandardColumnType.ILLEGAL      ),  
        SMALLDATETIME           (StandardColumnType.INT64        ), 
        SMALLMONEY              (StandardColumnType.DOUBLE       ), 
        SMALLINT                (StandardColumnType.INT32        ),  
        SMALLSERIAL             (StandardColumnType.INT32        ), 
        SQL_VARIANT             (StandardColumnType.ILLEGAL      ), 
        SYSNAME                 (StandardColumnType.ILLEGAL      ), 
        TEXT                    (StandardColumnType.TEXT         ),
        TIME                    (StandardColumnType.INT64        ),  
        TIMEZ                   (StandardColumnType.INT64        ), 
        TIMESTAMP               (StandardColumnType.INT64        ), 
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.INT64        ), 
        TIMESTAMP_WITH_ZONE          (StandardColumnType.INT64        ), 
        TSQUERY                 (StandardColumnType.ILLEGAL      ), 
        TSVECTOR                (StandardColumnType.ILLEGAL      ), 
        TXID_SNAPSHOT           (StandardColumnType.ILLEGAL      ), 
        UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL      ), 
        UUID                    (StandardColumnType.ILLEGAL      ), 
        UROWID                  (StandardColumnType.ILLEGAL      ), 
        VARBIT                  (StandardColumnType.ILLEGAL      ), 
        TINYBLOB                (StandardColumnType.ILLEGAL      ),  
        TINYINT                 (StandardColumnType.INT32        ), 
        TINYTEXT                (StandardColumnType.TEXT         ),  
        VARBINARY               (StandardColumnType.TEXT         ), 
        VARCHAR                 (StandardColumnType.TEXT         ), 
        VARCHAR2                (StandardColumnType.TEXT         ),  
        XML                     (StandardColumnType.TEXT         ),
        YEAR                    (StandardColumnType.INT32        );  
        private final TypeMetadata standard;
        IoTDBColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
