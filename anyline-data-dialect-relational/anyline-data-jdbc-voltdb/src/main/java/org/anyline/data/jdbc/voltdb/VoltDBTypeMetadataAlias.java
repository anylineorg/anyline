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

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum VoltDBTypeMetadataAlias implements TypeMetadataAlias {
        /*
        * 注意日期时间格式
        * For String variables, the text must be formatted as either YYYY-MM-DD hh.mm.ss.nnnnnn or just the date portion YYYY-MM-DD.
        * */
        BFILE                   (StandardTypeMetadata.ILLEGAL               ),
        BINARY_DOUBLE           (StandardTypeMetadata.DECIMAL                ),
        BINARY_FLOAT            (StandardTypeMetadata.FLOAT           ),
        BIGINT                  (StandardTypeMetadata.BIGINT                ),
        BIGSERIAL               (StandardTypeMetadata.BIGINT                ),
        BINARY                  (StandardTypeMetadata.VARBINARY                ),
        BIT                     (StandardTypeMetadata.TINYINT                   ),
        BLOB                    (StandardTypeMetadata.VARBINARY                  ),
        BOOL                    (StandardTypeMetadata.TINYINT                   ),
        BOX                     (StandardTypeMetadata.ILLEGAL               ),
        BYTEA                   (StandardTypeMetadata.ILLEGAL             ),
        CHAR                    (StandardTypeMetadata.VARCHAR                  ),
        CIDR                    (StandardTypeMetadata.ILLEGAL               ),
        CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
        CLOB                    (StandardTypeMetadata.VARCHAR                  ),
        DATE                    (StandardTypeMetadata.TIMESTAMP                  ),
        DATETIME                (StandardTypeMetadata.TIMESTAMP              ),
        DATETIME2               (StandardTypeMetadata.TIMESTAMP             ),
        DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP        ),
        DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
        DOUBLE                  (StandardTypeMetadata.DECIMAL                ),
        ENUM                    (StandardTypeMetadata.ILLEGAL                  ),
        FLOAT                   (StandardTypeMetadata.FLOAT           ),
        FLOAT4                  (StandardTypeMetadata.FLOAT           ),
        FLOAT8                  (StandardTypeMetadata.FLOAT           ),
        GEOGRAPHY               (StandardTypeMetadata.GEOGRAPHY               ),
        GEOMETRY                (StandardTypeMetadata.GEOGRAPHY              ),
        GEOMETRYCOLLECTION      (StandardTypeMetadata.GEOGRAPHY    ),
        HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
        IMAGE                   (StandardTypeMetadata.VARBINARY                  ),
        INET                    (StandardTypeMetadata.ILLEGAL               ),
        INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
        INT                     (StandardTypeMetadata.INTEGER                   ),
        INT2                    (StandardTypeMetadata.INTEGER                   ),
        INT4                    (StandardTypeMetadata.INTEGER                   ),
        INT8                    (StandardTypeMetadata.BIGINT                ),
        INTEGER                 (StandardTypeMetadata.INTEGER                   ),
        JSON                    (StandardTypeMetadata.VARCHAR                  ),
        JSONB                   (StandardTypeMetadata.VARBINARY                  ),
        LINE                    (StandardTypeMetadata.GEOGRAPHY            ),
        LINESTRING              (StandardTypeMetadata.GEOGRAPHY            ),
        LONG                    (StandardTypeMetadata.BIGINT                ),
        LONGBLOB                (StandardTypeMetadata.VARBINARY             ),
        LONGTEXT                (StandardTypeMetadata.VARCHAR              ),
        LSEG                    (StandardTypeMetadata.ILLEGAL               ),
        MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
        MONEY                   (StandardTypeMetadata.DECIMAL               ),
        NUMBER                  (StandardTypeMetadata.DECIMAL               ),
        NCHAR                   (StandardTypeMetadata.VARCHAR               ),
        NCLOB                   (StandardTypeMetadata.VARCHAR                  ),
        NTEXT                   (StandardTypeMetadata.VARCHAR                  ),
        NVARCHAR                (StandardTypeMetadata.VARCHAR               ),
        NVARCHAR2               (StandardTypeMetadata.VARCHAR               ),
        PATH                    (StandardTypeMetadata.ILLEGAL               ),
        MEDIUMBLOB              (StandardTypeMetadata.GEOGRAPHY            ),
        MEDIUMINT               (StandardTypeMetadata.GEOGRAPHY             ),
        MEDIUMTEXT              (StandardTypeMetadata.GEOGRAPHY            ),
        MULTILINE               (StandardTypeMetadata.GEOGRAPHY       ),
        MULTILINESTRING         (StandardTypeMetadata.GEOGRAPHY       ),
        MULTIPOINT              (StandardTypeMetadata.GEOGRAPHY            ),
        MULTIPOLYGON            (StandardTypeMetadata.GEOGRAPHY          ),
        NUMERIC                 (StandardTypeMetadata.DECIMAL               ),
        POINT                   (StandardTypeMetadata.GEOGRAPHY_POINT                 ),
        GEOGRAPHY_POINT         (StandardTypeMetadata.GEOGRAPHY_POINT                 ),
        POLYGON                 (StandardTypeMetadata.POLYGON               ),
        REAL                    (StandardTypeMetadata.FLOAT                  ),
        RAW                     (StandardTypeMetadata.ILLEGAL               ),
        ROWID                   (StandardTypeMetadata.ILLEGAL               ),
        SERIAL                  (StandardTypeMetadata.TINYINT               ),
        SERIAL2                 (StandardTypeMetadata.TINYINT               ),
        SERIAL4                 (StandardTypeMetadata.INTEGER                   ),
        SERIAL8                 (StandardTypeMetadata.BIGINT                ),
        SET                     (StandardTypeMetadata.ILLEGAL                   ),
        SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP              ),
        SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
        SMALLINT                (StandardTypeMetadata.TINYINT               ),
        SMALLSERIAL             (StandardTypeMetadata.TINYINT               ),
        SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
        STRING                  (StandardTypeMetadata.VARCHAR                ),
        SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
        TEXT                    (StandardTypeMetadata.VARCHAR                  ),
        TIME                    (StandardTypeMetadata.TIMESTAMP                  ),
        TIMEZ                   (StandardTypeMetadata.TIMESTAMP                  ),
        TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP             ),
        TIMESTAMP_WITH_TIME_ZONE          (StandardTypeMetadata.TIMESTAMP             ),
        TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
        TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
        TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
        UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
        UUID                    (StandardTypeMetadata.ILLEGAL               ),
        UROWID                  (StandardTypeMetadata.ILLEGAL               ),
        VARBIT                  (StandardTypeMetadata.VARBINARY             ),
        TINYBLOB                (StandardTypeMetadata.VARBINARY              ),
        TINYINT                 (StandardTypeMetadata.TINYINT               ),
        TINYTEXT                (StandardTypeMetadata.VARCHAR              ),
        VARBINARY               (StandardTypeMetadata.VARBINARY             ),
        VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
        VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
        XML                     (StandardTypeMetadata.VARCHAR                  ),
        YEAR                    (StandardTypeMetadata.TIMESTAMP                  );

        private final TypeMetadata standard;
        private int ignoreLength = -1;
        private int ignorePrecision = -1;
        private int ignoreScale = -1;
        private String lengthRefer;
        private String precisionRefer;
        private String scaleRefer;
        private TypeMetadata.Config config;

        VoltDBTypeMetadataAlias(TypeMetadata standard){
                this.standard = standard;
        }

        VoltDBTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
                this.standard = standard;
                this.lengthRefer = lengthRefer;
                this.precisionRefer = precisionRefer;
                this.scaleRefer = scaleRefer;
                this.ignoreLength = ignoreLength;
                this.ignorePrecision = ignorePrecision;
                this.ignoreScale = ignoreScale;
        }

        VoltDBTypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
                this.standard = standard;
                this.ignoreLength = ignoreLength;
                this.ignorePrecision = ignorePrecision;
                this.ignoreScale = ignoreScale;
        }

        @Override
        public TypeMetadata standard() {
                return standard;
        }

        @Override
        public TypeMetadata.Config config() {
                if(null == config){
                        config = new TypeMetadata.Config();
                        config.setLengthRefer(lengthRefer).setPrecisionRefer(precisionRefer).setScaleRefer(scaleRefer);
                        config.setIgnoreLength(ignoreLength).setIgnorePrecision(ignorePrecision).setIgnoreScale(ignoreScale);
                }
                return config;
        }
}
