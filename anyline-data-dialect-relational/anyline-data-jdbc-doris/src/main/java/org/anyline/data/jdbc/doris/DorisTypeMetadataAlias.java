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


package org.anyline.data.jdbc.doris;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum DorisTypeMetadataAlias implements TypeMetadataAlias {
        ARRAY                   (StandardTypeMetadata.ARRAY                ),
        AGG_STATE               (StandardTypeMetadata.AGG_STATE            ),
        BFILE                   (StandardTypeMetadata.ILLEGAL              ),
        BINARY_DOUBLE           (StandardTypeMetadata.DOUBLE               ),
        BINARY_FLOAT            (StandardTypeMetadata.FLOAT                ),
        BIGINT                  (StandardTypeMetadata.BIGINT               ),
        BIGSERIAL               (StandardTypeMetadata.BIGINT               ),
        BINARY                  (StandardTypeMetadata.ILLEGAL              ),
        BIT                     (StandardTypeMetadata.TINYINT              ),
        BLOB                    (StandardTypeMetadata.ILLEGAL              ),
        BOOL                    (StandardTypeMetadata.BOOL                 ),
        BOX                     (StandardTypeMetadata.ILLEGAL              ),
        BYTEA                   (StandardTypeMetadata.ILLEGAL              ),
        CHAR                    (StandardTypeMetadata.CHAR                 ),
        CIDR                    (StandardTypeMetadata.ILLEGAL              ),
        CIRCLE                  (StandardTypeMetadata.ILLEGAL              ),
        CLOB                    (StandardTypeMetadata.VARCHAR              ),
        DATE                    (StandardTypeMetadata.DATE                 ),
        DATETIME                (StandardTypeMetadata.DATETIME             ),
        DATETIME2               (StandardTypeMetadata.DATETIME             ),
        DATETIMEOFFSET          (StandardTypeMetadata.DATETIME             ),
        DECIMAL                 (StandardTypeMetadata.DECIMAL              ),
        DOUBLE                  (StandardTypeMetadata.DOUBLE               , 1, 1, 1),
        ENUM                    (StandardTypeMetadata.ILLEGAL              ),
        FLOAT                   (StandardTypeMetadata.FLOAT                ,1, 1, 1),
        FLOAT4                  (StandardTypeMetadata.FLOAT                ),
        FLOAT8                  (StandardTypeMetadata.FLOAT                ),
        GEOGRAPHY               (StandardTypeMetadata.ILLEGAL              ),
        GEOMETRY                (StandardTypeMetadata.ILLEGAL              ),
        GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL              ),
        HIERARCHYID             (StandardTypeMetadata.ILLEGAL              ),
        HLL                     (StandardTypeMetadata.HLL                  ),
        HYPERLOGLOG             (StandardTypeMetadata.HLL                  ),
        IMAGE                   (StandardTypeMetadata.ILLEGAL              ),
        INET                    (StandardTypeMetadata.ILLEGAL              ),
        INTERVAL                (StandardTypeMetadata.ILLEGAL              ),
        INT                     (StandardTypeMetadata.INT                  ),
        INT2                    (StandardTypeMetadata.INT                  ),
        INT4                    (StandardTypeMetadata.INT                  ),
        INT8                    (StandardTypeMetadata.BIGINT               ),
        INTEGER                 (StandardTypeMetadata.INT                  ),
        JSON                    (StandardTypeMetadata.JSON                 ),
        JSONB                   (StandardTypeMetadata.ILLEGAL              ),
        LARGEINT                    (StandardTypeMetadata.LARGEINT              ),
        LINE                    (StandardTypeMetadata.ILLEGAL              ),
        LINESTRING              (StandardTypeMetadata.ILLEGAL              ),
        LONG                    (StandardTypeMetadata.BIGINT               ),
        LONGBLOB                (StandardTypeMetadata.ILLEGAL              ),
        LONGTEXT                (StandardTypeMetadata.VARCHAR              ),
        LSEG                    (StandardTypeMetadata.ILLEGAL              ),
        MACADDR                 (StandardTypeMetadata.ILLEGAL              ),
        MONEY                   (StandardTypeMetadata.DECIMAL              ),
        NUMBER                  (StandardTypeMetadata.DECIMAL              ),
        NCHAR                   (StandardTypeMetadata.VARCHAR              ),
        NCLOB                   (StandardTypeMetadata.VARCHAR              ),
        NTEXT                   (StandardTypeMetadata.VARCHAR              ),
        NVARCHAR                (StandardTypeMetadata.VARCHAR              ),
        NVARCHAR2               (StandardTypeMetadata.VARCHAR              ),
        PATH                    (StandardTypeMetadata.ILLEGAL              ),
        MAP                     (StandardTypeMetadata.MAP                  ),
        MEDIUMBLOB              (StandardTypeMetadata.ILLEGAL              ),
        MEDIUMINT               (StandardTypeMetadata.INT                  ),
        MEDIUMTEXT              (StandardTypeMetadata.INT                  ),
        MULTILINE               (StandardTypeMetadata.ILLEGAL              ),
        MULTILINESTRING         (StandardTypeMetadata.ILLEGAL              ),
        MULTIPOINT              (StandardTypeMetadata.ILLEGAL              ),
        MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL              ),
        NUMERIC                 (StandardTypeMetadata.DECIMAL              ),
        POINT                   (StandardTypeMetadata.ILLEGAL              ),
        GEOGRAPHY_POINT         (StandardTypeMetadata.ILLEGAL              ),
        POLYGON                 (StandardTypeMetadata.ILLEGAL              ),
        REAL                    (StandardTypeMetadata.ILLEGAL              ),
        RAW                     (StandardTypeMetadata.ILLEGAL              ),
        ROWID                   (StandardTypeMetadata.ILLEGAL              ),
        SERIAL                  (StandardTypeMetadata.INT               ),
        SERIAL2                 (StandardTypeMetadata.INT               ),
        SERIAL4                 (StandardTypeMetadata.INT                   ),
        SERIAL8                 (StandardTypeMetadata.BIGINT                ),
        SET                     (StandardTypeMetadata.ILLEGAL                   ),
        SMALLDATETIME           (StandardTypeMetadata.DATETIME              ),
        SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
        SMALLINT                (StandardTypeMetadata.SMALLINT               ),
        SMALLSERIAL             (StandardTypeMetadata.TINYINT               ),
        SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
        STRING                  (StandardTypeMetadata.STRING                ),
        STRUCT                  (StandardTypeMetadata.STRUCT                ),
        SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
        TEXT                    (StandardTypeMetadata.VARCHAR                  ),
        TIME                    (StandardTypeMetadata.DATETIME                  ),
        TIMEZ                   (StandardTypeMetadata.DATETIME                  ),
        TIMESTAMP               (StandardTypeMetadata.DATETIME             ),
        TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.DATETIME             ),
        TIMESTAMP_WITH_TIME_ZONE          (StandardTypeMetadata.DATETIME             ),
        TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
        TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
        TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
        UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
        UUID                    (StandardTypeMetadata.ILLEGAL               ),
        UROWID                  (StandardTypeMetadata.ILLEGAL               ),
        VARBIT                  (StandardTypeMetadata.BITMAP             ),
        TINYBLOB                (StandardTypeMetadata.BITMAP              ),
        TINYINT                 (StandardTypeMetadata.TINYINT               ),
        TINYTEXT                (StandardTypeMetadata.TINYTEXT              ),
        VARBINARY               (StandardTypeMetadata.BITMAP             ),
        VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
        VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
        XML                     (StandardTypeMetadata.VARCHAR                  ),
        YEAR                    (StandardTypeMetadata.DATE                  );

        private final TypeMetadata standard;
        private int ignoreLength = -1;
        private int ignorePrecision = -1;
        private int ignoreScale = -1;
        private String lengthRefer;
        private String precisionRefer;
        private String scaleRefer;
        private TypeMetadata.Config config;

        DorisTypeMetadataAlias(TypeMetadata standard){
                this.standard = standard;
        }

        DorisTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
                this.standard = standard;
                this.lengthRefer = lengthRefer;
                this.precisionRefer = precisionRefer;
                this.scaleRefer = scaleRefer;
                this.ignoreLength = ignoreLength;
                this.ignorePrecision = ignorePrecision;
                this.ignoreScale = ignoreScale;
        }

        DorisTypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
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
