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
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;
public enum IoTDBColumnTypeAlias implements ColumnTypeAlias {
        BFILE                   (StandardTypeMetadata.ILLEGAL      ),
        BINARY_DOUBLE           (StandardTypeMetadata.DOUBLE       ),
        BINARY_FLOAT            (StandardTypeMetadata.ILLEGAL      ),
        BIGINT                  (StandardTypeMetadata.INT64        ),
        BIGSERIAL               (StandardTypeMetadata.INT64        ),
        BINARY                  (StandardTypeMetadata.ILLEGAL      ),
        BIT                     (StandardTypeMetadata.ILLEGAL      ),
        BLOB                    (StandardTypeMetadata.ILLEGAL      ),
        BOOL                    (StandardTypeMetadata.ILLEGAL      ),
        BOX                     (StandardTypeMetadata.ILLEGAL      ),
        BYTEA                   (StandardTypeMetadata.ILLEGAL      ),
        CHAR                    (StandardTypeMetadata.TEXT         ),
        CIDR                    (StandardTypeMetadata.ILLEGAL      ),
        CIRCLE                  (StandardTypeMetadata.ILLEGAL      ),
        CLOB                    (StandardTypeMetadata.TEXT         ),
        DATE                    (StandardTypeMetadata.INT64        ),
        DATETIME                (StandardTypeMetadata.INT64        ),
        DATETIME2               (StandardTypeMetadata.INT64        ),
        DATETIMEOFFSET          (StandardTypeMetadata.INT64        ),
        DECIMAL                 (StandardTypeMetadata.DOUBLE       ),
        DOUBLE                  (StandardTypeMetadata.DOUBLE       ),
        ENUM                    (StandardTypeMetadata.ILLEGAL      ),
        FLOAT                   (StandardTypeMetadata.FLOAT        ),
        FLOAT4                  (StandardTypeMetadata.FLOAT        ),
        FLOAT8                  (StandardTypeMetadata.FLOAT        ),
        GEOGRAPHY               (StandardTypeMetadata.ILLEGAL      ),
        GEOMETRY                (StandardTypeMetadata.ILLEGAL      ),
        GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL      ),
        HIERARCHYID             (StandardTypeMetadata.ILLEGAL      ),
        IMAGE                   (StandardTypeMetadata.ILLEGAL      ),
        INET                    (StandardTypeMetadata.ILLEGAL      ),
        INTERVAL                (StandardTypeMetadata.ILLEGAL      ),
        INT                     (StandardTypeMetadata.INT32        ),
        INT2                    (StandardTypeMetadata.INT32        ),
        INT4                    (StandardTypeMetadata.INT32        ),
        INT8                    (StandardTypeMetadata.INT64        ),
        INT32                    (StandardTypeMetadata.INT32       ), //    , iotdb
        INT64                    (StandardTypeMetadata.INT64       ), //    , iotdb
        INTEGER                 (StandardTypeMetadata.INT32        ),
        JSON                    (StandardTypeMetadata.TEXT         ),
        JSONB                   (StandardTypeMetadata.ILLEGAL      ),
        LINE                    (StandardTypeMetadata.ILLEGAL      ),
        LINESTRING              (StandardTypeMetadata.ILLEGAL      ),
        LONG                    (StandardTypeMetadata.INT64        ),
        LONGBLOB                (StandardTypeMetadata.ILLEGAL      ),
        LONGTEXT                (StandardTypeMetadata.TEXT         ),
        LSEG                    (StandardTypeMetadata.ILLEGAL      ),
        MACADDR                 (StandardTypeMetadata.ILLEGAL      ),
        MONEY                   (StandardTypeMetadata.DOUBLE       ),
        NUMBER                  (StandardTypeMetadata.DOUBLE       ),
        NCHAR                   (StandardTypeMetadata.TEXT         ),
        NCLOB                   (StandardTypeMetadata.TEXT         ),
        NTEXT                   (StandardTypeMetadata.TEXT         ),
        NVARCHAR                (StandardTypeMetadata.TEXT         ),
        NVARCHAR2               (StandardTypeMetadata.TEXT         ),
        PATH                    (StandardTypeMetadata.ILLEGAL      ),
        MEDIUMBLOB              (StandardTypeMetadata.ILLEGAL      ),
        MEDIUMINT               (StandardTypeMetadata.INT32        ),
        MEDIUMTEXT              (StandardTypeMetadata.TEXT         ),
        MULTILINE               (StandardTypeMetadata.ILLEGAL      ),
        MULTILINESTRING         (StandardTypeMetadata.ILLEGAL      ),
        MULTIPOINT              (StandardTypeMetadata.ILLEGAL      ),
        MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL      ),
        NUMERIC                 (StandardTypeMetadata.DOUBLE       ),
        POINT                   (StandardTypeMetadata.ILLEGAL      ),
        POLYGON                 (StandardTypeMetadata.ILLEGAL      ),
        REAL                    (StandardTypeMetadata.DOUBLE       ),
        RAW                     (StandardTypeMetadata.ILLEGAL      ),
        ROWID                   (StandardTypeMetadata.ILLEGAL      ),
        SERIAL                  (StandardTypeMetadata.INT32        ),
        SERIAL2                 (StandardTypeMetadata.INT32        ),
        SERIAL4                 (StandardTypeMetadata.INT32        ),
        SERIAL8                 (StandardTypeMetadata.INT64        ),
        SET                     (StandardTypeMetadata.ILLEGAL      ),
        SMALLDATETIME           (StandardTypeMetadata.INT64        ),
        SMALLMONEY              (StandardTypeMetadata.DOUBLE       ),
        SMALLINT                (StandardTypeMetadata.INT32        ),
        SMALLSERIAL             (StandardTypeMetadata.INT32        ),
        SQL_VARIANT             (StandardTypeMetadata.ILLEGAL      ),
        SYSNAME                 (StandardTypeMetadata.ILLEGAL      ),
        TEXT                    (StandardTypeMetadata.TEXT         ),
        TIME                    (StandardTypeMetadata.INT64        ),
        TIMEZ                   (StandardTypeMetadata.INT64        ),
        TIMESTAMP               (StandardTypeMetadata.INT64        ),
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.INT64        ),
        TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.INT64        ),
        TSQUERY                 (StandardTypeMetadata.ILLEGAL      ),
        TSVECTOR                (StandardTypeMetadata.ILLEGAL      ),
        TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL      ),
        UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL      ),
        UUID                    (StandardTypeMetadata.ILLEGAL      ),
        UROWID                  (StandardTypeMetadata.ILLEGAL      ),
        VARBIT                  (StandardTypeMetadata.ILLEGAL      ),
        TINYBLOB                (StandardTypeMetadata.ILLEGAL      ),
        TINYINT                 (StandardTypeMetadata.INT32        ),
        TINYTEXT                (StandardTypeMetadata.TEXT         ),
        VARBINARY               (StandardTypeMetadata.TEXT         ),
        VARCHAR                 (StandardTypeMetadata.TEXT         ),
        VARCHAR2                (StandardTypeMetadata.TEXT         ),
        XML                     (StandardTypeMetadata.TEXT         ),
        YEAR                    (StandardTypeMetadata.INT32        );
        private final TypeMetadata standard;
        IoTDBColumnTypeAlias(TypeMetadata standard){
                this.standard = standard;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }
}
